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
        drawRect(xPosition, yPosition, xPosition+this.width, yPosition+this.height, boxColor);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        this.drawCenteredString(mc.fontRendererObj, displayString, (int)((xPosition+width/2)/scale), (int)((yPosition+(this.height-(8*scale))/2)/scale), fontColor);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
