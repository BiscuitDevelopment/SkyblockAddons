package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;

import java.awt.*;

public class ChromaManager {

    private static boolean coloringTextChroma = false;

    private static float currentHue = 0;
    private static Color currentColor = new Color(Color.HSBtoRGB(0, 0.72F, 0.90F));

    public static void increment() {
        currentHue += SkyblockAddons.getInstance().getUtils().denormalizeScale(SkyblockAddons.getInstance().getConfigValues().getChromaSpeed(), 0.1F, 10, 0.5F);
        if (currentHue > 360) {
            currentHue = 0;
        }
        currentColor = new Color(Color.HSBtoRGB(currentHue / 360F, 0.72F, 0.90F));
    }

    public static Color getCurrentColor() {
        return currentColor;
    }

    public static void setFeature(Feature feature, boolean state) {
        if (state) {
            SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().add(feature);
        } else {
            SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().remove(feature);
        }
    }

    /**
     * Before rending a string that supports chroma, call this method so it marks the text
     * to have the color fade applied to it.<br><br>
     *
     * After calling this & doing the drawString, make sure to call {@link ChromaManager#doneRenderingText()}.
     *
     * @param feature The feature to check if fade chroma is enabled.
     */
    public static void renderingText(Feature feature) {
        if (SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().contains(feature) &&
                SkyblockAddons.getInstance().getConfigValues().getChromaMode() == EnumUtils.ChromaMode.FADE) {
            coloringTextChroma = true;
        }
    }

    /**
     * Disables any chroma stuff.
     */
    public static void doneRenderingText() {
        coloringTextChroma = false;
    }

    public static boolean isColoringTextChroma() {
        return coloringTextChroma;
    }
}
