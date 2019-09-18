package codes.biscuit.skyblockaddons.mixins;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class MixinItem {

    /*@Redirect(method = "showDurabilityBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemDamaged()Z", ordinal = 0))
    private boolean showDurabilityBar(ItemStack stack) { //Item item, ItemStack stack
        if (SkyblockAddons.getInstance().getUtils().isOnSkyblock() && SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.HIDE_DURABILITY))
            return false;

        return stack.isItemDamaged();
    }*/

}
