package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;

public class ButtonSlider extends GuiButton {

    private float min;
    private float max;
    private float step;

    private float valuePercentage;
    private boolean dragging;

    private SkyblockAddons main = SkyblockAddons.getInstance();

    private OnSliderChangeCallback sliderCallback;

    private String prefix = null;

    public ButtonSlider(double x, double y, int width, int height, float initialValue, float min, float max, float step, OnSliderChangeCallback sliderCallback) {
        super(0, (int)x, (int)y, "");
        this.valuePercentage = 0;
        this.displayString = "";
        this.valuePercentage = initialValue;
        this.width = width;
        this.height = height;
        this.sliderCallback = sliderCallback;
        this.min = min;
        this.max = max;
        this.step = step;
        this.displayString = String.valueOf(getRoundedValue(denormalizeScale(valuePercentage)));
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(buttonTextures);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.blendFunc(770, 771);
        int boxAlpha = 100;
        if (hovered) {
            boxAlpha = 170;
        }
        drawRect(this.xPosition, this.yPosition, this.xPosition+this.width, this.yPosition+this.height, main.getUtils().getDefaultColor(boxAlpha));
        this.mouseDragged(mc, mouseX, mouseY);
        int j = 14737632;
        if (packedFGColour != 0) {
            j = packedFGColour;
        } else if (!this.enabled) {
            j = 10526880;
        } else if (this.hovered) {
            j = 16777120;
        }
        drawCenteredString(mc.fontRendererObj, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
    }

    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.valuePercentage = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
                this.valuePercentage = MathHelper.clamp_float(valuePercentage, 0.0F, 1.0F);
                valueUpdated();
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawRect(this.xPosition + (int) (this.valuePercentage * (float) (this.width - 8))+1, this.yPosition, this.xPosition + (int) (this.valuePercentage * (float) (this.width - 8))+7, this.yPosition + this.height, ColorCode.GRAY.getColor());
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.valuePercentage = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
            this.valuePercentage = MathHelper.clamp_float(this.valuePercentage, 0.0F, 1.0F);
            valueUpdated();
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

    private float getRoundedValue(float value) {
        return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    public float denormalizeScale(float value) {
        return MathUtils.denormalizeSliderValue(value, min, max, step);
    }

    public void valueUpdated() {
        sliderCallback.sliderUpdated(valuePercentage);
        this.displayString = (prefix != null ? prefix : "") + getRoundedValue(denormalizeScale(valuePercentage));
    }

    public abstract static class OnSliderChangeCallback {

        public abstract void sliderUpdated(float value);
    }

    public ButtonSlider setPrefix(String text) {
        prefix = text;
        this.displayString = prefix + getRoundedValue(denormalizeScale(valuePercentage));
        return this;
    }
}

