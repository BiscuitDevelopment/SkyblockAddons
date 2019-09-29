package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Language;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ButtonLanguage extends GuiButton {

    private static ResourceLocation FEATURE_BACKGROUND = new ResourceLocation("skyblockaddons", "featurebackground.png");

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
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            GlStateManager.color(1,1,1,0.7F);
            mc.getTextureManager().bindTexture(FEATURE_BACKGROUND);
            drawModalRectWithCustomSizedTexture(x, y,0,0,width,height,width,height);
            GlStateManager.color(1,1,1,1F);
            try {
                mc.getTextureManager().bindTexture(new ResourceLocation("skyblockaddons", "flags/"+language.getFlagPath()+".png"));
                drawModalRectWithCustomSizedTexture(x+width-32, y, 0, 0, 30, 26, 30, 26);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            int fontColor = main.getUtils().getDefaultBlue(255);
            if (hovered) {
                fontColor = new Color(255, 255, 160, 255).getRGB();
            }
            main.getConfigValues().loadLanguageFile(language);
            this.drawCenteredString(mc.fontRenderer, Message.LANGUAGE.getMessage(), x+width/2, y+10, fontColor);
        }
    }

    public Language getLanguage() {
        return language;
    }
}
