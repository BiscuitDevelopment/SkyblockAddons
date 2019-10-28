package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity<T extends EntityLivingBase> {

    private boolean isCoolPerson;

    @Redirect(method = "rotateCorpse", at = @At(value = "INVOKE",
            target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean equals(String s, Object anObject) {
        isCoolPerson = s.equals("Biscut") || s.equals("Pinpointed");
        // no don't ask to be added lol
        return s.equals("Dinnerbone") || isCoolPerson;
    } //cough nothing to see here

    @Redirect(method = "rotateCorpse", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isWearing(Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z", ordinal = 0))
    private boolean isWearing(EntityPlayer entityPlayer, EnumPlayerModelParts p_175148_1_) {
        return (!isCoolPerson && entityPlayer.isWearing(p_175148_1_)) ||
                (isCoolPerson && !entityPlayer.isWearing(p_175148_1_));
    }

    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", shift = At.Shift.BEFORE, ordinal = 0))
    private void renderBefore(T entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_,
                             float scaleFactor, CallbackInfo ci) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (!main.getConfigValues().isRemoteDisabled(Feature.HALLOWEEN) && main.getUtils().isHalloween() && entitylivingbaseIn instanceof EntityPlayer) {
            GlStateManager.pushMatrix(); // this makes players translucent
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.40F);
//            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.alphaFunc(516, 0.003921569F);
        }
    }

    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V",
            shift = At.Shift.AFTER, ordinal = 0))
    private void renderAfter(T entitylivingbaseIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float scaleFactor, CallbackInfo ci) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (!main.getConfigValues().isRemoteDisabled(Feature.HALLOWEEN) && main.getUtils().isHalloween() && entitylivingbaseIn instanceof EntityPlayer) {
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.popMatrix();
//            GlStateManager.depthMask(true);
        }
    }
}


