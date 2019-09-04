package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.BUTTON_MAX_WIDTH;

public class ButtonToggle extends ButtonFeature {

    private SkyblockAddons main;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonToggle(double x, double y, int width, int height, String buttonText, SkyblockAddons main, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.main = main;
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            int alpha;
            float alphaMultiplier = 1F;
            if (main.getUtils().isFadingIn()) {
                long timeSinceOpen = System.currentTimeMillis() - timeOpened;
                int fadeMilis = 500;
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = (float) timeSinceOpen / fadeMilis;
                }
                alpha = (int) (255 * alphaMultiplier);
            } else {
                alpha = 255;
            }
            hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            if (alpha < 4) alpha = 4;
            int boxColor;
            int fontColor = new Color(224, 224, 224, alpha).getRGB();
            int boxAlpha = 100;
            if (hovered) {
                boxAlpha = 170;
                fontColor = new Color(255, 255, 160, alpha).getRGB();
            }
            // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
            boxAlpha *= alphaMultiplier;
            // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
            if (main.getConfigValues().isDisabled(feature)) {
                boxColor = ConfigColor.RED.getColor(boxAlpha);
            } else {
                boxColor = ConfigColor.GREEN.getColor(boxAlpha);
            }
            GlStateManager.enableBlend();
            float scale = 1;
            int stringWidth = mc.fontRendererObj.getStringWidth(displayString);
            float widthLimit = BUTTON_MAX_WIDTH -10;
            if (feature == Feature.WARNING_TIME) {
                widthLimit = 90;
            }
            if (stringWidth > widthLimit) {
                scale = 1/(stringWidth/widthLimit);
            }
            drawRect(xPosition, yPosition, xPosition+this.width, yPosition+this.height, boxColor);
            float scaleMultiplier = 1/scale;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            this.drawCenteredString(mc.fontRendererObj, displayString, (int)((xPosition+width/2)*scaleMultiplier), (int)((yPosition+(this.height-(8/scaleMultiplier))/2)*scaleMultiplier), fontColor);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            if (hovered) {
                SkyblockAddonsGui.setTooltipFeature(feature);
            }
        }
    }
}
