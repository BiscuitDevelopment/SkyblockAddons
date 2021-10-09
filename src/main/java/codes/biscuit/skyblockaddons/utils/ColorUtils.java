package codes.biscuit.skyblockaddons.utils;

import net.minecraft.client.renderer.GlStateManager;

public class ColorUtils {

    private static final SkyblockColor SKYBLOCK_COLOR = new SkyblockColor();

    /**
     * Binds a color given its rgb integer representation.
     */
    public static void bindWhite() {
        bindColor(1F, 1F, 1F, 1F);
    }

    /**
     * Binds a color given its red, green, blue, and alpha color values.
     */
    public static void bindColor(float r, float g, float b, float a) {
        GlStateManager.color(r, g, b, a);
    }

    /**
     * Binds a color given its red, green, blue, and alpha color values.
     */
    public static void bindColor(int r, int g, int b, int a) {
        bindColor(r / 255F, g / 255F, b / 255F, a / 255F);
    }

    /**
     * Binds a color given its red, green, blue, and alpha color values, multiplying
     * all color values by the specified multiplier (for example to make the color darker).
     */
    private static void bindColor(int r, int g, int b, int a, float colorMultiplier) {
        bindColor(r / 255F * colorMultiplier, g / 255F * colorMultiplier, b / 255F * colorMultiplier, a / 255F);
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
    public static int setColorAlpha(int color, float alpha) {
        return setColorAlpha(color, getAlphaIntFromFloat(alpha));
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

    public static float getAlphaFloat(int color) {
        return getAlpha(color) / 255F;
    }

    public static int getAlphaIntFromFloat(float alpha) {
        return (int) (alpha * 255);
    }

    public static int getColor(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static SkyblockColor getDummySkyblockColor(int color) {
        return getDummySkyblockColor(SkyblockColor.ColorAnimation.NONE, color);
    }

    public static SkyblockColor getDummySkyblockColor(int r, int g, int b, int a) {
        return getDummySkyblockColor(SkyblockColor.ColorAnimation.NONE, getColor(r, g, b, a));
    }

    public static SkyblockColor getDummySkyblockColor(int r, int g, int b, float a) {
        return getDummySkyblockColor(r, g, b, getAlphaIntFromFloat(a));
    }

    public static SkyblockColor getDummySkyblockColor(SkyblockColor.ColorAnimation colorAnimation) {
        return getDummySkyblockColor(colorAnimation, -1);
    }

    public static SkyblockColor getDummySkyblockColor(int color, boolean chroma) {
        return getDummySkyblockColor(chroma ? SkyblockColor.ColorAnimation.CHROMA : SkyblockColor.ColorAnimation.NONE, color);
    }

    public static SkyblockColor getDummySkyblockColor(SkyblockColor.ColorAnimation colorAnimation, int color) {
        return SKYBLOCK_COLOR.setColorAnimation(colorAnimation).setColor(color);
    }
}
