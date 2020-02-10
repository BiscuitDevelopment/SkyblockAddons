package codes.biscuit.skyblockaddons.utils.discord;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Location;
import codes.biscuit.skyblockaddons.utils.SkyblockDate;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import net.minecraftforge.fml.common.FMLLog;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class DiscordRPCManager implements IPCListener {

    private static final long APPLICATION_ID = 653443797182578707L;
    private static final long UPDATE_PERIOD = 3000L;

    private final SkyblockAddons main;
    private IPCClient client;
    private DiscordStatus detailsLine;
    private DiscordStatus stateLine;
    private OffsetDateTime startTimestamp;

    private Timer updateTimer;

    public DiscordRPCManager(final SkyblockAddons main) {
        this.main = main;
    }

    public void start() {
        FMLLog.info("Starting Discord RP...");
        if(isActive()) {
            return;
        }

        stateLine = main.getConfigValues().getDiscordStatus();
        detailsLine = main.getConfigValues().getDiscordDeatils();
        startTimestamp = OffsetDateTime.now();
        client = new IPCClient(APPLICATION_ID);
        client.setListener(this);
        try {
            client.connect();
        } catch (Exception e) {
            FMLLog.warning("Failed to connect to Discord RPC: %s", e.getMessage());
        }
    }

    public void stop() {
        client.close();
    }

    public boolean isActive() {
        return client != null;
    }

    private void updatePresence() {
        final Location location = SkyblockAddons.getInstance().getUtils().getLocation();
        final SkyblockDate skyblockDate = SkyblockAddons.getInstance().getUtils().getCurrentDate();
        final String skyblockDateString = skyblockDate != null ? skyblockDate.toString() : "";

        // Early Winter 10th, 12:10am - Village
        final String largeImageDescription = String.format("%s - %s", skyblockDateString, location.getScoreboardName());
        final String smallImageDescription = String.format("%s v%s", SkyblockAddons.MOD_NAME, SkyblockAddons.VERSION);
        RichPresence presence = new RichPresence.Builder()
                .setState(stateLine.getDisplayString())
                .setDetails(detailsLine.getDisplayString())
                .setStartTimestamp(startTimestamp)
                .setLargeImage(location.getDiscordIconKey(), largeImageDescription)
                .setSmallImage("biscuit", smallImageDescription)
                .build();
        client.sendRichPresence(presence);
    }

    public void setStateLine(DiscordStatus status) {
        this.stateLine = status;
        if(isActive()) {
            updatePresence();
        }
    }

    public void setDetailsLine(DiscordStatus status) {
        this.detailsLine = status;
        if(isActive()) {
            updatePresence();
        }
    }

    @Override
    public void onReady(IPCClient client) {
        FMLLog.info("Discord RPC started");
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updatePresence();
            }
        }, 0, UPDATE_PERIOD);
    }

    @Override
    public void onClose(IPCClient client, JSONObject json) {
        FMLLog.warning("Discord RPC closed");
        this.client = null;
        cancelTimer();
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        FMLLog.warning("Discord RPC disconnected");
        this.client = null;
        cancelTimer();
    }

    private void cancelTimer() {
        if(updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }
}
