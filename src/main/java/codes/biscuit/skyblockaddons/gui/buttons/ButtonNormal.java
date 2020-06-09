package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.CoordsPair;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ButtonNormal extends ButtonFeature {

    private static ResourceLocation FEATURE_BACKGROUND = new ResourceLocation("skyblockaddons", "gui/featurebackground.png");

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
            if (main.getConfigValues().isRemoteDisabled(feature)) {
                fontColor = new Color(60,60,60).getRGB();
            }
            GlStateManager.enableBlend();
            GlStateManager.color(1,1,1,0.7F);
            if (main.getConfigValues().isRemoteDisabled(feature)) {
                GlStateManager.color(0.3F,0.3F,0.3F,0.7F);
            }
            mc.getTextureManager().bindTexture(FEATURE_BACKGROUND);
            drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height);

            EnumUtils.FeatureCredit creditFeature = EnumUtils.FeatureCredit.fromFeature(feature);

            // Wrap the feature name into 2 lines.
            String[] wrappedString = main.getUtils().wrapSplitText(displayString, 28);
            if (wrappedString.length > 2) { // If it makes more than 2 lines,
                StringBuilder lastLineString = new StringBuilder(); // combine all the last
                for (int i = 1; i < wrappedString.length; i++) { // lines and combine them
                    lastLineString.append(wrappedString[i]); // back into the second line.
                    if (i != wrappedString.length-1) {
                        lastLineString.append(" ");
                    }
                }

                wrappedString = new String[]{wrappedString[0], lastLineString.toString()};
            }

            int textX = xPosition + width / 2;
            int textY = yPosition;

            boolean multiline = wrappedString.length > 1;

            for (int i = 0; i < wrappedString.length; i++) {
                String line = wrappedString[i];

                float scale = 1;
                int stringWidth = MinecraftReflection.FontRenderer.getStringWidth(line);
                float widthLimit = SkyblockAddonsGui.BUTTON_MAX_WIDTH - 10;
                if (feature == Feature.WARNING_TIME) {
                    widthLimit = 90;
                }
                if (stringWidth > widthLimit) {
                    scale = 1 / (stringWidth / widthLimit);
                }
                if (feature == Feature.GENERAL_SETTINGS) textY -= 5;

                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                int offset = 9;
                if (creditFeature != null) offset -= 4;
                offset += (10 - 10*scale); // If the scale is small gotta move it down a bit or else its too mushed with the above line.
                MinecraftReflection.FontRenderer.drawCenteredString(line, (textX / scale), (textY / scale) + offset, fontColor);
                GlStateManager.popMatrix();

                // If its not the last line, add to the Y.
                if (multiline && i == 0) {
                    textY += 10;
                }
            }

            if (creditFeature != null) {
                float scale = 0.8F;
                if (multiline) { // If its 2 lines the credits have to be smaller.
                    scale = 0.6F;
                }
                float creditsY = (textY / scale) + 23;
                if (multiline) {
                    creditsY += 3; // Since its smaller the scale is wierd to move it down a tiny bit.
                }

                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                MinecraftReflection.FontRenderer.drawCenteredString(creditFeature.getAuthor(), (textX / scale), creditsY, fontColor);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            if (feature == Feature.LANGUAGE) {
                GlStateManager.color(1,1,1,1F);
                try {
                    mc.getTextureManager().bindTexture(new ResourceLocation("skyblockaddons", "flags/"+main.getConfigValues().getLanguage().getFlagPath()+".png"));
                    if (main.getUtils().isHalloween()) {
                        mc.getTextureManager().bindTexture(new ResourceLocation("skyblockaddons", "flags/halloween.png"));
                    }
                    drawModalRectWithCustomSizedTexture(xPosition + width / 2 - 20, yPosition + 20, 0, 0, 38, 30, 38, 30);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (feature == Feature.EDIT_LOCATIONS) {
                GlStateManager.color(1,1,1,1F);
                try {
                    mc.getTextureManager().bindTexture(new ResourceLocation("skyblockaddons", "gui/move.png"));
                    drawModalRectWithCustomSizedTexture(xPosition + width / 2 - 12, yPosition + 22, 0, 0, 25, 25, 25, 25);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (main.getConfigValues().isRemoteDisabled(feature)) {
                MinecraftReflection.FontRenderer.drawCenteredString(Message.MESSAGE_FEATURE_DISABLED.getMessage(), textX, textY + 6 , main.getUtils().getDefaultBlue(alpha));
            }
        }
    }

    public CoordsPair getCreditsCoords(EnumUtils.FeatureCredit credit) {
        String[] wrappedString = main.getUtils().wrapSplitText(displayString, 28);
        boolean multiLine = wrappedString.length > 1;

        float scale = 0.8F;
        if (multiLine) { // If its 2 lines the credits have to be smaller.
            scale = 0.6F;
        }

        int y = (int)((yPosition/scale) + (multiLine ? 30 : 21)); // If its a smaller scale, you gotta move it down more.

        if (multiLine) { // When there's multiple lines the second line is moved 10px down.
            y += 10;
        }

        int x = (int)((xPosition+width/2)/scale) - MinecraftReflection.FontRenderer.getStringWidth(credit.getAuthor()) / 2 - 17;
        return new CoordsPair(x, y);
    }

    public boolean isMultilineButton() {
        String[] wrappedString = main.getUtils().wrapSplitText(displayString, 28);
        return wrappedString.length > 1;
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        if (feature == Feature.LANGUAGE || feature == Feature.EDIT_LOCATIONS || feature == Feature.GENERAL_SETTINGS) {
            super.playPressSound(soundHandlerIn);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (feature == Feature.LANGUAGE || feature == Feature.EDIT_LOCATIONS || feature == Feature.GENERAL_SETTINGS) {
            return super.mousePressed(mc, mouseX, mouseY);
        }
        return false;
    }
}
