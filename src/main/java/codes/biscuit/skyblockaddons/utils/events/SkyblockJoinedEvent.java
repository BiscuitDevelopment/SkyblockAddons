package codes.biscuit.skyblockaddons.utils.events;

import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This is fired by {@link Utils#checkGameLocationDate()} when the player joins Hypixel Skyblock.
 */
public class SkyblockJoinedEvent extends Event {
    // This is intentionally empty since there's no useful data we need to include.
}