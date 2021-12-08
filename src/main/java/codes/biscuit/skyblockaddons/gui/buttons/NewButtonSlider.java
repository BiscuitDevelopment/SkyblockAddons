package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.MathUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

public class NewButtonSlider extends GuiButton {

    private final SkyblockAddons main = SkyblockAddons.getInstance();
    private final float min;
    private final float max;
    private final float step;
    private final UpdateCallback<Float> sliderCallback;
    private String prefix = "";

    private boolean dragging;
    private float normalizedValue;

    public NewButtonSlider(double x, double y, int width, int height, float value, float min, float max, float step, UpdateCallback<Float> sliderCallback) {
        super(0, (int) x, (int) y, "");
        this.width = width;
        this.height = height;
        this.sliderCallback = sliderCallback;
        this.min = min;
        this.max = max;
        this.step = step;
        this.normalizedValue = MathUtils.normalizeSliderValue(value, min, max, step);
        this.displayString = Utils.roundForString(value, 2);
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
            ScaledResolution sr = new ScaledResolution(mc);
            float minecraftScale = sr.getScaleFactor();
            float floatMouseX = Mouse.getX() / minecraftScale;

            if (this.dragging) {
                this.normalizedValue = (floatMouseX - (this.xPosition + 4)) / (float) (this.width - 8);
                this.normalizedValue = MathHelper.clamp_float(normalizedValue, 0.0F, 1.0F);
                onUpdate();
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawRect(this.xPosition + (int) (this.normalizedValue * (float) (this.width - 8))+1, this.yPosition, this.xPosition + (int) (this.normalizedValue * (float) (this.width - 8))+7, this.yPosition + this.height, ColorCode.GRAY.getColor());
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.normalizedValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
            this.normalizedValue = MathHelper.clamp_float(this.normalizedValue, 0.0F, 1.0F);
            onUpdate();
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

    public NewButtonSlider setPrefix(String text) {
        prefix = text;
        this.updateDisplayString();
        return this;
    }

    private void onUpdate() {
        sliderCallback.onUpdate(denormalize());
        this.updateDisplayString();
    }

    private void updateDisplayString() {
        this.displayString = prefix + Utils.roundForString(denormalize(), 2);
    }

    public float denormalize() {
        return MathUtils.denormalizeSliderValue(normalizedValue, min, max, step);
    }
}

