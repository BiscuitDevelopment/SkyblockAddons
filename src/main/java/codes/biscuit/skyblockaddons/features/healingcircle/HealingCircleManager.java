package codes.biscuit.skyblockaddons.features.healingcircle;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Set;

public class HealingCircleManager {

    private static SkyblockAddons main = SkyblockAddons.getInstance();
    @Getter private static Set<HealingCircle> healingCircles = Sets.newConcurrentHashSet();

    public static void addHealingCircleParticle(HealingCircleParticle healingCircleParticle) {
        HealingCircle nearbyHealingCircle = null;
        for (HealingCircle healingCircle : healingCircles) {
            if (healingCircle.hasCachedCenterPoint()) {
                Point2D.Double circleCenter = healingCircle.getCircleCenter();
                if (healingCircleParticle.getPoint().distance(circleCenter.getX(), circleCenter.getY()) < (HealingCircle.DIAMETER + 2) / 2F) {
                    nearbyHealingCircle = healingCircle;
                    break;
                }
            } else {
                if (healingCircleParticle.getPoint().distance(healingCircle.getAverageX(), healingCircle.getAverageZ()) < HealingCircle.DIAMETER + 2) {
                    nearbyHealingCircle = healingCircle;
                    break;
                }
            }
        }

        if (nearbyHealingCircle != null) {
            nearbyHealingCircle.addPoint(healingCircleParticle);
        } else {
            healingCircles.add(new HealingCircle(healingCircleParticle));
        }
    }

    public static void renderHealingCircleOverlays(float partialTicks) {
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_HEALING_CIRCLE_WALL)) {

            Iterator<HealingCircle> healingCircleIterator = healingCircles.iterator();
            while (healingCircleIterator.hasNext()) {
                HealingCircle healingCircle = healingCircleIterator.next();

                healingCircle.removeOldParticles();
                if (System.currentTimeMillis() - healingCircle.getCreation() > 1000 && healingCircle.getParticlesPerSecond() < 10) {
                    healingCircleIterator.remove();
                    continue;
                }

                GlStateManager.pushMatrix();
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);

                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.enableDepth();
                GlStateManager.enableBlend();
                GlStateManager.depthFunc(GL11.GL_LEQUAL);
                GlStateManager.disableCull();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.enableAlpha();
                GlStateManager.disableTexture2D();

                Color color = main.getConfigValues().getColorObject(Feature.SHOW_HEALING_CIRCLE_WALL);
                GlStateManager.color(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, 0.2F);
                Point2D.Double circleCenter = healingCircle.getCircleCenter();
                if (circleCenter != null && !Double.isNaN(circleCenter.getX()) && !Double.isNaN(circleCenter.getY())) {
                    main.getUtils().drawCylinder(circleCenter.getX(), 0, circleCenter.getY(), HealingCircle.DIAMETER / 2F, 255, partialTicks);
                }

                GlStateManager.enableCull();
                GlStateManager.enableTexture2D();
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }
        }
    }
}
