package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonToggleTwo extends ButtonFeature {

    private static ResourceLocation TOGGLE_ON = new ResourceLocation("skyblockaddons", "toggleon.png");
    private static ResourceLocation TOGGLE_OFF = new ResourceLocation("skyblockaddons", "toggleoff.png");

    private SkyblockAddons main;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonToggleTwo(double x, double y, String buttonText, SkyblockAddons main, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.main = main;
        this.feature = feature;
        this.width = 31;
        this.height = 15;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            float alphaMultiplier = 1F;
            if (main.getUtils().isFadingIn()) {
                long timeSinceOpen = System.currentTimeMillis() - timeOpened;
                int fadeMilis = 500;
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = (float) timeSinceOpen / fadeMilis;
                }
            }
            hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
            // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
//            float scale = 1;
//            int stringWidth = mc.fontRendererObj.getStringWidth(displayString);
//            float widthLimit = BUTTON_MAX_WIDTH -10;
//            if (feature == Feature.WARNING_TIME) {
//                widthLimit = 90;
//            }
//            if (stringWidth > widthLimit) {
//                scale = 1/(stringWidth/widthLimit);
//            }
            GlStateManager.enableBlend();
            GlStateManager.color(1,1,1,alphaMultiplier*0.7F);
            if (hovered) {
                GlStateManager.color(1,1,1,1);
            }
            if (main.getConfigValues().isEnabled(feature)) {
                mc.getTextureManager().bindTexture(TOGGLE_ON);
            } else {
                mc.getTextureManager().bindTexture(TOGGLE_OFF);
            }
            drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height);
//            drawRect(xPosition, yPosition, xPosition+this.width, yPosition+this.height, boxColor);
//            float scaleMultiplier = 1/scale;
//            GlStateManager.pushMatrix();
//            GlStateManager.scale(scale, scale, 1);
//            this.drawCenteredString(mc.fontRendererObj, "ON", (int)((xPosition+width/2)*scaleMultiplier), (int)((yPosition+(this.height-(8/scaleMultiplier))/2)*scaleMultiplier), fontColor);
//            GlStateManager.disableBlend();
//            GlStateManager.popMatrix();
//            if (hovered) {
//                SkyblockAddonsGui.setTooltipFeature(feature);
//            }
        }
    }
}
