package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.CoordsPair;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonLocation extends GuiButton {

    private SkyblockAddons main;
    static Feature hoveredFeature;
    private Feature feature;
    private int boxXOne;
    private int boxXTwo;
    private int boxYOne;
    private int boxYTwo;
    private float scaleMultiplier;

    ButtonLocation(SkyblockAddons main, int width, int height, Feature feature) {
        super(-1, 0, 0, null);
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
            if (feature == Feature.MANA_BAR || feature == Feature.HEALTH_BAR) {
                CoordsPair coordsPair = main.getConfigValues().getCoords(feature);
                float x = coordsPair.getX();
                float y = coordsPair.getY();
                xPosition = Math.round(x * sr.getScaledWidth());
                yPosition = Math.round(y * sr.getScaledHeight());
                short barWidth = 92;
                float left = x * sr.getScaledWidth() + 14;
                float top = y * sr.getScaledHeight() + 10;
                float barX = left*scaleMultiplier-60;
                float barY = top*scaleMultiplier-10;

                boxXOne = Math.round(barX-3);
                boxXTwo = Math.round(barX+barWidth+3);
                boxYOne = Math.round(barY-3);
                boxYTwo = Math.round(barY+9);
                hovered = mouseX >= boxXOne/scaleMultiplier && mouseY >= boxYOne/scaleMultiplier && mouseX < boxXTwo/scaleMultiplier && mouseY < boxYTwo/scaleMultiplier;
                int boxAlpha = 100;
                if (hovered) {
                    boxAlpha = 170;
                }
                int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
                drawRect(boxXOne, boxYOne,
                        boxXTwo, boxYTwo, boxColor);
                Feature colorFeature = null;
                if (feature == Feature.MANA_BAR) colorFeature = Feature.MANA_BAR_COLOR;
                else if (feature == Feature.HEALTH_BAR) colorFeature = Feature.HEALTH_BAR_COLOR;
                main.getPlayerListener().drawBar(feature, scaleMultiplier, mc, sr, colorFeature, this);
            } else if (feature == Feature.SKELETON_BAR) {
                CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.SKELETON_BAR);
                xPosition = Math.round(coordsPair.getX() * sr.getScaledWidth());
                yPosition = Math.round(coordsPair.getY() * sr.getScaledHeight());

                float barX = xPosition*scaleMultiplier;
                float barY = ((yPosition+2)*scaleMultiplier);

                boxXOne = Math.round(barX-3);
                boxXTwo = Math.round(barX+48);
                boxYOne = Math.round(barY-3);
                boxYTwo = Math.round(barY+18);
                hovered = mouseX >= boxXOne/scaleMultiplier && mouseY >= boxYOne/scaleMultiplier && mouseX < boxXTwo/scaleMultiplier && mouseY < boxYTwo/scaleMultiplier;
//                hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                int boxAlpha = 100;
                if (hovered) {
                    boxAlpha = 170;
                }
                int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
                drawRect(boxXOne, boxYOne, boxXTwo, boxYTwo, boxColor);
                for (int boneCounter = 0; boneCounter < 3; boneCounter++) {
                    mc.getRenderItem().renderItemIntoGUI(PlayerListener.BONE_ITEM, Math.round(((xPosition+boneCounter*15*scale)*scaleMultiplier)), Math.round(barY));
                }
            } else if (feature == Feature.MANA_TEXT || feature == Feature.HEALTH_TEXT ||
                    feature == Feature.DEFENCE_TEXT || feature == Feature.DEFENCE_PERCENTAGE) {
                CoordsPair coordsPair = main.getConfigValues().getCoords(feature);
                float coordX = coordsPair.getX();
                float coordsY = coordsPair.getY();
                xPosition = Math.round(coordX * sr.getScaledWidth());
                yPosition = Math.round(coordsY * sr.getScaledHeight());
                String text = "123/321";
                float stringWidth = mc.ingameGUI.getFontRenderer().getStringWidth(text);
                float x = coordX * sr.getScaledWidth() + 60 - stringWidth / 2;
                float y = coordsY * sr.getScaledHeight() + 4;
                x+=width/2;
                y+=height/2;
                float barX = x*scaleMultiplier-60;
                float barY = y*scaleMultiplier;

                boxXOne = Math.round(barX-3);
                boxXTwo = Math.round(barX+stringWidth+3);
                boxYOne = Math.round(barY-13);
                boxYTwo = Math.round(barY+1);
                hovered = mouseX >= boxXOne/scaleMultiplier && mouseY >= boxYOne/scaleMultiplier && mouseX < boxXTwo/scaleMultiplier && mouseY < boxYTwo/scaleMultiplier;
                int boxAlpha = 100;
                if (hovered) {
                    boxAlpha = 170;
                }
                int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
                drawRect(boxXOne, boxYOne,
                        boxXTwo, boxYTwo, boxColor);
                Feature colorFeature = null;
                if (feature == Feature.MANA_TEXT) colorFeature = Feature.MANA_TEXT_COLOR;
                else if (feature == Feature.HEALTH_TEXT) colorFeature = Feature.HEALTH_TEXT_COLOR;
                else if (feature == Feature.DEFENCE_TEXT) colorFeature = Feature.DEFENCE_TEXT_COLOR;
                else if (feature == Feature.DEFENCE_PERCENTAGE) colorFeature = Feature.DEFENCE_PERCENTAGE_COLOR;
                main.getPlayerListener().drawText(feature, scaleMultiplier, mc, sr, colorFeature);
            } else if (feature == Feature.DEFENCE_ICON) {
                CoordsPair coordsPair = main.getConfigValues().getCoords(feature);
                float x = coordsPair.getX();
                float y = coordsPair.getY();
                xPosition = Math.round(x * sr.getScaledWidth());
                yPosition = Math.round(y * sr.getScaledHeight());
                short barWidth = 9;
                float barX = xPosition * scaleMultiplier;
                float barY = yPosition * scaleMultiplier;

                boxXOne = Math.round(barX - 3);
                boxXTwo = Math.round(barX + (barWidth + 7)*(scale));
                boxYOne = Math.round(barY - 3);
                boxYTwo = Math.round(barY + 16*(scale));

                hovered = mouseX >= boxXOne / scaleMultiplier && mouseY >= boxYOne / scaleMultiplier && mouseX < boxXTwo / scaleMultiplier && mouseY < boxYTwo / scaleMultiplier;
                int boxAlpha = 100;
                if (hovered) {
                    boxAlpha = 170;
                }
                int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
                drawRect(boxXOne, boxYOne,
                        boxXTwo, boxYTwo, boxColor);
                scale *= 1.5;
                GlStateManager.scale(scale,scale,1);
                scaleMultiplier = 1F/scale;
                main.getPlayerListener().drawIcon(scale, mc, sr, this);
                scale /= 1.5;
                GlStateManager.scale(scale,scale,1);
                scaleMultiplier = 1F/scale;
            }
            if (hovered) {
                hoveredFeature = feature;
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
