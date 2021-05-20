package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.features.EntityOutlines.EntityOutlineRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RenderGlobalHook {

    private static boolean stopLookingForOptifine = false;

    private static Method isFastRender = null;
    private static Method isShaders = null;
    private static Method isAntialiasing = null;

    private static final Logger logger = SkyblockAddons.getLogger();

    public static boolean shouldRenderSkyblockItemOutlines() {
        Minecraft mc = Minecraft.getMinecraft();
        RenderGlobal renderGlobal = mc.renderGlobal;
        SkyblockAddons main = SkyblockAddons.getInstance();

        // Vanilla Conditions
        if (renderGlobal.entityOutlineFramebuffer == null || renderGlobal.entityOutlineShader == null || mc.thePlayer == null)
            return false;

        // Skyblock Conditions
        if (!main.getUtils().isOnSkyblock()) {
            return false;
        }
        // If there are no outlines to render
        if (EntityOutlineRenderer.isCacheEmpty()) {
            return false;
        }

        // Optifine Conditions
        if (!stopLookingForOptifine && isFastRender == null) {
            try {
                Class<?> config = Class.forName("Config");

                try {
                    isFastRender = config.getMethod("isFastRender");
                    isShaders = config.getMethod("isShaders");
                    isAntialiasing = config.getMethod("isAntialiasing");
                } catch (Exception ex) {
                    logger.warn("Couldn't find Optifine methods for entity outlines.");
                    stopLookingForOptifine = true;
                }
            } catch (Exception ex) {
                logger.info("Couldn't find Optifine for entity outlines.");
                stopLookingForOptifine = true;
            }
        }

        boolean isFastRenderValue = false;
        boolean isShadersValue = false;
        boolean isAntialiasingValue = false;
        if (isFastRender != null) {
            try {
                isFastRenderValue = (boolean) isFastRender.invoke(null);
                isShadersValue = (boolean) isShaders.invoke(null);
                isAntialiasingValue = (boolean) isAntialiasing.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                logger.warn("An error occurred while calling Optifine methods for entity outlines...", ex);
            }
        }

        return !isFastRenderValue && !isShadersValue && !isAntialiasingValue;
    }

    public static void afterFramebufferDraw() {
        GlStateManager.enableDepth();
    }

    public static boolean blockRenderingSkyblockItemOutlines(ICamera camera, float partialTicks, double x, double y, double z) {
        return EntityOutlineRenderer.renderEntityOutlines(camera, partialTicks, x, y, z);
    }

    public static void onAddBlockBreakParticle(int breakerId, BlockPos pos, int progress) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        // On public islands, hypixel sends a progress = 10 update once it registers the start of block breaking
        if (breakerId == 0 && main.getUtils().getLocation() != Location.ISLAND &&
                pos.equals(MinecraftHook.prevClickBlock) && progress == 10) {
            //System.out.println(progress);
            MinecraftHook.startMineTime = System.currentTimeMillis();
        }

    }
}
