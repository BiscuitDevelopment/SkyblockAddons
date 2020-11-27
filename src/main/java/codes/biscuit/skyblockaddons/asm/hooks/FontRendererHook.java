package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.misc.ChromaManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class FontRendererHook {

    public static void changeTextColor() {
        if (ChromaManager.isColoringTextChroma()) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

            float[] HSB = Color.RGBtoHSB((int)(fontRenderer.red * 255), (int)(fontRenderer.green * 255), (int)(fontRenderer.blue * 255), null);
            int newColor = ChromaManager.getChromaColor(fontRenderer.posX, fontRenderer.posY, HSB, (int)(fontRenderer.alpha * 255));

            fontRenderer.red = (float)(newColor >> 16 & 255) / 255.0F;
            fontRenderer.green = (float)(newColor >> 8 & 255) / 255.0F;
            fontRenderer.blue = (float)(newColor & 255) / 255.0F;

            // Swap blue & green because they are swapped in FontRenderer's color model.
            GlStateManager.color(fontRenderer.red, fontRenderer.blue, fontRenderer.green, fontRenderer.alpha);
        }
    }
}
