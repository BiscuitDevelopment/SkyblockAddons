package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.item.ItemStack;

public class ItemHook {

    public static boolean isItemDamaged(ItemStack stack) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
            if(CooldownManager.isOnCooldown(stack)) {
                return true;
            }
        }
        return stack.isItemDamaged();
    }

    public static void getDurabilityForDisplay(ItemStack stack, ReturnValue<Double> returnValue) { //Item item, ItemStack stack
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
            if(CooldownManager.isOnCooldown(stack)) {
                returnValue.cancel(CooldownManager.getRemainingCooldownPercent(stack));
            }
        }
    }
}
