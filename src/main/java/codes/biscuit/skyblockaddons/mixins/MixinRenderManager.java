package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import codes.biscuit.skyblockaddons.features.JerryPresent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderManager.class)
public class MixinRenderManager {
    
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void shouldRender(Entity entityIn, ICamera cam, double three, double four, double five, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock()) {
            Location currentLocation = main.getUtils().getLocation();

            if (main.getConfigValues().isEnabled(Feature.HIDE_BONES) && main.getInventoryUtils().isWearingSkeletonHelmet()) {
                if (entityIn instanceof EntityItem && entityIn.ridingEntity instanceof EntityArmorStand && entityIn.ridingEntity.isInvisible()) {
                    EntityItem entityItem = (EntityItem) entityIn;
                    if (entityItem.getEntityItem().getItem().equals(Items.bone)) {
                        cir.setReturnValue(false);
                    }
                }
            }
            if (mc.theWorld != null && main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_NEAR_NPCS) && currentLocation != Location.GUEST_ISLAND && currentLocation != Location.THE_CATACOMBS) {
                if (entityIn instanceof EntityOtherPlayerMP && !NPCUtils.isNPC(entityIn) && NPCUtils.isNearNPC(entityIn)) {
                    cir.setReturnValue(false);
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_SPAWN_POINT_PLAYERS)) {
                BlockPos entityPosition = entityIn.getPosition();
                if (entityIn instanceof EntityPlayer && entityPosition.getX() == -2 && entityPosition.getY() == 70 && entityPosition.getZ() == -69 && currentLocation == Location.VILLAGE) {
                    cir.setReturnValue(false);
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_IN_LOBBY)) {
                if (currentLocation == Location.VILLAGE || currentLocation == Location.AUCTION_HOUSE || currentLocation == Location.BANK) {
                    if ((entityIn instanceof EntityOtherPlayerMP || entityIn instanceof EntityFX || entityIn instanceof EntityItemFrame) &&
                            !NPCUtils.isNPC(entityIn) && entityIn.getDistanceSqToEntity(mc.thePlayer) > (7 * 7)) {
                        cir.setReturnValue(false);
                    }
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_OTHER_PLAYERS_PRESENTS)) {
                JerryPresent present = JerryPresent.getJerryPresents().get(entityIn.getUniqueID());
                if (present != null && present.shouldHide()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
