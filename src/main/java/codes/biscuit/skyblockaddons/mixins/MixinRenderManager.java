package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public class MixinRenderManager {

    @Inject(method = "shouldRender", at = @At(value = "HEAD"), cancellable = true)
    private void shouldRenderRedirect(Entity entityIn, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            if (entityIn instanceof EntityItem &&
                    entityIn.ridingEntity instanceof EntityArmorStand && entityIn.ridingEntity.isInvisible()) { // Conditions for skeleton helmet flying bones
                if (main.getConfigValues().isEnabled(Feature.HIDE_BONES)) {
                    cir.setReturnValue(false);
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_AUCTION_HOUSE_PLAYERS) && entityIn instanceof EntityOtherPlayerMP) {
                if (EnumUtils.SkyblockNPC.isNearNPC(entityIn)) {
                    cir.setReturnValue(false);
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_IN_LOBBY)) {
                if (main.getUtils().getLocation() == EnumUtils.Location.VILLAGE || main.getUtils().getLocation() == EnumUtils.Location.AUCTION_HOUSE ||
                        main.getUtils().getLocation() == EnumUtils.Location.BANK) {
                    if ((entityIn instanceof EntityOtherPlayerMP || entityIn instanceof EntityFX || entityIn instanceof EntityItemFrame) &&
                            entityIn.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) > 7) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }
}
