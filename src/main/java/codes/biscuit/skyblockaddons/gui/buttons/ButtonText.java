package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

class ButtonText extends ButtonFeature {

    /**
     * Create a button that displays text.
     */
    ButtonText(int buttonId, int x, int y, String buttonText, Feature feature) {
        super(buttonId, x, y, buttonText, feature);
    }

    void drawButtonBoxAndText(Minecraft mc, int boxColor, float scale, int fontColor) {
        drawRect(x, y, x + this.width, y + this.height, boxColor);
        float scaleMultiplier = 1/scale;
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        this.drawCenteredString(mc.fontRenderer, displayString, (int) ((x + width / 2) * scaleMultiplier), (int) ((y + (this.height - (8 / scaleMultiplier)) / 2) * scaleMultiplier), fontColor);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
