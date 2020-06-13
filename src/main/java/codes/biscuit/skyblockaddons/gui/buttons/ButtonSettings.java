package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonSettings extends ButtonFeature {

    private static ResourceLocation GEAR = new ResourceLocation("skyblockaddons", "gui/gear.png");

    private SkyblockAddons main;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonSettings(double x, double y, String buttonText, SkyblockAddons main, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.main = main;
        this.feature = feature;
        this.width = 15;
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
            GlStateManager.enableBlend();
            GlStateManager.color(1,1,1,alphaMultiplier*0.7F);
            if (hovered) {
                GlStateManager.color(1,1,1,1);
            }
            mc.getTextureManager().bindTexture(GEAR);
            drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height);
        }
    }


}
