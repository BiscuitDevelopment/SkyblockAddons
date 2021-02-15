package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.misc.ManualChromaManager;
import codes.biscuit.skyblockaddons.shader.Shader;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenShader;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenTexturedShader;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class FontRendererHook {


    private static  Class<? extends Shader> savedShader = null;


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


    public static void saveShaderState() {
        savedShader = ShaderManager.getInstance().getActiveShaderType();
    }

    public static void restoreShaderState() {
        if (savedShader == null) {
            ShaderManager.getInstance().disableShader();
        }
        else {
            ShaderManager.getInstance().enableShader(savedShader);
        }
    }

    public static void toggleChromaOn() {
        //ColorUtils.getDummySkyblockColor(28, 29, 41, 230)
        //DrawUtils.begin2D(GL11.GL_TRIANGLE_STRIP, ColorUtils.getDummySkyblockColor(SkyblockColor.ColorAnimation.CHROMA));
        ColorUtils.bindWhite();
        ShaderManager.getInstance().enableShader(ChromaScreenTexturedShader.class);
    }

    public static void toggleChromaOff() {
        // TODO: use DrawUtils.end to have manual chroma
        ShaderManager.getInstance().disableShader();
    }
}
