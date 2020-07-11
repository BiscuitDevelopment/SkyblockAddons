package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class ButtonSwitchTab extends GuiButton {

    private SkyblockAddons main;
    private EnumUtils.GuiTab currentTab;
    private EnumUtils.GuiTab tab;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonSwitchTab(double x, double y, int width, int height, String buttonText, SkyblockAddons main,
                           EnumUtils.GuiTab tab, EnumUtils.GuiTab currentTab) {
        super(0,(int)x,(int)y,width, height,buttonText);
        this.main = main;
        this.width = width;
        this.height = height;
        this.currentTab = currentTab;
        this.tab = tab;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            float alphaMultiplier = 1F;
            if (main.getUtils().isFadingIn()) {
                long timeSinceOpen = System.currentTimeMillis() - timeOpened;
                int fadeMilis = 500;
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = (float) timeSinceOpen / fadeMilis;
                }
            }
            hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            if (currentTab == tab) hovered = false;
            if (alphaMultiplier < 0.1) alphaMultiplier = 0.1F;
            int boxColor = main.getUtils().getDefaultBlue((int)(alphaMultiplier*50));
            int fontColor;
            if (currentTab != tab) {
                fontColor = main.getUtils().getDefaultBlue((int)(alphaMultiplier*255));
            } else {
                fontColor = main.getUtils().getDefaultBlue((int)(alphaMultiplier*127));
            }
            if (hovered) {
                fontColor = new Color(255, 255, 160, (int)(alphaMultiplier*255)).getRGB();
            }
            drawRect(xPosition, yPosition, xPosition+width, yPosition+height, boxColor);
            float scale = 1.4F;
            float scaleMultiplier = 1/scale;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            GlStateManager.enableBlend();
            drawCenteredString(mc.fontRendererObj, displayString, (int)((xPosition+width/2)*scaleMultiplier),
                    (int)((yPosition+(this.height-(8/scaleMultiplier))/2)*scaleMultiplier), fontColor);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (currentTab != tab) super.playPressSound(soundHandlerIn);
    }

    public EnumUtils.GuiTab getTab() {
        return tab;
    }
}
