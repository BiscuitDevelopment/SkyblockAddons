package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.misc.ChromaManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class DrawUtils {

    public static void drawScaledCustomSizeModalRect(float x, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight, boolean linearTexture) {
        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + uWidth) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();

        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    public static void drawCylinderInWorld(double x, double y, double z, float radius, float height, float partialTicks) {
        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        double viewX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double) partialTicks;
        double viewY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double) partialTicks;
        double viewZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double) partialTicks;
        x -= viewX;
        y -= viewY;
        z -= viewZ;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        float currentAngle = 0;
        float angleStep = 0.1F;
        while (currentAngle < 2 * Math.PI) {
            float xOffset = radius * (float) Math.cos(currentAngle);
            float zOffset = radius * (float) Math.sin(currentAngle);
            worldrenderer.pos(x + xOffset, y + height, z + zOffset).endVertex();
            worldrenderer.pos(x + xOffset, y + 0, z + zOffset).endVertex();
            currentAngle += angleStep;
        }
        worldrenderer.pos(x + radius, y + height, z).endVertex();
        worldrenderer.pos(x + radius, y + 0.0, z).endVertex();
        tessellator.draw();
    }

    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        drawModalRectWithCustomSizedTexture(x, y, u, v, width, height, textureWidth, textureHeight, false);
    }

    /**
     * Draws a textured rectangle at z = 0. Args: x, y, u, v, width, height, textureWidth, textureHeight
     */
    public static void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight, boolean linearTexture) {
        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + width) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();

        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    public static void drawRect(double left, double top, double right, double bottom, int color) {
        drawRect(left, top, right, bottom, color, false);
    }

    /**
     * Draws a solid color rectangle with the specified coordinates and color (ARGB format). Args: x1, y1, x2, y2, color
     */
    public static void drawRect(double left, double top, double right, double bottom, int color, boolean chroma) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        if (!chroma) {
            float f3 = (float) (color >> 24 & 255) / 255.0F;
            float f = (float) (color >> 16 & 255) / 255.0F;
            float f1 = (float) (color >> 8 & 255) / 255.0F;
            float f2 = (float) (color & 255) / 255.0F;
            GlStateManager.color(f, f1, f2, f3);
        }
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (chroma) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            posChromaColor(worldrenderer, left, bottom);
            posChromaColor(worldrenderer, right, bottom);
            posChromaColor(worldrenderer, right, top);
            posChromaColor(worldrenderer, left, top);
        } else {
            worldrenderer.begin(7, DefaultVertexFormats.POSITION);
            worldrenderer.pos(left, bottom, 0.0D).endVertex();
            worldrenderer.pos(right, bottom, 0.0D).endVertex();
            worldrenderer.pos(right, top, 0.0D).endVertex();
            worldrenderer.pos(left, top, 0.0D).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void posChromaColor(WorldRenderer worldRenderer, double x, double y) {
        int color = ChromaManager.getChromaColor((float) x, (float) y, 255);
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color & 255) / 255.0F;
        float f2 = (float) (color >> 8 & 255) / 255.0F;
        worldRenderer.pos(x, y, 0.0D).color(f, f1, f2, f3).endVertex();
    }

    /**E
     * Draws a solid color rectangle with the specified coordinates and color (ARGB format). Args: x1, y1, x2, y2, color
     */
    public static void drawRectOutline(float x, float y, int w, int h, int thickness, int color, boolean chroma) {
        if (chroma) {
            drawSegmentedLineVertical(x - thickness, y, thickness, h, color, true);
            drawSegmentedLineHorizontal(x - thickness, y - thickness, w + thickness * 2, thickness, color, true);
            drawSegmentedLineVertical(x + w, y, thickness, h, color, true);
            drawSegmentedLineHorizontal(x - thickness, y + h, w + thickness * 2, thickness, color, true);
        } else {
            drawRect(x - thickness, y, thickness, h, color);
            drawRect(x - thickness, y - thickness, w + thickness * 2, thickness, color);
            drawRect(x + w, y, thickness, h, color);
            drawRect(x - thickness, y + h, w + thickness * 2, thickness, color);
        }
    }

    public static void drawSegmentedLineHorizontal(float x, float y, float w, float h, int color, boolean chroma) {
        int segments = (int) (w / 10);
        float length = w / segments;

        for (int segment = 0; segment < segments; segment++) {
            float start = x + length * segment;
            drawRect(start, y, start + length, y + h, color, chroma);
        }
    }

    public static void drawSegmentedLineVertical(float x, float y, float w, float h, int color, boolean chroma) {
        int segments = (int) (h / 10);
        float length = h / segments;

        for (int segment = 0; segment < segments; segment++) {
            float start = y + length * segment;
            drawRect(x, start, x + w, start + length, color, chroma);
        }
    }

    public static void drawText(String text, float x, float y, int color) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        if (SkyblockAddons.getInstance().getConfigValues().getTextStyle() == EnumUtils.TextStyle.STYLE_TWO) {
            int colorAlpha = Math.max(ColorUtils.getAlpha(color), 4);
            int colorBlack = new Color(0, 0, 0, colorAlpha / 255F).getRGB();
            String strippedText = TextUtils.stripColor(text);
            fontRenderer.drawString(strippedText, x + 1, y + 0, colorBlack, false);
            fontRenderer.drawString(strippedText, x + -1, y + 0, colorBlack, false);
            fontRenderer.drawString(strippedText, x + 0, y + 1, colorBlack, false);
            fontRenderer.drawString(strippedText, x + 0, y + -1, colorBlack, false);
            fontRenderer.drawString(text, x + 0, y + 0, color, false);
        } else {
            fontRenderer.drawString(text, x + 0, y + 0, color, true);
        }
    }

    public static void drawCenteredText(String text, float x, float y, int color) {
        drawText(text, x - Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2F, y, color);
    }
}
