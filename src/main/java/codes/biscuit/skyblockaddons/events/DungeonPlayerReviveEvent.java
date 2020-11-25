package codes.biscuit.skyblockaddons.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is fired when a player is revived in Skyblock Dungeons.
 * It includes the usernames of the revived player and their reviver.
 * {@code EntityPlayer} instances for both of them are included as well if the players are within the client's render
 * distance. Otherwise, they will be {@code null}.
 *
 * @see codes.biscuit.skyblockaddons.listeners.PlayerListener#onChatReceive(ClientChatReceivedEvent)
 */
public class DungeonPlayerReviveEvent extends Event {
    /** The username of the player that was revived */
    public final String revivedPlayerName;
    /** The player that was revived */
    public final EntityPlayer revivedPlayer;
    /** The username of the player who did the reviving */
    public final String reviverPlayerName;
    /** The player who did the reviving */
    public final EntityPlayer reviverPlayer;

    /**
     * Creates a new instance of {@code DungeonPlayerReviveEvent} with the given usernames of the revived player and
     * their reviver. Both of the {@code EntityPlayer} instances will be set to {@code null}. This should be used when
     * both players are outside of the client's render distance.
     *
     * @param revivedPlayerName The username of the player that was revived
     * @param reviverPlayerName The username of the player who did the reviving
     */
    public DungeonPlayerReviveEvent(String revivedPlayerName, String reviverPlayerName) {
        this.revivedPlayerName = revivedPlayerName;
        revivedPlayer = null;
        this.reviverPlayerName = reviverPlayerName;
        reviverPlayer = null;
    }

    /**
     * Creates a new instance of {@code DungeonPlayerReviveEvent} with the given usernames of the revived player and
     * their reviver as well as the {@code EntityPlayer} instance of the revived player.
     * {@code reviverPlayer} will be set to {@code null}. This should be used when the reviver is outside of the
     * client's render distance.
     *
     * @param revivedPlayerName The username of the player that was revived
     * @param revivedPlayer The {@code EntityPlayer} instance of the revived player
     * @param reviverPlayerName The username of the player who did the reviving
     */
    public DungeonPlayerReviveEvent(String revivedPlayerName, EntityPlayer revivedPlayer, String reviverPlayerName) {
        this.revivedPlayerName = revivedPlayerName;
        this.revivedPlayer = revivedPlayer;
        this.reviverPlayerName = reviverPlayerName;
        reviverPlayer = null;
    }

    /**
     * Creates a new instance of {@code DungeonPlayerReviveEvent} with the given usernames of the revived player and
     * their reviver as well as the {@code EntityPlayer} instances of both.
     *
     * @param revivedPlayerName The username of the player that was revived
     * @param revivedPlayer The {@code EntityPlayer} instance of the revived player
     * @param reviverPlayerName The username of the player who did the reviving
     * @param reviverPlayer The {@code EntityPlayer} instance of the reviver
     */
    public DungeonPlayerReviveEvent(String revivedPlayerName, EntityPlayer revivedPlayer, String reviverPlayerName, EntityPlayer reviverPlayer) {
        this.revivedPlayerName = revivedPlayerName;
        this.revivedPlayer = revivedPlayer;
        this.reviverPlayerName = reviverPlayerName;
        this.reviverPlayer = reviverPlayer;
    }
}
