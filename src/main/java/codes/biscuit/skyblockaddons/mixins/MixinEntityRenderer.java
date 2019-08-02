package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "getMouseOver", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void intersectsWith(float partialTicks, CallbackInfo ci, Entity entity, double d0, double d1, Vec3 vec3, boolean flag, boolean b, Vec3 vec31, Vec3 vec32, Vec3 vec33, float f, List<Entity> list, double d2, int i) {
        if (SkyblockAddons.instance.getUtils().isOnSkyblock()) { // conditions for the invisible zombie that Skeleton hat bones are riding
            list.removeIf(listEntity -> listEntity instanceof EntityZombie && listEntity.isInvisible() && listEntity.riddenByEntity instanceof EntityItem);
            if (!SkyblockAddons.instance.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_AUCTION_HOUSE_PLAYERS)) {
                double auctionX = 17.5;
                double auctionY = 71;
                double auctionZ = -78.5;
                list.removeIf(listEntity -> listEntity.getDistance(auctionX, auctionY, auctionZ) <= 3 && (listEntity.posX != auctionX || listEntity.posY != auctionY || listEntity.posZ != auctionZ));
            }
        }
    }
}
