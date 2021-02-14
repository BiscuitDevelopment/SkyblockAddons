package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.FishParticleManager;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleManager;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityAuraFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFishWakeFX;
import net.minecraft.item.ItemFishingRod;

public class EffectRendererHook {

    public static void onAddParticle(EntityFX entity) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        //if (main.getUtils().isOnSkyblock()) {
            if (main.getUtils().isInDungeon() && main.getConfigValues().isEnabled(Feature.SHOW_HEALING_CIRCLE_WALL) && entity instanceof EntityAuraFX && entity.posY % 1 == 0.0D) {
                HealingCircleManager.addHealingCircleParticle(new HealingCircleParticle(entity.posX, entity.posZ));
            }
            // TODO: Check if bobber is cast
            else if (Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemFishingRod && main.getConfigValues().isEnabled(Feature.FISHING_PARTICLE_OVERLAY) && entity instanceof EntityFishWakeFX) {
                FishParticleManager.onFishWakeSpawn((EntityFishWakeFX) entity);
            }
        //}
    }
}
