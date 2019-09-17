package codes.biscuit.skyblockaddons.mixins;

import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity {

    private boolean isCoolPerson;

    @Redirect(method = "rotateCorpse", at = @At(value = "INVOKE",
            target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean equals(String s, Object anObject) {
        isCoolPerson = s.equals("Biscut");
        return s.equals("Dinnerbone") || isCoolPerson;
    } //cough nothing to see here

    @Redirect(method = "rotateCorpse", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isWearing(Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z", ordinal = 0))
    private boolean isWearing(EntityPlayer entityPlayer, EnumPlayerModelParts p_175148_1_) {
        return (!isCoolPerson && entityPlayer.isWearing(p_175148_1_)) ||
                (isCoolPerson && !entityPlayer.isWearing(p_175148_1_));
    }
}


