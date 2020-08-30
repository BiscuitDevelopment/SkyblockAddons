package codes.biscuit.skyblockaddons.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is fired when a player is revived in Skyblock Dungeons.
 *
 * @see codes.biscuit.skyblockaddons.listeners.PlayerListener#onChatReceive(ClientChatReceivedEvent)
 */
public class DungeonPlayerReviveEvent extends Event {
    /** The player that was revived */
    public final EntityPlayer revivedPlayer;
    /** The player who did the reviving */
    public final EntityPlayer revivingPlayer;

    /**
     * Creates a new instance of {@code DungeonPlayerReviveEvent} with the given revived player and {@code revivingPlayer}
     * set to {@code null}. This should be used when the reviving player isn't known.
     *
     * @param revivedPlayer the player that was revived
     */
    public DungeonPlayerReviveEvent(EntityPlayer revivedPlayer) {
        this.revivedPlayer = revivedPlayer;
        revivingPlayer = null;
    }

    /**
     * Creates a new instance of {@code DungeonPlayerReviveEvent} with the given revived player and reviving player.
     *
     * @param revivedPlayer the player that was revived
     * @param revivingPlayer the player that revived {@code revivedPlayer}
     */
    public DungeonPlayerReviveEvent(EntityPlayer revivedPlayer, EntityPlayer revivingPlayer) {
        this.revivedPlayer = revivedPlayer;
        this.revivingPlayer = revivingPlayer;
    }
}
