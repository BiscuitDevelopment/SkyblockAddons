package codes.biscuit.skyblockaddons.events;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when all the slots in an open {@code GuiChest} are done loading.
 * This is used to run logic that depends on Skyblock menus being fully loaded.
 */
public class InventoryLoadingDoneEvent extends Event {
}
