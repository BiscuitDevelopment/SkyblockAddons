package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class ButtonModify extends ButtonText {

    private SkyblockAddons main;

    private Feature feature;

    /**
     * Create a button for adding or subtracting a number.
     */
    public ButtonModify(double x, double y, int width, int height, String buttonText, SkyblockAddons main, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.main = main;
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        int boxColor;
        int boxAlpha = 100;
        if (hovered && !hitMaximum()) {
            boxAlpha = 170;
        }
        if (hitMaximum()) {
            boxColor = ConfigColor.GRAY.getColor(boxAlpha);
        } else {
            if (feature == Feature.ADD) {
                boxColor = ConfigColor.GREEN.getColor(boxAlpha);
            } else {
                boxColor = ConfigColor.RED.getColor(boxAlpha);
            }
        }
        GlStateManager.enableBlend();
        int fontColor = new Color(224, 224, 224, 255).getRGB();
        if (hovered && !hitMaximum()) {
            fontColor = new Color(255, 255, 160, 255).getRGB();
        }
        drawButtonBoxAndText(mc, boxColor, 1, fontColor);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (!hitMaximum()) {
            super.playPressSound(soundHandlerIn);
        }
    }

    private boolean hitMaximum() {
        return (feature == Feature.SUBTRACT && main.getConfigValues().getWarningSeconds() == 1) ||
                (feature == Feature.ADD && main.getConfigValues().getWarningSeconds() == 99);
    }
}
