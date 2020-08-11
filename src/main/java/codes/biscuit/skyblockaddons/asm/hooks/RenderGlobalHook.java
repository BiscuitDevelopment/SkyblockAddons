package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.util.List;

public class RenderGlobalHook {

    private static boolean stopLookingForOptifine = false;

    private static Method isFastRender = null;
    private static Method isShaders = null;
    private static Method isAntialiasing = null;

    private static FloatBuffer BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4);

    public static boolean shouldRenderSkyblockItemOutlines() {
        Minecraft mc = Minecraft.getMinecraft();
        RenderGlobal renderGlobal = mc.renderGlobal;
        SkyblockAddons main = SkyblockAddons.getInstance();

        // Vanilla Conditions
        if (renderGlobal.entityOutlineFramebuffer == null || renderGlobal.entityOutlineShader == null || mc.thePlayer == null) return false;

        // Skyblock Conditions
        if (!main.getUtils().isOnSkyblock()) {
            return false;
        }
        if (!main.getConfigValues().isEnabled(Feature.MAKE_DROPPED_ITEMS_GLOW) && !main.getConfigValues().isEnabled(Feature.MAKE_DUNGEON_TEAMMATES_GLOW)) {
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
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                    stopLookingForOptifine = true;
                    main.getLogger().warn("[SkyblockAddons] Couldn't find optifine methods for entity outlines...");
                }
            } catch (ClassNotFoundException ex1) {
                stopLookingForOptifine = true;
                main.getLogger().info("[SkyblockAddons] Didn't find optifine for entity outlines");
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
                ex.printStackTrace();
                SkyblockAddons.getInstance().getLogger().warn("[SkyblockAddons] Failed to call optifine methods for entity outlines...");
            }
        }

        return !isFastRenderValue && !isShadersValue && !isAntialiasingValue;
    }

    public static void afterFramebufferDraw() {
        GlStateManager.enableDepth();
    }

    private static AxisAlignedBB AUCTION_HOUSE = new AxisAlignedBB(14, 70, -89, 27, 81, -70);

    public static boolean blockRenderingSkyblockItemOutlines(ICamera camera, float partialTicks, double x, double y, double z, List<Entity> entities) {
        boolean shouldRenderOutlines = shouldRenderSkyblockItemOutlines();

        if (shouldRenderOutlines) {
            Minecraft mc = Minecraft.getMinecraft();
            RenderGlobal renderGlobal = mc.renderGlobal;
            SkyblockAddons main = SkyblockAddons.getInstance();

            mc.theWorld.theProfiler.endStartSection("entityOutlines");
            renderGlobal.entityOutlineFramebuffer.framebufferClear();

            GlStateManager.depthFunc(519);
            GlStateManager.disableFog();
            renderGlobal.entityOutlineFramebuffer.bindFramebuffer(false);
            RenderHelper.disableStandardItemLighting();
            mc.getRenderManager().setRenderOutlines(true);

            for (Entity entity : entities) {
                try {
                    if (!(entity instanceof EntityItem) && !(entity instanceof EntityPlayer)) {
                        continue;
                    }

                    if (entity instanceof EntityItem && (!main.getConfigValues().isEnabled(Feature.MAKE_DROPPED_ITEMS_GLOW) ||
                            (!main.getConfigValues().isEnabled(Feature.SHOW_GLOWING_ITEMS_ON_ISLAND) && main.getUtils().getLocation() == Location.ISLAND))) {
                        continue;
                    }

                    if (entity instanceof EntityPlayer && !main.getConfigValues().isEnabled(Feature.MAKE_DUNGEON_TEAMMATES_GLOW)) {
                        continue;
                    }


                    if (!main.getUtils().isInDungeon() && entity instanceof EntityPlayer) {
                        continue;
                    }

                    if (entity == mc.thePlayer) {
                        continue;
                    }

                    Location location = main.getUtils().getLocation();

                    if (entity instanceof EntityItem && (location == Location.VILLAGE || location == Location.AUCTION_HOUSE
                            || location == Location.BANK || location == Location.BAZAAR) &&
                            AUCTION_HOUSE.isVecInside(entity.getPositionVector())) {
                        continue;
                    }

                    boolean isInView = (entity instanceof EntityPlayer || entity.isInRangeToRender3d(x, y, z)) &&
                            (entity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entity.getEntityBoundingBox()) || entity.riddenByEntity == mc.thePlayer);
                    if (!isInView) {
                        continue;
                    }

                    int color;
                    if (entity instanceof EntityItem) {
                        color = getOutlineColor(((EntityItem) entity).getEntityItem());
                    } else {
                        color = getOutlineColor((EntityPlayer) entity);
                    }
                    if (color == Integer.MAX_VALUE) {
                        continue;
                    }

                    GlStateManager.enableColorMaterial();
                    enableOutlineMode(color);
                    mc.getRenderManager().renderEntitySimple(entity, partialTicks);
                    disableOutlineMode();
                    GlStateManager.disableColorMaterial();

                } catch (Throwable ex) {
                    ex.printStackTrace(); // Just move on to the next entity...
                }
            }

            mc.getRenderManager().setRenderOutlines(false);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.depthMask(false);
            renderGlobal.entityOutlineShader.loadShaderGroup(partialTicks);
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            GlStateManager.enableFog();
            GlStateManager.enableBlend();
            GlStateManager.enableColorMaterial();
            GlStateManager.depthFunc(515);
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();

            mc.getFramebuffer().bindFramebuffer(false);
        }

        return !shouldRenderOutlines;
    }

    public static int getOutlineColor(ItemStack itemStack) {
        ItemRarity itemRarity = ItemUtils.getRarity(itemStack);
        if (itemRarity != null) {
            return Minecraft.getMinecraft().fontRendererObj.getColorCode(itemRarity.getColorCode().getCode());
        }

        return Integer.MAX_VALUE;
    }

    public static int getOutlineColor(EntityPlayer player) {
        ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam)player.getTeam();

        if (scoreplayerteam != null) {
            String formattedName = FontRenderer.getFormatFromString(scoreplayerteam.getColorPrefix());

            if (formattedName.length() >= 2) {
                return Minecraft.getMinecraft().fontRendererObj.getColorCode(formattedName.charAt(1));
            }
        }

        return Integer.MAX_VALUE;
    }

    public static void enableOutlineMode(int color) {
        BUF_FLOAT_4.put(0, (float)(color >> 16 & 255) / 255.0F);
        BUF_FLOAT_4.put(1, (float)(color >> 8 & 255) / 255.0F);
        BUF_FLOAT_4.put(2, (float)(color & 255) / 255.0F);
        BUF_FLOAT_4.put(3, 1);
        GL11.glTexEnv(8960, 8705, BUF_FLOAT_4);
        GL11.glTexEnvi(8960, 8704, 34160);
        GL11.glTexEnvi(8960, 34161, 7681);
        GL11.glTexEnvi(8960, 34176, 34166);
        GL11.glTexEnvi(8960, 34192, 768);
        GL11.glTexEnvi(8960, 34162, 7681);
        GL11.glTexEnvi(8960, 34184, 5890);
        GL11.glTexEnvi(8960, 34200, 770);
    }

    public static void disableOutlineMode() {
        GL11.glTexEnvi(8960, 8704, 8448);
        GL11.glTexEnvi(8960, 34161, 8448);
        GL11.glTexEnvi(8960, 34162, 8448);
        GL11.glTexEnvi(8960, 34176, 5890);
        GL11.glTexEnvi(8960, 34184, 5890);
        GL11.glTexEnvi(8960, 34192, 768);
        GL11.glTexEnvi(8960, 34200, 770);
    }
}
