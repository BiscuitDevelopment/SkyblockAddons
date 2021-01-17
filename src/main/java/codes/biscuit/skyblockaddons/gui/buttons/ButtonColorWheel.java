package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonColorWheel extends ButtonFeature {

    private static final ResourceLocation COLOR_WHEEL = new ResourceLocation("skyblockaddons", "gui/colorwheel.png");
    private static final int SIZE = 10;

    public float x;
    public float y;
    
    public ButtonColorWheel(float x, float y, Feature feature) {
        super(0, 0, 0, "", feature);
        width = SIZE;
        height = SIZE;

        this.x = x;
        this.y = y;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        float scale = SkyblockAddons.getInstance().getConfigValues().getGuiScale(feature);
        this.hovered = mouseX >= this.x*scale && mouseY >= this.y*scale &&
                mouseX < this.x*scale + this.width*scale && mouseY < this.y*scale + this.height*scale;
        GlStateManager.color(1,1,1, hovered ? 1 : 0.5F);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale,scale,1);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(COLOR_WHEEL);
        SkyblockAddons.getInstance().getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, 10, 10, 10, 10, true);
        GlStateManager.popMatrix();
    }

    public static int getSize() {
        return SIZE;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        float scale = SkyblockAddons.getInstance().getConfigValues().getGuiScale(feature);
        return mouseX >= this.x*scale && mouseY >= this.y*scale &&
                mouseX < this.x*scale + this.width*scale && mouseY < this.y*scale + this.height*scale;
    }
}
