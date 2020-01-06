package codes.biscuit.skyblockaddons.utils.discord;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
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

    private IPCClient client;
    private DiscordStatus detailsLine = DiscordStatus.NONE;
    private DiscordStatus stateLine = DiscordStatus.NONE;
    private OffsetDateTime startTimestamp;

    private Timer updateTimer;

    public void start() {
        FMLLog.info("Starting Discord RP...");
        if(isActive()) {
            return;
        }

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

    public void updatePresence() {
        EnumUtils.Location location = SkyblockAddons.getInstance().getUtils().getLocation();
        SkyblockDate skyblockDate = SkyblockAddons.getInstance().getUtils().getCurrentDate();
        RichPresence presence = new RichPresence.Builder()
                .setState(stateLine.getDisplayString())
                .setDetails(detailsLine.getDisplayString())
                .setStartTimestamp(startTimestamp)
                .setLargeImage(location.getDiscordIconKey(), skyblockDate.toString())
                .setSmallImage("biscuit", "SkyblockAddons v1.5")
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
