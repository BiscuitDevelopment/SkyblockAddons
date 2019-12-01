package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.CooldownManager;
import codes.biscuit.skyblockaddons.utils.Feature;
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

    public double getDurabilityForDisplay(ItemStack stack, ReturnValue<Double> returnValue) { //Item item, ItemStack stack
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
            if(CooldownManager.isOnCooldown(stack)) {
                returnValue.cancel(CooldownManager.getRemainingCooldownPercent(stack));
            }
        }
        ReturnValue<Double> returnValue2 = new ReturnValue<>();
        if (returnValue2.isCancelled()) {
            return returnValue2.getReturnValue();
        }

        return 1D;
    }
}
