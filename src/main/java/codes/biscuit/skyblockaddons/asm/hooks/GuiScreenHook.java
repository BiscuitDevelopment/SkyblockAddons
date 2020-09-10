package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.features.backpacks.Backpack;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackManager;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.input.Keyboard;

public class GuiScreenHook {

    private static final int MADDOX_BATPHONE_COOLDOWN = 20 * 1000;

    /**
     * The last time the backpack preview freeze key was pressed.
     * This is to stop multiple methods that handle similar logic from
     * performing the same actions multiple times.
     */
    private static long lastBackpackFreezeKey = -1;

    public static void renderBackpack(ItemStack stack, int x, int y, ReturnValue<?> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (stack.getItem().equals(Items.skull) && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW)) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_HOLDING_SHIFT) && !GuiScreen.isShiftKeyDown()) {
                return;
            }

            Container playerContainer = Minecraft.getMinecraft().thePlayer.openContainer;
            if (playerContainer instanceof ContainerChest) { // Avoid showing backpack preview in auction stuff.
                IInventory chestInventory = ((ContainerChest) playerContainer).getLowerChestInventory();
                if (chestInventory.hasCustomName()) {
                    String chestName = chestInventory.getDisplayName().getUnformattedText();
                    if (chestName.contains("Auction") || "Your Bids".equals(chestName)) {

                        // Show preview for backpacks in player inventory if enabled.
                        if (!main.getConfigValues().isEnabled(Feature.BACKPACK_PREVIEW_AH)) {
                            return;
                        }

                        /*
                        If the backpack is in the auction house window, ignore it.
                         */
                        for (int i = 0; i < chestInventory.getSizeInventory(); i++) {
                            if (ItemStack.areItemStackTagsEqual(chestInventory.getStackInSlot(i), stack)) {
                                return;
                            }
                        }
                    }
                }
            }

            Backpack backpack = BackpackManager.getFromItem(stack);
            if (backpack != null) {
                /*
                 Don't render the backpack preview if in the backpack is used to represent a crafting recipe or the
                 result of one.
                 */
                if (BackpackManager.isBackpackCraftingMenuItem(stack)) {
                    return;
                }

                backpack.setX(x);
                backpack.setY(y);
                if (isFreezeKeyDown() && System.currentTimeMillis() - lastBackpackFreezeKey > 500) {
                    lastBackpackFreezeKey = System.currentTimeMillis();
                    GuiContainerHook.setFreezeBackpack(!GuiContainerHook.isFreezeBackpack());
                    main.getUtils().setBackpackToPreview(backpack);
                }
                if (!GuiContainerHook.isFreezeBackpack()) {
                    main.getUtils().setBackpackToPreview(backpack);
                }
                main.getPlayerListener().onItemTooltip(new ItemTooltipEvent(stack, null, null, false));
                returnValue.cancel();
            }
        }
        if (GuiContainerHook.isFreezeBackpack()) {
            returnValue.cancel();
        }
    }

    private static boolean isFreezeKeyDown() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getFreezeBackpackKey().isKeyDown()) return true;
        if (main.getFreezeBackpackKey().isPressed()) return true;
        try {
            if (Keyboard.isKeyDown(main.getFreezeBackpackKey().getKeyCode())) return true;
        } catch (Exception ignored) {}

        return false;
    }

    public static void handleComponentClick(IChatComponent component) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && component != null && "§2§l[OPEN MENU]".equals(component.getUnformattedText()) &&
                !CooldownManager.isOnCooldown(InventoryUtils.MADDOX_BATPHONE_DISPLAYNAME)) {// The prompt when Maddox picks up the phone.
            CooldownManager.put(InventoryUtils.MADDOX_BATPHONE_DISPLAYNAME, MADDOX_BATPHONE_COOLDOWN);
        }
    }

    static long getLastBackpackFreezeKey() {
        return lastBackpackFreezeKey;
    }

    static void setLastBackpackFreezeKey(long lastBackpackFreezeKey) {
        GuiScreenHook.lastBackpackFreezeKey = lastBackpackFreezeKey;
    }
}
