package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(LayerCustomHead.class)
public class MixinEntityLayerCustomHead {

    private final Pattern otherHalfLetters = Pattern.compile("[a-z0-9_]");

    @Redirect(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getCurrentArmor(I)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    private ItemStack getCurrentArmor(EntityLivingBase entityLivingBase, int slotIn) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (entityLivingBase instanceof EntityPlayer) {
            String nameFirstLetter = entityLivingBase.getName().substring(0, 1);
            ItemStack item = new ItemStack(Item.getItemFromBlock(Blocks.lit_pumpkin));
            Matcher matcher = otherHalfLetters.matcher(nameFirstLetter);
            if (matcher.matches()) {
                item = new ItemStack(Item.getItemFromBlock(Blocks.pumpkin));
            }
            if (!main.getConfigValues().isRemoteDisabled(Feature.HALLOWEEN) && main.getUtils().isHalloween() && !entityLivingBase.isInvisible()) {
                return item;
            }
        }
        return entityLivingBase.getCurrentArmor(slotIn);
    }
}
