package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.dev.DevUtils;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

/**
 * This listener listens for events that happen while a {@code GuiScreen} is open.
 *
 * @author ILikePlayingGames
 * @version 1.0
 */
public class GuiScreenListener {
    private final SkyblockAddons main;

    private long lastDevKeyEvent = 0L;

    public GuiScreenListener(SkyblockAddons main) {
        this.main = main;
    }

    /**
     * Listens for key presses while a GUI is open
     *
     * @param event the {@code GuiScreenEvent.KeyboardInputEvent} to listen for
     */
    @SubscribeEvent()
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent event) {
        int eventKey = Keyboard.getEventKey();

        // Forge key binding key press detection doesn't work in GUIs
        if (eventKey != Keyboard.KEY_NONE && eventKey == main.getDevKey().getKeyCode()) {
            event.setCanceled(true);

            // For some reason four key presses are detected for each actual press so count only the first one.
            if (Minecraft.getSystemTime() - lastDevKeyEvent > 100L) {

                // Copy Item NBT
                if (main.isDevMode()) {
                    GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

                    // Check if the player is in an inventory.
                    if (GuiContainer.class.isAssignableFrom(currentScreen.getClass())) {
                        Slot currentSlot = ((GuiContainer) currentScreen).getSlotUnderMouse();

                        if (currentSlot != null && currentSlot.getHasStack()) {
                            DevUtils.copyNBTTagToClipboard(currentSlot.getStack().getTagCompound(),
                                    ChatFormatting.GREEN + "Item data was copied to clipboard!");
                        }
                    }
                }
                else {
                    main.getUtils().sendMessage(ChatFormatting.RED + "Developer mode is off. This button does nothing.");
                }
            }

            lastDevKeyEvent = Minecraft.getSystemTime();
        }
    }
}
