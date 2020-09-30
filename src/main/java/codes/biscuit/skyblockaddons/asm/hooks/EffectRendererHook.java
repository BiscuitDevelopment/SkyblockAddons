package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleManager;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleParticle;
import net.minecraft.client.particle.EntityAuraFX;
import net.minecraft.client.particle.EntityFX;

public class EffectRendererHook {

    public static void onAddParticle(EntityFX entity) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isEnabled(Feature.SHOW_HEALING_CIRCLE_WALL) && entity instanceof EntityAuraFX && entity.posY % 1 == 0.0D) {
            HealingCircleManager.addHealingCircleParticle(new HealingCircleParticle(entity.posX, entity.posZ));
        }
    }
}
