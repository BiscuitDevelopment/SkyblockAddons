package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.draw.DrawStateFontRenderer;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class FontRendererHook {

    private static final SkyblockColor CHROMA_COLOR = new SkyblockColor(0xFFFFFFFF).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA);
    private static final DrawStateFontRenderer DRAW_CHROMA = new DrawStateFontRenderer(CHROMA_COLOR);
    private static final SkyblockColor CHROMA_COLOR_SHADOW = new SkyblockColor(0xFF555555).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA);
    private static final DrawStateFontRenderer DRAW_CHROMA_SHADOW = new DrawStateFontRenderer(CHROMA_COLOR_SHADOW);
    private static DrawStateFontRenderer currentDrawState = null;

    @SuppressWarnings("unused")
    public static void changeTextColor() {
        if (currentDrawState.shouldManuallyRecolorFont()) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
            currentDrawState.bindAnimatedColor(fontRenderer.posX, fontRenderer.posY);
        }
    }


    public static void setupFeatureFont(Feature feature) {
        if (SkyblockAddons.getInstance().getConfigValues().getChromaMode() == EnumUtils.ChromaMode.FADE &&
                SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().contains(feature)) {
            DRAW_CHROMA.setupMulticolorFeature(SkyblockAddons.getInstance().getConfigValues().getGuiScale(feature));
            DRAW_CHROMA_SHADOW.setupMulticolorFeature(SkyblockAddons.getInstance().getConfigValues().getGuiScale(feature));
        }
    }

    public static void endFeatureFont() {
        DRAW_CHROMA.endMulticolorFeature();
        DRAW_CHROMA_SHADOW.endMulticolorFeature();
    }

    public static float patcherColorChange(int style, float color) {
        return style == 22 ? 1F : color;
    }

    // WILL NOT WORK WITH SHADOW
    public static void patcherToggleChroma(int style) {
        if (style == 22) {
            toggleChromaOn();
        }
        else {
            restoreChromaState();
        }
    }

    /**
     * Called in patcher code to stop patcher optimization and do vanilla render
     * @param s string to render
     * @return true to override
     */
    @SuppressWarnings("unused")
    // TODO: Really not sure why hashing strings to control when to override patcher causes HUGE amounts of lag...Used cacheBuilder and HashMap and idk what's going on
    public static boolean shouldOverridePatcher(String s) {
        //return chromaStrings.get(s) == null || chromaStrings.get(s);
        return true;
    }

    /**
     * Called on chroma string to update cache
     * @param s string with chroma format tag
     */
    @SuppressWarnings("unused")
    public static void stringWithChroma(String s) {
        //chromaStrings.put(s, true);
    }

    /**
     * Called on string termination to update cache
     * @param s string with chroma format tag
     */
    @SuppressWarnings("unused")
    public static void endOfString(String s) {
        //if (!chromaStrings.containsKey(s)) {
        //    chromaStrings.put(s, false);
        //}
    }

    /**
     * Called to save the current shader state
     */
    @SuppressWarnings("unused")
    public static void beginRenderString(boolean shadow) {
        currentDrawState = shadow ? DRAW_CHROMA_SHADOW : DRAW_CHROMA;
        if (SkyblockAddons.isFullyInitialized()) {
            currentDrawState.loadFeatureColorEnv();
        }
    }

    /**
     * Called to restore the saved chroma state
     */
    @SuppressWarnings("unused")
    public static void restoreChromaState() {
        if (SkyblockAddons.isFullyInitialized()) {
            currentDrawState.restoreColorEnv();
        }
    }

    /**
     * Called to turn chroma on
     */
    @SuppressWarnings("unused")
    public static void toggleChromaOn() {
        if (SkyblockAddons.isFullyInitialized()) {
            currentDrawState.newColorEnv().bindActualColor();
        }
    }

    @SuppressWarnings("unused")
    public static void endRenderString() {
        if (SkyblockAddons.isFullyInitialized()) {
            currentDrawState.endColorEnv();
        }
    }
}
