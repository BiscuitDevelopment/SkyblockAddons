package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.math.BigDecimal;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.WIDTH_LIMIT;

public class SettingsGui extends GuiScreen {

    private SkyblockAddons main;
    private boolean openingLocations = false;

    public SettingsGui(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public void initGui() {
        addButton(height*0.25, ConfigValues.Message.SETTING_WARNING_COLOR, Feature.WARNING_COLOR, 1);
        addButton(height*0.41, ConfigValues.Message.SETTING_CONFIRMATION_COLOR, Feature.CONFIRMATION_COLOR, 1);
        addButton(height*0.33, ConfigValues.Message.SETTING_MANA_TEXT_COLOR, Feature.MANA_TEXT_COLOR, 1);
        addButton(height*0.49, ConfigValues.Message.SETTING_MANA_BAR_COLOR, Feature.MANA_BAR_COLOR, 1);
        addButton(height*0.25, ConfigValues.Message.SETTING_WARNING_TIME, Feature.WARNING_TIME, 3);
        addButton(height*0.25, ConfigValues.Message.SETTING_EDIT_LOCATIONS, Feature.EDIT_LOCATIONS, 2);
        addButton(height*0.25, ConfigValues.Message.SETTING_WARNING_COLOR, Feature.WARNING_COLOR, 1);
        addButton(height*0.41, ConfigValues.Message.SETTING_CONFIRMATION_COLOR, Feature.CONFIRMATION_COLOR, 1);
        addButton(height*0.25, ConfigValues.Message.SETTING_WARNING_COLOR, Feature.WARNING_COLOR, 1);
        addSlider();
        int twoThirds = width/3*2;
        buttonList.add(new ButtonRegular(0, twoThirds, height*0.25, "+", main, Feature.ADD, 20, 20));
    }

    private void addSlider() {
        String text = main.getConfigValues().getMessage(ConfigValues.Message.SETTING_GUI_SCALE, String.valueOf(getRoundedValue(
                main.getUtils().denormalizeValue(main.getConfigValues().getGuiScale(), ButtonSlider.VALUE_MIN, ButtonSlider.VALUE_MAX, ButtonSlider.VALUE_STEP))));
        int halfWidth = width/2;
        int boxWidth = fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > WIDTH_LIMIT) boxWidth = WIDTH_LIMIT;
        int boxHeight = 20;
        int x = halfWidth-(boxWidth/2);
        int y = (int)(height*0.33);
        buttonList.add(new ButtonSlider(0, x, y, boxWidth, boxHeight, main));
    }

    private float getRoundedValue(float value) {
        return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private void addButton(double y, ConfigValues.Message message, Feature feature, int collumn) {
        String text = null;
        if (message != null) {
            text = main.getConfigValues().getMessage(message);
        }
        int halfWidth = width/2;
        int oneThird = width/3;
        int twoThirds = oneThird*2;
        int widthLimit = WIDTH_LIMIT;
        if (feature == Feature.WARNING_TIME) {
            widthLimit = 100;
        }
        int boxWidth = fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > widthLimit) boxWidth = widthLimit;
        int boxHeight = 20;
        int x = 0;
        if (collumn == 1) {
            x = oneThird-boxWidth-30;
        } else if (collumn == 2) {
            x = halfWidth-(boxWidth/2);
        } else if (collumn == 3) {
            x = twoThirds+25;
        }
        if (feature == Feature.WARNING_TIME) buttonList.add(new ButtonRegular(0, x+boxWidth+5, height*0.25, "-", main, Feature.SUBTRACT, 20, 20));
        buttonList.add(new ButtonRegular(0, x, y, text, main, feature, boxWidth, boxHeight));
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
