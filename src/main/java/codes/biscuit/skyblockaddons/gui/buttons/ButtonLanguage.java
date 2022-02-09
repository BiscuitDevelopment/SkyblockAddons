package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.data.DataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ButtonLanguage extends GuiButton {

    private static ResourceLocation FEATURE_BACKGROUND = new ResourceLocation("skyblockaddons", "gui/featurebackground.png");

    private final SkyblockAddons main;
    private final Language language;
    private final String languageName;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonLanguage(double x, double y, String buttonText, SkyblockAddons main, Language language) {
        super(0, (int)x, (int)y, buttonText);
        this.language = language;
        DataUtils.loadLocalizedStrings(language, false);
        this.languageName = Translations.getMessage("language");
        this.main = main;
        this.width = 140;
        this.height = 25;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            DrawUtils.drawRect(xPosition, yPosition, width, height,  ColorUtils.getDummySkyblockColor(28, 29, 41, 230), 4);

            GlStateManager.color(1,1,1,1F);
            try {
                mc.getTextureManager().bindTexture(language.getResourceLocation());
                DrawUtils.drawModalRectWithCustomSizedTexture(xPosition+width-32, yPosition, 0, 0, 30, 26, 30, 26, true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            int fontColor = main.getUtils().getDefaultBlue(255);
            if (hovered) {
                fontColor = new Color(255, 255, 160, 255).getRGB();
            }
            drawCenteredString(mc.fontRendererObj, languageName, xPosition+width/2, yPosition+10, fontColor);
        }
    }

    public Language getLanguage() {
        return language;
    }
}
