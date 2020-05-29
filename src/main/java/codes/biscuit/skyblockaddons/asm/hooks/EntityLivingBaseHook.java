package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

public class EntityLivingBaseHook {

    public static void onResetHurtTime(EntityLivingBase player) {
        if (player == Minecraft.getMinecraft().thePlayer) {
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (!main.getUtils().isOnSkyblock() || !main.getConfigValues().isEnabled(Feature.COMBAT_TIMER_DISPLAY)) {
                return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            List<Entity> nearEntities = mc.theWorld.getEntitiesWithinAABB(Entity.class,
                    new AxisAlignedBB(mc.thePlayer.posX - 3, mc.thePlayer.posY - 2, mc.thePlayer.posZ - 3, mc.thePlayer.posX + 3, mc.thePlayer.posY + 2, mc.thePlayer.posZ + 3));
            boolean foundPossibleAttacker = false;

            for (Entity entity : nearEntities) {
                if (entity instanceof EntityMob || entity instanceof EntityWolf || entity instanceof IProjectile) {
                    foundPossibleAttacker = true;
                    break;
                }
            }

            if (foundPossibleAttacker) {
                SkyblockAddons.getInstance().getUtils().setLastDamaged(System.currentTimeMillis());
            }
        }
    }
}
