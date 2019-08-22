package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.BUTTON_MAX_WIDTH;

public class ButtonSolid extends ButtonText {

    private SkyblockAddons main;

    private Feature feature;
    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button that has a solid color and text.
     */
    public ButtonSolid( double x, double y, int width, int height, String buttonText, SkyblockAddons main, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.main = main;
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
//        if (feature == Feature.ANCHOR_POINT) {
//            if (ButtonLocation.getLastHoveredFeature() == null) {
//                return;
//            }
//            displayString = Message.SETTING_ANCHOR_POINT.getMessage();
//            width = mc.fontRendererObj.getStringWidth(displayString)+10;
//            ScaledResolution sr = new ScaledResolution(mc);
//            xPosition = sr.getScaledWidth()/2-width/2;
//        }
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
        int boxAlpha = 100;
        if (hovered && feature != Feature.WARNING_TIME) boxAlpha = 170;
        // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
        boxAlpha *= alphaMultiplier;
        int boxColor = main.getUtils().getDefaultColor(boxAlpha);
        GlStateManager.enableBlend();
        if (alpha < 4) alpha = 4;
        int fontColor = new Color(224, 224, 224, alpha).getRGB();
        if (hovered && feature != Feature.WARNING_TIME) {
            fontColor = new Color(255, 255, 160, alpha).getRGB();
        }
        String originalString = displayString;
        float scale = 1;
        int stringWidth = mc.fontRendererObj.getStringWidth(displayString);
        float widthLimit = BUTTON_MAX_WIDTH -10;
        if (feature == Feature.WARNING_TIME) {
            widthLimit = 90;
        }
        if (stringWidth > widthLimit) {
            scale = 1/(stringWidth/widthLimit);
        }
//        if (feature == Feature.ANCHOR_POINT) scale = 1;
        drawButtonBoxAndText(mc, boxColor, scale, fontColor);
        if (!originalString.equals(displayString) && mc.currentScreen instanceof SkyblockAddonsGui) {
            main.getUtils().setFadingIn(false);
            main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.MAIN);
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (feature != Feature.WARNING_TIME) super.playPressSound(soundHandlerIn);
    }
}
