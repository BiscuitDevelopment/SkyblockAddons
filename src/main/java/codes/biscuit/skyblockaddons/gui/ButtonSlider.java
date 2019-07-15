package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;

public class ButtonSlider extends GuiButton {

    private static final String BUTTON_TEXT = "Gui Scale ";

    private float sliderValue;
    private boolean dragging;
    public static float VALUE_MIN = 1;
    public static float VALUE_MAX = 5;
    public static float VALUE_STEP = 0.1F;

    private SkyblockAddons main;

    ButtonSlider(int buttonID, double x, double y, int width, int height, SkyblockAddons main) {
        super(buttonID, (int)x, (int)y, width, height, "");
        this.sliderValue = main.getConfigValues().getGuiScale();
        this.displayString = BUTTON_TEXT+getRoundedValue(main.getUtils().denormalizeValue(sliderValue, VALUE_MIN, VALUE_MAX, VALUE_STEP));
        this.main = main;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRendererObj;
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
            drawRect(this.xPosition, this.yPosition, this.xPosition+this.width, this.yPosition+this.height, ConfigColor.DARK_GRAY.getColor(boxAlpha));
            this.mouseDragged(mc, mouseX, mouseY);
            int j = 14737632;

            if (packedFGColour != 0)
            {
                j = packedFGColour;
            }
            else
            if (!this.enabled)
            {
                j = 10526880;
            }
            else if (this.hovered)
            {
                j = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
        }
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
                this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
                float scaleValue = main.getUtils().denormalizeValue(this.sliderValue, VALUE_MIN, VALUE_MAX, VALUE_STEP);
                this.sliderValue = main.getUtils().normalizeValue(scaleValue, VALUE_MIN, VALUE_MAX, VALUE_STEP);
                main.getConfigValues().setGuiScale(sliderValue);
                this.displayString = BUTTON_TEXT+getRoundedValue(main.getUtils().denormalizeValue(sliderValue, VALUE_MIN, VALUE_MAX, VALUE_STEP));
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8))+1, this.yPosition, this.xPosition + (int) (this.sliderValue * (float) (this.width - 8))+7, this.yPosition + 20, ConfigColor.GRAY.getColor(255));
//            drawRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4 + 4, this.yPosition + 20, ConfigColor.GRAY.getColor(255));
//            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
//            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
            main.getConfigValues().setGuiScale(sliderValue);
//            mc.gameSettings.setOptionFloatValue(this.options, this.options.denormalizeValue(this.sliderValue));
            this.displayString = BUTTON_TEXT+getRoundedValue(main.getUtils().denormalizeValue(sliderValue, VALUE_MIN, VALUE_MAX, VALUE_STEP));
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

    private float getRoundedValue(float value) {
        return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }
}

