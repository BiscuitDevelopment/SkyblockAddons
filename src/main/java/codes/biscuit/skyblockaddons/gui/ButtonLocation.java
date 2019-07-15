package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonLocation extends GuiButton {

    private SkyblockAddons main;
    private Feature feature;
    private int boxXOne;
    private int boxXTwo;
    private int boxYOne;
    private int boxYTwo;
    private float scaleMultiplier;

    ButtonLocation(int buttonId, SkyblockAddons main, int width, int height, Feature feature) {
        super(buttonId, 0, 0, null);
        this.main = main;
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            ScaledResolution sr = new ScaledResolution(mc);
            float scale = main.getUtils().denormalizeValue(main.getConfigValues().getGuiScale(), ButtonSlider.VALUE_MIN, ButtonSlider.VALUE_MAX, ButtonSlider.VALUE_STEP);
            scaleMultiplier = 1F/scale;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            if (feature == Feature.MANA_BAR) {
                xPosition = (int) (main.getConfigValues().getManaBarX() * sr.getScaledWidth());
                yPosition = (int) (main.getConfigValues().getManaBarY() * sr.getScaledHeight());
                short barWidth = 92;

                float manaFill = (float) 123 / 321;
                int left = (int) (main.getConfigValues().getManaBarX() * sr.getScaledWidth()) + 14;
                int filled = (int) (manaFill * barWidth);
                int top = (int) (main.getConfigValues().getManaBarY() * sr.getScaledHeight()) + 10;

                float barX = left*scaleMultiplier-60;
                float barY = top*scaleMultiplier-10;

                boxXOne = (int)barX-3;
                boxXTwo = (int)barX+barWidth+3;
                boxYOne = (int)barY-3;
                boxYTwo = (int)barY+9;
                hovered = mouseX >= boxXOne/scaleMultiplier && mouseY >= boxYOne/scaleMultiplier && mouseX < boxXTwo/scaleMultiplier && mouseY < boxYTwo/scaleMultiplier;
                int boxAlpha = 100;
                if (hovered) {
                    boxAlpha = 170;
                }
                int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
                drawRect(boxXOne, boxYOne,
                        boxXTwo, boxYTwo, boxColor);

                mc.getTextureManager().bindTexture(PlayerListener.MANA_BARS);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();
                int textureY = main.getConfigValues().getColor(Feature.MANA_BAR_COLOR).ordinal()*10;
                drawTexturedModalRect(barX, barY, 0, textureY, barWidth, 5);
                if (filled > 0) {
                    drawTexturedModalRect(barX, barY, 0, textureY+5, filled, 5);
                }
            } else if (feature == Feature.SKELETON_BAR) {
                xPosition = (int) (main.getConfigValues().getSkeletonBarX() * sr.getScaledWidth());
                yPosition = (int) (main.getConfigValues().getSkeletonBarY() * sr.getScaledHeight());

                int barX = (int)(xPosition*scaleMultiplier);
                int barY = (int)((yPosition+2)*scaleMultiplier);
//
                boxXOne = barX-3;
                boxXTwo = barX+48;
                boxYOne = barY-3;
                boxYTwo = barY+18;
                hovered = mouseX >= boxXOne/scaleMultiplier && mouseY >= boxYOne/scaleMultiplier && mouseX < boxXTwo/scaleMultiplier && mouseY < boxYTwo/scaleMultiplier;
//                hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                int boxAlpha = 100;
                if (hovered) {
                    boxAlpha = 170;
                }
                int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
                drawRect(boxXOne, boxYOne, boxXTwo, boxYTwo, boxColor);
                for (int boneCounter = 0; boneCounter < 3; boneCounter++) {
                    mc.getRenderItem().renderItemIntoGUI(PlayerListener.BONE, (int)((xPosition+boneCounter*15*scale)*scaleMultiplier), barY);
                }
            } else if (feature == Feature.MANA_TEXT) {
                xPosition = (int) (main.getConfigValues().getManaTextX() * sr.getScaledWidth());
                yPosition = (int) (main.getConfigValues().getManaTextY() * sr.getScaledHeight());
                String text = "123/321";
                int stringWidth = mc.ingameGUI.getFontRenderer().getStringWidth(text);
                int x = (int) (main.getConfigValues().getManaTextX() * sr.getScaledWidth()) + 60 - stringWidth / 2;
                int y = (int) (main.getConfigValues().getManaTextY() * sr.getScaledHeight()) + 4;
                x+=width/2;
                y+=height/2;
                int barX = (int)(x*scaleMultiplier)-60;
                int barY = (int)(y*scaleMultiplier);

                boxXOne = barX-3;
                boxXTwo = barX+stringWidth+3;
                boxYOne = barY-13;
                boxYTwo = barY+1;
                hovered = mouseX >= boxXOne/scaleMultiplier && mouseY >= boxYOne/scaleMultiplier && mouseX < boxXTwo/scaleMultiplier && mouseY < boxYTwo/scaleMultiplier;
                int boxAlpha = 100;
                if (hovered) {
                    boxAlpha = 170;
                }
                int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
                drawRect(boxXOne, boxYOne,
                        boxXTwo, boxYTwo, boxColor);
//                hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
//                int boxAlpha = 100;
//                if (hovered) {
//                    boxAlpha = 170;
//                }
//                int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
//                drawRect(xPosition, yPosition, xPosition + this.width, yPosition + this.height, boxColor);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();
                int color = main.getConfigValues().getColor(Feature.MANA_TEXT_COLOR).getColor(255);
                FontRenderer fr = mc.fontRendererObj;
                fr.drawString(text, barX+1, barY-10, 0);
                fr.drawString(text, barX-1, barY-10, 0);
                fr.drawString(text, barX, barY+1-10, 0);
                fr.drawString(text, barX, barY-1-10, 0);
                fr.drawString(text, barX, barY-10, color);
                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && mouseX >= boxXOne/scaleMultiplier && mouseY >= boxYOne/scaleMultiplier && mouseX < boxXTwo/scaleMultiplier && mouseY < boxYTwo/scaleMultiplier;
    }

    Feature getFeature() {
        return feature;
    }
}
