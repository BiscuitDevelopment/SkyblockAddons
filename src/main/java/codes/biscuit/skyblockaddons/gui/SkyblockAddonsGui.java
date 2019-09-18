package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.GuiIngameForge;

import java.awt.*;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SkyblockAddonsGui extends GuiScreen {

    public static final int BUTTON_MAX_WIDTH = 140;
//    private static Feature tooltipFeature;

    private EnumUtils.SkyblockAddonsGuiTab tab;
    private SkyblockAddons main;
    private int page;
    private GuiTextField magmaTextField = null;
    private int row = 1;
    private int collumn = 1;
    private int displayCount;

    private long timeOpened = System.currentTimeMillis();

    private static Set<Feature> colorSettingFeatures = Sets.newHashSet(Feature.MAGMA_WARNING, Feature.DROP_CONFIRMATION,
            Feature.MANA_BAR, Feature.MANA_TEXT, Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.DEFENCE_TEXT,
            Feature.DEFENCE_PERCENTAGE, Feature.MAGMA_BOSS_TIMER, Feature.DARK_AUCTION_TIMER,
            Feature.FULL_INVENTORY_WARNING, Feature.MINION_FULL_WARNING, Feature.MINION_STOP_WARNING, Feature.SUMMONING_EYE_ALERT);

    private static Set<Feature> guiScaleFeatures = Sets.newHashSet(Feature.ITEM_PICKUP_LOG, Feature.HEALTH_UPDATES,
            Feature.MANA_BAR, Feature.MANA_TEXT, Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.DEFENCE_TEXT,
            Feature.DEFENCE_PERCENTAGE, Feature.MAGMA_BOSS_TIMER, Feature.SKELETON_BAR, Feature.DARK_AUCTION_TIMER,
            Feature.DEFENCE_ICON);

    /**
     * The main gui, opened with /sba.
     */
    public SkyblockAddonsGui(SkyblockAddons main, int page, EnumUtils.SkyblockAddonsGuiTab tab) {
        this.tab = tab;
        this.main = main;
        this.page = page;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void initGui() {
        row = 1;
        collumn = 1;
        displayCount = findDisplayCount();
        addLanguageButton();
        addEditLocationsButton();
        // Add the buttons for each page.
        Feature[] array;
        if (tab == EnumUtils.SkyblockAddonsGuiTab.FEATURES) {
            array = new Feature[]{Feature.SHOW_ENCHANTMENTS_REFORGES, Feature.SHOW_BACKPACK_PREVIEW,
                    Feature.MINION_FULL_WARNING, Feature.FULL_INVENTORY_WARNING,
                    Feature.IGNORE_ITEM_FRAME_CLICKS, Feature.HIDE_FOOD_ARMOR_BAR, Feature.HIDE_HEALTH_BAR,
                    Feature.AVOID_BREAKING_STEMS, Feature.AVOID_BREAKING_BOTTOM_SUGAR_CANE, Feature.MAGMA_WARNING, Feature.HIDE_PLAYERS_IN_LOBBY, Feature.MINION_STOP_WARNING,
                    Feature.SHOW_ITEM_ANVIL_USES, Feature.LOCK_SLOTS, Feature.DONT_OPEN_PROFILES_WITH_BOW, Feature.STOP_DROPPING_SELLING_RARE_ITEMS,
                    Feature.MAKE_ENDERCHESTS_GREEN_IN_END, Feature.SUMMONING_EYE_ALERT, Feature.DONT_RESET_CURSOR_INVENTORY,
                    Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS, Feature.DROP_CONFIRMATION};
        } else if (tab == EnumUtils.SkyblockAddonsGuiTab.FIXES) {
            array = new Feature[]{Feature.HIDE_BONES, Feature.DISABLE_EMBER_ROD, Feature.HIDE_AUCTION_HOUSE_PLAYERS,
                    Feature.STOP_BOW_CHARGE_FROM_RESETTING, Feature.AVOID_PLACING_ENCHANTED_ITEMS, Feature.PREVENT_MOVEMENT_ON_DEATH};
        } else if (tab == EnumUtils.SkyblockAddonsGuiTab.GUI_FEATURES) {
            array = new Feature[]{Feature.MAGMA_BOSS_TIMER, Feature.MANA_BAR, Feature.MANA_TEXT, Feature.DEFENCE_TEXT, Feature.DEFENCE_PERCENTAGE,
                    Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.DEFENCE_ICON, Feature.SKELETON_BAR, Feature.HEALTH_UPDATES,
                    Feature.ITEM_PICKUP_LOG, Feature.DARK_AUCTION_TIMER};
        } else {
            array = new Feature[]{Feature.TEXT_STYLE, Feature.WARNING_TIME};
        }
        int skip = (page-1)*displayCount;

        boolean max = page == 1;
        buttonList.add(new ButtonArrow(width/2-15-50, height-70, main, ButtonArrow.ArrowType.LEFT, max));
        max = array.length-skip-displayCount <= 0;
        buttonList.add(new ButtonArrow(width/2-15+50, height-70, main, ButtonArrow.ArrowType.RIGHT, max));

        for (Feature feature : array) {
            if (skip == 0) {
                if (feature == Feature.TEXT_STYLE || feature == Feature.WARNING_TIME) {
                    addButton(feature, EnumUtils.ButtonType.SOLID);
                } else {
                    addButton(feature, EnumUtils.ButtonType.TOGGLE);
                }
            } else {
                skip--;
            }
        }

        this.addTabs();
    }

    private void addTabs() {
        int collumn = 1;
        for (EnumUtils.SkyblockAddonsGuiTab loopTab : EnumUtils.SkyblockAddonsGuiTab.values()) {
            if (tab != loopTab) {
                String text = "";
                switch (loopTab) {
                    case FEATURES:
                        text = Message.TAB_FEATURES.getMessage();
                        break;
                    case FIXES:
                        text = Message.TAB_FIXES.getMessage();
                        break;
                    case GUI_FEATURES:
                        text = Message.TAB_GUI_FEATURES.getMessage();
                        break;
                    case GENERAL_SETTINGS:
                        text = Message.TAB_GENERAL_SETTINGS.getMessage();
                        break;
                }
                float stringWidth = fontRenderer.getStringWidth(text);
                int tabX = 0;
                int halfWidth = width/2;
                if (collumn == 1) {
                    tabX = (int)Math.round(halfWidth-140-(stringWidth/2)*1.4);
                } else if (collumn == 2) {
                    tabX = (int)Math.round(halfWidth-(stringWidth/2)*1.4);
                } else if (collumn == 3) {
                    tabX = (int)Math.round(halfWidth+140-(stringWidth/2)*1.4);
                }
                buttonList.add(new ButtonSwitchTab(tabX, 70, (int)(stringWidth*1.4),
                                                   14, text, main, loopTab, tab));
                collumn++;
            }
        }
    }

    private int findDisplayCount() {
        int maxX = new ScaledResolution(mc).getScaledHeight()-70-50;
        int displayCount = 0;
        for (int row = 1; row < 99; row++) {
            if (getRowHeight(row) < maxX) {
                displayCount+=3;
            } else {
                return displayCount;
            }
        }
        return displayCount;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
//        tooltipFeature = null;
        long timeSinceOpen = System.currentTimeMillis() - timeOpened;
        float alphaMultiplier; // This all calculates the alpha for the fade-in effect.
        alphaMultiplier = 0.5F;
        if (main.getUtils().isFadingIn()) {
            int fadeMilis = 500;
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = (float) timeSinceOpen / (fadeMilis * 2);
            }
        }
        int alpha = (int)(255*alphaMultiplier); // Alpha of the text will increase from 0 to 127 over 500ms.

        int startColor = new Color(0,0, 0, alpha).getRGB(); // Black
        int endColor = new Color(0,0, 0, (int)(alpha*1.5)).getRGB(); // Orange
        drawGradientRect(0, 0, width, height, startColor, endColor);
        GlStateManager.enableBlend();

        if (alpha < 4) alpha = 4; // Text under 4 alpha appear 100% transparent for some reason o.O
        int defaultBlue = main.getUtils().getDefaultBlue(alpha*2);

        // The text at the top of the GUI
        drawScaledString("SkyblockAddons", 28, defaultBlue, 2.5F, 0);
        drawScaledString("v" + SkyblockAddons.VERSION + " by Biscut", 49, defaultBlue, 1.3, 50);
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }

    /**
     * Code to perform the button toggles, openings of other gui's/pages, and language changes.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonFeature) {
            Feature feature = ((ButtonFeature)abstractButton).getFeature();
            if (abstractButton instanceof ButtonSettings) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main, feature, 1, page, tab, getSettings(feature)));
                return;
            }
            if (feature == Feature.LANGUAGE) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main, Feature.LANGUAGE, 1, page,tab, null));
            }  else if (feature == Feature.EDIT_LOCATIONS) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main, page, tab));
            } else if (abstractButton instanceof ButtonToggle) {
                if (main.getConfigValues().isDisabled(feature)) {
                    main.getConfigValues().getDisabledFeatures().remove(feature);
                } else {
                    main.getConfigValues().getDisabledFeatures().add(feature);
                    if (feature == Feature.HIDE_FOOD_ARMOR_BAR) { // Reset the vanilla bars when disabling these two features.
                        GuiIngameForge.renderArmor = true; // The food gets automatically enabled, no need to include it.
                    } else if (feature == Feature.HIDE_HEALTH_BAR) {
                        GuiIngameForge.renderHealth = true;
                    }
                }
            } else if (abstractButton instanceof ButtonSolid && feature == Feature.TEXT_STYLE) {
                main.getConfigValues().setTextStyle(main.getConfigValues().getTextStyle().getNextType());
                Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, page, tab));
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
            }
        } else if (abstractButton instanceof ButtonArrow) {
            ButtonArrow arrow = (ButtonArrow)abstractButton;
            if (arrow.isNotMax()) {
                main.getUtils().setFadingIn(false);
                if (arrow.getArrowType() == ButtonArrow.ArrowType.RIGHT) {
                    mc.displayGuiScreen(new SkyblockAddonsGui(main, ++page, tab));
                } else {
                    mc.displayGuiScreen(new SkyblockAddonsGui(main, --page, tab));
                }
            }
        } else if (abstractButton instanceof ButtonSwitchTab) {
            ButtonSwitchTab tab = (ButtonSwitchTab)abstractButton;
            if (tab.getTab() != this.tab) {
                main.getUtils().setFadingIn(false);
                mc.displayGuiScreen(new SkyblockAddonsGui(main, 1, tab.getTab()));
            }
        }
    }

    /**
     * To avoid repeating the code for scaled text, use this instead.
     */
    private void drawScaledString(String text, int y, int color, double scale, int xOff) {
        double x = width / 2.0;
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        drawCenteredString(fontRenderer, text,
                (int)(x/scale)+xOff, (int)(y/scale), color);
        GlStateManager.popMatrix();
    }

    /**
     * Adds a button, limiting its width and setting the correct position.
     */
    private void addButton(Feature feature, EnumUtils.ButtonType buttonType) {
        if (displayCount == 0) return;
        if (main.getConfigValues().isRemoteDisabled(feature)) { // Don't display features that I have disabled
            return;
        }
        String text = feature.getMessage();
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 50;
        int x = 0;
        if (collumn == 1) {
            x = halfWidth-90-boxWidth;
        } else if (collumn == 2) {
            x = halfWidth-(boxWidth/2);
        } else if (collumn == 3) {
            x = halfWidth+90;
        }
        double y = getRowHeight(row);
        if (buttonType == EnumUtils.ButtonType.TOGGLE) {
            buttonList.add(new ButtonNormal(x, y, text, main, feature));

            if (!getSettings(feature).isEmpty()) {
                buttonList.add(new ButtonSettings(x + boxWidth - 33, y + boxHeight - 23, text, main, feature));
            }
            buttonList.add(new ButtonToggle(x+40, y+boxHeight-23, main, feature));
        } else if (buttonType == EnumUtils.ButtonType.SOLID) {
            buttonList.add(new ButtonNormal(x, y, text, main, feature));

            if (feature == Feature.TEXT_STYLE) {
                buttonList.add(new ButtonSolid(x + 35, y + boxHeight - 23, 70, 15, "", main, feature));
            } else if (feature == Feature.WARNING_TIME) {
                int solidButtonX = x+(boxWidth/2)-17;
                buttonList.add(new ButtonModify(solidButtonX-20, y + boxHeight - 23, 15, 15, "+", main, Feature.ADD));
                buttonList.add(new ButtonSolid(solidButtonX, y + boxHeight - 23, 35, 15, "", main, feature));
                buttonList.add(new ButtonModify(solidButtonX+35+5, y + boxHeight - 23, 15, 15,"-", main, Feature.SUBTRACT));

            }
        }
        collumn++;
        if (collumn > 3) {
            collumn = 1;
            row++;
        }
        displayCount--;
    }

    private Set<EnumUtils.FeatureSetting> getSettings(Feature feature) {
        Set<EnumUtils.FeatureSetting> settings = EnumSet.noneOf(EnumUtils.FeatureSetting.class);
        if (colorSettingFeatures.contains(feature)) settings.add(EnumUtils.FeatureSetting.COLOR);
        if (guiScaleFeatures.contains(feature)) settings.add(EnumUtils.FeatureSetting.GUI_SCALE);
        if (feature == Feature.MAGMA_BOSS_TIMER || feature == Feature.DARK_AUCTION_TIMER
                || feature == Feature.DROP_CONFIRMATION) settings.add(EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES);
        if (feature == Feature.DEFENCE_ICON) settings.add(EnumUtils.FeatureSetting.USE_VANILLA_TEXTURE);
//        if (feature == Feature.SUMMONING_EYE_ALERT || feature == Feature.MAGMA_WARNING
//                || feature == Feature.MINION_FULL_WARNING || feature == Feature.MINION_STOP_WARNING
//                || feature == Feature.FULL_INVENTORY_WARNING) settings.add(EnumUtils.FeatureSetting.WARNING_TIME);
        if (feature == Feature.SHOW_BACKPACK_PREVIEW) {
            settings.add(EnumUtils.FeatureSetting.BACKPACK_STYLE);
            settings.add(EnumUtils.FeatureSetting.SHOW_ONLY_WHEN_HOLDING_SHIFT);
            settings.add(EnumUtils.FeatureSetting.MAKE_INVENTORY_COLORED);
        }
        return settings;
    }

    private void addLanguageButton() {
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 50;
        int x = halfWidth+90;
        double y = getRowHeight(displayCount/3.0+1);
        buttonList.add(new ButtonNormal(x, y, boxWidth, boxHeight, "Language: "+Feature.LANGUAGE.getMessage(), main, Feature.LANGUAGE));
    }

    private void addEditLocationsButton() {
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 50;
        int x = halfWidth-90-boxWidth;
        double y = getRowHeight(displayCount/3.0+1);
        buttonList.add(new ButtonNormal(x, y, boxWidth, boxHeight, Feature.EDIT_LOCATIONS.getMessage(), main, Feature.EDIT_LOCATIONS));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (magmaTextField != null) {
            if (magmaTextField.isFocused()) {
                magmaTextField.textboxKeyTyped(typedChar, keyCode);
                try {
                    String[] stringSplit = magmaTextField.getText().split(Pattern.quote(":"), 3);
                    int[] magmaTimes = new int[3];
                    for (int timePart = 0; timePart < stringSplit.length; timePart++) {
                        magmaTimes[timePart] = Integer.parseInt(stringSplit[timePart]);
                    }
                    int magmaTime = 0;
                    magmaTime += magmaTimes[0] * 3600;
                    magmaTime += magmaTimes[1] * 60;
                    magmaTime += magmaTimes[2];
                    main.getPlayerListener().setMagmaAccuracy(EnumUtils.MagmaTimerAccuracy.EXACTLY);
                    main.getPlayerListener().setMagmaTime(magmaTime, false); // will save on exit instead
                } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (magmaTextField != null) {
            magmaTextField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    // Each row is spaced 0.08 apart, starting at 0.17.
    private double getRowHeight(double row) {
        row--;
        return 95+(row*60); //height*(0.18+(row*0.08));
    }

    /**
     * Save the config when exiting.
     */
    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
    }

//    public static void setTooltipFeature(Feature tooltipFeature) {
//        SkyblockAddonsGui.tooltipFeature = tooltipFeature;
//    }
}
