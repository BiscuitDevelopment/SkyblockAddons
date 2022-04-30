package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderEnderman.class)
public class MixinRenderEnderman {
    @Shadow @Final private static ResourceLocation endermanTextures;
    private static final ResourceLocation BLANK_ENDERMAN_TEXTURE = new ResourceLocation("skyblockaddons", "blankenderman.png");

    @Inject(method = "getEntityTexture", at = @At("HEAD"), cancellable = true)
    protected void checkBlankTexture(CallbackInfoReturnable<ResourceLocation> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getUtils().getLocation() == Location.DRAGONS_NEST && main.getConfigValues().isEnabled(Feature.CHANGE_ZEALOT_COLOR)) {
            cir.setReturnValue(BLANK_ENDERMAN_TEXTURE);
        }
    }
}
