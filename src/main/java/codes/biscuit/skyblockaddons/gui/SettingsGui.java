package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.features.discordrpc.DiscordStatus;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SettingsGui extends GuiScreen {

    private static ResourceLocation FEATURE_BACKGROUND = new ResourceLocation("skyblockaddons", "gui/featurebackground.png");

    private SkyblockAddons main = SkyblockAddons.getInstance();
    private int page;
    private float row = 1;
    private int collumn = 1;
    private int displayCount;
    private Feature feature;
    private int lastPage;
    private EnumUtils.GuiTab lastTab;
    private boolean closingGui;
    private List<EnumUtils.FeatureSetting> settings;
    private boolean reInit = false;

    private long timeOpened = System.currentTimeMillis();

    /**
     * The main gui, opened with /sba.
     */
    public SettingsGui(Feature feature, int page, int lastPage, EnumUtils.GuiTab lastTab, List<EnumUtils.FeatureSetting> settings) {
        this.feature = feature;
        this.page = page;
        this.lastPage = lastPage;
        this.lastTab = lastTab;
        this.settings = settings;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        row = 1;
        collumn = 1;
        buttonList.clear();
        if (feature == Feature.LANGUAGE) {
            displayCount = findDisplayCount();
            // Add the buttons for each page.
            int skip = (page - 1) * displayCount;

            boolean max = page == 1;
            buttonList.add(new ButtonArrow(width / 2 - 15 - 50, height - 70, main, ButtonArrow.ArrowType.LEFT, max));
            max = Language.values().length - skip - displayCount <= 0;
            buttonList.add(new ButtonArrow(width / 2 - 15 + 50, height - 70, main, ButtonArrow.ArrowType.RIGHT, max));

            for (Language language : Language.values()) {
                if (skip == 0) {
                    if (language == Language.ENGLISH) continue;
                    if (language == Language.CHINESE_TRADITIONAL) {
                        addLanguageButton(Language.ENGLISH);
                    }
                    addLanguageButton(language);
                } else {
                    skip--;
                }
            }
        } else {
            for (EnumUtils.FeatureSetting setting : settings) {
                addButton(setting);
            }
        }
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
            mc.getTextureManager().bindTexture(FEATURE_BACKGROUND);
            int halfWidth = width / 2;
            int boxWidth = 140;
            int x = halfWidth - 90 - boxWidth;
            int width = halfWidth + 90 + boxWidth;
            width -= x;
            float numSettings = settings.size();
            if (settings.contains(EnumUtils.FeatureSetting.DISCORD_RP_STATE)) {
                if (main.getConfigValues().getDiscordStatus() == DiscordStatus.CUSTOM) numSettings++;
                if (main.getConfigValues().getDiscordStatus() == DiscordStatus.AUTO_STATUS) {
                    numSettings++;
                    if (main.getConfigValues().getDiscordAutoDefault() == DiscordStatus.CUSTOM) {
                        numSettings++;
                    }
                }
                numSettings += 0.4;
            }
            if (settings.contains(EnumUtils.FeatureSetting.DISCORD_RP_DETAILS)) {
                if (main.getConfigValues().getDiscordDetails() == DiscordStatus.CUSTOM) numSettings++;
                if (main.getConfigValues().getDiscordDetails() == DiscordStatus.AUTO_STATUS) {
                    numSettings++;
                    if (main.getConfigValues().getDiscordAutoDefault() == DiscordStatus.CUSTOM) {
                        numSettings++;
                    }
                }
                numSettings += 0.4;
            }
            int height = (int) (getRowHeightSetting(numSettings) - 50);
            int y = (int) getRowHeight(1);
            GlStateManager.enableBlend();
            GlStateManager.color(1, 1, 1, 0.7F);
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
            SkyblockAddonsGui.drawScaledString(this, Message.SETTING_SETTINGS.getMessage(), 110, defaultBlue, 1.5, 0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
        if (feature == Feature.LANGUAGE) {
            main.getUtils().loadLanguageFile(false);
        }
    }

    /**
     * Code to perform the button toggles, openings of other gui's/pages, and language changes.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonLanguage) {
            Language language = ((ButtonLanguage) abstractButton).getLanguage();
            main.getConfigValues().setLanguage(language);
            main.getUtils().loadLanguageFile(true);
            main.setKeyBindingDescriptions();
            returnToGui();
        } else if (abstractButton instanceof ButtonSwitchTab) {
            ButtonSwitchTab tab = (ButtonSwitchTab)abstractButton;
            mc.displayGuiScreen(new SkyblockAddonsGui(1, tab.getTab()));
        } else if (abstractButton instanceof ButtonOpenColorMenu) {
            closingGui = true;
            mc.displayGuiScreen(new ColorSelectionGui(feature, EnumUtils.GUIType.SETTINGS, lastTab, lastPage));
        } else if (abstractButton instanceof ButtonToggleTitle) {
            ButtonFeature button = (ButtonFeature) abstractButton;
            Feature feature = button.getFeature();
            if (feature == null) return;
            if (main.getConfigValues().isDisabled(feature)) {
                main.getConfigValues().getDisabledFeatures().remove(feature);
            } else {
                main.getConfigValues().getDisabledFeatures().add(feature);
                if (feature == Feature.HIDE_FOOD_ARMOR_BAR) { // Reset the vanilla bars when disabling these two features.
                    GuiIngameForge.renderArmor = true; // The food gets automatically enabled, no need to include it.
                } else if (feature == Feature.HIDE_HEALTH_BAR) {
                    GuiIngameForge.renderHealth = true;
                } else if (feature == Feature.REPEAT_FULL_INVENTORY_WARNING) {
                    // Remove queued warnings when the repeat setting is turned off.
                    main.getScheduler().removeQueuedFullInventoryWarnings();
                }
            }
        } else if (feature == Feature.SHOW_BACKPACK_PREVIEW) {
            main.getConfigValues().setBackpackStyle(main.getConfigValues().getBackpackStyle().getNextType());
            closingGui = true;
            Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(feature, page, lastPage, lastTab, settings));
            closingGui = false;
        } else if (feature == Feature.POWER_ORB_STATUS_DISPLAY && abstractButton instanceof ButtonSolid) {
            main.getConfigValues().setPowerOrbDisplayStyle(main.getConfigValues().getPowerOrbDisplayStyle().getNextType());
            closingGui = true;
            Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(feature, page, lastPage, lastTab, settings));
            closingGui = false;
        } else if (abstractButton instanceof ButtonArrow) {
            ButtonArrow arrow = (ButtonArrow) abstractButton;
            if (arrow.isNotMax()) {
                main.getUtils().setFadingIn(false);
                if (arrow.getArrowType() == ButtonArrow.ArrowType.RIGHT) {
                    closingGui = true;
                    mc.displayGuiScreen(new SettingsGui(feature, ++page, lastPage, lastTab, settings));
                } else {
                    closingGui = true;
                    mc.displayGuiScreen(new SettingsGui(feature, --page, lastPage, lastTab, settings));
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
        if (collumn == 1) {
            x = halfWidth - 90 - boxWidth;
        } else if (collumn == 2) {
            x = halfWidth - (boxWidth / 2);
        } else if (collumn == 3) {
            x = halfWidth + 90;
        }
        double y = getRowHeight(row);
        buttonList.add(new ButtonLanguage(x, y, text, main, language));
        collumn++;
        if (collumn > 3) {
            collumn = 1;
            row++;
        }
        displayCount--;
    }

    private void addButton(EnumUtils.FeatureSetting setting) {
        int halfWidth = width / 2;
        int boxWidth = 100;
        int x = halfWidth - (boxWidth / 2);
        double y = getRowHeightSetting(row);
        if (setting == EnumUtils.FeatureSetting.COLOR) {
            buttonList.add(new ButtonOpenColorMenu(x, y, 100, 20, Message.SETTING_CHANGE_COLOR.getMessage(), main, feature));
        } else if (setting == EnumUtils.FeatureSetting.GUI_SCALE) {
            buttonList.add(new ButtonGuiScale(x, y, 100, 20, main, feature));
        } else if (setting == EnumUtils.FeatureSetting.REPEATING) {
            boxWidth = 31;
            x = halfWidth - (boxWidth / 2);
            y = getRowHeightSetting(row);

            Feature settingFeature = null;
            if (feature == Feature.FULL_INVENTORY_WARNING) {
                settingFeature = Feature.REPEAT_FULL_INVENTORY_WARNING;
            } else if (feature == Feature.BOSS_APPROACH_ALERT) {
                settingFeature = Feature.REPEAT_SLAYER_BOSS_WARNING;
            }

            buttonList.add(new ButtonToggleTitle(x, y, Message.SETTING_REPEATING.getMessage(), main, settingFeature));
        } else if (setting == EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES) {
            boxWidth = 31;
            x = halfWidth - (boxWidth / 2);
            y = getRowHeightSetting(row);

            Feature settingFeature = null;
            if (feature == Feature.MAGMA_BOSS_TIMER) {
                settingFeature = Feature.SHOW_MAGMA_TIMER_IN_OTHER_GAMES;
            } else if (feature == Feature.DARK_AUCTION_TIMER) {
                settingFeature = Feature.SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES;
            } else if (feature == Feature.DROP_CONFIRMATION) {
                settingFeature = Feature.DOUBLE_DROP_IN_OTHER_GAMES;
            }

            buttonList.add(new ButtonToggleTitle(x, y, Message.SETTING_SHOW_IN_OTHER_GAMES.getMessage(), main, settingFeature));
        } else if (setting == EnumUtils.FeatureSetting.BACKPACK_STYLE) {
            boxWidth = 140;
            x = halfWidth - (boxWidth / 2);
            buttonList.add(new ButtonSolid(x, y, 140, 20, Message.SETTING_BACKPACK_STYLE.getMessage(), main, feature));
        } else if (setting == EnumUtils.FeatureSetting.ENABLE_MESSAGE_WHEN_ACTION_PREVENTED) {
            boxWidth = 31;
            x = halfWidth - (boxWidth / 2);

            Feature settingFeature = null;
            if (feature == Feature.ONLY_MINE_ORES_DEEP_CAVERNS) {
                settingFeature = Feature.ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS;
            } else if (feature == Feature.AVOID_BREAKING_STEMS) {
                settingFeature = Feature.ENABLE_MESSAGE_WHEN_BREAKING_STEMS;
            } else if (feature == Feature.ONLY_MINE_VALUABLES_NETHER) {
                settingFeature = Feature.ENABLE_MESSAGE_WHEN_MINING_NETHER;
            } else if (feature == Feature.ONLY_BREAK_LOGS_PARK) {
                settingFeature = Feature.ENABLE_MESSAGE_WHEN_BREAKING_PARK;
            }


            buttonList.add(new ButtonToggleTitle(x, y, Message.SETTING_ENABLE_MESSAGE_WHEN_ACTION_PREVENTED.getMessage(), main, settingFeature));
        } else if (setting == EnumUtils.FeatureSetting.POWER_ORB_DISPLAY_STYLE) {
            boxWidth = 140;
            x = halfWidth - (boxWidth / 2);
            buttonList.add(new ButtonSolid(x, y, 140, 20, Message.SETTING_POWER_ORB_DISPLAY_STYLE.getMessage(), main, feature));
        } else if (setting == EnumUtils.FeatureSetting.DISCORD_RP_DETAILS || setting == EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
            boxWidth = 140;
            x = halfWidth - (boxWidth / 2);
            DiscordStatus currentStatus;
            if (setting == EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
                currentStatus = main.getConfigValues().getDiscordStatus();
            } else {
                currentStatus = main.getConfigValues().getDiscordDetails();
            }

            buttonList.add(new ButtonTextNew(halfWidth, (int) y - 10, setting == EnumUtils.FeatureSetting.DISCORD_RP_DETAILS ? Message.MESSAGE_FIRST_STATUS.getMessage() :
                    Message.MESSAGE_SECOND_STATUS.getMessage(), true, 0xFFFFFFFF));
            buttonList.add(new ButtonSelect(x, (int) y, boxWidth, 20, Arrays.asList(DiscordStatus.values()), currentStatus.ordinal(), index -> {
                final DiscordStatus selectedStatus = DiscordStatus.values()[index];
                if (setting == EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
                    main.getDiscordRPCManager().setStateLine(selectedStatus);
                    main.getConfigValues().setDiscordStatus(selectedStatus);
                } else {
                    main.getDiscordRPCManager().setDetailsLine(selectedStatus);
                    main.getConfigValues().setDiscordDetails(selectedStatus);
                }
                SettingsGui.this.reInit = true;
            }));

            if (currentStatus == DiscordStatus.AUTO_STATUS) {
                row++;
                row += 0.4;
                boxWidth = 140;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);

                buttonList.add(new ButtonTextNew(halfWidth, (int) y - 10, Message.MESSAGE_FALLBACK_STATUS.getMessage(), true, 0xFFFFFFFF));
                currentStatus = main.getConfigValues().getDiscordAutoDefault();
                buttonList.add(new ButtonSelect(x, (int) y, boxWidth, 20, Arrays.asList(DiscordStatus.values()), currentStatus.ordinal(), index -> {
                    final DiscordStatus selectedStatus = DiscordStatus.values()[index];
                    main.getConfigValues().setDiscordAutoDefault(selectedStatus);
                    SettingsGui.this.reInit = true;
                }));
            }

            if (currentStatus == DiscordStatus.CUSTOM) {
                row++;
                halfWidth = width / 2;
                boxWidth = 200;
                x = halfWidth - (boxWidth / 2);
                y = getRowHeightSetting(row);

                EnumUtils.DiscordStatusEntry discordStatusEntry = EnumUtils.DiscordStatusEntry.DETAILS;
                if (setting == EnumUtils.FeatureSetting.DISCORD_RP_STATE) {
                    discordStatusEntry = EnumUtils.DiscordStatusEntry.STATE;
                }
                final EnumUtils.DiscordStatusEntry finalDiscordStatusEntry = discordStatusEntry;
                ButtonInputFieldWrapper inputField = new ButtonInputFieldWrapper(x, (int) y, 200, 20, main.getConfigValues().getCustomStatus(discordStatusEntry),
                        null, 100, false, updatedValue -> main.getConfigValues().setCustomStatus(finalDiscordStatusEntry, updatedValue));
                buttonList.add(inputField);
            }

            row += 0.4;
        } else if (setting == EnumUtils.FeatureSetting.MAP_ZOOM) {
            boxWidth = 100; // Default size and stuff.
            x = halfWidth-(boxWidth/2);
            y = getRowHeightSetting(row);
            buttonList.add(new ButtonSlider(x, y, 100, 20, main.getConfigValues().getMapZoom().getValue(), 0.5F, 5F, 0.1F, new ButtonSlider.OnSliderChangeCallback() {
                @Override
                public void sliderUpdated(float value) {
                    main.getConfigValues().getMapZoom().setValue(value);
                }
            }).setPrefix("Map Zoom: "));
        } else if (setting == EnumUtils.FeatureSetting.COLOUR_BY_RARITY) {
            boxWidth = 31;
            x = halfWidth - boxWidth / 2;
            y = this.getRowHeightSetting(this.row);
            Feature settingFeature = null;
            if (this.feature == Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE) {
                settingFeature = Feature.BASE_STAT_BOOST_COLOR_BY_RARITY;

            } else if (feature == Feature.REVENANT_SLAYER_TRACKER) {
                settingFeature = Feature.REVENANT_COLOR_BY_RARITY;

            } else if (feature == Feature.TARANTULA_SLAYER_TRACKER) {
                settingFeature = Feature.TARANTULA_COLOR_BY_RARITY;

            } else if (feature == Feature.SVEN_SLAYER_TRACKER) {
                settingFeature = Feature.SVEN_COLOR_BY_RARITY;

            } else if (feature == Feature.DRAGON_STATS_TRACKER) {
                settingFeature = Feature.DRAGON_STATS_TRACKER_COLOR_BY_RARITY;
            }

            buttonList.add(new ButtonToggleTitle(x, y, Message.SETTING_COLOR_BY_RARITY.getMessage(), this.main, settingFeature));
        } else if (setting == EnumUtils.FeatureSetting.EXPANDED) {
            boxWidth = 31;
            x = halfWidth - (boxWidth / 2);
            y = getRowHeightSetting(row);

            Feature settingFeature = null;
            if (feature == Feature.SKILL_DISPLAY) {
                settingFeature = Feature.ACTIONS_UNTIL_NEXT_LEVEL;
            }

            buttonList.add(new ButtonToggleTitle(x, y, Message.SETTING_EXPANDED.getMessage(), main, settingFeature));
        } else if (setting == EnumUtils.FeatureSetting.TEXT_MODE) {
            boxWidth = 31;
            x = halfWidth - (boxWidth / 2);
            y = getRowHeightSetting(row);

            Feature settingFeature = null;
            if (feature == Feature.REVENANT_SLAYER_TRACKER) {
                settingFeature = Feature.REVENANT_TEXT_MODE;

            } else if (feature == Feature.TARANTULA_SLAYER_TRACKER) {
                settingFeature = Feature.TARANTULA_TEXT_MODE;

            } else if (feature == Feature.SVEN_SLAYER_TRACKER) {
                settingFeature = Feature.SVEN_TEXT_MODE;

            } else if (feature == Feature.DRAGON_STATS_TRACKER_TEXT_MODE) {
                settingFeature = Feature.DRAGON_STATS_TRACKER_TEXT_MODE;
            }

            buttonList.add(new ButtonToggleTitle(x, y, Message.SETTING_TEXT_MODE.getMessage(), main, settingFeature));
        } else if (setting == EnumUtils.FeatureSetting.DRAGONS_NEST_ONLY) {
            boxWidth = 31;
            x = halfWidth - (boxWidth / 2);
            y = getRowHeightSetting(row);

            Feature settingFeature = null;
            if (feature == Feature.DRAGON_STATS_TRACKER) {
                settingFeature = Feature.DRAGON_STATS_TRACKER_NEST_ONLY;
            } else if (feature == Feature.ZEALOT_COUNTER) {
                settingFeature = Feature.ZEALOT_COUNTER_NEST_ONLY;
            } else if (feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
                settingFeature = Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE_NEST_ONLY;
            } else if (feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
                settingFeature = Feature.SHOW_TOTAL_ZEALOT_COUNT_NEST_ONLY;
            } else if (feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
                settingFeature = Feature.SHOW_SUMMONING_EYE_COUNT_NEST_ONLY;
            }

            buttonList.add(new ButtonToggleTitle(x, y, setting.getMessage().getMessage(), main, settingFeature));
        }
        else {
            boxWidth = 31; // Default size and stuff.
            x = halfWidth - (boxWidth / 2);
            y = getRowHeightSetting(row);
            buttonList.add(new ButtonToggleTitle(x, y, setting.getMessage().getMessage(), main, setting.getFeatureEquivalent()));
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
