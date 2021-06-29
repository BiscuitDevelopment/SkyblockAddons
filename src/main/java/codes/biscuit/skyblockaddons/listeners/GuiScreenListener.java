package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DevUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * This listener listens for events that happen while a {@link GuiScreen} is open.
 *
 * @author ILikePlayingGames
 * @version 1.1
 */
public class GuiScreenListener {

    private final SkyblockAddons main = SkyblockAddons.getInstance();

    /**
     * Listens for key presses while a GUI is open
     *
     * @param event the {@code GuiScreenEvent.KeyboardInputEvent} to listen for
     */
    @SubscribeEvent()
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        int eventKey = Keyboard.getEventKey();

        if (main.isDevMode() && eventKey == main.getDeveloperCopyNBTKey().getKeyCode() && Keyboard.getEventKeyState()) {
            // Copy Item NBT
            GuiScreen currentScreen = event.gui;

            // Check if the player is in an inventory.
            if (GuiContainer.class.isAssignableFrom(currentScreen.getClass())) {
                Slot currentSlot = ((GuiContainer) currentScreen).getSlotUnderMouse();

                if (currentSlot != null && currentSlot.getHasStack()) {
                    DevUtils.copyNBTTagToClipboard(currentSlot.getStack().serializeNBT(), ColorCode.GREEN + "Item data was copied to clipboard!");
                }
            }
        }
    }

    @SubscribeEvent()
    public void onMouseClick(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!main.getUtils().isOnSkyblock()) {
            return;
        }

        int eventButton = Mouse.getEventButton();

        // Ignore button up
        if (!Mouse.getEventButtonState()) {
            return;
        }

        if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && event.gui instanceof GuiContainer) {
            GuiContainer guiContainer = (GuiContainer) event.gui;

            if (eventButton >= 0) {
                /*
                This prevents swapping items in/out of locked hotbar slots when using a hotbar key binding that is bound
                to a mouse button.
                 */
                for (int i = 0; i < 9; i++) {
                    if (eventButton - 100 == Minecraft.getMinecraft().gameSettings.keyBindsHotbar[i].getKeyCode()) {
                        Slot slot = guiContainer.getSlotUnderMouse();
                        Slot hotbarSlot = guiContainer.inventorySlots.getSlot(guiContainer.inventorySlots.inventorySlots.size() - (9 - i));

                        if (slot == null || hotbarSlot == null) {
                            return;
                        }

                        //SkyblockAddons.getLogger().debug("Slot " + slot.slotNumber + ": " + (slot.getHasStack() ? slot.getStack().toString() : "Empty"));
                        //SkyblockAddons.getLogger().debug("Hotbar Slot: " + hotbarSlot.slotNumber + ": " + (hotbarSlot.getHasStack() ? hotbarSlot.getStack().toString() : "Empty"));

                        if (main.getConfigValues().getLockedSlots().contains(i + 36)) {
                            if (!slot.getHasStack() && !hotbarSlot.getHasStack()) {
                                return;
                            } else {
                                main.getUtils().playLoudSound("note.bass", 0.5);
                                main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_SLOT_LOCKED.getMessage());
                                event.setCanceled(true);
                            }
                        }
                    }
                }

                //TODO: Cover shift-clicking into locked slots
            }
        }
    }
}
