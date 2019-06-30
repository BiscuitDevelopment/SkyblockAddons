package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    private Item lastItem = null;
    private long lastDrop = System.currentTimeMillis();

    @Inject(method = "dropOneItem", at = @At(value = "HEAD"), cancellable = true)
    private void dropOneItemConfirmation(boolean dropAll, CallbackInfoReturnable<EntityItem> cir) {
        if (!SkyblockAddons.INSTANCE.getConfigValues().getDisabledFeatures().contains(Feature.DROP_CONFIRMATION)) {
            ItemStack heldItemStack = Minecraft.getMinecraft().thePlayer.getHeldItem();
            if (heldItemStack != null) {
                Item heldItem = heldItemStack.getItem();
                if (lastItem != null && lastItem == heldItem && System.currentTimeMillis() - lastDrop < 3000) {
                    lastDrop = System.currentTimeMillis();
                } else {
                    Utils.sendMessage(SkyblockAddons.INSTANCE.getConfigValues().getConfirmationColor().getChatFormatting() + "Drop this item again to confirm!");
                    lastItem = heldItem;
                    lastDrop = System.currentTimeMillis();
                    cir.setReturnValue(null);
                }
            }
        }
    }
}
