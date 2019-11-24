package codes.biscuit.skyblockaddons.mixins.old;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "getMouseOver", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getMouseOver(float partialTicks, CallbackInfo ci, Entity entity, double d0, double d1, Vec3 vec3, boolean flag, boolean b, Vec3 vec31, Vec3 vec32, Vec3 vec33, float f, List<Entity> list, double d2, int j) {
        removeEntities(list);
    }

    // This method exists in a debug enviroment instead
    @Inject(method = "getMouseOver", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getMouseOver(float partialTicks, CallbackInfo ci, Entity entity, double d0, double d1, Vec3 vec3, boolean flag, int i, Vec3 vec31, Vec3 vec32, Vec3 vec33, float f, List<Entity> list, double d2, int j) {
        removeEntities(list);
    }

    @Inject(method = "getNightVisionBrightness", at = @At("HEAD"), cancellable = true)
    private void preventBlink(EntityLivingBase p_getNightVisionBrightness_1_, float p_getNightVisionBrightness_2_, CallbackInfoReturnable<Float> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isEnabled(Feature.AVOID_BLINKING_NIGHT_VISION)) {
            cir.setReturnValue(1.0F);
        }
    }

    private void removeEntities(List<Entity> list) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            if (!GuiScreen.isCtrlKeyDown() && main.getConfigValues().isEnabled(Feature.IGNORE_ITEM_FRAME_CLICKS)) {
                list.removeIf(listEntity -> listEntity instanceof EntityItemFrame);
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_AUCTION_HOUSE_PLAYERS)) {
                list.removeIf((entity -> entity instanceof EntityOtherPlayerMP && EnumUtils.SkyblockNPC.isNearNPC(entity)));
            }
        }
    }
}
