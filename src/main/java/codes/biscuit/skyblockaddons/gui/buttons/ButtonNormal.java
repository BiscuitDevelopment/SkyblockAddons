package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.BUTTON_MAX_WIDTH;

public class ButtonNormal extends ButtonFeature {

    private static ResourceLocation FEATURE_BACKGROUND = new ResourceLocation("skyblockaddons", "feature-background.png");

    private SkyblockAddons main;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonNormal(double x, double y, String buttonText, SkyblockAddons main, Feature feature) {
        this((int)x, (int)y, 140, 50, buttonText, main, feature);
    }

    public ButtonNormal(double x, double y, int width, int height, String buttonText, SkyblockAddons main, Feature feature) {
        super(0, (int)x, (int)y, buttonText, feature);
        this.main = main;
        this.feature = feature;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            int alpha;
            float alphaMultiplier = 1F;
            if (main.getUtils().isFadingIn()) {
                long timeSinceOpen = System.currentTimeMillis() - timeOpened;
                int fadeMilis = 500;
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = (float) timeSinceOpen / fadeMilis;
                }
                alpha = (int) (255 * alphaMultiplier);
            } else {
                alpha = 255;
            }
            hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            if (alpha < 4) alpha = 4;
            int fontColor = main.getUtils().getDefaultBlue(alpha);
//            if (hovered) {
//                fontColor = new Color(255, 255, 160, alpha).getRGB();
//            }
            // Alpha multiplier is from 0 to 1, multiplying it creates the fade effect.
            // Regular features are red if disabled, green if enabled or part of the gui feature is enabled.
            GlStateManager.enableBlend();
            float scale = 1;
            int stringWidth = mc.fontRendererObj.getStringWidth(displayString);
            float widthLimit = BUTTON_MAX_WIDTH -10;
            if (feature == Feature.WARNING_TIME) {
                widthLimit = 90;
            }
            if (stringWidth > widthLimit) {
                scale = 1/(stringWidth/widthLimit);
            }
            GlStateManager.color(1,1,1,0.7F);
            mc.getTextureManager().bindTexture(FEATURE_BACKGROUND);
            drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height);
            float scaleMultiplier = 1/scale;
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            this.drawCenteredString(mc.fontRendererObj, displayString, (int)((xPosition+width/2)*scaleMultiplier), (int)(yPosition*scaleMultiplier+10), fontColor);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            if (feature == Feature.LANGUAGE) {
                GlStateManager.color(1,1,1,1F);
                try {
                    mc.getTextureManager().bindTexture(new ResourceLocation("skyblockaddons", "flags/"+main.getConfigValues().getLanguage().getPath()+".png"));
                    drawModalRectWithCustomSizedTexture(xPosition + width / 2 - 20, yPosition + 20, 0, 0, 38, 30, 38, 30);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
    }
}
