package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.OverlayEffectRenderer;
import codes.biscuit.skyblockaddons.features.fishParticles.FishParticleManager;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleManager;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleParticle;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityAuraFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFishWakeFX;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashSet;
import java.util.Set;



public class EffectRendererHook {

    private static Set<OverlayEffectRenderer> effectRenderers = new HashSet<>();

    @SuppressWarnings("unused")
    public static void onAddParticle(EntityFX entity) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;

        if (main.getUtils().isOnSkyblock()) {
            if (main.getUtils().isInDungeon() && main.getConfigValues().isEnabled(Feature.SHOW_HEALING_CIRCLE_WALL) && entity instanceof EntityAuraFX && entity.posY % 1 == 0.0D) {
                HealingCircleManager.addHealingCircleParticle(new HealingCircleParticle(entity.posX, entity.posZ));
            }
            else if (player != null && player.fishEntity != null && main.getConfigValues().isEnabled(Feature.FISHING_PARTICLE_OVERLAY) && entity instanceof EntityFishWakeFX) {
                FishParticleManager.onFishWakeSpawn((EntityFishWakeFX) entity);
            }
        }
    }

    @Getter
    public static class OverlayInfo {
        private final float rotationX;
        private final float rotationZ;
        private final float rotationYZ;
        private final float rotationXY;
        private final float rotationXZ;
        private final float partialTicks;
        private final TextureManager renderer;
        private final WorldRenderer worldRenderer;
        private final Entity renderViewEntity;


        public OverlayInfo (float thePartialTicks) {
            rotationX = ActiveRenderInfo.getRotationX();
            rotationZ = ActiveRenderInfo.getRotationZ();
            rotationYZ = ActiveRenderInfo.getRotationYZ();
            rotationXY = ActiveRenderInfo.getRotationXY();
            rotationXZ = ActiveRenderInfo.getRotationXZ();
            partialTicks = thePartialTicks;
            renderer = Minecraft.getMinecraft().effectRenderer.renderer;
            worldRenderer = Tessellator.getInstance().getWorldRenderer();
            renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        }

    }

    /**
     * Called every frame directly after particle rendering to overlay modified particles to the screen.
     * @param partialTicks a float in [0, 1) indicating the progress to the next tick
     */
    @SuppressWarnings("unused")
    public static void renderParticleOverlays(float partialTicks) {
        OverlayInfo info = new OverlayInfo(partialTicks);

        for (OverlayEffectRenderer renderer : effectRenderers) {
            renderer.renderOverlayParticles(info);
        }
    }


    /**
     * Called from {@link OverlayEffectRenderer} during object initialization to render the registered particles every frame.
     * @param renderer the attached renderer
     */
    public static void registerOverlay(OverlayEffectRenderer renderer) {
        effectRenderers.add(renderer);
    }
}
