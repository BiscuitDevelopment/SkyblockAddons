package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class RenderManagerHook {

    private static final int HIDE_RADIUS_SQUARED = 7 * 7;

    public static void shouldRender(Entity entityIn, ReturnValue<Boolean> returnValue) {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock()) {
            Location currentLocation = main.getUtils().getLocation();

            if (main.getConfigValues().isEnabled(Feature.HIDE_BONES)) {
                ItemStack helmet = Minecraft.getMinecraft().thePlayer.getCurrentArmor(3);

                if (helmet != null && Objects.equals(ItemUtils.getSkyBlockItemID(helmet), "SKELETON_HELMET")) {
                    if (entityIn instanceof EntityItem &&
                            entityIn.ridingEntity instanceof EntityArmorStand && entityIn.ridingEntity.isInvisible()) {
                        EntityItem entityItem = (EntityItem) entityIn;

                        if (entityItem.getEntityItem().getItem().equals(Items.bone)) {
                            returnValue.cancel();
                        }
                    }
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_NEAR_NPCS) && mc.theWorld != null) {
                if (entityIn instanceof EntityOtherPlayerMP && !NPCUtils.isNPC(entityIn) && NPCUtils.isNearNPC(entityIn)) {
                    returnValue.cancel();
                }
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_IN_LOBBY)) {
                if (currentLocation == Location.VILLAGE || currentLocation == Location.AUCTION_HOUSE || currentLocation == Location.BANK) {
                    if ((entityIn instanceof EntityOtherPlayerMP || entityIn instanceof EntityFX || entityIn instanceof EntityItemFrame) &&
                            !NPCUtils.isNPC(entityIn) && entityIn.getDistanceSqToEntity(mc.thePlayer) > HIDE_RADIUS_SQUARED) {
                        returnValue.cancel();
                    }
                }
            }
        }
    }
}
