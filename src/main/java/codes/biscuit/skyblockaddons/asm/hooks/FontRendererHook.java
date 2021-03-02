package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.misc.ManualChromaManager;
import codes.biscuit.skyblockaddons.shader.Shader;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenTexturedShader;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.HashMap;

public class FontRendererHook {


    private static  Class<? extends Shader> savedShader = null;
    private static boolean wasManuallyChromaShading = false;
    private static final HashMap<String, Boolean> chromaStrings = new HashMap<>();

    @SuppressWarnings("unused")
    public static void changeTextColor() {
        if (ManualChromaManager.isColoringTextChroma() && !SkyblockColor.shouldUseChromaShaders()) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

            float[] HSB = Color.RGBtoHSB((int)(fontRenderer.red * 255), (int)(fontRenderer.green * 255), (int)(fontRenderer.blue * 255), null);
            int newColor = ManualChromaManager.getChromaColor(fontRenderer.posX, fontRenderer.posY, HSB, (int)(fontRenderer.alpha * 255));

            fontRenderer.red = (float)(newColor >> 16 & 255) / 255.0F;
            fontRenderer.green = (float)(newColor >> 8 & 255) / 255.0F;
            fontRenderer.blue = (float)(newColor & 255) / 255.0F;
            // Swap blue & green because they are swapped in FontRenderer's color model.
            GlStateManager.color(fontRenderer.red, fontRenderer.blue, fontRenderer.green, fontRenderer.alpha);
        }
    }


    public static float patcherColorChange(int style, float color) {
        return style == 22 ? 1F : color;
    }

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
    public static void saveChromaState() {
        savedShader = ShaderManager.getInstance().getActiveShaderType();
        wasManuallyChromaShading = ManualChromaManager.isColoringTextChroma();
    }

    /**
     * Called to restore the saved chroma state
     */
    @SuppressWarnings("unused")
    public static void restoreChromaState() {
        // Online data not fetched before a color code will cause null pointer exception
        if (SkyblockAddons.getInstance().getOnlineData() == null) {
            return;
        }
        if (SkyblockColor.shouldUseChromaShaders()) {
            if (savedShader == null) {
                ShaderManager.getInstance().disableShader();
            }
            else {
                ShaderManager.getInstance().enableShader(savedShader);
            }
        }
        else {
            ManualChromaManager.setColoringTextChroma(wasManuallyChromaShading);
        }
    }

    /**
     * Called to turn chroma on
     */
    @SuppressWarnings("unused")
    public static void toggleChromaOn() {
        // Online data not fetched before a color code will cause null pointer exception
        if (SkyblockAddons.getInstance().getOnlineData() == null) {
            return;
        }
        if (SkyblockColor.shouldUseChromaShaders()) {
            ColorUtils.bindWhite();
            ShaderManager.getInstance().enableShader(ChromaScreenTexturedShader.class);
        }
        else {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
            fontRenderer.red = 1F;
            fontRenderer.green = 1F;
            fontRenderer.blue = 1F;
            fontRenderer.alpha = 1F;
            ManualChromaManager.setColoringTextChroma(true);
        }
    }
}
