package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.events.SkyblockJoinedEvent;
import codes.biscuit.skyblockaddons.utils.events.SkyblockLeftEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public class NetworkListener {
    private final SkyblockAddons main;
    private final Logger logger;

    public NetworkListener() {
        main = SkyblockAddons.getInstance();
        logger = main.getLogger();
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        // Leave Skyblock when the player disconnects
        EVENT_BUS.post(new SkyblockLeftEvent());
    }

    @SubscribeEvent
    public void onSkyblockJoined(SkyblockJoinedEvent event) {
        logger.info("Joined Skyblock");
        main.getUtils().setOnSkyblock(true);
        if (main.getConfigValues().isEnabled(Feature.DISCORD_RPC)) {
            main.getDiscordRPCManager().start();
        }
    }

    @SubscribeEvent
    public void onSkyblockLeft(SkyblockLeftEvent event) {
        logger.info("Left Skyblock");
        main.getUtils().setOnSkyblock(false);
        if (main.getDiscordRPCManager().isActive()) {
            main.getDiscordRPCManager().stop();
        }
    }
}