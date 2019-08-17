package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityZombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public class MixinRenderManager {

    @Inject(method = "shouldRender", at = @At(value = "HEAD"), cancellable = true)
    private void shouldRenderRedirect(Entity entityIn, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        double auctionX = 17.5;
        double auctionY = 71;
        double auctionZ = -78.5;
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (entityIn instanceof EntityItem &&
                entityIn.getRidingEntity() instanceof EntityZombie && entityIn.getRidingEntity().isInvisible()) { // Conditions for Skeleton Hat flying bones
            entityIn.getRidingEntity().preventEntitySpawning = false; // To allow you to place blocks
            if (!main.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_BONES)) {
                cir.setReturnValue(false);
            }
        }
        EnumUtils.Location location = main.getUtils().getLocation();
        if ((location == EnumUtils.Location.VILLAGE || location == EnumUtils.Location.AUCTION_HOUSE)) {
            if (!main.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_AUCTION_HOUSE_PLAYERS) && entityIn instanceof EntityOtherPlayerMP) {
                if (entityIn.getDistance(auctionX, auctionY, auctionZ) <= 3 && (entityIn.posX != auctionX || entityIn.posY != auctionY || entityIn.posZ != auctionZ)) { // Coords of the auction master.
                    cir.setReturnValue(false);
                }
            }
            if (!main.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_PLAYERS_IN_LOBBY) &&
                    (entityIn instanceof EntityOtherPlayerMP /*TODO Particles are no longer entities|| entityIn instanceof Particle*/ || entityIn instanceof EntityItemFrame) &&
                    entityIn.getDistance(Minecraft.getMinecraft().player) > 7) {
                cir.setReturnValue(false);
            }
        }
    }
}
