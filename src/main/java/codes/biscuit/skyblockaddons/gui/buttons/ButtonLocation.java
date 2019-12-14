package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.nifty.color.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonLocation extends ButtonFeature {

    // So we know the latest hovered feature (used for arrow key movement).
    private static Feature lastHoveredFeature = null;

    private SkyblockAddons main;
    private int lastMouseX;
    private int lastMouseY;

    /**
     * Create a button that allows you to change the location of a GUI element.
     */
    public ButtonLocation(SkyblockAddons main, Feature feature) {
        super(-1, 0, 0, null, feature);
        this.main = main;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        float scale = main.getConfigValues().getGuiScale(feature);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);

        if (feature == Feature.DEFENCE_ICON) { // this one is just a little different
            scale *= 1.5;
            GlStateManager.scale(scale,scale,1);
            main.getRenderListener().drawIcon(scale, mc, this);
            scale /= 1.5;
            GlStateManager.scale(scale,scale,1);
        } else {
            feature.draw(scale, mc, this);
        }
        GlStateManager.popMatrix();

        if (hovered) {
            lastHoveredFeature = feature;
        }
    }

    /**
     * This just updates the hovered status and draws the box around each feature. To avoid repetitive code.
     */
    public void checkHoveredAndDrawBox(int boxXOne, int boxXTwo, int boxYOne, int boxYTwo, float scale) {
        hovered = lastMouseX >= boxXOne * scale && lastMouseY >= boxYOne * scale && lastMouseX < boxXTwo * scale && lastMouseY < boxYTwo * scale;
        int boxAlpha = 100;
        if (hovered) {
            boxAlpha = 170;
        }
        int boxColor = ChatFormatting.GRAY.getColor(boxAlpha).asRGB();
        drawRect(boxXOne, boxYOne,
                boxXTwo, boxYTwo, boxColor);
    }

    /**
     * Because the box changes with the scale, have to override this.
     */
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && hovered;
    }

    public static Feature getLastHoveredFeature() {
        return lastHoveredFeature;
    }

    public int getLastMouseY() {
        return lastMouseY;
    }

    public int getLastMouseX() {
        return lastMouseX;
    }
}
