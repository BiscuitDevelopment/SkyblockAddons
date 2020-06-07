package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonColorWheel extends ButtonFeature {

    private static final ResourceLocation COLOR_WHEEL = new ResourceLocation("skyblockaddons", "colorwheel.png");
    private static final int SIZE = 10;

    public ButtonColorWheel(int x, int y, Feature feature) {
        super(0, x, y, "", feature);
        width = SIZE;
        height = SIZE;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        float scale = SkyblockAddons.getInstance().getConfigValues().getGuiScale(feature);
        this.hovered = mouseX >= this.xPosition*scale && mouseY >= this.yPosition*scale &&
                mouseX < this.xPosition*scale + this.width*scale && mouseY < this.yPosition*scale + this.height*scale;
        GlStateManager.color(1,1,1, hovered ? 1 : 0.5F);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale,scale,1);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(COLOR_WHEEL);
        Gui.drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, 10, 10, 10, 10);
        GlStateManager.popMatrix();
    }

    public static int getSize() {
        return SIZE;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        float scale = SkyblockAddons.getInstance().getConfigValues().getGuiScale(feature);
        return mouseX >= this.xPosition*scale && mouseY >= this.yPosition*scale &&
                mouseX < this.xPosition*scale + this.width*scale && mouseY < this.yPosition*scale + this.height*scale;
    }
}
