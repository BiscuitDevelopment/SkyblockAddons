package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonArrow extends GuiButton {

    private static ResourceLocation ARROW_RIGHT = new ResourceLocation("skyblockaddons", "arrowright.png");
    private static ResourceLocation ARROW_LEFT = new ResourceLocation("skyblockaddons", "arrowleft.png");

    private SkyblockAddons main;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();
    private ArrowType arrowType;
    private boolean max;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonArrow(double x, double y, SkyblockAddons main, ArrowType arrowType, boolean max) {
        super(0, (int)x, (int)y, null);
        this.main = main;
        this.width = 30;
        this.height = 30;
        this.arrowType = arrowType;
        this.max = max;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            float alphaMultiplier = 1F;
            if (main.getUtils().isFadingIn()) {
                long timeSinceOpen = System.currentTimeMillis() - timeOpened;
                int fadeMilis = 500;
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = (float) timeSinceOpen / fadeMilis;
                }
            }
            hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
            // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
            GlStateManager.enableBlend();
            if (arrowType == ArrowType.RIGHT) {
                mc.getTextureManager().bindTexture(ARROW_RIGHT);
            } else {
                mc.getTextureManager().bindTexture(ARROW_LEFT);
            }
            if (max) {
                GlStateManager.color(0.5F, 0.5F, 0.5F, alphaMultiplier * 0.5F);
            } else {
                GlStateManager.color(1, 1, 1, alphaMultiplier * 0.7F);
                if (hovered) {
                    GlStateManager.color(1, 1, 1, 1);
                }
            }
            drawModalRectWithCustomSizedTexture(x, y,0,0,width,height,width,height);
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (!max) {
            super.playPressSound(soundHandlerIn);
        }
    }

    public boolean isNotMax() {
        return !max;
    }

    public ArrowType getArrowType() {
        return arrowType;
    }

    public enum ArrowType {
        LEFT,RIGHT
    }
}
