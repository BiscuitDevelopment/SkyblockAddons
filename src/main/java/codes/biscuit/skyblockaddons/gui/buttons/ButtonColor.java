package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.BUTTON_MAX_WIDTH;

public class ButtonColor extends ButtonText {

    private SkyblockAddons main;

    /**
     * Create a button that displays the color of whatever feature it is assigned to.
     */
    public ButtonColor(double x, double y, int width, int height, String buttonText, SkyblockAddons main, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.main = main;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int boxColor;
        int fontColor = new Color(224, 224, 224, 255).getRGB();
        int boxAlpha = 100;
        if (hovered) {
            boxAlpha = 170;
            fontColor = new Color(255, 255, 160, 255).getRGB();
        }
        boxColor = main.getConfigValues().getColor(feature).getColor(boxAlpha);
        // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
        GlStateManager.enableBlend();
        float scale = 1;
        int stringWidth = mc.fontRenderer.getStringWidth(displayString);
        float widthLimit = BUTTON_MAX_WIDTH -10;
        if (stringWidth > widthLimit) {
            scale = 1/(stringWidth/widthLimit);
        }
        drawButtonBoxAndText(mc, boxColor, scale, fontColor);
    }
}
