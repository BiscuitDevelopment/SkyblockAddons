package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.nifty.color.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class ButtonResize extends ButtonFeature {

    private int lastMouseX;
    private int lastMouseY;

    public ButtonResize(int x, int y, Feature feature) {
        super(0, x, y, "", feature);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        float scale = SkyblockAddons.getInstance().getConfigValues().getGuiScale(feature);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale,scale,1);
        hovered = mouseX >= (xPosition-3)*scale && mouseY >= (yPosition-3)*scale && mouseX < (xPosition+3)*scale && mouseY < (yPosition+3)* scale;
        int color = hovered ? ChatFormatting.WHITE.getRGB() : ChatFormatting.WHITE.getColor(127).getRGB();
        drawRect(xPosition-3,yPosition-3, xPosition+3, yPosition+3, color);
        GlStateManager.scale(scale,scale,1);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return hovered;
    }

    public int getLastMouseX() {
        return lastMouseX;
    }

    public int getLastMouseY() {
        return lastMouseY;
    }
}
