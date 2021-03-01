package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.FishParticleManager;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleManager;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleParticle;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenTexturedShader;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityAuraFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFishWakeFX;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



public class EffectRendererHook {

    private static Set<EntityFX>[][] particlesToOutline = initializeOutlines();

    @SuppressWarnings("unchecked")
    private static Set<EntityFX>[][] initializeOutlines() {
        Set<EntityFX>[][] tmp = new Set[4][2];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                tmp[i][j] = new HashSet<>();
            }
        }
        return tmp;
    }

    public static void addParticleToOutline(EntityFX particle) {
        if (particle == null) {
            return;
        }
        int i = particle.getFXLayer();
        int j = particle.getAlpha() != 1.0F ? 0 : 1;

        if (particlesToOutline[i][j].size() >= 4000)
        {
            Iterator<EntityFX> itr = particlesToOutline[i][j].iterator();
            itr.next();
            itr.remove();
        }
        particlesToOutline[i][j].add(particle);
    }

    public static void clearParticleCache() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                particlesToOutline[i][j].clear();
            }
        }
    }


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


    public static void renderParticleOutlines(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        float rotationX = ActiveRenderInfo.getRotationX();
        float rotationZ = ActiveRenderInfo.getRotationZ();
        float rotationYZ = ActiveRenderInfo.getRotationYZ();
        float rotationXY = ActiveRenderInfo.getRotationXY();
        float rotationXZ = ActiveRenderInfo.getRotationXZ();

        Entity entity = mc.getRenderViewEntity();
        EntityFX.interpPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
        EntityFX.interpPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
        EntityFX.interpPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;

        ResourceLocation particleTextures = EffectRenderer.particleTextures;

        TextureManager renderer = mc.effectRenderer.renderer;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        Logger logger = SkyblockAddons.getLogger();

        GlStateManager.disableFog();
        RenderHelper.disableStandardItemLighting();

        // Use chroma shader for now... TODO: Add option to change particle color
        ColorUtils.bindWhite();
        ShaderManager.getInstance().enableShader(ChromaScreenTexturedShader.class);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                particlesToOutline[i][j].removeIf(entityFX -> entityFX.isDead);
                if (!particlesToOutline[i][j].isEmpty()) {
                    GlStateManager.depthMask(j == 1);
                    if (i == 1) {
                        renderer.bindTexture(TextureMap.locationBlocksTexture);
                    } else {
                        renderer.bindTexture(particleTextures);
                    }

                    GlStateManager.enableColorMaterial();
                    RenderGlobalHook.enableOutlineMode(0xFFFFFF); // white TODO: Option to change color...

                    worldRenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

                    for (EntityFX effect : particlesToOutline[i][j]) {
                        try {
                            effect.renderParticle(worldRenderer, entity, partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
                        }
                        catch (Throwable ex) {
                            logger.warn("Couldn't render outline for effect " + effect.toString() + ".");
                            logger.catching(ex); // Just move on to the next entity...
                        }
                    }
                    tessellator.draw();
                    RenderGlobalHook.disableOutlineMode();
                    GlStateManager.disableColorMaterial();
                }
            }
        }
        ShaderManager.getInstance().disableShader();
    }
}
