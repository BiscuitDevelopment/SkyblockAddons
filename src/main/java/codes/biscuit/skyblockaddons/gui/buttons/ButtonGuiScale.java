package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.objects.FloatPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ButtonGuiScale extends ButtonFeature {

    private float sliderValue;
    private boolean dragging;
    private final Boolean isXScale;

    private final SkyblockAddons main;

    public ButtonGuiScale(double x, double y, int width, int height, SkyblockAddons main, Feature feature) {
        super(0, (int) x, (int) y, "", feature);
        float sliderValue = main.getConfigValues().getGuiScale(feature, false);

        if (Float.isInfinite(sliderValue) || Float.isNaN(sliderValue)) {
            throw new NumberFormatException("GUI scale for feature " + feature.getId() + " is infinite or NaN.");
        }

        this.sliderValue = sliderValue;
        this.displayString = Message.SETTING_GUI_SCALE.getMessage(String.valueOf(getRoundedValue(main.getConfigValues().getGuiScale(feature))));
        this.main = main;
        this.width = width;
        this.height = height;
        this.isXScale = null;
    }

    /**
     * Overloaded for x and y scale (only used on bars currently)
     */
    public ButtonGuiScale(double x, double y, int width, int height, SkyblockAddons main, Feature feature, boolean isXScale) {
        super(0, (int) x, (int) y, "", feature);
        FloatPair sizes = main.getConfigValues().getSizes(feature);
        if (isXScale) {
            this.sliderValue = sizes.getX();
            this.displayString = EnumUtils.FeatureSetting.GUI_SCALE_X.getMessage(String.valueOf(getRoundedValue(main.getConfigValues().getSizesX(feature))));
        } else {
            this.sliderValue = sizes.getY();
            this.displayString = EnumUtils.FeatureSetting.GUI_SCALE_Y.getMessage(String.valueOf(getRoundedValue(main.getConfigValues().getSizesX(feature))));
        }
        this.isXScale = isXScale;
        this.main = main;
        this.width = width;
        this.height = height;
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
                this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
                this.sliderValue = MathHelper.clamp_float(sliderValue, 0.0F, 1.0F);
                setNewScale();
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8))+1, this.yPosition, this.xPosition + (int) (this.sliderValue * (float) (this.width - 8))+7, this.yPosition + this.height, ColorCode.GRAY.getColor());
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
            setNewScale();
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
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    private void setNewScale() {
        if (isXScale == null) {
            main.getConfigValues().setGuiScale(feature, sliderValue);
            this.displayString = Message.SETTING_GUI_SCALE.getMessage(String.valueOf(getRoundedValue(main.getConfigValues().getGuiScale(feature))));
        }
        // For x and y scaling
        else {
            FloatPair sizes = main.getConfigValues().getSizes(feature);
            if (isXScale) {
                sizes.setX(sliderValue);
                this.displayString = EnumUtils.FeatureSetting.GUI_SCALE_X.getMessage(String.valueOf(getRoundedValue(main.getConfigValues().getSizesX(feature))));
            } else {
                sizes.setY(sliderValue);
                this.displayString = EnumUtils.FeatureSetting.GUI_SCALE_Y.getMessage(String.valueOf(getRoundedValue(main.getConfigValues().getSizesY(feature))));
            }
        }
    }
}

