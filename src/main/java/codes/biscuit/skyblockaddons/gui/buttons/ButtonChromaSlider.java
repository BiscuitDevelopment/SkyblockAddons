package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;

public class ButtonChromaSlider extends GuiButton {

    private static final float MIN_VALUE = 0.1F;
    private static final float MAX_VALUE = 10F;
    private static final float STEP = 0.5F;

    private float sliderValue;
    private boolean dragging;

    private SkyblockAddons main;
    private Feature feature;

    public ButtonChromaSlider(double x, double y, int width, int height, SkyblockAddons main, Feature feature) {
        super(0, (int)x, (int)y, "");
        this.sliderValue = 0;
        this.displayString = "";

        if (feature == Feature.CHROMA_SPEED) {
            this.sliderValue = main.getConfigValues().getChromaSpeed();
            this.displayString = String.valueOf(getRoundedValue(denormalizeScale(sliderValue)));
        }

        this.main = main;
        this.width = width;
        this.height = height;
        this.feature = feature;
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
        MinecraftReflection.FontRenderer.drawCenteredString(this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
    }

    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
                this.sliderValue = MathHelper.clamp_float(sliderValue, 0.0F, 1.0F);

                if (feature == Feature.CHROMA_SPEED) {
                    main.getConfigValues().setChromaSpeed(sliderValue);
                    this.displayString = String.valueOf(getRoundedValue(denormalizeScale(sliderValue)));
                }
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8))+1, this.yPosition, this.xPosition + (int) (this.sliderValue * (float) (this.width - 8))+7, this.yPosition + this.height, ChatFormatting.GRAY.getRGB());
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);

            if (feature == Feature.CHROMA_SPEED) {
                main.getConfigValues().setChromaSpeed(sliderValue);
                this.displayString = String.valueOf(getRoundedValue(denormalizeScale(sliderValue)));
            }
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

    public static float denormalizeScale(float value) {
        return snapToStepClamp(ButtonChromaSlider.MIN_VALUE + (ButtonChromaSlider.MAX_VALUE - ButtonChromaSlider.MIN_VALUE) *
                MathHelper.clamp_float(value, 0.0F, 1.0F));
    }

    private static float snapToStepClamp(float value) {
        value = ButtonChromaSlider.STEP * (float) Math.round(value / ButtonChromaSlider.STEP);
        return MathHelper.clamp_float(value, ButtonChromaSlider.MIN_VALUE, ButtonChromaSlider.MAX_VALUE);
    }

}

