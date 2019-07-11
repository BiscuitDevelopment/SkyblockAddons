package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public class MixinItem {

    @Redirect(method = "showDurabilityBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemDamaged()Z", ordinal = 0))
    private boolean showDurabilityBar(ItemStack stack) { //Item item, ItemStack stack
        if (SkyblockAddons.INSTANCE.getUtils().isOnSkyblock()) {
            return false;
        }
        return stack.isItemDamaged();
    }
}
