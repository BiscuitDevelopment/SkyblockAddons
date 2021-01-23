package codes.biscuit.skyblockaddons.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * This event is fired when a player dies in Skyblock.
 * It includes the {@code EntityPlayer} of the player who died, their username and the cause of death.
 * The {@code EntityPlayer} may be {@code null} if a player dies outside of the client's render distance.
 *
 * @see codes.biscuit.skyblockaddons.listeners.PlayerListener#onChatReceive(ClientChatReceivedEvent)
 */
public class SkyblockPlayerDeathEvent extends PlayerEvent {

    /** The player's cause of death */
    public final String cause;

    /**
     * Creates a new instance of {@code SkyblockPlayerDeathEvent} with the given {@code EntityPlayer},
     * and cause of death. {@code player} can be {@code null} if the entity isn't loaded.
     *
     * @param player the player who died
     * @param cause the player's cause of death
     */
    public SkyblockPlayerDeathEvent(EntityPlayer player, String cause) {
        super(player);
        this.cause = cause;
    }
}
