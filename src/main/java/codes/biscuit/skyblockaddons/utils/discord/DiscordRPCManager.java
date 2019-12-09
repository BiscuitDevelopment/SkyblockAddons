package codes.biscuit.skyblockaddons.utils.discord;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import net.minecraftforge.fml.common.FMLLog;
import org.json.JSONObject;

import java.time.OffsetDateTime;

public class DiscordRPCManager implements IPCListener {

    private static final long APPLICATION_ID = 653443797182578707L;

    private IPCClient client;
    private DiscordStatus detailsLine = DiscordStatus.PURSE;
    private DiscordStatus stateLine = DiscordStatus.STATS;
    private OffsetDateTime startTimestamp;

    public void start() {
        startTimestamp = OffsetDateTime.now();
        client = new IPCClient(APPLICATION_ID);
        client.setListener(this);
        try {
            client.connect();
        } catch (NoDiscordClientException e) {
            FMLLog.warning("Failed to connect to Discord RPC.");
        }
    }

    public void stop() {
        client.close();
    }

    public boolean isActive() {
        return client != null;
    }

    public void updatePresence() {
        RichPresence presence = new RichPresence.Builder()
                .setState(stateLine.getDisplayMessage())
                .setDetails(detailsLine.getDisplayMessage())
                .setStartTimestamp(startTimestamp)
                .build();
        client.sendRichPresence(presence);
    }

    public void setFirstLine(DiscordStatus status) {
        this.stateLine = status;
        if(isActive()) {
            updatePresence();
        }
    }

    public void setSecondLine(DiscordStatus status) {
        this.detailsLine = status;
        if(isActive()) {
            updatePresence();
        }
    }

    @Override
    public void onReady(IPCClient client) {
        FMLLog.info("Discord RPC started");
        updatePresence();
    }

    @Override
    public void onClose(IPCClient client, JSONObject json) {
        FMLLog.warning("Discord RPC closed");
        this.client = null;
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        FMLLog.warning("Discord RPC disconnected");
        this.client = null;
    }
}
