package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.features.enchants.EnchantListLayout;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DataUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils.FeatureSetting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class EnchantmentSettingsGui extends SettingsGui {

    private final List<FeatureSetting> ENCHANT_COLORING = Arrays.asList(FeatureSetting.HIGHLIGHT_ENCHANTMENTS,
            FeatureSetting.PERFECT_ENCHANT_COLOR, FeatureSetting.GREAT_ENCHANT_COLOR, FeatureSetting.GOOD_ENCHANT_COLOR,
            FeatureSetting.POOR_ENCHANT_COLOR, FeatureSetting.COMMA_ENCHANT_COLOR);
    private final List<FeatureSetting> ORGANIZATION = Arrays.asList(FeatureSetting.ENCHANT_LAYOUT,
            FeatureSetting.HIDE_ENCHANTMENT_LORE, FeatureSetting.HIDE_GREY_ENCHANTS);


    private int maxPage;

    public EnchantmentSettingsGui(Feature feature, int page, int lastPage, EnumUtils.GuiTab lastTab, List<FeatureSetting> settings) {
        super(feature, page, lastPage, lastTab, settings);
        maxPage = 1;
        for (FeatureSetting setting : settings) {
            if (!(ENCHANT_COLORING.contains(setting) || ORGANIZATION.contains(setting))) {
                maxPage = 2;
                break;
            }
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        row = 1;
        column = 1;
        buttonList.clear();
        for (FeatureSetting setting : settings) {
            if (page == 0) {
                if (ORGANIZATION.contains(setting)) {
                    addButton(setting);
                }
            }
            if (page == 1) {
                if (ENCHANT_COLORING.contains(setting)) {
                    addButton(setting);
                }
            } else if (page == 2 &&
                    !(ENCHANT_COLORING.contains(setting) || ORGANIZATION.contains(setting))) {
                addButton(setting);
            }
        }
        buttonList.add(new ButtonArrow(width / 2 - 15 - 150, height - 70, main, ButtonArrow.ArrowType.LEFT, page == 0));
        buttonList.add(new ButtonArrow(width / 2 - 15 + 150, height - 70, main, ButtonArrow.ArrowType.RIGHT, page == maxPage));
    }


    private int findDisplayCount() {
        int maxX = new ScaledResolution(mc).getScaledHeight() - 70 - 25;
        int displayCount = 0;
        for (int row = 1; row < 99; row++) {
            if (getRowHeight(row) < maxX) {
                displayCount += 3;
            } else {
                return displayCount;
            }
        }
        return displayCount;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.reInit) {
            this.reInit = false;
            this.initGui();
        }
        long timeSinceOpen = System.currentTimeMillis() - timeOpened;
        float alphaMultiplier; // This all calculates the alpha for the fade-in effect.
        alphaMultiplier = 0.5F;
        if (main.getUtils().isFadingIn()) {
            int fadeMilis = 500;
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = (float) timeSinceOpen / (fadeMilis * 2);
            }
        }
        int alpha = (int) (255 * alphaMultiplier); // Alpha of the text will increase from 0 to 127 over 500ms.

        int startColor = new Color(0, 0, 0, (int) (alpha * 0.5)).getRGB();
        int endColor = new Color(0, 0, 0, alpha).getRGB();
        drawGradientRect(0, 0, width, height, startColor, endColor);
        GlStateManager.enableBlend();

        if (alpha < 4) alpha = 4; // Text under 4 alpha appear 100% transparent for some reason o.O
        int defaultBlue = main.getUtils().getDefaultBlue(alpha * 2);

        SkyblockAddonsGui.drawDefaultTitleText(this, alpha * 2);

        if (feature != Feature.LANGUAGE) {
            int halfWidth = width / 2;
            int boxWidth = 140;
            int x = halfWidth - 90 - boxWidth;
            int width = halfWidth + 90 + boxWidth;
            width -= x;
            float numSettings;
            if (page == 0) {
                numSettings = ORGANIZATION.size();
            } else if (page == 1) {
                numSettings = ENCHANT_COLORING.size();
            } else {
                numSettings = Math.max(settings.size() - ORGANIZATION.size() - ENCHANT_COLORING.size(), 1);
            }
            int height = (int) (getRowHeightSetting(numSettings) - 50);
            int y = (int) getRowHeight(1);
            GlStateManager.enableBlend();
            DrawUtils.drawRect(x, y, width, height, ColorUtils.getDummySkyblockColor(28, 29, 41, 230), 4);

            SkyblockAddonsGui.drawScaledString(this, Message.SETTING_SETTINGS.getMessage(), 110, defaultBlue, 1.5, 0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }

    /**
     * Code to perform the button toggles, openings of other gui's/pages, and language changes.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonLanguage) {
            Language language = ((ButtonLanguage) abstractButton).getLanguage();
            main.getConfigValues().setLanguage(language);
            DataUtils.loadLocalizedStrings(true);
            main.setKeyBindingDescriptions();
            returnToGui();
        } else if (abstractButton instanceof ButtonSwitchTab) {
            ButtonSwitchTab tab = (ButtonSwitchTab) abstractButton;
            mc.displayGuiScreen(new SkyblockAddonsGui(1, tab.getTab()));
        } else if (abstractButton instanceof ButtonOpenColorMenu) {
            closingGui = true;
            // Temp fix until feature re-write. Open a color selection panel specific to the color setting
            Feature f = ((ButtonOpenColorMenu) abstractButton).feature;
            if (f == Feature.ENCHANTMENT_PERFECT_COLOR || f == Feature.ENCHANTMENT_GREAT_COLOR ||
                    f == Feature.ENCHANTMENT_GOOD_COLOR || f == Feature.ENCHANTMENT_POOR_COLOR ||
                    f == Feature.ENCHANTMENT_COMMA_COLOR) {
                mc.displayGuiScreen(new ColorSelectionGui(f, EnumUtils.GUIType.SETTINGS, lastTab, page));
            } else {
                mc.displayGuiScreen(new ColorSelectionGui(feature, EnumUtils.GUIType.SETTINGS, lastTab, lastPage));
            }
        } else if (abstractButton instanceof ButtonToggleTitle) {
            ButtonFeature button = (ButtonFeature) abstractButton;
            Feature feature = button.getFeature();
            if (feature == null) return;
            if (main.getConfigValues().isDisabled(feature)) {
                main.getConfigValues().getDisabledFeatures().remove(feature);
            } else {
                main.getConfigValues().getDisabledFeatures().add(feature);
            }
        } else if (abstractButton instanceof ButtonArrow) {
            ButtonArrow arrow = (ButtonArrow) abstractButton;
            if (arrow.isNotMax()) {
                main.getUtils().setFadingIn(false);
                if (arrow.getArrowType() == ButtonArrow.ArrowType.RIGHT) {
                    closingGui = true;
                    mc.displayGuiScreen(new EnchantmentSettingsGui(feature, ++page, lastPage, lastTab, settings));
                } else {
                    closingGui = true;
                    mc.displayGuiScreen(new EnchantmentSettingsGui(feature, --page, lastPage, lastTab, settings));
                }
            }
        }
    }

    private void addLanguageButton(Language language) {
        if (displayCount == 0) return;
        String text = feature.getMessage();
        int halfWidth = width / 2;
        int boxWidth = 140;
        int x = 0;
        if (column == 1) {
            x = halfWidth - 90 - boxWidth;
        } else if (column == 2) {
            x = halfWidth - (boxWidth / 2);
        } else if (column == 3) {
            x = halfWidth + 90;
        }
        double y = getRowHeight(row);
        buttonList.add(new ButtonLanguage(x, y, text, main, language));
        column++;
        if (column > 3) {
            column = 1;
            row++;
        }
        displayCount--;
    }

    private void addButton(FeatureSetting setting) {

        int halfWidth = width / 2;
        int boxWidth = 100;
        int x = halfWidth - (boxWidth / 2);
        double y = getRowHeightSetting(row);
        if (setting == FeatureSetting.COLOR) {
            buttonList.add(new ButtonOpenColorMenu(x, y, 100, 20, Message.SETTING_CHANGE_COLOR.getMessage(), main, feature));
            // Temp hardcode until feature rewrite
        } else if (setting == FeatureSetting.PERFECT_ENCHANT_COLOR || setting == FeatureSetting.GREAT_ENCHANT_COLOR ||
                setting == FeatureSetting.GOOD_ENCHANT_COLOR || setting == FeatureSetting.POOR_ENCHANT_COLOR ||
                setting == FeatureSetting.COMMA_ENCHANT_COLOR) {
            buttonList.add(new ButtonOpenColorMenu(x, y, 100, 20, setting.getMessage(), main, setting.getFeatureEquivalent()));
        } else if (setting == FeatureSetting.ENCHANT_LAYOUT) {
            boxWidth = 140;
            x = halfWidth - (boxWidth / 2);
            EnchantListLayout currentStatus = main.getConfigValues().getEnchantLayout();

            buttonList.add(new ButtonTextNew(halfWidth, (int) y - 10, Translations.getMessage("enchantLayout.title"), true, 0xFFFFFFFF));
            buttonList.add(new ButtonSelect(x, (int) y, boxWidth, 20, Arrays.asList(EnchantListLayout.values()), currentStatus.ordinal(), index -> {
                final EnchantListLayout enchantLayout = EnchantListLayout.values()[index];
                main.getConfigValues().setEnchantLayout(enchantLayout);
                reInit = true;
            }));

            row += 0.4;
        } else {
            boxWidth = 31; // Default size and stuff.
            x = halfWidth - (boxWidth / 2);
            y = getRowHeightSetting(row);
            buttonList.add(new ButtonToggleTitle(x, y, setting.getMessage(), main, setting.getFeatureEquivalent()));
        }
        row++;
    }

    // Each row is spaced 0.08 apart, starting at 0.17.
    private double getRowHeight(double row) {
        row--;
        return 95 + (row * 30); //height*(0.18+(row*0.08));
    }

    private double getRowHeightSetting(double row) {
        row--;
        return 140 + (row * 35); //height*(0.18+(row*0.08));
    }

    @Override
    public void onGuiClosed() {
        if (!closingGui) {
            returnToGui();
        }
        Keyboard.enableRepeatEvents(false);
    }

    private void returnToGui() {
        closingGui = true;
        main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        ButtonInputFieldWrapper.callKeyTyped(buttonList, typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ButtonInputFieldWrapper.callUpdateScreen(buttonList);
    }
}
