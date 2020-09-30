package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.events.SkyblockJoinedEvent;
import codes.biscuit.skyblockaddons.events.SkyblockLeftEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public class NetworkListener {
    private final SkyblockAddons main;

    public NetworkListener() {
        main = SkyblockAddons.getInstance();
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        // Leave Skyblock when the player disconnects
        EVENT_BUS.post(new SkyblockLeftEvent());
    }

    @SubscribeEvent
    public void onSkyblockJoined(SkyblockJoinedEvent event) {
        SkyblockAddons.getLogger().info("Detected joining skyblock!");
        main.getUtils().setOnSkyblock(true);
        if (main.getConfigValues().isEnabled(Feature.DISCORD_RPC)) {
            main.getDiscordRPCManager().start();
        }
    }

    @SubscribeEvent
    public void onSkyblockLeft(SkyblockLeftEvent event) {
        SkyblockAddons.getLogger().info("Detected leaving skyblock!");
        main.getUtils().setOnSkyblock(false);
        if (main.getDiscordRPCManager().isActive()) {
            main.getDiscordRPCManager().stop();
        }
    }
}