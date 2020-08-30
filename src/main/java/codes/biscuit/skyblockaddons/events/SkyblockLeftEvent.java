package codes.biscuit.skyblockaddons.events;

import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This is fired by {@link Utils#parseSidebar()} when the player leaves Hypixel Skyblock or disconnects from a server.
 */
public class SkyblockLeftEvent extends Event {
    // This is intentionally empty since there's no useful data we need to include.
}