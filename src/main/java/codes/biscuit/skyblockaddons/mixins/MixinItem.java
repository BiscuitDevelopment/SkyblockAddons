package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class MixinItem {

    @Redirect(method = "showDurabilityBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemDamaged()Z", ordinal = 0))
    private boolean showDurabilityBar(ItemStack stack) { //Item item, ItemStack stacks
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
            double cooldown = main.getUtils().getItemCooldown(stack);
            if (cooldown != -1 && cooldown < 1) return true;
        }
        return stack.isItemDamaged();
    }

    @Inject(method = "getDurabilityForDisplay", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private void showDurabilityBar(ItemStack stack, CallbackInfoReturnable<Double> cir) { //Item item, ItemStack stack
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
            double cooldown = main.getUtils().getItemCooldown(stack);
            if (cooldown != -1) cir.setReturnValue(1-cooldown);
        }
    }
}
