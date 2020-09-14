package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private static Set<Long> nightVisionEffectsToRemove = new HashSet<>();

    public static boolean onRemovePotionEffect(EntityLivingBase entityLivingBase, int potionID) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        // 16 -> Night Vision
        if (potionID == 16 && entityLivingBase == Minecraft.getMinecraft().thePlayer &&
                main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.AVOID_BLINKING_NIGHT_VISION)) {

            long now = System.currentTimeMillis();
            nightVisionEffectsToRemove.add(now);

            main.getNewScheduler().scheduleDelayedTask(new SkyblockRunnable() {
                @Override
                public void run() {
                    if (nightVisionEffectsToRemove.remove(now)) {
                        entityLivingBase.removePotionEffect(potionID);
                    }
                }
            }, 2);

            return true;
        }

        return false;
    }

    public static void onAddPotionEffect(EntityLivingBase entityLivingBase, PotionEffect potionEffect) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        // 16 -> Night Vision, Night Vision Charm duration is 300 ticks...
        if (potionEffect.getPotionID() == 16 && potionEffect.getDuration() == 300 && entityLivingBase == Minecraft.getMinecraft().thePlayer &&
                main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.AVOID_BLINKING_NIGHT_VISION)) {
            nightVisionEffectsToRemove.clear();
        }
    }
}
