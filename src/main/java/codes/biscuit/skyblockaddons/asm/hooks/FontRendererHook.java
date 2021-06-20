package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import codes.biscuit.skyblockaddons.utils.draw.DrawStateFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.LinkedHashMap;
import java.util.Map;

public class FontRendererHook {

    private static final SkyblockColor CHROMA_COLOR = new SkyblockColor(0xFFFFFFFF).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA);
    private static final DrawStateFontRenderer DRAW_CHROMA = new DrawStateFontRenderer(CHROMA_COLOR);
    private static final SkyblockColor CHROMA_COLOR_SHADOW = new SkyblockColor(0xFF555555).setColorAnimation(SkyblockColor.ColorAnimation.CHROMA);
    private static final DrawStateFontRenderer DRAW_CHROMA_SHADOW = new DrawStateFontRenderer(CHROMA_COLOR_SHADOW);
    private static DrawStateFontRenderer currentDrawState = null;
    private static final MaxSizeHashMap<String, Boolean> stringsWithChroma = new MaxSizeHashMap<>(1000);

    public static void changeTextColor() {
        if (currentDrawState != null && currentDrawState.shouldManuallyRecolorFont()) {
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

    /**
     * Called in patcher code to stop patcher optimization and do vanilla render
     * @param s string to render
     * @return true to override
     */
    public static boolean shouldOverridePatcher(String s) {
        //return chromaStrings.get(s) == null || chromaStrings.get(s);
        if (stringsWithChroma.get(s) != null) {
            return stringsWithChroma.get(s);
        }
        // Check if there is a "§z" colorcode in the string and cache it
        boolean hasChroma = false;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '§') {
                i++;
                if (i < s.length() && s.charAt(i) == 'z') {
                    hasChroma = true;
                    break;
                }
            }
        }
        stringsWithChroma.put(s, hasChroma);
        return hasChroma;
    }


    /**
     * Called to save the current shader state
     */
    public static void beginRenderString(boolean shadow) {
        float alpha = Minecraft.getMinecraft() == null || Minecraft.getMinecraft().fontRendererObj == null ? 1 : Minecraft.getMinecraft().fontRendererObj.alpha;
        if (shadow) {
            currentDrawState = DRAW_CHROMA_SHADOW;
            CHROMA_COLOR_SHADOW.setColor((int) (255 * alpha) << 24 | 0x555555);
        } else {
            currentDrawState = DRAW_CHROMA;
            CHROMA_COLOR.setColor((int) (255 * alpha) << 24 | 0xFFFFFF);
        }
        if (SkyblockAddons.isFullyInitialized()) {
            currentDrawState.loadFeatureColorEnv();
        }
    }

    /**
     * Called to restore the saved chroma state
     */
    public static void restoreChromaState() {
        if (SkyblockAddons.isFullyInitialized()) {
            currentDrawState.restoreColorEnv();
        }
    }

    /**
     * Called to turn chroma on
     */
    public static void toggleChromaOn() {
        if (SkyblockAddons.isFullyInitialized()) {
            currentDrawState.newColorEnv().bindActualColor();
        }
    }

    /**
     * Called to turn chroma off after the full string has been rendered (before returning)
     */
    public static void endRenderString() {
        if (SkyblockAddons.isFullyInitialized()) {
            currentDrawState.endColorEnv();
        }
    }

    /**
     * HashMap with upper limit on storage size. Used to enforce the font renderer cache not getting too large over time
     */
    public static class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;

        public MaxSizeHashMap(int maxSize) {
            super();
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
    }
}
