package codes.biscuit.skyblockaddons.mixins.old;

import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LayerCustomHead.class)
public class MixinEntityLayerCustomHead {

//    private final Pattern otherHalfLetters = Pattern.compile("[a-z0-9_]");
//
//    @Redirect(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getCurrentArmor(I)Lnet/minecraft/item/ItemStack;", ordinal = 0))
//    private ItemStack getCurrentArmor(EntityLivingBase entityLivingBase, int slotIn) {
//        SkyblockAddons main = SkyblockAddons.getInstance();
//        if (entityLivingBase instanceof EntityPlayer) {
//            String nameFirstLetter = entityLivingBase.getName().substring(0, 1);
//            ItemStack item = new ItemStack(Item.getItemFromBlock(Blocks.lit_pumpkin));
//            Matcher matcher = otherHalfLetters.matcher(nameFirstLetter);
//            if (matcher.matches()) {
//                item = new ItemStack(Item.getItemFromBlock(Blocks.pumpkin));
//            }
//            if (!main.getConfigValues().isRemoteDisabled(Feature.HALLOWEEN) && main.getUtils().isHalloween() && !entityLivingBase.isInvisible()) {
//                return item;
//            }
//        }
//        return entityLivingBase.getCurrentArmor(slotIn);
//    }
}
