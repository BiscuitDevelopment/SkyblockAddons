package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.Language;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ButtonLanguage extends GuiButton {

    private static ResourceLocation FEATURE_BACKGROUND = new ResourceLocation("skyblockaddons", "gui/featurebackground.png");

    private Language language;
    private SkyblockAddons main;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonLanguage(double x, double y, String buttonText, SkyblockAddons main, Language language) {
        super(0, (int)x, (int)y, buttonText);
        this.language = language;
        this.main = main;
        this.width = 140;
        this.height = 25;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            GlStateManager.color(1,1,1,0.7F);
            mc.getTextureManager().bindTexture(FEATURE_BACKGROUND);
            main.getUtils().drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height, true);
            GlStateManager.color(1,1,1,1F);
            try {
                mc.getTextureManager().bindTexture(language.getResourceLocation());
                main.getUtils().drawModalRectWithCustomSizedTexture(xPosition+width-32, yPosition, 0, 0, 30, 26, 30, 26, true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            int fontColor = main.getUtils().getDefaultBlue(255);
            if (hovered) {
                fontColor = new Color(255, 255, 160, 255).getRGB();
            }
            main.getUtils().loadLanguageFile(language);
            MinecraftReflection.FontRenderer.drawCenteredString(Message.LANGUAGE.getMessage(), xPosition+width/2, yPosition+10, fontColor);
        }
    }

    public Language getLanguage() {
        return language;
    }
}
