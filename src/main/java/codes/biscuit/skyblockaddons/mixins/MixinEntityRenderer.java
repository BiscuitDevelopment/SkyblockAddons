package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "getMouseOver", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getMouseOver(float partialTicks, CallbackInfo ci, Entity entity, double d0, Vec3d Vec3d, boolean flag, boolean b, double d1, Vec3d Vec3d1, Vec3d Vec3d2, Vec3d Vec3d3, float f, List<Entity> list, double d2, int j) {
        removeEntities(list);
    }

    // This method exists in a debug enviroment instead
    @Inject(method = "getMouseOver", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void getMouseOver(float partialTicks, CallbackInfo ci, Entity entity, double d0, Vec3d Vec3d, boolean flag, int i, double d1, Vec3d Vec3d1, Vec3d Vec3d2, Vec3d Vec3d3, float f, List<Entity> list, double d2, int j) {
        removeEntities(list);
    }

    private void removeEntities(List<Entity> list) {
        if (SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            // conditions for the invisible zombie that Skeleton hat bones are riding
            //list.removeIf(listEntity -> listEntity instanceof EntityZombie && listEntity.isInvisible() && listEntity.getRidingEntity() instanceof EntityItem);

            if (!GuiScreen.isCtrlKeyDown() && !SkyblockAddons.getInstance().getConfigValues().isDisabled(Feature.IGNORE_ITEM_FRAME_CLICKS))
                list.removeIf(listEntity -> listEntity instanceof EntityItemFrame);

            if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.HIDE_AUCTION_HOUSE_PLAYERS))
                list.removeIf(EnumUtils.SkyblockNPC.AUCTION_MASTER::isNearEntity);
        }
    }

}
