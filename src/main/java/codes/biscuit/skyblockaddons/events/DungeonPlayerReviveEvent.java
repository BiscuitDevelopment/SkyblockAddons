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

    /** The player that was revived */
    public final EntityPlayer revivedPlayer;
    /** The player who did the reviving */
    public final EntityPlayer reviverPlayer;

    /**
     * Creates a new instance of {@code DungeonPlayerReviveEvent} with the given usernames of the revived player and
     * their reviver as well as the {@code EntityPlayer} instances of both.
     *
     * @param revivedPlayer The {@code EntityPlayer} instance of the revived player
     * @param reviverPlayer The {@code EntityPlayer} instance of the reviver
     */
    public DungeonPlayerReviveEvent(EntityPlayer revivedPlayer, EntityPlayer reviverPlayer) {
        this.revivedPlayer = revivedPlayer;
        this.reviverPlayer = reviverPlayer;
    }
}
