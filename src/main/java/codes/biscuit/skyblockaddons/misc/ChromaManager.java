package codes.biscuit.skyblockaddons.misc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import lombok.Getter;

import java.awt.*;

public class ChromaManager {

    @Getter private static boolean coloringTextChroma;
    private static float featureScale;

    private static float[] defaultColorHSB = {0, 0.75F, 0.9F};

    /**
     * Before rending a string that supports chroma, call this method so it marks the text
     * to have the color fade applied to it.<br><br>
     *
     * After calling this & doing the drawString, make sure to call {@link ChromaManager#doneRenderingText()}.
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

    public static int getChromaColor(float x, float y) {
        return getChromaColor(x, y, defaultColorHSB);
    }

    public static int getChromaColor(float x, float y, float[] currentHSB) {
        x *= featureScale;
        y *= featureScale;

        float chromaWidth = SkyblockAddons.getInstance().getUtils().denormalizeScale(SkyblockAddons.getInstance().getConfigValues().getChromaFadeWidth(), 1, 42, 1) / 360F;
        float chromaSpeed = SkyblockAddons.getInstance().getUtils().denormalizeScale(SkyblockAddons.getInstance().getConfigValues().getChromaSpeed(), 0.1F, 10, 0.5F) / 360F;

        long ticks = SkyblockAddons.getInstance().getNewScheduler().getTotalTicks();

        float newHue = (x / 4F * chromaWidth + y / 4F * chromaWidth - ticks * chromaSpeed) % 1;

        if (currentHSB[2] < 0.3) { // Keep shadows as shadows
            return Color.HSBtoRGB(newHue, currentHSB[1], currentHSB[2]);
        } else {
            return Color.HSBtoRGB(newHue, defaultColorHSB[1], defaultColorHSB[2]);
        }
    }

    /**
     * Disables any chroma stuff.
     */
    public static void doneRenderingText() {
        coloringTextChroma = false;
    }
}
