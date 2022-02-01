package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.hooks.GuiChestHook;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreviewManager;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DevUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This listener listens for events that happen while a {@link GuiScreen} is open.
 *
 * @author ILikePlayingGames
 * @version 1.5
 */
public class GuiScreenListener {

    private final SkyblockAddons main = SkyblockAddons.getInstance();

    /** Time in milliseconds of the last time a {@code GuiContainer} was closed */
    @Getter
    private long lastContainerCloseMs = -1;

    /** Time in milliseconds of the last time a backpack was opened, used by {@link Feature#BACKPACK_OPENING_SOUND}. */
    @Getter
    private long lastBackpackOpenMs = -1;

    @SubscribeEvent
    public void beforeInit(GuiScreenEvent.InitGuiEvent.Pre e) {
        if (!main.getUtils().isOnSkyblock()) {
            return;
        }

        GuiScreen guiScreen = e.gui;

        if (guiScreen instanceof GuiChest) {
            Minecraft mc = Minecraft.getMinecraft();
            GuiChest guiChest = (GuiChest) guiScreen;
            InventoryType inventoryType = SkyblockAddons.getInstance().getInventoryUtils().updateInventoryType(guiChest);
            InventoryBasic chestInventory = (InventoryBasic) guiChest.lowerChestInventory;

            // Backpack opening sound
            if (main.getConfigValues().isEnabled(Feature.BACKPACK_OPENING_SOUND) && chestInventory.hasCustomName()) {
                if (chestInventory.getDisplayName().getUnformattedText().contains("Backpack")) {
                    lastBackpackOpenMs = System.currentTimeMillis();

                    if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
                        mc.thePlayer.playSound("mob.horse.armor", 0.5F, 1);
                    } else {
                        mc.thePlayer.playSound("mob.horse.leather", 0.5F, 1);
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW)) {
                if (inventoryType == InventoryType.STORAGE_BACKPACK || inventoryType == InventoryType.ENDER_CHEST) {
                    try {
                        ContainerPreviewManager.saveStorageContainerInventory(chestInventory,
                                SkyblockAddons.getInstance().getInventoryUtils().getInventoryKey());
                    } catch (Exception exception) {
                        main.getUtils().sendErrorMessage(exception.getMessage());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if (!main.getUtils().isOnSkyblock()) {
            return;
        }

        GuiScreen guiScreen = e.gui;
        GuiScreen oldGuiScreen = Minecraft.getMinecraft().currentScreen;

        // Closing a container
        if (guiScreen == null && oldGuiScreen instanceof GuiContainer) {
            lastContainerCloseMs = System.currentTimeMillis();
        }

        // Closing or switching to a different GuiChest
        if (oldGuiScreen instanceof GuiChest) {
            ContainerPreviewManager.onContainerClose();
            GuiChestHook.onGuiClosed();
        }
    }

    /**
     * Listens for key presses while a GUI is open
     *
     * @param event the {@code GuiScreenEvent.KeyboardInputEvent} to listen for
     */
    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        int eventKey = Keyboard.getEventKey();

        if (main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE) && eventKey == main.getDeveloperCopyNBTKey().getKeyCode() && Keyboard.getEventKeyState()) {
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

    @SubscribeEvent
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

                        if (main.getConfigValues().getLockedSlots().contains(i + 36)) {
                            if (!slot.getHasStack() && !hotbarSlot.getHasStack()) {
                                return;
                            } else {
                                main.getUtils().playLoudSound("note.bass", 0.5);
                                main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Translations.getMessage("messages.slotLocked"));
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
