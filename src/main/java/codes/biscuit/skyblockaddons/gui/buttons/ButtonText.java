package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import net.minecraft.client.renderer.GlStateManager;

class ButtonText extends ButtonFeature {

    /**
     * Create a button that displays text.
     */
    ButtonText(int buttonId, int x, int y, String buttonText, Feature feature) {
        super(buttonId, x, y, buttonText, feature);
    }

    void drawButtonBoxAndText(int boxColor, float scale, int fontColor) {
        drawRect(xPosition, yPosition, xPosition+this.width, yPosition+this.height, boxColor);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        DrawUtils.drawCenteredText(displayString, ((xPosition+width/2)/scale), ((yPosition+(this.height-(8*scale))/2)/scale), fontColor);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
