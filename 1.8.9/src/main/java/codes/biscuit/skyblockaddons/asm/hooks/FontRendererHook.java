package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
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

            float chromaWidth = (SkyblockAddons.getInstance().getUtils().denormalizeScale(SkyblockAddons.getInstance().getConfigValues().getChromaFadeWidth(), 1, 42, 1)/360) % 1F;
            int newColorRGB = Color.HSBtoRGB(HSB[0] + chromaWidth, HSB[1], HSB[2]);

            fontRenderer.red = (float)(newColorRGB >> 16 & 255) / 255.0F;
            fontRenderer.green = (float)(newColorRGB >> 8 & 255) / 255.0F;
            fontRenderer.blue = (float)(newColorRGB & 255) / 255.0F;

            // Swap blue & green because they are swapped in FontRenderer's color model.
            GlStateManager.color(fontRenderer.red, fontRenderer.blue, fontRenderer.green, fontRenderer.alpha);
        }
    }
}
