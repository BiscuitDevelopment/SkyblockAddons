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

import java.awt.*;

public class ButtonLocation extends GuiButton {

    private SkyblockAddons main;
    private Feature feature;

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
            if (feature == Feature.MANA_BAR) {
                xPosition = (int) (main.getConfigValues().getManaBarX() * sr.getScaledWidth());
                yPosition = (int) (main.getConfigValues().getManaBarY() * sr.getScaledHeight());
                hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                int boxAlpha = 100;
                if (hovered) {
                    boxAlpha = 170;
                }
                int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
                drawRect(xPosition, yPosition, xPosition + this.width, yPosition + this.height, boxColor);
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(icons);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();

                short barWidth = 92;

                float manaFill = (float) 99 / 123;
                int filled = (int) (manaFill * barWidth);
                drawTexturedModalRect(xPosition + 14, yPosition + 10, 10, 84, barWidth, 5);
                if (filled > 0) {
                    drawTexturedModalRect(xPosition + 14, yPosition + 10, 10, 89, filled, 5);
                }

                int color = new Color(47, 71, 249).getRGB();
                String text = 99 + "/" + 123;
                int x = xPosition + 60 - mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2;
                int y = yPosition + 4;
                FontRenderer fontRenderer = mc.ingameGUI.getFontRenderer();
                fontRenderer.drawString(text, x + 1, y, 0);
                fontRenderer.drawString(text, x - 1, y, 0);
                fontRenderer.drawString(text, x, y + 1, 0);
                fontRenderer.drawString(text, x, y - 1, 0);
                fontRenderer.drawString(text, x, y, color);
                GlStateManager.enableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                GlStateManager.disableBlend();
            } else {
                xPosition = (int) (main.getConfigValues().getSkeletonBarX() * sr.getScaledWidth());
                yPosition = (int) (main.getConfigValues().getSkeletonBarY() * sr.getScaledHeight());
                hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                int boxAlpha = 100;
                if (hovered) {
                    boxAlpha = 170;
                }
                int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
                drawRect(xPosition, yPosition, xPosition + this.width, yPosition + this.height, boxColor);
                int width = (int)(main.getConfigValues().getSkeletonBarX()*sr.getScaledWidth());
                int height = (int)(main.getConfigValues().getSkeletonBarY()*sr.getScaledHeight());
                for (int boneCounter = 0; boneCounter < 3; boneCounter++) {
                    mc.getRenderItem().renderItemIntoGUI(PlayerListener.BONE, width+(boneCounter*15)+2, height+2);
                }
            }
        }
    }

    Feature getFeature() {
        return feature;
    }
}
