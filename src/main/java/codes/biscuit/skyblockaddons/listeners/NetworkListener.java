package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.events.SkyblockJoinedEvent;
import codes.biscuit.skyblockaddons.events.SkyblockLeftEvent;
import codes.biscuit.skyblockaddons.misc.scheduler.ScheduledTask;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;

public class NetworkListener {

    private static final Logger logger = SkyblockAddons.getLogger();

    private final SkyblockAddons main;
    private ScheduledTask updateHealth;

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
        logger.info("Detected joining skyblock!");
        main.getUtils().setOnSkyblock(true);
        if (main.getConfigValues().isEnabled(Feature.DISCORD_RPC)) {
            main.getDiscordRPCManager().start();
        }
        updateHealth = main.getNewScheduler().scheduleRepeatingTask(new SkyblockRunnable() {
            @Override
            public void run() {
                main.getPlayerListener().updateLastSecondHealth();
            }
        }, 0, 20);

        DataUtils.onSkyblockJoined();
    }

    @SubscribeEvent
    public void onSkyblockLeft(SkyblockLeftEvent event) {
        logger.info("Detected leaving skyblock!");
        main.getUtils().setOnSkyblock(false);
        main.getUtils().setProfileName("Unknown");
        if (main.getDiscordRPCManager().isActive()) {
            main.getDiscordRPCManager().stop();
        }
        if (updateHealth != null) {
            main.getNewScheduler().cancel(updateHealth);
            updateHealth = null;
        }
    }
}