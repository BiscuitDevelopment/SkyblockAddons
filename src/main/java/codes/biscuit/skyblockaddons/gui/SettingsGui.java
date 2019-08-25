package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.math.BigDecimal;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.BUTTON_MAX_WIDTH;

public class SettingsGui extends GuiScreen {

    private SkyblockAddons main;
    private boolean cancelScreenReturn = false;

    SettingsGui(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public void initGui() {
        // Add all the buttons in the settings
        addButton(0.25, Message.SETTING_WARNING_COLOR, Feature.MAGMA_WARNING, 1, EnumUtils.ButtonType.COLOR);
        addButton(0.33, Message.SETTING_CONFIRMATION_COLOR, Feature.DROP_CONFIRMATION, 1, EnumUtils.ButtonType.COLOR);
        addButton(0.41, Message.SETTING_MANA_BAR_COLOR, Feature.MANA_BAR, 1, EnumUtils.ButtonType.COLOR);
        addButton(0.49, Message.SETTING_MANA_TEXT_COLOR, Feature.MANA_TEXT, 1, EnumUtils.ButtonType.COLOR);
        addButton(0.57, Message.SETTING_HEALTH_BAR_COLOR, Feature.HEALTH_BAR, 1, EnumUtils.ButtonType.COLOR);
        addButton(0.65, Message.SETTING_HEALTH_TEXT_COLOR, Feature.HEALTH_TEXT, 1, EnumUtils.ButtonType.COLOR);
        addButton(0.73, Message.SETTING_DEFENCE_TEXT_COLOR, Feature.DEFENCE_TEXT, 1, EnumUtils.ButtonType.COLOR);
        addButton(0.81, Message.SETTING_DEFENCE_PERCENTAGE_COLOR, Feature.DEFENCE_PERCENTAGE, 1, EnumUtils.ButtonType.COLOR);

        addButton(0.25, Message.SETTING_DISABLE_DOUBLE_DROP, Feature.DISABLE_DOUBLE_DROP_AUTOMATICALLY, 2, EnumUtils.ButtonType.TOGGLE);
        addButton(0.33, Message.SETTING_USE_VANILLA_TEXTURE_DEFENCE, Feature.USE_VANILLA_TEXTURE_DEFENCE, 2, EnumUtils.ButtonType.TOGGLE);
        addButton(0.41, Message.SETTING_SHOW_BACKPACK_HOLDING_SHIFT, Feature.SHOW_BACKPACK_HOLDING_SHIFT, 2, EnumUtils.ButtonType.TOGGLE);
        addButton(0.49, Message.SETTING_SHOW_MAGMA_TIMER_IN_OTHER_GAMES, Feature.SHOW_MAGMA_TIMER_IN_OTHER_GAMES, 2, EnumUtils.ButtonType.TOGGLE);
        addButton(0.57, Message.SETTING_SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES, Feature.SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES, 2, EnumUtils.ButtonType.TOGGLE);


        addButton(0.73, Message.SETTING_MAGMA_BOSS_TIMER_COLOR, Feature.MAGMA_BOSS_TIMER, 2, EnumUtils.ButtonType.COLOR);
        addButton(0.81, Message.SETTING_DARK_AUCTION_TIMER_COLOR, Feature.DARK_AUCTION_TIMER, 2, EnumUtils.ButtonType.COLOR);

        addButton(0.25, Message.SETTING_WARNING_TIME, Feature.WARNING_TIME, 3, EnumUtils.ButtonType.SOLID);
        addButton(0.33, Message.SETTING_BACKPACK_STYLE, Feature.BACKPACK_STYLE, 3, EnumUtils.ButtonType.SOLID);
        addSlider();
        addButton(0.49, Message.SETTING_TEXT_STYLE, Feature.TEXT_STYLE, 3, EnumUtils.ButtonType.SOLID);
        int twoThirds = width/3*2;
        buttonList.add(new ButtonModify(twoThirds, height*0.25, 20, 20, "+", main, Feature.ADD));
    }

    /**
     * Just adds the slider using the same rescale process as the regular buttons in {@link #addButton}
     */
    private void addSlider() {
        String text = Message.SETTING_GUI_SCALE.getMessage(String.valueOf(getRoundedValue(
                main.getUtils().denormalizeValue(main.getConfigValues().getGuiScale(), ButtonSlider.GUI_SCALE_MINIMUM, ButtonSlider.GUI_SCALE_MAXIMUM, ButtonSlider.GUI_SCALE_STEP))));
        int oneThird = width/3;
        int twoThirds = oneThird*2;
        int boxWidth = fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        int boxHeight = 20;
        int x = twoThirds+25;
        int y = (int)(height*0.41);
        buttonList.add(new ButtonSlider(0, x, y, boxWidth, boxHeight, main));
    }

    private float getRoundedValue(float value) {
        return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private void addButton(double y, Message message, Feature feature, int collumn, EnumUtils.ButtonType buttonType) {
        if (main.getConfigValues().isRemoteDisabled(feature)) { // Don't display features that I have disabled
            return;
        }
        y *= height; // The float from 0 to 1 is multiplied by the total height.
        String text = null;
        if (message != null) {
            text = message.getMessage();
        }
        int halfWidth = width/2;
        int oneThird = width/3;
        int twoThirds = oneThird*2;
        int widthLimit = BUTTON_MAX_WIDTH;
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
        if (buttonType == EnumUtils.ButtonType.COLOR) {
            buttonList.add(new ButtonColor(x, y, boxWidth, boxHeight, text, main, feature));
        } else if (buttonType == EnumUtils.ButtonType.SOLID) {
            // Also ad the subtract button when adding the warning time, so its lined up correctly.
            if (feature == Feature.WARNING_TIME) buttonList.add(new ButtonModify(x+boxWidth+5, height*0.25, 20, 20,"-", main, Feature.SUBTRACT));
            buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, feature));
        } else if (buttonType == EnumUtils.ButtonType.TOGGLE) {
            buttonList.add(new ButtonToggle(x, y, boxWidth, boxHeight, text, main, feature));
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int startColor = new Color(0,0, 0, 127).getRGB(); // Black
        int endColor = new Color(0,0, 0, 180).getRGB(); // Orange
        drawGradientRect(0, 0, width, height, startColor, endColor);
        GlStateManager.enableBlend();

        GlStateManager.pushMatrix();
        float scale = 2.5F; // Draw the settings label in big boy font size
        GlStateManager.scale(scale, scale, 1);
        int blue = new Color(189,236,252, 255).getRGB();
        drawCenteredString(fontRendererObj, Message.SETTING_SETTINGS.getMessage(),
                (int)(width/2/scale), (int)(height*0.12/scale), blue);
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }

    /**
     * Change settings when buttons are clicked.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonFeature) {
            Feature feature = ((ButtonFeature)abstractButton).getFeature();
            if (abstractButton instanceof ButtonColor) {
                main.getConfigValues().setNextColor(feature);
            } else if (abstractButton instanceof ButtonModify) {
                if (feature == Feature.ADD) {
                    if (main.getConfigValues().getWarningSeconds() < 99) {
                        main.getConfigValues().setWarningSeconds(main.getConfigValues().getWarningSeconds() + 1);
                    }
                } else {
                    if (main.getConfigValues().getWarningSeconds() > 1) {
                        main.getConfigValues().setWarningSeconds(main.getConfigValues().getWarningSeconds() - 1);
                    }
                }
            } else if (abstractButton instanceof ButtonToggle) {
                if (main.getConfigValues().isDisabled(feature)) {
                    main.getConfigValues().getDisabledFeatures().remove(feature);
                } else {
                    main.getConfigValues().getDisabledFeatures().add(feature);
                }
            } else if (feature == Feature.BACKPACK_STYLE || feature == Feature.TEXT_STYLE) {
                if (feature == Feature.BACKPACK_STYLE) {
                    main.getConfigValues().setBackpackStyle(main.getConfigValues().getBackpackStyle().getNextType());
                } else {
                    main.getConfigValues().setTextStyle(main.getConfigValues().getTextStyle().getNextType());
                }
                cancelScreenReturn = true;
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main));
                cancelScreenReturn = false;
            }
        }
    }

    /**
     * Open up the last GUI (main), and save the config.
     */
    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
        if (!cancelScreenReturn) {
            main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.MAIN);
        }
    }
}
