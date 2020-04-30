package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

@Getter
public class ButtonResize extends ButtonFeature {

    private int lastMouseX;
    private int lastMouseY;

    private Corner corner;

    public ButtonResize(int x, int y, Feature feature, Corner corner) {
        super(0, x, y, "", feature);
        this.corner = corner;
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
        GlStateManager.popMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return hovered;
    }

    public enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }
}
