package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

public class EntityLivingBaseHook {

    public static void onResetHurtTime(EntityLivingBase entity) {
        if (entity == Minecraft.getMinecraft().thePlayer) {
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (!main.getUtils().isOnSkyblock() || !main.getConfigValues().isEnabled(Feature.COMBAT_TIMER_DISPLAY)) {
                return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            List<Entity> nearEntities = mc.theWorld.getEntitiesWithinAABB(Entity.class,
                    new AxisAlignedBB(mc.thePlayer.posX - 3, mc.thePlayer.posY - 2, mc.thePlayer.posZ - 3, mc.thePlayer.posX + 3, mc.thePlayer.posY + 2, mc.thePlayer.posZ + 3));
            boolean foundPossibleAttacker = false;

            for (Entity nearEntity : nearEntities) {
                if (nearEntity instanceof EntityMob || nearEntity instanceof EntityWolf || nearEntity instanceof IProjectile) {
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
