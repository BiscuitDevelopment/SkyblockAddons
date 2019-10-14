package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ButtonToggle extends ButtonFeature {

    private static ResourceLocation TOGGLE_ON = new ResourceLocation("skyblockaddons", "toggleon.png");
    private static ResourceLocation TOGGLE_OFF = new ResourceLocation("skyblockaddons", "toggleoff.png");

    private SkyblockAddons main;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonToggle(double x, double y, SkyblockAddons main, Feature feature) {
        super(0, (int)x, (int)y, "", feature);
        this.main = main;
        this.feature = feature;
        this.width = 31;
        this.height = 15;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        float alphaMultiplier = 1F;
        if (main.getUtils().isFadingIn()) {
            long timeSinceOpen = System.currentTimeMillis() - timeOpened;
            int fadeMilis = 500;
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = (float) timeSinceOpen / fadeMilis;
            }
        }
        hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
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
        drawModalRectWithCustomSizedTexture(x, y,0,0,width,height,width,height);
    }
}
