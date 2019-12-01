package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;

public class RenderManagerHook {

    public static void shouldRender(Entity entityIn, ReturnValue<Boolean> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            if (entityIn instanceof EntityItem &&
                    entityIn.ridingEntity instanceof EntityArmorStand && entityIn.ridingEntity.isInvisible()) { // Conditions for skeleton helmet flying bones
                if (main.getConfigValues().isEnabled(Feature.HIDE_BONES)) {
                    returnValue.cancel();
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_AUCTION_HOUSE_PLAYERS) && entityIn instanceof EntityOtherPlayerMP) {
                if (EnumUtils.SkyblockNPC.isNearNPC(entityIn)) {
                    returnValue.cancel();
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_IN_LOBBY)) {
                if (main.getUtils().getLocation() == EnumUtils.Location.VILLAGE || main.getUtils().getLocation() == EnumUtils.Location.AUCTION_HOUSE ||
                        main.getUtils().getLocation() == EnumUtils.Location.BANK) {
                    if ((entityIn instanceof EntityOtherPlayerMP || entityIn instanceof EntityFX || entityIn instanceof EntityItemFrame) &&
                            entityIn.getDistanceToEntity(Minecraft.getMinecraft().thePlayer) > 7) {
                        returnValue.cancel();
                    }
                }
            }
        }
    }
}
