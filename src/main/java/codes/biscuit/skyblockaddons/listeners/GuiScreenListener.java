package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.hooks.GuiChestHook;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.events.InventoryLoadingDoneEvent;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreviewManager;
import codes.biscuit.skyblockaddons.features.dungeonmap.DungeonMapManager;
import codes.biscuit.skyblockaddons.gui.LocationEditGui;
import codes.biscuit.skyblockaddons.misc.scheduler.ScheduledTask;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
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
import net.minecraftforge.common.MinecraftForge;
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

    private InventoryChangeListener inventoryChangeListener;
    private InventoryBasic listenedInventory;
    private ScheduledTask inventoryChangeTimeCheckTask;

    /** Time in milliseconds of the last time a {@code GuiContainer} was closed */
    @Getter
    private long lastContainerCloseMs = -1;

    /** Time in milliseconds of the last time a backpack was opened, used by {@link Feature#BACKPACK_OPENING_SOUND}. */
    @Getter
    private long lastBackpackOpenMs = -1;

    /** Time in milliseconds of the last time an item in the currently open {@code GuiContainer} changed */
    private long lastInventoryChangeMs = -1;

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
            addInventoryChangeListener(chestInventory);

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
                    ContainerPreviewManager.onContainerOpen(chestInventory);
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
            if (inventoryChangeListener != null) {
                removeInventoryChangeListener(listenedInventory);
            }

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

        if (main.getConfigValues().isEnabled(Feature.DUNGEONS_MAP_DISPLAY) &&
                main.getConfigValues().isEnabled(Feature.CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD) &&
                Minecraft.getMinecraft().currentScreen instanceof LocationEditGui) {
            if (Keyboard.isKeyDown(main.getKeyBindings().get(5).getKeyCode()) && Keyboard.getEventKeyState()) {
                DungeonMapManager.decreaseZoomByStep();
            } else if (Keyboard.isKeyDown(main.getKeyBindings().get(4).getKeyCode()) && Keyboard.getEventKeyState()) {
                DungeonMapManager.increaseZoomByStep();
            }
        }
    }

    @SubscribeEvent
    public void onInventoryLoadingDone(InventoryLoadingDoneEvent e) {
        removeInventoryChangeListener(listenedInventory);
        lastInventoryChangeMs = -1;
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

    /**
     * Called when a slot in the currently opened {@code GuiContainer} changes. Used to determine if all its items have been loaded.
     */
    void onInventoryChanged(InventoryBasic inventory) {
        long currentTimeMs = System.currentTimeMillis();

        if (inventory.getStackInSlot(inventory.getSizeInventory() - 1) != null) {
            MinecraftForge.EVENT_BUS.post(new InventoryLoadingDoneEvent());
        } else {
            lastInventoryChangeMs = currentTimeMs;
        }
    }

    /**
     * Adds a change listener to a given inventory.
     *
     * @param inventory the inventory to add the change listener to
     */
    private void addInventoryChangeListener(InventoryBasic inventory) {
        if (inventory == null) {
            throw new NullPointerException("Tried to add listener to null inventory.");
        }

        lastInventoryChangeMs = System.currentTimeMillis();
        inventoryChangeListener = new InventoryChangeListener(this);
        inventory.addInventoryChangeListener(inventoryChangeListener);
        listenedInventory = inventory;
        inventoryChangeTimeCheckTask = main.getNewScheduler().scheduleRepeatingTask(new SkyblockRunnable() {
            @Override
            public void run() {
                checkLastInventoryChangeTime();
            }
        }, 20, 5);
    }

    /**
     * Checks whether it has been more than one second since the last inventory change, which indicates inventory
     * loading is most likely finished. Could trigger incorrectly with a lag spike.
     */
    private void checkLastInventoryChangeTime() {
        if (listenedInventory != null) {
            if (lastInventoryChangeMs > -1 && System.currentTimeMillis() - lastInventoryChangeMs > 1000) {
                MinecraftForge.EVENT_BUS.post(new InventoryLoadingDoneEvent());
            }
        }
    }

    /**
     * Removes {@link #inventoryChangeListener} from a given {@link InventoryBasic}.
     *
     * @param inventory the {@code InventoryBasic} to remove the listener from
     */
    private void removeInventoryChangeListener(InventoryBasic inventory) {
        if (inventory == null) {
            throw new NullPointerException("Tried to remove listener from null inventory.");
        }

        if (inventoryChangeListener != null) {
            try {
                inventory.removeInventoryChangeListener(inventoryChangeListener);
            } catch (NullPointerException e) {
                SkyblockAddons.getInstance().getUtils().sendErrorMessage(
                        "Tried to remove an inventory listener from a container that has no listeners.");
            }

            if (inventoryChangeTimeCheckTask != null) {
                if (!inventoryChangeTimeCheckTask.isCanceled()) {
                    inventoryChangeTimeCheckTask.cancel();
                }
            }

            inventoryChangeListener = null;
            listenedInventory = null;
            inventoryChangeTimeCheckTask = null;
        }
    }
}
