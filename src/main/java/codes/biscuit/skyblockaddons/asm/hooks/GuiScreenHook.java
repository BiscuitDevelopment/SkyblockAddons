package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackManager;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreview;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.utils.InventoryUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
    @Getter @Setter private static long lastBackpackFreezeKey = -1;

    //TODO Fix for Hypixel localization
    public static void renderBackpack(ItemStack stack, int x, int y, ReturnValue<?> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW) && (stack.getItem() == Items.skull ||
                stack.getItem() == Item.getItemFromBlock(Blocks.dropper)) ) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_HOLDING_SHIFT) && !GuiScreen.isShiftKeyDown()) {
                return;
            }

            // Avoid showing backpack preview in auction stuff.
            Container playerContainer = Minecraft.getMinecraft().thePlayer.openContainer;
            if (playerContainer instanceof ContainerChest) {
                IInventory chestInventory = ((ContainerChest) playerContainer).getLowerChestInventory();
                if (chestInventory.hasCustomName()) {
                    String chestName = chestInventory.getDisplayName().getUnformattedText();
                    if (chestName.contains("Auction") || "Your Bids".equals(chestName)) {

                        // Make sure this backpack is in the auction house and not just in your inventory before cancelling.
                        for (int slotNumber = 0; slotNumber < chestInventory.getSizeInventory(); slotNumber++) {
                            if (chestInventory.getStackInSlot(slotNumber) == stack) {
                                return;
                            }
                        }
                    }
                }
            }

            ContainerPreview containerPreview = BackpackManager.getFromItem(stack);
            if (containerPreview != null) {
                /*
                 Don't render the backpack preview if in the backpack is used to represent a crafting recipe or the
                 result of one.
                 */
                if (ItemUtils.isMenuItem(stack)) {
                    return;
                }

                containerPreview.setX(x);
                containerPreview.setY(y);
                if (isFreezeKeyDown() && System.currentTimeMillis() - lastBackpackFreezeKey > 500) {
                    lastBackpackFreezeKey = Minecraft.getSystemTime();
                    GuiContainerHook.setFreezeBackpack(!GuiContainerHook.isFreezeBackpack());
                    main.getUtils().setContainerPreviewToRender(containerPreview);
                }
                if (!GuiContainerHook.isFreezeBackpack()) {
                    main.getUtils().setContainerPreviewToRender(containerPreview);
                }
                main.getPlayerListener().onItemTooltip(new ItemTooltipEvent(stack, null, null, false));
                returnValue.cancel();
            }

            if (main.getConfigValues().isEnabled(Feature.SHOW_PERSONAL_COMPACTOR_PREVIEW)) {
                /*
                 Don't render the compactor preview if in the backpack is used to represent a crafting recipe or the
                 result of one.
                 */
                if (ItemUtils.isMenuItem(stack)) {
                    return;
                }

                ItemStack[] items = ItemUtils.getPersonalCompactorContents(stack);

                if (items != null) {
                    main.getPlayerListener().onItemTooltip(new ItemTooltipEvent(stack, null, null, false));
                    returnValue.cancel();
                    String name = TextUtils.stripColor(stack.getDisplayName());

                    // Remove the reforge like it does in the actual menu
                    if (ItemUtils.getReforge(stack) != null) {
                        int firstSpace = name.indexOf(' ');
                        if (name.length() > firstSpace + 1) {
                            name = name.substring(firstSpace + 1);
                        }
                    }

                    main.getUtils().setContainerPreviewToRender(new ContainerPreview(items, name, null, x, y));
                }
            }
        }

        if (GuiContainerHook.isFreezeBackpack()) {
            returnValue.cancel();
        }
    }

    /**
     * Returns whether the backpack freeze key is down
     *
     * @return {@code true} if the backpack freeze key is down, {@code false} otherwise
     */
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
}
