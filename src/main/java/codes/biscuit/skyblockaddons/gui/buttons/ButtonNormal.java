package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.CoordsPair;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.BUTTON_MAX_WIDTH;

public class ButtonNormal extends ButtonFeature {

    private static ResourceLocation FEATURE_BACKGROUND = new ResourceLocation("skyblockaddons", "featurebackground.png");

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
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
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
            hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            if (alpha < 4) alpha = 4;
            int fontColor = main.getUtils().getDefaultBlue(alpha);
            GlStateManager.enableBlend();
            float scale = 1;
            int stringWidth = mc.fontRenderer.getStringWidth(displayString);
            float widthLimit = BUTTON_MAX_WIDTH -10;
            if (feature == Feature.WARNING_TIME) {
                widthLimit = 90;
            }
            if (stringWidth > widthLimit) {
                scale = 1/(stringWidth/widthLimit);
            }
            GlStateManager.color(1,1,1,0.7F);
            mc.getTextureManager().bindTexture(FEATURE_BACKGROUND);
            drawModalRectWithCustomSizedTexture(this.x, this.y,0,0,width,height,width,height);

            int textX = this.x+width/2;
            int textY = this.y;

            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            int offset = 9;
            EnumUtils.FeatureCredit creditFeature = EnumUtils.FeatureCredit.fromFeature(feature);
            if (creditFeature != null) offset-=4;
            this.drawCenteredString(mc.fontRenderer, displayString, (int)(textX/scale), (int)(textY/scale)+offset, fontColor);
            GlStateManager.popMatrix();

            if (creditFeature != null) {
                scale = 0.8F;
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                this.drawCenteredString(mc.fontRenderer, creditFeature.getAuthor(), (int) (textX / scale), (int) (textY / scale) + 23, fontColor);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            if (feature == Feature.LANGUAGE) {
                GlStateManager.color(1,1,1,1F);
                try {
                    mc.getTextureManager().bindTexture(new ResourceLocation("skyblockaddons", "flags/"+main.getConfigValues().getLanguage().getFlagPath()+".png"));
                    drawModalRectWithCustomSizedTexture(this.x + width / 2 - 20, this.y + 20, 0, 0, 38, 30, 38, 30);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (feature == Feature.EDIT_LOCATIONS) {
                GlStateManager.color(1,1,1,1F);
                try {
                    mc.getTextureManager().bindTexture(new ResourceLocation("skyblockaddons", "move.png"));
                    drawModalRectWithCustomSizedTexture(this.x + width / 2 - 12, this.y + 22, 0, 0, 25, 25, 25, 25);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public CoordsPair getCreditsCoords(EnumUtils.FeatureCredit credit) {
        float scale = 0.8F;
        int x = (int)((this.x+width/2)/scale) - Minecraft.getMinecraft().fontRenderer.getStringWidth(credit.getAuthor()) / 2 - 17;
        int y = (int) (this.y/scale) + 21;
        return new CoordsPair(x,y);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (feature == Feature.LANGUAGE || feature == Feature.EDIT_LOCATIONS) {
            return super.mousePressed(mc, mouseX, mouseY);
        }
        return false;
    }

}
