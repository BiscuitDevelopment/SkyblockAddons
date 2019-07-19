package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class SettingsGui extends GuiScreen {

    private SkyblockAddons main;
    private boolean openingLocations = false;

    public SettingsGui(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public void initGui() {
        int halfWidth = width/2;
        int oneThird = width/3;
        int twoThirds = oneThird*2;
        int boxHeight = 20;
        int boxWidth = 100;
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.25, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_WARNING_COLOR), main, Feature.WARNING_COLOR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.41, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_CONFIRMATION_COLOR), main, Feature.CONFIRMATION_COLOR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.33, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_MANA_TEXT_COLOR), main, Feature.MANA_TEXT_COLOR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.49, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_MANA_BAR_COLOR), main, Feature.MANA_BAR_COLOR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, twoThirds+25, height*0.25, null, main, Feature.WARNING_TIME, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, halfWidth-(boxWidth/2), height*0.25, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_EDIT_LOCATIONS), main, Feature.EDIT_LOCATIONS, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, halfWidth-(boxWidth/2), height*0.41, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_LANGUAGE)+
                main.getConfigValues().getMessage(ConfigValues.Message.LANGUAGE), main, Feature.LANGUAGE, boxWidth, boxHeight));
        buttonList.add(new ButtonSlider(0, halfWidth-(boxWidth/2), height*0.33, boxWidth, boxHeight, main));
        boxWidth = 20;
        buttonList.add(new ButtonRegular(0, twoThirds, height*0.25, "+", main, Feature.ADD, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, twoThirds+100+20+5+5, height*0.25, "-", main, Feature.SUBTRACT, boxWidth, boxHeight));
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int alpha = 127;

        int startColor = new Color(0,0, 0, alpha).getRGB(); // Black
        int endColor = new Color(0,0, 0, (int)(alpha*1.5)).getRGB(); // Orange
        drawGradientRect(0, 0, width, height, startColor, endColor);
        GlStateManager.enableBlend();

        GlStateManager.pushMatrix();
        float scale = 2.5F;
        float scaleMultiplier = 1F/scale; // Keeps the proportions of the text intact.
        GlStateManager.scale(scale, scale, 1);
        int blue = new Color(189,236,252, alpha*2).getRGB();
        drawCenteredString(fontRendererObj, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_SETTINGS),
                (int)(width/2*scaleMultiplier), (int)(height*0.12*scaleMultiplier), blue);
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }


    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonRegular) {
            Feature feature = ((ButtonRegular) abstractButton).getFeature();
            if (feature.getButtonType() == Feature.ButtonType.COLOR) {
                main.getConfigValues().setColor(feature, main.getConfigValues().getColor(feature).getNextColor());
            } else if (feature.getButtonType() == Feature.ButtonType.MODIFY) {
                if (feature == Feature.ADD) {
                    if (main.getConfigValues().getWarningSeconds() < 99) {
                        main.getConfigValues().setWarningSeconds(main.getConfigValues().getWarningSeconds() + 1);
                    }
                } else {
                    if (main.getConfigValues().getWarningSeconds() > 1) {
                        main.getConfigValues().setWarningSeconds(main.getConfigValues().getWarningSeconds() - 1);
                    }
                }
            } else if (feature == Feature.EDIT_LOCATIONS) {
                openingLocations = true;
                Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main));
            } else if (feature == Feature.LANGUAGE) {
                main.getConfigValues().setLanguage(main.getConfigValues().getLanguage().getNextLanguage());
                main.getConfigValues().loadLanguageFile();
            }
        }
    }

    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
        if (!openingLocations) {
            main.getUtils().setFadingIn(false);
            main.getPlayerListener().setOpenMainGUI(true);
        }
    }
}
