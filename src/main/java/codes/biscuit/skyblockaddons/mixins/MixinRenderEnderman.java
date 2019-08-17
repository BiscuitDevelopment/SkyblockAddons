package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderEnderman.class)
public abstract class MixinRenderEnderman {


    private ModelEnderman endermanModel = (ModelEnderman) getMainModel();

    @Shadow
    public abstract ModelEnderman getMainModel();
    private static final ResourceLocation pinkEndermanTexture = new ResourceLocation("skyblockaddons", "pinkenderman.png");

    //TODO add a toggle for this
    @Inject(method = "getEntityTexture", at = @At(value = "HEAD"), cancellable = true)
    private void getEntityTexture(EntityEnderman entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            if (endermanModel.isCarrying) {
                cir.setReturnValue(pinkEndermanTexture);
            }
        }
    }
}
