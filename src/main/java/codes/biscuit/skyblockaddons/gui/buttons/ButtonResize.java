package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

@Getter
public class ButtonResize extends ButtonFeature {

    private int size = 2;

    private Corner corner;

    public float x;
    public float y;

    private float cornerOffsetX;
    private float cornerOffsetY;

    public ButtonResize(float x, float y, Feature feature, Corner corner) {
        super(0, 0, 0, "", feature);
        this.corner = corner;
        this.x = x;
        this.y = y;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {

        float scale = SkyblockAddons.getInstance().getConfigValues().getGuiScale(feature);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale,scale,1);
        hovered = mouseX >= (x-size)*scale && mouseY >= (y-size)*scale && mouseX < (x+size)*scale && mouseY < (y+size)* scale;
        int color = hovered ? ChatFormatting.WHITE.getRGB() : ChatFormatting.WHITE.getColor(70).getRGB();
        SkyblockAddons.getInstance().getUtils().drawRect(x-size,y-size, x+size, y+size, color);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        ScaledResolution sr = new ScaledResolution(mc);
        float minecraftScale = sr.getScaleFactor();
        float floatMouseX = Mouse.getX() / minecraftScale;
        float floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale;

        cornerOffsetX = floatMouseX;
        cornerOffsetY = floatMouseY;

        return hovered;
    }

    public enum Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }
}
