package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.DevUtils;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * This listener listens for events that happen while a {@link GuiScreen} is open.
 *
 * @author ILikePlayingGames
 * @version 1.0
 */
public class GuiScreenListener {

    private SkyblockAddons main = SkyblockAddons.getInstance();

    /**
     * Listens for key presses while a GUI is open
     *
     * @param event the {@code GuiScreenEvent.KeyboardInputEvent} to listen for
     */
    @SubscribeEvent()
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent event) {
        int eventKey = Keyboard.getEventKey();

        if (eventKey == DevUtils.DEV_KEY && Keyboard.getEventKeyState()) {
            // Copy Item NBT
            if (main.isDevMode()) {
                GuiScreen currentScreen = event.gui;

                // Check if the player is in an inventory.
                if (GuiContainer.class.isAssignableFrom(currentScreen.getClass())) {
                    Slot currentSlot = ((GuiContainer) currentScreen).getSlotUnderMouse();

                    if (currentSlot != null && currentSlot.getHasStack()) {
                        DevUtils.copyNBTTagToClipboard(currentSlot.getStack().serializeNBT(),
                                ColorCode.GREEN + "Item data was copied to clipboard!");
                    }
                }
            }
        }
    }
}
