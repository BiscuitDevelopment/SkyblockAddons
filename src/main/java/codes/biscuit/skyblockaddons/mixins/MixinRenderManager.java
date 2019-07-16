package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public abstract class MixinRenderManager {

    @Inject(method = "shouldRender", at = @At(value = "HEAD"), cancellable = true)
    private void shouldRenderRedirect(Entity entityIn, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (entityIn instanceof EntityItem &&
                    entityIn.ridingEntity instanceof EntityZombie && entityIn.ridingEntity.isInvisible()) { // Conditions for Skeleton Hat flying bones
            entityIn.ridingEntity.preventEntitySpawning = false; // To allow you to place blocks
            if (!SkyblockAddons.INSTANCE.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_BONES)) {
                cir.setReturnValue(false);
            }
        }
    }
}
