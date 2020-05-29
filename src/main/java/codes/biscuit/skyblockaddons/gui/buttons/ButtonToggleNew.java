package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public class ButtonToggleNew extends GuiButton {

    private static ResourceLocation TOGGLE_INSIDE_CIRCLE = new ResourceLocation("skyblockaddons", "toggleinsidecircle.png");
    private static ResourceLocation TOGGLE_BORDER = new ResourceLocation("skyblockaddons", "toggleborder.png");
    private static ResourceLocation TOGGLE_INSIDE_BACKGROUND = new ResourceLocation("skyblockaddons", "toggleinsidebackground.png");

    private int circlePaddingLeft;
    private int animationSlideDistance;
    private int animationSlideTime = 150;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    private long animationButtonClicked = -1;

    private Supplier<Boolean> enabledSupplier;
    private Runnable onClickRunnable;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonToggleNew(double x, double y, int height, Supplier<Boolean> enabledSupplier, Runnable onClickRunnable) {
        super(0, (int)x, (int)y, "");
        this.width = (int)Math.round(height*2.07);
        this.height = height;
        this.enabledSupplier = enabledSupplier;
        this.onClickRunnable = onClickRunnable;

        circlePaddingLeft = height/3;
        animationSlideDistance = Math.round(height*0.8F);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        float alphaMultiplier = 1F;
        if (main.getUtils().isFadingIn()) {
            long timeSinceOpen = System.currentTimeMillis() - timeOpened;
            int fadeMilis = 500;
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = (float) timeSinceOpen / fadeMilis;
            }
        }
        hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        GlStateManager.enableBlend();
        GlStateManager.color(1,1,1,alphaMultiplier*0.7F);
        if (hovered) {
            GlStateManager.color(1,1,1,1);
        }

        main.getUtils().bindRGBColor(0xFF1e252e);
        mc.getTextureManager().bindTexture(TOGGLE_BORDER);
        drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height);

        boolean enabled = enabledSupplier.get();
        if (enabled) {
            main.getUtils().bindColorInts(36, 255, 98, 255); // Green
        } else {
            main.getUtils().bindColorInts(222, 68, 76, 255); // Red
        }

        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_BACKGROUND);
        drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height);

        int startingX = getButtonStartingX(enabled);
        int slideAnimationOffset = 0;

        if (animationButtonClicked != -1) {
            startingX = getButtonStartingX(!enabled); // They toggled so start from the opposite side.

            int timeSinceOpen = (int)(System.currentTimeMillis() - animationButtonClicked);
            int animationTime = animationSlideTime;
            if (timeSinceOpen > animationTime) {
                timeSinceOpen = animationTime;
            }

            slideAnimationOffset = animationSlideDistance * timeSinceOpen/animationTime;
        }

        startingX += enabled ? slideAnimationOffset : -slideAnimationOffset;

        GlStateManager.color(1,1,1,1);
        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_CIRCLE);
        int circleSize = Math.round(height*0.6F); // 60% of the height.
        int y = Math.round(yPosition+(this.height*0.2F)); // 20% OF the height.
        drawModalRectWithCustomSizedTexture(startingX, y,0,0, circleSize, circleSize, circleSize, circleSize);
    }

    /**
     * The inside circle starts at either the left or right
     * side depending on whether this button is enabled.
     * This returns that x position.
     */
    private int getButtonStartingX(boolean enabled) {
        if (!enabled)  {
            return xPosition + circlePaddingLeft;
        } else {
            return getButtonStartingX(false) + animationSlideDistance;
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean pressed = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        if (pressed) {
            this.animationButtonClicked = System.currentTimeMillis();
            onClickRunnable.run();
        }

        return pressed;
    }
}
