package codes.biscuit.skyblockaddons.mixins;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderLivingBase.class)
public class MixinRendererLivingEntity {

    private boolean isCoolPerson;

    @Redirect(method = "applyRotations", at = @At(value = "INVOKE",
            target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean equals(String dinnerbone, Object s) {
        isCoolPerson = (s.equals("Biscut") || s.equals("ErdbeerbaerLP")); //cough absolutely nothing changed here
        return s.equals(dinnerbone) || isCoolPerson;
    } //cough nothing to see here

    @Redirect(method = "applyRotations", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isWearing(Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z", ordinal = 0))
    private boolean isWearing(EntityPlayer entityPlayer, EnumPlayerModelParts p_175148_1_) {
        return isCoolPerson || entityPlayer.isWearing(p_175148_1_);
    }
}


