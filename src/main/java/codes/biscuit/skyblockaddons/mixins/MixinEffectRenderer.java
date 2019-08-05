package codes.biscuit.skyblockaddons.mixins;

import net.minecraft.client.particle.EffectRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EffectRenderer.class)
public class MixinEffectRenderer {

//    @Inject(method = "spawnEffectParticle", at = @At(value = "HEAD"), cancellable = true)
//    private void dropOneItemConfirmation(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int[] parameters, CallbackInfoReturnable<EntityFX> cir) {
//        particleBlocks.add(new BlockPos(xCoord, yCoord, zCoord));
//    }
}
