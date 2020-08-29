package codes.biscuit.skyblockaddons.events;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is fired when the client player dies in Skyblock.
 *
 * @see codes.biscuit.skyblockaddons.listeners.PlayerListener#onChatReceive(ClientChatReceivedEvent)
 */
public class PlayerSPDeathEvent extends Event {
    /** The player's cause of death */
    public final String cause;

    /**
     * Creates a new instance of {@code PlayerSPDeathEvent} with the given cause of death.
     *
     * @param cause the player's cause of death
     */
    public PlayerSPDeathEvent(String cause) {
        this.cause = cause;
    }
}
