package codes.biscuit.skyblockaddons.features.EntityOutlines;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent.Type.NO_XRAY;
import static codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent.Type.XRAY;

/**
 * Class to handle all entity outlining, including xray and no-xray rendering
 * Features that include entity outlining should subscribe to the {@link RenderEntityOutlineEvent}.
 * <p>
 * See {@link FeatureItemOutlines} for an example of how to add specific entities based on predicates
 */
public class EntityOutlineRenderer {

    private static final Logger logger = SkyblockAddons.getLogger();
    private static final CachedInfo entityRenderCache = new CachedInfo();
    private static boolean stopLookingForOptifine = false;
    private static Method isFastRender = null;
    private static Method isShaders = null;
    private static Method isAntialiasing = null;
    private static final Framebuffer swapBuffer = initSwapBuffer();

    /**
     * @return a new framebuffer with the size of the main framebuffer
     */
    private static Framebuffer initSwapBuffer() {
        Framebuffer main = Minecraft.getMinecraft().getFramebuffer();
        Framebuffer framebuffer = new Framebuffer(main.framebufferTextureWidth, main.framebufferTextureHeight, true);
        framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        return framebuffer;
    }


    /**
     * Renders xray and no-xray entity outlines.
     *
     * @param camera       the current camera
     * @param partialTicks the progress to the next tick
     * @param x            the camera x position
     * @param y            the camera y position
     * @param z            the camera z position
     */
    public static boolean renderEntityOutlines(ICamera camera, float partialTicks, double x, double y, double z) {
        boolean shouldRenderOutlines = shouldRenderEntityOutlines();

        if (shouldRenderOutlines && !isCacheEmpty() && MinecraftForgeClient.getRenderPass() == 0) {
            Minecraft mc = Minecraft.getMinecraft();
            RenderGlobal renderGlobal = mc.renderGlobal;
            RenderManager renderManager = mc.getRenderManager();

            mc.theWorld.theProfiler.endStartSection("entityOutlines");

            // Clear and bind the outline framebuffer
            renderGlobal.entityOutlineFramebuffer.framebufferClear();
            renderGlobal.entityOutlineFramebuffer.bindFramebuffer(false);

            // Vanilla options
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableFog();
            mc.getRenderManager().setRenderOutlines(true);

            // SBA options
            DrawUtils.enableOutlineMode();

            // Render x-ray outlines first, ignoring the depth buffer bit
            if (!isXrayCacheEmpty()) {

                // Xray is enabled by disabling depth testing
                GlStateManager.depthFunc(GL11.GL_ALWAYS);
                for (Map.Entry<Entity, Integer> entityAndColor : entityRenderCache.getXrayCache().entrySet()) {
                    // Test if the entity should render, given the player's camera position
                    if (shouldRender(camera, entityAndColor.getKey(), x, y, z)) {
                        try {
                            if (!(entityAndColor.getKey() instanceof EntityLivingBase)) {
                                DrawUtils.outlineColor(entityAndColor.getValue());
                            }
                            renderManager.renderEntityStatic(entityAndColor.getKey(), partialTicks, true);
                        } catch (Exception ignored) {
                        }
                    }
                }
                // Reset depth function
                GlStateManager.depthFunc(GL11.GL_LESS);
            }
            // Render no-xray outlines second, taking into consideration the depth bit
            if (!isNoXrayCacheEmpty()) {
                if (!isNoOutlineCacheEmpty()) {
                    // Render other entities + terrain that may occlude an entity outline into a depth buffer
                    copyBuffers(mc.getFramebuffer(), swapBuffer, GL11.GL_DEPTH_BUFFER_BIT);
                    swapBuffer.bindFramebuffer(false);
                    // Copy terrain + other entities depth into outline frame buffer to now switch to no-xray outlines
                    if (entityRenderCache.getNoOutlineCache() != null) {
                        for (Entity entity : entityRenderCache.getNoOutlineCache()) {
                            // Test if the entity should render, given the player's instantaneous camera position
                            if (shouldRender(camera, entity, x, y, z)) {
                                try {
                                    renderManager.renderEntityStatic(entity, partialTicks, true);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }

                    // Copy the entire depth buffer of everything that might occlude outline to outline framebuffer
                    copyBuffers(swapBuffer, renderGlobal.entityOutlineFramebuffer, GL11.GL_DEPTH_BUFFER_BIT);
                    renderGlobal.entityOutlineFramebuffer.bindFramebuffer(false);
                }
                // If there are no entities that can occlude the outlines, just copy the terrain depth buffer over
                else {
                    copyBuffers(mc.getFramebuffer(), renderGlobal.entityOutlineFramebuffer, GL11.GL_DEPTH_BUFFER_BIT);
                }

                // Xray disabled by re-enabling traditional depth testing
                for (Map.Entry<Entity, Integer> entityAndColor : entityRenderCache.getNoXrayCache().entrySet()) {
                    // Test if the entity should render, given the player's instantaneous camera position
                    if (shouldRender(camera, entityAndColor.getKey(), x, y, z)) {
                        try {
                            if (!(entityAndColor.getKey() instanceof EntityLivingBase)) {
                                DrawUtils.outlineColor(entityAndColor.getValue());
                            }
                            renderManager.renderEntityStatic(entityAndColor.getKey(), partialTicks, true);

                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            // SBA options
            DrawUtils.disableOutlineMode();

            // Vanilla options
            RenderHelper.enableStandardItemLighting();
            mc.getRenderManager().setRenderOutlines(false);

            // Load the outline shader
            GlStateManager.depthMask(false);
            renderGlobal.entityOutlineShader.loadShaderGroup(partialTicks);
            GlStateManager.depthMask(true);

            // Reset GL/framebuffers for next render layers
            GlStateManager.enableLighting();
            mc.getFramebuffer().bindFramebuffer(false);
            GlStateManager.enableFog();
            GlStateManager.enableBlend();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
        }

        return !shouldRenderOutlines;
    }


    public static Integer getCustomOutlineColor(EntityLivingBase entity) {
        if (entityRenderCache.getXrayCache() != null && entityRenderCache.getXrayCache().containsKey(entity)) {
            return entityRenderCache.getXrayCache().get(entity);
        }
        if (entityRenderCache.getNoXrayCache() != null && entityRenderCache.getNoXrayCache().containsKey(entity)) {
            return entityRenderCache.getNoXrayCache().get(entity);
        }
        return null;
    }

    /**
     * Caches optifine settings and determines whether outlines should be rendered
     *
     * @return {@code true} iff outlines should be rendered
     */
    public static boolean shouldRenderEntityOutlines() {
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

    /**
     * Apply the same rendering standards as in {@link net.minecraft.client.renderer.RenderGlobal#renderEntities(Entity, ICamera, float)} lines 659 to 669
     *
     * @param camera the current camera
     * @param entity the entity to render
     * @param x      the camera x position
     * @param y      the camera y position
     * @param z      the camera z position
     * @return whether the entity should be rendered
     */
    private static boolean shouldRender(ICamera camera, Entity entity, double x, double y, double z) {
        Minecraft mc = Minecraft.getMinecraft();
        //if (considerPass && !entity.shouldRenderInPass(MinecraftForgeClient.getRenderPass())) {
        //    return false;
        //}
        // Only render the view entity when sleeping or in 3rd person mode mode
        if (entity == mc.getRenderViewEntity() &&
                !((mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping()) ||
                        mc.gameSettings.thirdPersonView != 0)) {
            return false;
        }
        // Only render if renderManager would render and the world is loaded at the entity
        return mc.theWorld.isBlockLoaded(new BlockPos(entity)) && (mc.getRenderManager().shouldRender(entity, camera, x, y, z) || entity.riddenByEntity == mc.thePlayer);
    }

    /**
     * Function that copies a portion of a framebuffer to another framebuffer.
     * <p>
     * Note that this requires GL3.0 to function properly
     * <p>
     * The major use of this function is to copy the depth-buffer portion of the world framebuffer to the entity outline framebuffer.
     * This enables us to perform no-xray outlining on entities, as we can use the world framebuffer's depth testing on the outline frame buffer
     *
     * @param frameToCopy   the framebuffer from which we are copying data
     * @param frameToPaste  the framebuffer onto which we are copying the data
     * @param buffersToCopy the bit mask indicating the sections to copy (see {@link GL11#GL_DEPTH_BUFFER_BIT}, {@link GL11#GL_COLOR_BUFFER_BIT}, {@link GL11#GL_STENCIL_BUFFER_BIT})
     */
    private static void copyBuffers(Framebuffer frameToCopy, Framebuffer frameToPaste, int buffersToCopy) {
        if (OpenGlHelper.isFramebufferEnabled()) {
            OpenGlHelper.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, frameToCopy.framebufferObject);
            OpenGlHelper.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameToPaste.framebufferObject);
            GL30.glBlitFramebuffer(0, 0, frameToCopy.framebufferWidth, frameToCopy.framebufferHeight,
                    0, 0, frameToPaste.framebufferWidth, frameToPaste.framebufferHeight,
                    buffersToCopy, GL11.GL_NEAREST);
        }
    }

    public static boolean isCacheEmpty() {
        return isXrayCacheEmpty() && isNoXrayCacheEmpty();
    }

    private static boolean isXrayCacheEmpty() {
        return entityRenderCache.xrayCache == null || entityRenderCache.xrayCache.isEmpty();
    }

    private static boolean isNoXrayCacheEmpty() {
        return entityRenderCache.noXrayCache == null || entityRenderCache.noXrayCache.isEmpty();
    }

    private static boolean isNoOutlineCacheEmpty() {
        return entityRenderCache.noOutlineCache == null || entityRenderCache.noOutlineCache.isEmpty();
    }

    private static boolean emptyLastTick = false;

    /**
     * Updates the cache at the start of every minecraft tick to improve efficiency.
     * Identifies and caches all entities in the world that should be outlined.
     * <p>
     * Calls to {@link #shouldRender(ICamera, Entity, double, double, double)} are frustum based, rely on partialTicks,
     * and so can't be updated on a per-tick basis without losing information.
     * <p>
     * This works since entities are only updated once per tick, so the inclusion or exclusion of an entity
     * to be outlined can be cached each tick with no loss of data
     *
     * @param event the client tick event
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld != null && shouldRenderEntityOutlines()) {
                // These events need to be called in this specific order for the xray to have priority over the no xray
                // Get all entities to render xray outlines
                RenderEntityOutlineEvent xrayOutlineEvent = new RenderEntityOutlineEvent(XRAY, null);
                MinecraftForge.EVENT_BUS.post(xrayOutlineEvent);
                // Get all entities to render no xray outlines, using pre-filtered entities (no need to test xray outlined entities)
                RenderEntityOutlineEvent noxrayOutlineEvent = new RenderEntityOutlineEvent(NO_XRAY, xrayOutlineEvent.getEntitiesToChooseFrom());
                MinecraftForge.EVENT_BUS.post(noxrayOutlineEvent);
                // Cache the entities for future use
                entityRenderCache.setXrayCache(xrayOutlineEvent.getEntitiesToOutline());
                entityRenderCache.setNoXrayCache(noxrayOutlineEvent.getEntitiesToOutline());
                entityRenderCache.setNoOutlineCache(noxrayOutlineEvent.getEntitiesToChooseFrom());

                if (isCacheEmpty()) {
                    if (!emptyLastTick) {
                        mc.renderGlobal.entityOutlineFramebuffer.framebufferClear();
                    }
                    emptyLastTick = true;
                } else {
                    emptyLastTick = false;
                }
            } else if (!emptyLastTick) {
                entityRenderCache.setXrayCache(null);
                entityRenderCache.setNoXrayCache(null);
                entityRenderCache.setNoOutlineCache(null);
                mc.renderGlobal.entityOutlineFramebuffer.framebufferClear();
                emptyLastTick = true;
            }
        }
    }

    private static class CachedInfo {
        @Getter
        @Setter
        private HashMap<Entity, Integer> xrayCache = null;
        @Getter
        @Setter
        private HashMap<Entity, Integer> noXrayCache = null;
        @Getter
        @Setter
        private HashSet<Entity> noOutlineCache = null;
    }
}
