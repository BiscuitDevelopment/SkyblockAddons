package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.utils.Backpack;
import codes.biscuit.skyblockaddons.utils.CooldownManager;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.input.Keyboard;

public class GuiScreenHook {

    private static final int MADDOX_BATPHONE_COOLDOWN = 1 * 60 * 1000;

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
                IInventory chest = ((ContainerChest) playerContainer).getLowerChestInventory();
                if (chest.hasCustomName()) {
                    String chestName = chest.getDisplayName().getUnformattedText();
                    if (chestName.contains("Auction") || "Your Bids".equals(chestName)) {

                        // Make an exception for the new year cake bag.
                        String itemName = stack.getDisplayName();
                        if (!itemName.contains("New Year Cake Bag")) {
                            return;
                        }
                    }
                }
            }
            Backpack backpack = Backpack.getFromItem(stack);
            if (backpack != null) {
                backpack.setX(x);
                backpack.setY(y);
                if (isFreezeKeyDown(main) && System.currentTimeMillis() - lastBackpackFreezeKey > 500) {
                    lastBackpackFreezeKey = System.currentTimeMillis();
                    GuiContainerHook.setFreezeBackpack(!GuiContainerHook.isFreezeBackpack());
                    main.getUtils().setBackpackToRender(backpack);
                }
                if (!GuiContainerHook.isFreezeBackpack()) {
                    main.getUtils().setBackpackToRender(backpack);
                }
                main.getPlayerListener().onItemTooltip(new ItemTooltipEvent(stack, null, null, false));
                returnValue.cancel();
            }
        }
        if (GuiContainerHook.isFreezeBackpack()) {
            returnValue.cancel();
        }
    }

    private static boolean isFreezeKeyDown(SkyblockAddons main) {
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
