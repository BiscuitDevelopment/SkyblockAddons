package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.misc.ManualChromaManager;
import codes.biscuit.skyblockaddons.shader.Shader;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenTexturedShader;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class FontRendererHook {


    private static  Class<? extends Shader> savedShader = null;
    //private static final Cache<Object, Object> chromaStrings = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

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

    /**
     * Called in patcher code to stop patcher optimization and do vanilla render
     * @param s string to render
     * @return true to override
     */
    @SuppressWarnings("unused")
    public static boolean shouldOverridePatcher(String s) {
        //return chromaStrings.getIfPresent(s) == null || chromaStrings.get(s);
        return true;
    }

    /**
     * Called to save the current shader state
     */
    @SuppressWarnings("unused")
    public static void saveShaderState() {
        savedShader = ShaderManager.getInstance().getActiveShaderType();
    }

    /**
     * Called to restore the saved shader state
     */
    @SuppressWarnings("unused")
    public static void restoreShaderState() {
        if (savedShader == null) {
            ShaderManager.getInstance().disableShader();
        }
        else {
            ShaderManager.getInstance().enableShader(savedShader);
        }
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
     * Called to turn chroma on
     * TODO: Manual Chroma?
     * TODO: What if chroma already on? Will bind white ruin?
     */
    @SuppressWarnings("unused")
    public static void toggleChromaOn() {
        ColorUtils.bindWhite();
        ShaderManager.getInstance().enableShader(ChromaScreenTexturedShader.class);
    }

    /**
     * Called to turn chroma off
     * TODO: Manual Chroma?
     */
    @SuppressWarnings("unused")
    public static void toggleChromaOff() {
        if (savedShader == null) {
            ShaderManager.getInstance().disableShader();
        }
    }
}
