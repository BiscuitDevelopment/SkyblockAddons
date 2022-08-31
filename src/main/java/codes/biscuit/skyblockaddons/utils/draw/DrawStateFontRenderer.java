package codes.biscuit.skyblockaddons.utils.draw;

import codes.biscuit.skyblockaddons.core.chroma.MulticolorShaderManager;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import net.minecraft.client.renderer.GlStateManager;

public class DrawStateFontRenderer extends DrawState2D {

    protected boolean multicolorFeatureOverride;
    protected boolean isActive;
    protected float featureScale = 1;

    public DrawStateFontRenderer(SkyblockColor theColor) {
        super(theColor, true, false);
    }

    public void setupMulticolorFeature(float theFeatureScale) {
        if (color.drawMulticolorManually()) {
            featureScale = theFeatureScale;
        }
        multicolorFeatureOverride = true;
    }

    public void endMulticolorFeature() {
        if (color.drawMulticolorManually()) {
            featureScale = 1;
        }
        multicolorFeatureOverride = false;
    }

    public void loadFeatureColorEnv() {
        if (multicolorFeatureOverride) {
            newColorEnv();
        }
    }

    public void restoreColorEnv() {
        if (color.drawMulticolorUsingShader()) {
            //noinspection StatementWithEmptyBody
            if (multicolorFeatureOverride) {
                // TODO: change order of restore to bind white here after font renderer binds the other color
            } else {
                MulticolorShaderManager.getInstance().end();
            }
        }
        isActive = false;
    }

    public DrawStateFontRenderer newColorEnv() {
        super.newColorEnv();
        isActive = true;
        return this;
    }

    public DrawStateFontRenderer endColorEnv() {
        super.endColorEnv();
        isActive = false;
        return this;
    }

    public DrawStateFontRenderer bindAnimatedColor(float x, float y) {
        // Handle feature scale here
        int colorInt = color.getTintAtPosition(x * featureScale, y * featureScale);
        GlStateManager.color(ColorUtils.getRed(colorInt) / 255F, ColorUtils.getGreen(colorInt) / 255F, ColorUtils.getBlue(colorInt) / 255F, ColorUtils.getAlpha(colorInt) / 255F);
        return this;
    }

    public boolean shouldManuallyRecolorFont() {
        return (multicolorFeatureOverride || isActive) && color.drawMulticolorManually();
    }
}
