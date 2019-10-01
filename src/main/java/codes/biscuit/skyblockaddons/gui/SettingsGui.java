package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Language;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;

import java.awt.*;
import java.util.Set;

public class SettingsGui extends GuiScreen {

    private static ResourceLocation FEATURE_BACKGROUND = new ResourceLocation("skyblockaddons", "featurebackground.png");

    private SkyblockAddons main;
    private int page;
    private int row = 1;
    private int collumn = 1;
    private int displayCount;
    private Feature feature;
    private int lastPage;
    private EnumUtils.SkyblockAddonsGuiTab lastTab;
    private boolean closingGui = false;
    private Set<EnumUtils.FeatureSetting> settings;

    private long timeOpened = System.currentTimeMillis();

    /**
     * The main gui, opened with /sba.
     */
    SettingsGui(SkyblockAddons main, Feature feature, int page,
                int lastPage, EnumUtils.SkyblockAddonsGuiTab lastTab, Set<EnumUtils.FeatureSetting> settings) {
        this.main = main;
        this.feature = feature;
        this.page = page;
        this.lastPage = lastPage;
        this.lastTab = lastTab;
        this.settings = settings;
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @Override
    public void initGui() {
        if (feature == Feature.LANGUAGE) {
            row = 1;
            collumn = 1;
            displayCount = findDisplayCount();
            // Add the buttons for each page.
            int skip = (page-1)*displayCount;

            boolean max = page == 1;
            buttonList.add(new ButtonArrow(width/2-15-50, height-70, main, ButtonArrow.ArrowType.LEFT, max));
            max = Language.values().length-skip-displayCount <= 0;
            buttonList.add(new ButtonArrow(width/2-15+50, height-70, main, ButtonArrow.ArrowType.RIGHT, max));

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

        addTabs();
    }

    private void addTabs() {
        int collumn = 1;
        for (EnumUtils.SkyblockAddonsGuiTab loopTab : EnumUtils.SkyblockAddonsGuiTab.values()) {
            if (lastTab != loopTab) {
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
                int stringWidth = fontRendererObj.getStringWidth(text);
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
                        14, text, main, loopTab, lastTab));
                collumn++;
            }
        }
    }


    private int findDisplayCount() {
        int maxX = new ScaledResolution(mc).getScaledHeight()-70-25;
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

        int startColor = new Color(0,0, 0, alpha).getRGB();
        int endColor = new Color(0,0, 0, (int)(alpha*1.5)).getRGB();
        drawGradientRect(0, 0, width, height, startColor, endColor);
        GlStateManager.enableBlend();

        if (alpha < 4) alpha = 4; // Text under 4 alpha appear 100% transparent for some reason o.O
        int defaultBlue = main.getUtils().getDefaultBlue(alpha*2);

        // The text at the top of the GUI
        drawScaledString("SkyblockAddons", 28, defaultBlue, 2.5F);
        drawScaledString("v" + SkyblockAddons.VERSION + " by Biscut", 49, defaultBlue, 1.3, 50);
//        drawScaledString("Features", 0.21, defaultBlue, 1.5, -100, 0);
        if (feature != Feature.LANGUAGE) {
            mc.getTextureManager().bindTexture(FEATURE_BACKGROUND);
            int halfWidth = width/2;
            int boxWidth = 140;
            int x = halfWidth-90-boxWidth;
            int width = halfWidth+90+boxWidth;
            width -= x;
            int height = (int)(getRowHeight(6)-getRowHeight(1));
            int y =(int)getRowHeight(1);
            GlStateManager.enableBlend();
            GlStateManager.color(1,1,1,0.7F);
            drawModalRectWithCustomSizedTexture(x, y,0,0,width,height,width,height);
            drawScaledString(Message.SETTING_SETTINGS.getMessage(), 110, defaultBlue, 1.5);
        }
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
        if (feature == Feature.LANGUAGE) {
            main.getConfigValues().loadLanguageFile();
        }
    }

    /**
     * Code to perform the button toggles, openings of other gui's/pages, and language changes.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonLanguage) {
            Language language = ((ButtonLanguage)abstractButton).getLanguage();
            main.getConfigValues().setLanguage(language);
            main.getConfigValues().loadLanguageFile();
            main.loadKeyBindingDescriptions();
            returnToGui();
        } else if (abstractButton instanceof ButtonSwitchTab) {
            ButtonSwitchTab tab = (ButtonSwitchTab)abstractButton;
            mc.displayGuiScreen(new SkyblockAddonsGui(main, 1, tab.getTab()));
        } else if (abstractButton instanceof ButtonColor) {
            main.getConfigValues().setNextColor(feature);
        } else if (abstractButton instanceof ButtonToggleTitle) {
            ButtonFeature button = (ButtonFeature)abstractButton;
            Feature feature = button.getFeature();
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
        } else if (feature == Feature.SHOW_BACKPACK_PREVIEW) {
            main.getConfigValues().setBackpackStyle(main.getConfigValues().getBackpackStyle().getNextType());
            closingGui = true;
            Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main, feature, page, lastPage, lastTab, settings));
            closingGui = false;
        } else if (abstractButton instanceof ButtonArrow) {
            ButtonArrow arrow = (ButtonArrow)abstractButton;
            if (arrow.isNotMax()) {
                main.getUtils().setFadingIn(false);
                if (arrow.getArrowType() == ButtonArrow.ArrowType.RIGHT) {
                    closingGui = true;
                    mc.displayGuiScreen(new SettingsGui(main, feature, ++page, lastPage, lastTab, settings));
                } else {
                    closingGui = true;
                    mc.displayGuiScreen(new SettingsGui(main, feature, --page, lastPage, lastTab, settings));
                }
            }
        }
    }

    private void drawScaledString(String text, int y, int color, double scale) {
        drawScaledString(text, y, color, scale, 0);
    }

    /**
     * To avoid repeating the code for scaled text, use this instead.
     */
    private void drawScaledString(String text, int y, int color, double scale, int xOff) {
        double x = width/2;
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        drawCenteredString(fontRendererObj, text,
                (int)(x/scale)+xOff, (int)(y/scale), color);
        GlStateManager.popMatrix();
    }

    private void addLanguageButton(Language language) {
        if (displayCount == 0) return;
        String text = feature.getMessage();
        int halfWidth = width/2;
        int boxWidth = 140;
        int x = 0;
        if (collumn == 1) {
            x = halfWidth-90-boxWidth;
        } else if (collumn == 2) {
            x = halfWidth-(boxWidth/2);
        } else if (collumn == 3) {
            x = halfWidth+90;
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
        int halfWidth = width/2;
        int boxWidth = 100;
        int x = halfWidth-(boxWidth/2);
        double y = getRowHeightSetting(row);
        if (setting == EnumUtils.FeatureSetting.COLOR) {
            buttonList.add(new ButtonColor(x, y, 100, 20, Message.SETTING_CHANGE_COLOR.getMessage(), main, feature));
        } else if (setting == EnumUtils.FeatureSetting.GUI_SCALE) {
            buttonList.add(new ButtonGuiScale(x, y, 100, 20, main, feature));
        } else if (setting == EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES) {
            boxWidth = 31;
            x = halfWidth-(boxWidth/2);
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
        } else if (setting == EnumUtils.FeatureSetting.USE_VANILLA_TEXTURE) {
            row++;
            boxWidth = 31;
            x = halfWidth-(boxWidth/2);
            y = getRowHeightSetting(row);
            buttonList.add(new ButtonToggleTitle(x, y, Message.SETTING_USE_VANILLA_TEXTURE.getMessage(), main, Feature.USE_VANILLA_TEXTURE_DEFENCE));
        } else if (setting == EnumUtils.FeatureSetting.SHOW_ONLY_WHEN_HOLDING_SHIFT) {
            boxWidth = 31;
            x = halfWidth - (boxWidth / 2);
            y = getRowHeightSetting(row);
            buttonList.add(new ButtonToggleTitle(x, y, Message.SETTING_SHOW_ONLY_WHEN_HOLDING_SHIFT.getMessage(), main, Feature.SHOW_BACKPACK_HOLDING_SHIFT));
        } else if (setting == EnumUtils.FeatureSetting.MAKE_INVENTORY_COLORED) {
            boxWidth = 31;
            x = halfWidth - (boxWidth / 2);
            y = getRowHeightSetting(row);
            buttonList.add(new ButtonToggleTitle(x, y, Message.SETTING_MAKE_BACKPACK_INVENTORIES_COLORED.getMessage(), main, Feature.MAKE_BACKPACK_INVENTORIES_COLORED));
        } else if (setting == EnumUtils.FeatureSetting.BACKPACK_STYLE) {
            boxWidth = 140;
            x = halfWidth-(boxWidth/2);
            buttonList.add(new ButtonSolid(x, y, 140, 20, Message.SETTING_BACKPACK_STYLE.getMessage(), main, feature));
        }
        row++;
    }

    // Each row is spaced 0.08 apart, starting at 0.17.
    private double getRowHeight(double row) {
        row--;
        return 95+(row*30); //height*(0.18+(row*0.08));
    }

    private double getRowHeightSetting(double row) {
        row--;
        return 130+(row*35); //height*(0.18+(row*0.08));
    }

    @Override
    public void onGuiClosed() {
        if (!closingGui) {
            returnToGui();
        }
    }

    private void returnToGui() {
        closingGui = true;
        main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.MAIN, lastPage, lastTab);
    }
}
