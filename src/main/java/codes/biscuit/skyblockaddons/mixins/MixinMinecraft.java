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
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    private Item lastItem = null;
    private long lastDrop = System.currentTimeMillis();

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;dropOneItem(Z)Lnet/minecraft/entity/item/EntityItem;", ordinal = 0))
    private EntityItem dropOneItemConfirmation(EntityPlayerSP entityPlayerSP, boolean dropAll) {
        if (SkyblockAddons.INSTANCE.getConfigValues().getDisabledFeatures().contains(Feature.DROP_CONFIRMATION)) {
            System.out.println(SkyblockAddons.INSTANCE.getConfigValues().getDisabledFeatures());
            return entityPlayerSP.dropOneItem(dropAll);
        } else {
            ItemStack heldItemStack = entityPlayerSP.getHeldItem();
            System.out.println(heldItemStack);
            if (heldItemStack != null) {
                Item heldItem = heldItemStack.getItem();
                if (lastItem != null && lastItem == heldItem && System.currentTimeMillis() - lastDrop < 3000) {
                    lastDrop = System.currentTimeMillis();
                    return entityPlayerSP.dropOneItem(dropAll);
                } else {
                    Utils.sendMessage(SkyblockAddons.INSTANCE.getConfigValues().getConfirmationColor().getChatFormatting() + "Drop this item again to confirm!");
                    lastItem = heldItem;
                    lastDrop = System.currentTimeMillis();
                }
            }
        }
        return null;
    }
}
