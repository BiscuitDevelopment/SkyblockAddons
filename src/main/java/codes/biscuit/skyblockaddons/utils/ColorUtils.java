package codes.biscuit.skyblockaddons.utils;

import net.minecraft.client.renderer.GlStateManager;

public class ColorUtils {

    /**
     * Binds a color given its red, green, blue, and alpha color values.
     */
    public static void bindColor(int r, int g, int b, int a) {
        GlStateManager.color(r / 255F, g / 255F, b / 255F, a / 255F);
    }

    /**
     * Binds a color given its red, green, blue, and alpha color values, multiplying
     * all color values by the specified multiplier (for example to make the color darker).
     */
    private static void bindColor(int r, int g, int b, int a, float colorMultiplier) {
        GlStateManager.color(r / 255F * colorMultiplier, g / 255F * colorMultiplier, b / 255F * colorMultiplier, a / 255F);
    }

    /**
     * Binds a color given its rgb integer representation.
     */
    public static void bindColor(int color) {
        bindColor(getRed(color), getGreen(color), getBlue(color), getAlpha(color));
    }

    /**
     * Binds a color, multiplying all color values by the specified
     * multiplier (for example to make the color darker).
     */
    public static void bindColor(int color, float colorMultiplier) {
        bindColor(getRed(color), getGreen(color), getBlue(color), getAlpha(color), colorMultiplier);
    }

    /**
     * Takes the color input integer and sets its alpha color value,
     * returning the resulting color.
     */
    public static int setColorAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    public static int getRed(int color) {
        return color >> 16 & 0xFF;
    }

    public static int getGreen(int color) {
        return color >> 8 & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static int getAlpha(int color) {
        return color >> 24 & 0xFF;
    }
}
