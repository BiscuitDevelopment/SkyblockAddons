package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
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
import java.util.Set;
import java.util.regex.Pattern;

public class SkyblockAddonsGui extends GuiScreen {

    public static final int BUTTON_MAX_WIDTH = 140;
    private static Feature tooltipFeature;

    private EnumUtils.SkyblockAddonsGuiTab tab;
    private SkyblockAddons main;
    private int page;
    private GuiTextField magmaTextField = null;
    private int row = 1;
    private int collumn = 1;
    private int displayCount;

    private long timeOpened = System.currentTimeMillis();
    private static Set<Feature> settingFeatures = Sets.newHashSet(Feature.MAGMA_WARNING, Feature.DROP_CONFIRMATION,
            Feature.MANA_BAR, Feature.MANA_TEXT, Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.DEFENCE_TEXT, Feature.DEFENCE_PERCENTAGE,
            Feature.HIDE_HUD_BARS, Feature.SHOW_BACKPACK_PREVIEW);

    /**
     * The main gui, opened with /sba.
     */
    public SkyblockAddonsGui(SkyblockAddons main, int page, EnumUtils.SkyblockAddonsGuiTab tab) {
        this.tab = tab;
        this.main = main;
        this.page = page;
    }

    @Override
    public void initGui() {
        row = 1;
        collumn = 1;
        int scale = mc.gameSettings.guiScale;
        if (scale == 0) {
            displayCount = 6;
        } else if (scale == 1) {
            displayCount = 24;
        } else if (scale == 2) {
            displayCount = 15;
        } else if (scale == 3) {
            displayCount = 9;
        }
        addLanguageButton();
        // Add the buttons for each page.
        Feature[] array;
        if (tab == EnumUtils.SkyblockAddonsGuiTab.FEATURES) {
            array = new Feature[]{Feature.SHOW_ENCHANTMENTS_REFORGES, Feature.SHOW_BACKPACK_PREVIEW,
                    Feature.MINION_FULL_WARNING, Feature.FULL_INVENTORY_WARNING,
                    Feature.IGNORE_ITEM_FRAME_CLICKS, Feature.HIDE_FOOD_ARMOR_BAR, Feature.HIDE_HEALTH_BAR,
                    Feature.AVOID_BREAKING_STEMS, Feature.MAGMA_WARNING,
                    Feature.DROP_CONFIRMATION, Feature.HIDE_PLAYERS_IN_LOBBY, Feature.MINION_STOP_WARNING,
                    Feature.SHOW_ITEM_ANVIL_USES, Feature.LOCK_SLOTS, Feature.DONT_RESET_CURSOR_INVENTORY};
        } else if (tab == EnumUtils.SkyblockAddonsGuiTab.FIXES) {
            array = new Feature[]{Feature.HIDE_BONES, Feature.DISABLE_EMBER_ROD, Feature.HIDE_AUCTION_HOUSE_PLAYERS,
                    Feature.STOP_BOW_CHARGE_FROM_RESETTING, Feature.AVOID_PLACING_ENCHANTED_ITEMS, Feature.PREVENT_MOVEMENT_ON_DEATH
                    , Feature.HIDE_DURABILITY};
        } else if (tab == EnumUtils.SkyblockAddonsGuiTab.GUI_FEATURES) {
            array = new Feature[]{Feature.MAGMA_BOSS_TIMER, Feature.MANA_BAR, Feature.MANA_TEXT, Feature.DEFENCE_TEXT, Feature.DEFENCE_PERCENTAGE,
                    Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.DEFENCE_ICON, Feature.SKELETON_BAR, Feature.HEALTH_UPDATES,
                    Feature.ITEM_PICKUP_LOG, Feature.DARK_AUCTION_TIMER};
        } else {
            array = new Feature[]{};
        }
        int skip = (page-1)*displayCount;

        boolean max = page == 1;
        buttonList.add(new ButtonArrow(width/2-15-50, height-70, main, ButtonArrow.ArrowType.LEFT, max));
        max = array.length-skip-displayCount <= 0;
        buttonList.add(new ButtonArrow(width/2-15+50, height-70, main, ButtonArrow.ArrowType.RIGHT, max));

        for (Feature feature : array) {
            if (skip == 0) {
                addButton(feature, EnumUtils.ButtonType.TOGGLE);
            } else {
                skip--;
            }
        }
        ScaledResolution sr = new ScaledResolution(mc);
        float textScale = 1.4F;
        int x = sr.getScaledWidth()/2;
        int y = (int)(sr.getScaledHeight() * 0.21);
        String text = "Features";
        buttonList.add(new ButtonSwitchTab(x-180, y, (int)(fontRendererObj.getStringWidth(text)*textScale),
                14, text, main, EnumUtils.SkyblockAddonsGuiTab.FEATURES, tab));
        text = "Fixes";
        buttonList.add(new ButtonSwitchTab(x-80, y, (int)(fontRendererObj.getStringWidth(text)*textScale),
                14, text, main, EnumUtils.SkyblockAddonsGuiTab.FIXES, tab));
        text = "GUI Features";
        buttonList.add(new ButtonSwitchTab(x-20, y, (int)(fontRendererObj.getStringWidth(text)*textScale),
                14, text, main, EnumUtils.SkyblockAddonsGuiTab.GUI_FEATURES, tab));
        text = "General Settings";
        buttonList.add(new ButtonSwitchTab(x+90, y, (int)(fontRendererObj.getStringWidth(text)*textScale),
                14, text, main, EnumUtils.SkyblockAddonsGuiTab.GENERAL_SETTINGS, tab));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        tooltipFeature = null;
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
        drawScaledString("SkyblockAddons", 0.08, defaultBlue, 2.5F);
        drawScaledString("v" + SkyblockAddons.VERSION + " by Biscut", 0.08, defaultBlue, 1.3, 50, 15);
//        drawScaledString("Features", 0.21, defaultBlue, 1.5, -100, 0);
        defaultBlue = main.getUtils().getDefaultBlue(alpha);
//        drawScaledString("Fixes", 0.21, defaultBlue, 1.5, -50, 0);
//        drawScaledString("GUI Features ", 0.21, defaultBlue, 1.5,10,0);
//        drawScaledString("General Settings", 0.21, defaultBlue, 1.5, 100, 0);
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }

    /**
     * Code to perform the button toggles, openings of other gui's/pages, and language changes.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonFeature) {
            Feature feature = ((ButtonFeature)abstractButton).getFeature();
            if (feature == Feature.SETTINGS) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main));
            } else if (feature == Feature.LANGUAGE) {
//                main.getConfigValues().setLanguage(main.getConfigValues().getLanguage().getNextLanguage());
//                main.getConfigValues().loadLanguageFile();
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGuiTwo(main, Feature.LANGUAGE, 1, page,tab));
            }  else if (feature == Feature.EDIT_LOCATIONS) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main));
            } else if (feature == Feature.NEXT_PAGE) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, page+1, tab));
            } else if (feature == Feature.PREVIOUS_PAGE) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, page-1, tab));
            } else if (abstractButton instanceof ButtonToggleTwo) {
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
            }
        } else if (abstractButton instanceof ButtonArrow) {
            ButtonArrow arrow = (ButtonArrow)abstractButton;
            if (!arrow.isMax()) {
                main.getUtils().setFadingIn(false);
                if (arrow.getArrowType() == ButtonArrow.ArrowType.RIGHT) {
                    mc.displayGuiScreen(new SkyblockAddonsGui(main, ++page, tab));
                } else {
                    mc.displayGuiScreen(new SkyblockAddonsGui(main, --page, tab));
                }
            }
        } else if (abstractButton instanceof ButtonSwitchTab) {
            ButtonSwitchTab tab = (ButtonSwitchTab)abstractButton;
            main.getUtils().setFadingIn(false);
            mc.displayGuiScreen(new SkyblockAddonsGui(main, 1, tab.getTab()));
        }
    }

    private void drawScaledString(String text, double yMultiplier, int color, double scale) {
        drawScaledString(text, yMultiplier, color, scale, 0, 0);
    }

    /**
     * To avoid repeating the code for scaled text, use this instead.
     */
    private void drawScaledString(String text, double yMultiplier, int color, double scale, int xOff, int yOff) {
        double x = width/2;
        double y = height*yMultiplier;
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        drawCenteredString(fontRendererObj, text,
                (int)(x/scale)+xOff, (int)(y/scale)+yOff, color);
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
            if (settingFeatures.contains(feature)) {
                buttonList.add(new ButtonSettings(x + boxWidth - 33, y + boxHeight - 23, text, main, feature));
            }
            buttonList.add(new ButtonToggleTwo(x+40, y+boxHeight-23, text, main, feature));
        }
        collumn++;
        if (collumn > 3) {
            collumn = 1;
            row++;
        }
        displayCount--;
    }

    private void addLanguageButton() {
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 50;
        int x = halfWidth+90;
        double y = getRowHeight(displayCount/3+1);
        buttonList.add(new ButtonNormal(x, y, boxWidth, boxHeight, "Language: "+Feature.LANGUAGE.getMessage(), main, Feature.LANGUAGE));
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
                        magmaTimes[timePart] = Integer.valueOf(stringSplit[timePart]);
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
        return height*0.28+(row*60); //height*(0.18+(row*0.08));
    }

    /**
     * Save the config when exiting.
     */
    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
    }

    public static void setTooltipFeature(Feature tooltipFeature) {
        SkyblockAddonsGui.tooltipFeature = tooltipFeature;
    }

    public EnumUtils.SkyblockAddonsGuiTab getTab() {
        return tab;
    }
}
