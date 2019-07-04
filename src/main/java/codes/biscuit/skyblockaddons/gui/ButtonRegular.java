package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class ButtonRegular extends GuiButton {

    private SkyblockAddons main;

    private Feature feature;
    private long timeOpened = System.currentTimeMillis();

    ButtonRegular(int buttonId, double x, double y, String buttonText, SkyblockAddons main, Feature feature, int width, int height) {
        super(buttonId, (int)x, (int)y, buttonText);
        this.main = main;
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            long timeSinceOpen = System.currentTimeMillis() - timeOpened;
            float alphaMultiplier = 1F;
            int fadeMilis = 500;
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = (float)timeSinceOpen/fadeMilis;
            }
            int alpha = (int)(255*alphaMultiplier);

            hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int boxColor;
            int boxAlpha = 100;
            if (hovered && feature.getButtonType() != Feature.ButtonType.NEUTRAL && !hitMaximum()) {
                boxAlpha = 170;
            }
            if (feature.getButtonType() == Feature.ButtonType.REGULAR) {
                if ((feature == Feature.MANA_BAR && main.getConfigValues().getManaBarType() == Feature.ManaBarType.OFF) ||
                        (feature != Feature.MANA_BAR && main.getConfigValues().getDisabledFeatures().contains(feature))) {
                    boxColor = ConfigColor.RED.getColor(boxAlpha * alphaMultiplier);
                } else {
                    boxColor = ConfigColor.GREEN.getColor(boxAlpha * alphaMultiplier);
                }
            } else if (feature.getButtonType() == Feature.ButtonType.COLOR) {
                if (feature == Feature.WARNING_COLOR) {
                    boxColor = main.getConfigValues().getColor(Feature.WARNING_COLOR).getColor(boxAlpha*alphaMultiplier);
                } else {
                    boxColor = main.getConfigValues().getColor(Feature.CONFIRMATION_COLOR).getColor(boxAlpha*alphaMultiplier);
                }
            } else if (feature.getButtonType() == Feature.ButtonType.MODIFY) {
                if (hitMaximum()) {
                    boxColor = ConfigColor.GRAY.getColor(boxAlpha * alphaMultiplier);
                } else {
                    if (feature == Feature.ADD) {
                        boxColor = ConfigColor.GREEN.getColor(boxAlpha * alphaMultiplier);
                    } else {
                        boxColor = ConfigColor.RED.getColor(boxAlpha * alphaMultiplier);
                    }
                }
            } else if (feature.getButtonType() == Feature.ButtonType.SOLID) {
                if (feature == Feature.EDIT_LOCATIONS) {
                    boxColor = ConfigColor.GREEN.getColor(boxAlpha * alphaMultiplier);
                } else {
                    boxColor = ConfigColor.RED.getColor(boxAlpha * alphaMultiplier);
                }
            } else {
                boxColor = ConfigColor.GRAY.getColor(boxAlpha * alphaMultiplier);
            }
            drawRect(this.xPosition, this.yPosition, this.xPosition+this.width, this.yPosition+this.height, boxColor);
            GlStateManager.enableBlend();
            if (alpha < 4) alpha = 4;
            int fontColor = new Color(224, 224, 224, alpha).getRGB();
            if (hovered && feature.getButtonType() != Feature.ButtonType.NEUTRAL && !hitMaximum()) {
                fontColor = new Color(255, 255, 160, alpha).getRGB();
            }
            if (feature == Feature.WARNING_TIME) {
                displayString = "Warning Time: "+main.getConfigValues().getWarningSeconds()+"s";
            } else if (feature == Feature.MANA_BAR) {
                displayString = "Mana Bar: "+main.getConfigValues().getManaBarType().getDisplayText();
            }
            this.drawCenteredString(mc.fontRendererObj, displayString, xPosition+width/2, yPosition+(this.height-8)/2, fontColor);
            GlStateManager.disableBlend();
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        switch (feature.getButtonType()) {
            case NEUTRAL:
                return;
            case MODIFY:
                if (hitMaximum()) {
                    return;
                }
            default:
                super.playPressSound(soundHandlerIn);
        }
    }

    private boolean hitMaximum() {
        return (feature == Feature.SUBTRACT && main.getConfigValues().getWarningSeconds() == 1) ||
                (feature == Feature.ADD && main.getConfigValues().getWarningSeconds() == 99);
    }

    Feature getFeature() {
        return feature;
    }
}
