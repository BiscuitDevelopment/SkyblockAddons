package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.chroma.ManualChromaManager;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.utils.draw.DrawState2D;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;

import java.awt.*;
import java.util.LinkedList;

@Accessors(chain = true)
public class SkyblockColor {


    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final WorldRenderer worldRenderer = tessellator.getWorldRenderer();



    private static final int DEFAULT_COLOR = 0xFFFFFFFF;

    @Getter @Setter private ColorAnimation colorAnimation = ColorAnimation.NONE;

    private LinkedList<Integer> colors = new LinkedList<>();

    public SkyblockColor() {
        this(DEFAULT_COLOR);
    }

    public SkyblockColor(int color) {
        this.colors.add(color);
    }

    public SkyblockColor(int color, float alpha) {
        this.colors.add(ColorUtils.setColorAlpha(color, alpha));
    }

    public SkyblockColor(int r, int g, int b, int a) {
        this.colors.add(ColorUtils.getColor(r, g, b, a));
    }

    public SkyblockColor(int r, int g, int b, float a) {
        this.colors.add(ColorUtils.getColor(r, g, b, ColorUtils.getAlphaIntFromFloat(a)));
    }

    public int getColorAtPosition(float x, float y) {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            return ManualChromaManager.getChromaColor(x, y, ColorUtils.getAlpha(getColor()));
        }

        return colors.get(0);
    }

    public int getTintAtPosition(float x, float y) {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            return ManualChromaManager.getChromaColor(x, y, Color.RGBtoHSB(ColorUtils.getRed(getColor()), ColorUtils.getGreen(getColor()), ColorUtils.getBlue(getColor()), null) , ColorUtils.getAlpha(getColor()));
        }

        return colors.get(0);
    }

    public int getColorAtPosition(double x, double y, double z) {
        return getColorAtPosition((float) x, (float) y, (float) z);
    }

    public int getColorAtPosition(float x, float y, float z) {
        if (this.colorAnimation == ColorAnimation.CHROMA) {
            return ManualChromaManager.getChromaColor(x, y, z, ColorUtils.getAlpha(getColor()));
        }

        return colors.get(0);
    }

    public SkyblockColor setColor(int color) {
        return setColor(0, color);
    }

    public SkyblockColor setColor(int index, int color) {
        if (index >= colors.size()) {
            colors.add(color);
        } else {
            colors.set(index, color);
        }
        return this;
    }

    public boolean isMulticolor() {
        return colorAnimation != ColorAnimation.NONE;
    }

    public boolean isPositionalMulticolor() {
        return colorAnimation != ColorAnimation.NONE && SkyblockAddons.getInstance().getConfigValues().getChromaMode() != EnumUtils.ChromaMode.ALL_SAME_COLOR;
    }

    public int getColor() {
        return getColorSafe(0);
    }

    private int getColorSafe(int index) {
        while (index >= colors.size()) {
            colors.add(DEFAULT_COLOR);
        }
        return colors.get(index);
    }

    public boolean drawMulticolorManually() {
        return colorAnimation == ColorAnimation.CHROMA && !shouldUseChromaShaders();
    }

    public boolean drawMulticolorUsingShader() {
        return colorAnimation == ColorAnimation.CHROMA && shouldUseChromaShaders();
    }

    public static boolean shouldUseChromaShaders() {
        ConfigValues config = SkyblockAddons.getInstance().getConfigValues();
        return config.getChromaMode() != EnumUtils.ChromaMode.ALL_SAME_COLOR && ShaderManager.getInstance().areShadersSupported() && config.isEnabled(Feature.USE_NEW_CHROMA_EFFECT);
    }

    public enum ColorAnimation {
        NONE,
        CHROMA
    }
}
