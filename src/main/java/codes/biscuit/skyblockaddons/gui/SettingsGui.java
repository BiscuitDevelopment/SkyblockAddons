package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.math.BigDecimal;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.WIDTH_LIMIT;

public class SettingsGui extends GuiScreen {

    private SkyblockAddons main;
    private boolean cancelScreenReturn = false;

    SettingsGui(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public void initGui() {
        addButton(height*0.25, Message.SETTING_WARNING_COLOR, Feature.WARNING_COLOR, 1);
        addButton(height*0.41, Message.SETTING_CONFIRMATION_COLOR, Feature.CONFIRMATION_COLOR, 1);
        addButton(height*0.33, Message.SETTING_MANA_TEXT_COLOR, Feature.MANA_TEXT_COLOR, 1);
        addButton(height*0.49, Message.SETTING_MANA_BAR_COLOR, Feature.MANA_BAR_COLOR, 1);
        addButton(height*0.25, Message.SETTING_WARNING_TIME, Feature.WARNING_TIME, 3);
        addButton(height*0.25, Message.SETTING_WARNING_COLOR, Feature.WARNING_COLOR, 1);
        addButton(height*0.41, Message.SETTING_CONFIRMATION_COLOR, Feature.CONFIRMATION_COLOR, 1);
        addButton(height*0.33, Message.SETTING_BACKPACK_STYLE, Feature.BACKPACK_STYLE, 3);

        addButton(height*0.25, Message.SETTING_HEALTH_BAR_COLOR, Feature.HEALTH_BAR_COLOR, 2);
        addButton(height*0.33, Message.SETTING_HEALTH_TEXT_COLOR, Feature.HEALTH_TEXT_COLOR, 2);
        addButton(height*0.41, Message.SETTING_DEFENCE_TEXT_COLOR, Feature.DEFENCE_TEXT_COLOR, 2);
        addButton(height*0.49, Message.SETTING_DEFENCE_PERCENTAGE_COLOR, Feature.DEFENCE_PERCENTAGE_COLOR, 2);
        addButton(height*0.49, Message.SETTING_DISABLE_DOUBLE_DROP, Feature.DISABLE_DOUBLE_DROP_AUTOMATICALLY, 3);
        addButton(height*0.57, Message.SETTING_USE_VANILLA_TEXTURE_DEFENCE, Feature.USE_VANILLA_TEXTURE_DEFENCE, 3);
        addButton(height*0.65, Message.SETTING_SHOW_BACKPACK_HOLDING_SHIFT, Feature.SHOW_BACKPACK_HOLDING_SHIFT, 3);
        addSlider();
        int twoThirds = width/3*2;
        buttonList.add(new ButtonRegular(0, twoThirds, height*0.25, "+", main, Feature.ADD, 20, 20));
    }

    private void addSlider() {
        String text = main.getConfigValues().getMessage(Message.SETTING_GUI_SCALE, String.valueOf(getRoundedValue(
                main.getUtils().denormalizeValue(main.getConfigValues().getGuiScale(), ButtonSlider.VALUE_MIN, ButtonSlider.VALUE_MAX, ButtonSlider.VALUE_STEP))));
        int oneThird = width/3;
        int twoThirds = oneThird*2;
        int boxWidth = fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > WIDTH_LIMIT) boxWidth = WIDTH_LIMIT;
        int boxHeight = 20;
        int x = twoThirds+25;
        int y = (int)(height*0.41);
        buttonList.add(new ButtonSlider(0, x, y, boxWidth, boxHeight, main));
    }

    private float getRoundedValue(float value) {
        return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private void addButton(double y, Message message, Feature feature, int collumn) {
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
        drawCenteredString(fontRendererObj, main.getConfigValues().getMessage(Message.SETTING_SETTINGS),
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
            } else if (feature == Feature.BACKPACK_STYLE) {
                main.getConfigValues().setBackpackStyle(main.getConfigValues().getBackpackStyle().getNextType());
                cancelScreenReturn = true;
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main));
                cancelScreenReturn = false;
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
            } else if (feature.getButtonType() == Feature.ButtonType.REGULAR) {
                if (main.getConfigValues().getDisabledFeatures().contains(feature)) {
                    main.getConfigValues().getDisabledFeatures().remove(feature);
                } else {
                    main.getConfigValues().getDisabledFeatures().add(feature);
                }
            }
        }
    }

    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
        if (!cancelScreenReturn) {
            main.getPlayerListener().setOpenGUI(PlayerListener.GUIType.MAIN);
        }
    }
}
