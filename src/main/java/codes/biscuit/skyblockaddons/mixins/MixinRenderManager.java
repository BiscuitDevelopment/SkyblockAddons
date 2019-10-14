package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
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

    @Inject(
            method = "shouldRender",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private void shouldRender(Entity entityIn, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        EnumUtils.Location location = main.getUtils().getLocation();

        if (main.getConfigValues().isEnabled(Feature.HIDE_BONES)) {
            if (entityIn instanceof EntityItem && entityIn.isRiding() &&
                    entityIn.getRidingEntity() instanceof EntityArmorStand && entityIn.getRidingEntity().isInvisible()) { // Conditions for skeleton helmet flying bones
                cir.setReturnValue(false);
            }
        }

        if (main.getConfigValues().isEnabled(Feature.HIDE_AUCTION_HOUSE_PLAYERS) && entityIn instanceof EntityOtherPlayerMP) {
            for (EnumUtils.SkyblockNPC npc : EnumUtils.SkyblockNPC.values()) {
                if (npc.isAtLocation(location)) {
                    if (npc.isNearEntity(entityIn))
                        cir.setReturnValue(false);
                }
            }
        }

        if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_IN_LOBBY)) {
            if (location == EnumUtils.Location.VILLAGE ||
                    location == EnumUtils.Location.AUCTION_HOUSE ||
                    location == EnumUtils.Location.BANK) {
                // TODO: Particles are no longer entities (entityIn instanceof Particle)
                if ((entityIn instanceof EntityOtherPlayerMP || entityIn instanceof EntityItemFrame) &&
                entityIn.getDistance(Minecraft.getMinecraft().player) > 7) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

}