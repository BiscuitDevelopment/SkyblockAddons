package codes.biscuit.skyblockaddons.misc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

/**
 * This class is used to manual
 */
public class ManualChromaManager {

    @Getter private static boolean coloringTextChroma;
    @Getter private static float featureScale;

    private static float[] defaultColorHSB = {0, 0.75F, 0.9F};

    /**
     * Before rending a string that supports chroma, call this method so it marks the text
     * to have the color fade applied to it.<br><br>
     *
     * After calling this & doing the drawString, make sure to call {@link ManualChromaManager#doneRenderingText()}.
     *
     * @param feature The feature to check if fade chroma is enabled.
     */
    public static void renderingText(Feature feature) {
        if (SkyblockAddons.getInstance().getConfigValues().getChromaMode() == EnumUtils.ChromaMode.FADE &&
                SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().contains(feature)) {
            coloringTextChroma = true;
            featureScale = SkyblockAddons.getInstance().getConfigValues().getGuiScale(feature);
        }
    }

    // TODO Don't force alpha in the future...
    public static int getChromaColor(float x, float y, int alpha) {
        return getChromaColor(x, y, defaultColorHSB, alpha);
    }

    public static int getChromaColor(float x, float y, float[] currentHSB, int alpha) {
        x *= featureScale;
        y *= featureScale;
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        x *= scale;
        y *= scale;

        float chromaSize = SkyblockAddons.getInstance().getConfigValues().getChromaSize().floatValue() * (Minecraft.getMinecraft().displayWidth / 100F);
        float chromaSpeed = SkyblockAddons.getInstance().getConfigValues().getChromaSpeed().floatValue() / 360F;

        float ticks = (float) SkyblockAddons.getInstance().getNewScheduler().getTotalTicks() + Utils.getPartialTicks();
        float timeOffset = ticks * chromaSpeed;

        float newHue = ((x + y) / chromaSize - timeOffset) % 1;

        if (currentHSB[2] < 0.3) { // Keep shadows as shadows
            return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, currentHSB[1], currentHSB[2]), alpha);
        } else {
            float saturation = SkyblockAddons.getInstance().getConfigValues().getChromaSaturation().floatValue();
            float brightness = SkyblockAddons.getInstance().getConfigValues().getChromaBrightness().floatValue();
            return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, saturation, brightness), alpha);
        }
    }

    public static int getChromaColor(float x, float y, float z, int alpha) {
        float chromaSize = SkyblockAddons.getInstance().getConfigValues().getChromaSize().floatValue() * (Minecraft.getMinecraft().displayWidth / 100F);
        float chromaSpeed = SkyblockAddons.getInstance().getConfigValues().getChromaSpeed().floatValue() / 360F;

        float ticks = (float) SkyblockAddons.getInstance().getNewScheduler().getTotalTicks() + Utils.getPartialTicks();
        float timeOffset = ticks * chromaSpeed;

        float newHue = ((x - y + z) / (chromaSize / 20F) - timeOffset) % 1;

        float saturation = SkyblockAddons.getInstance().getConfigValues().getChromaSaturation().floatValue();
        float brightness = SkyblockAddons.getInstance().getConfigValues().getChromaBrightness().floatValue();
        return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, saturation, brightness), alpha);
    }

    /**
     * Disables any chroma stuff.
     */
    public static void doneRenderingText() {
        coloringTextChroma = false;
        featureScale = 1;
    }
}
