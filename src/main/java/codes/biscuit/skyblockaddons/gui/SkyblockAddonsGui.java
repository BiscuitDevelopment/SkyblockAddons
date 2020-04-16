package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.utils.CoordsPair;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.StringUtil;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.GuiIngameForge;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SkyblockAddonsGui extends GuiScreen {

    public static final int BUTTON_MAX_WIDTH = 140;
//    private static Feature tooltipFeature;

    private static String searchString = null;

    private GuiTextField featureSearchBar;
    private EnumUtils.GuiTab tab;
    private SkyblockAddons main;
    private int page;
//    private GuiTextField magmaTextField = null;
    private int row = 1;
    private int collumn = 1;
    private int displayCount;

    private long timeOpened = System.currentTimeMillis();

    private boolean cancelClose = false;

    /**
     * The main gui, opened with /sba.
     */
    public SkyblockAddonsGui(SkyblockAddons main, int page, EnumUtils.GuiTab tab) {
        this.tab = tab;
        this.main = main;
        this.page = page;
    }

    public SkyblockAddonsGui(SkyblockAddons main, int page, EnumUtils.GuiTab tab, String text) {
        this(main,page,tab);
    }


    @SuppressWarnings({"IntegerDivisionInFloatingPointContext"})
    @Override
    public void initGui() {
        row = 1;
        collumn = 1;
        displayCount = findDisplayCount();
        addLanguageButton();
        addEditLocationsButton();
        addFeaturedBanner();
        addGeneralSettingsButton();

        if (featureSearchBar == null) {
            featureSearchBar = new GuiTextField(2, this.fontRendererObj, width / 2 - 60, 69, 120, 15);
            featureSearchBar.setMaxStringLength(500);
            featureSearchBar.setFocused(true);
        }
        if (searchString != null) {
            featureSearchBar.setText(searchString);
        }

        // Add the buttons for each page.
        List<Feature> features = new LinkedList<>();
        for (Feature feature : tab != EnumUtils.GuiTab.GENERAL_SETTINGS ? Sets.newHashSet(Feature.values()) : Feature.getGeneralTabFeatures()) {
            if ((feature.isActualFeature() || tab == EnumUtils.GuiTab.GENERAL_SETTINGS) && !main.getConfigValues().isRemoteDisabled(feature)) { // Don't add disabled features yet
                if (matchesSearch(feature.getMessage())) { // Matches search.
                    features.add(feature);
                } else { // If a sub-setting matches the search show it up in the results as well.
                    for (EnumUtils.FeatureSetting setting : feature.getSettings()) {
                        try {
                            if (matchesSearch(setting.getMessage().getMessage())) {
                                features.add(feature);
                            }
                        } catch (Exception ignored) {} // Hit a message that probably needs variables to fill in, just skip it.
                    }
                }
            }
        }

        features.sort(Comparator.comparing(feature -> feature.getMessage()));

//        features.sort((o1, o2) -> { // TODO put all new features on the first page? or nah?
//            if (o1.isNew() && !o2.isNew()) {
//                return -1;
//            } else if (o2.isNew() && !o1.isNew()) {
//                return 1;
//            } else {
//                return 0;
//            }
//        });

        if (tab != EnumUtils.GuiTab.GENERAL_SETTINGS) {
            for (Feature feature : Feature.values())
                if (main.getConfigValues().isRemoteDisabled(feature) && matchesSearch(feature.getMessage()))
                    features.add(feature); // add disabled features at the end
        }

        int skip = (page - 1) * displayCount;

        boolean max = page == 1;
        buttonList.add(new ButtonArrow(width / 2 - 15 - 50, height - 70, main, ButtonArrow.ArrowType.LEFT, max));
        max = features.size() - skip - displayCount <= 0;
        buttonList.add(new ButtonArrow(width / 2 - 15 + 50, height - 70, main, ButtonArrow.ArrowType.RIGHT, max));

        buttonList.add(new ButtonSocial(width / 2 + 200, 30, main, EnumUtils.Social.YOUTUBE));
        buttonList.add(new ButtonSocial(width / 2 + 175, 30, main, EnumUtils.Social.DISCORD));
        buttonList.add(new ButtonSocial(width / 2 + 150, 30, main, EnumUtils.Social.GITHUB));

        for (Feature feature : features) {
            if (skip == 0) {
                if (feature == Feature.TEXT_STYLE || feature == Feature.WARNING_TIME || feature == Feature.CHROMA_MODE) {
                    addButton(feature, EnumUtils.ButtonType.SOLID);
                } else if (feature == Feature.CHROMA_SPEED || feature == Feature.CHROMA_FADE_WIDTH) {
                    addButton(feature, EnumUtils.ButtonType.CHROMA_SLIDER);
                } else {
                    addButton(feature, EnumUtils.ButtonType.TOGGLE);
                }
            } else {
                skip--;
            }
        }
        Keyboard.enableRepeatEvents(true);
    }

    private boolean matchesSearch(String text) {
        if (StringUtil.isEmpty(featureSearchBar.getText())) return true;

        return text.toLowerCase().contains(featureSearchBar.getText().toLowerCase());
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

        int startColor = new Color(0,0, 0, alpha).getRGB();
        int endColor = new Color(0,0, 0, (int)(alpha*1.5)).getRGB();
        drawGradientRect(0, 0, width, height, startColor, endColor);
        GlStateManager.enableBlend();

        if (alpha < 4) alpha = 4; // Text under 4 alpha appear 100% transparent for some reason o.O

        drawDefaultTitleText(this, alpha*2);

        featureSearchBar.drawTextBox();
        if (StringUtil.isEmpty(featureSearchBar.getText())) {
            MinecraftReflection.FontRenderer.drawString(Message.MESSAGE_SEARCH_FEATURES.getMessage(), width/2-60+4, 72, ChatFormatting.DARK_GRAY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }

    /**
     * Code to perform the button toggles, openings of other guis/pages, and language changes.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonFeature) {
            Feature feature = ((ButtonFeature)abstractButton).getFeature();
            if (abstractButton instanceof ButtonSettings) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main, feature, 1, page, tab, feature.getSettings()));
                return;
            }
            if (feature == Feature.LANGUAGE) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main, Feature.LANGUAGE, 1, page,tab, null));
            }  else if (feature == Feature.EDIT_LOCATIONS) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main, page, tab));
            }  else if (feature == Feature.GENERAL_SETTINGS) {
                if (tab == EnumUtils.GuiTab.GENERAL_SETTINGS) {
                    main.getUtils().setFadingIn(false);
                    Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, 1, EnumUtils.GuiTab.MAIN, featureSearchBar.getText()));
                } else {
                    main.getUtils().setFadingIn(false);
                    Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, 1, EnumUtils.GuiTab.GENERAL_SETTINGS, featureSearchBar.getText()));
                }
            } else if (abstractButton instanceof ButtonToggle) {
                if (main.getConfigValues().isRemoteDisabled(feature)) return;
                if (main.getConfigValues().isDisabled(feature)) {
                    main.getConfigValues().getDisabledFeatures().remove(feature);
                } else {
                    main.getConfigValues().getDisabledFeatures().add(feature);
                    if (feature == Feature.HIDE_FOOD_ARMOR_BAR) { // Reset the vanilla bars when disabling these two features.
                        GuiIngameForge.renderArmor = true; // The food gets automatically enabled, no need to include it.
                    } else if (feature == Feature.HIDE_HEALTH_BAR) {
                        GuiIngameForge.renderHealth = true;
                    } else if (feature == Feature.FULL_INVENTORY_WARNING) {
                        main.getInventoryUtils().setInventoryWarningShown(false);
                        main.getScheduler().removeQueuedFullInventoryWarnings();
                    }
                }
                ((ButtonToggle)abstractButton).onClick();
            } else if (abstractButton instanceof ButtonSolid) {
                if (feature == Feature.TEXT_STYLE) {
                    main.getConfigValues().setTextStyle(main.getConfigValues().getTextStyle().getNextType());
                    cancelClose = true;
                    Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, page, tab, featureSearchBar.getText()));
                    cancelClose = false;
                } else if (feature == Feature.CHROMA_MODE) {
                    main.getConfigValues().setChromaMode(main.getConfigValues().getChromaMode().getNextType());
                    cancelClose = true;
                    Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, page, tab, featureSearchBar.getText()));
                    cancelClose = false;
                }
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
            } else if (abstractButton instanceof ButtonCredit) {
                if (main.getConfigValues().isRemoteDisabled(feature)) return;
                EnumUtils.FeatureCredit credit = ((ButtonCredit)abstractButton).getCredit();
                try {
                    Desktop.getDesktop().browse(new URI(credit.getUrl()));
                } catch (Exception ignored) {}
            }
        } else if (abstractButton instanceof ButtonArrow) {
            ButtonArrow arrow = (ButtonArrow)abstractButton;
            if (arrow.isNotMax()) {
                main.getUtils().setFadingIn(false);
                if (arrow.getArrowType() == ButtonArrow.ArrowType.RIGHT) {
                    mc.displayGuiScreen(new SkyblockAddonsGui(main, ++page, tab, featureSearchBar.getText()));
                } else {
                    mc.displayGuiScreen(new SkyblockAddonsGui(main, --page, tab, featureSearchBar.getText()));
                }
            }
        } else if (abstractButton instanceof ButtonSwitchTab) {
            ButtonSwitchTab tab = (ButtonSwitchTab)abstractButton;
            if (tab.getTab() != this.tab) {
                main.getUtils().setFadingIn(false);
                mc.displayGuiScreen(new SkyblockAddonsGui(main, 1, tab.getTab(), featureSearchBar.getText()));
            }
        } else if (abstractButton instanceof ButtonSocial) {
            EnumUtils.Social social = ((ButtonSocial)abstractButton).getSocial();
            try {
                Desktop.getDesktop().browse(social.getUrl());
            } catch (Exception ignored) {}
        } else if (abstractButton instanceof ButtonBanner) {
            try {
                Desktop.getDesktop().browse(main.getUtils().getFeaturedURL());
            } catch (Exception ignored) {}
        }
    }

    /**
     * Draws the default text at the top at bottoms of the GUI.
     * @param gui The gui to draw the text on.
     */
    static void drawDefaultTitleText(GuiScreen gui, int alpha) {
        int defaultBlue = SkyblockAddons.getInstance().getUtils().getDefaultBlue(alpha);

        drawScaledString(gui, "SkyblockAddons", 28, defaultBlue, 2.5F, 0);
        drawScaledString(gui,"v" + SkyblockAddons.VERSION + " by Biscut", 49, defaultBlue, 1.3, 50);

        if (gui instanceof SkyblockAddonsGui) {
            drawScaledString(gui, "Special Credits: InventiveTalent - Magma Boss Timer API", gui.height - 22, defaultBlue, 1, 0);
        }
    }

    /**
     * Draws a centered string at the middle of the screen on the x axis, with a specified scale and location.
     *
     * @param text The text to draw.
     * @param y The y level to draw the text/
     * @param color The text color.
     * @param scale The scale to draw the text.
     * @param xOffset The offset from the center x that the text should be drawn at.
     */
    static void drawScaledString(GuiScreen guiScreen, String text, int y, int color, double scale, int xOffset) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        MinecraftReflection.FontRenderer.drawCenteredString(text, Math.round((float)guiScreen.width/2/scale)+xOffset,
                Math.round((float)y/scale), color);
        GlStateManager.popMatrix();
    }

    /**
     * Adds a button, limiting its width and setting the correct position.
     */
    private void addButton(Feature feature, EnumUtils.ButtonType buttonType) {
        if (displayCount == 0) return;
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
            ButtonNormal button = new ButtonNormal(x, y, text, main, feature);
            buttonList.add(button);

            EnumUtils.FeatureCredit credit = EnumUtils.FeatureCredit.fromFeature(feature);
            if (credit != null) {
                CoordsPair coords = button.getCreditsCoords(credit);
                buttonList.add(new ButtonCredit(coords.getX(), coords.getY(), text, main, credit, feature, button.isMultilineButton()));
            }

            if (feature.getSettings().size() > 0) {
                buttonList.add(new ButtonSettings(x + boxWidth - 33, y + boxHeight - 20, text, main, feature));
            }
            buttonList.add(new ButtonToggle(x+40, y+boxHeight-18, main, feature));
        } else if (buttonType == EnumUtils.ButtonType.SOLID) {
            buttonList.add(new ButtonNormal(x, y, text, main, feature));

            if (feature == Feature.TEXT_STYLE || feature == Feature.CHROMA_MODE) {
                buttonList.add(new ButtonSolid(x+10, y + boxHeight - 23, 120, 15, "", main, feature));
            } else if (feature == Feature.WARNING_TIME) {
                int solidButtonX = x+(boxWidth/2)-17;
                buttonList.add(new ButtonModify(solidButtonX-20, y + boxHeight - 23, 15, 15, "+", main, Feature.ADD));
                buttonList.add(new ButtonSolid(solidButtonX, y + boxHeight - 23, 35, 15, "", main, feature));
                buttonList.add(new ButtonModify(solidButtonX+35+5, y + boxHeight - 23, 15, 15,"-", main, Feature.SUBTRACT));

            }
        } else if (buttonType == EnumUtils.ButtonType.CHROMA_SLIDER) {
            buttonList.add(new ButtonNormal(x, y, text, main, feature));

            if (feature == Feature.CHROMA_SPEED) {
                buttonList.add(new ButtonChromaSlider(x + 35, y + boxHeight - 23, 70, 15, main, main.getConfigValues().getChromaSpeed(),
                        0.1F, 10, 0.5F, new ButtonChromaSlider.OnSliderChangeCallback() {
                    @Override
                    public void sliderUpdated(float value) {
                        main.getConfigValues().setChromaSpeed(value);
                    }
                }));
            } else if (feature == Feature.CHROMA_FADE_WIDTH) {
                buttonList.add(new ButtonChromaSlider(x + 35, y + boxHeight - 23, 70, 15, main, main.getConfigValues().getChromaFadeWidth(),
                        1, 42, 1, new ButtonChromaSlider.OnSliderChangeCallback() {
                    @Override
                    public void sliderUpdated(float value) {
                        main.getConfigValues().setChromaFadeWidth(value);
                    }
                }));
            }
        }

        if (feature.isNew()) {
            buttonList.add(new ButtonNewTag(x+boxWidth-15, (int)y+boxHeight-10));
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

    private void addEditLocationsButton() {
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 50;
        int x = halfWidth-90-boxWidth;
        double y = getRowHeight(displayCount/3+1);
        buttonList.add(new ButtonNormal(x, y, boxWidth, boxHeight, Feature.EDIT_LOCATIONS.getMessage(), main, Feature.EDIT_LOCATIONS));
    }

    private void addGeneralSettingsButton() {
        int halfWidth = width/2;
        int boxWidth = 140;
        int boxHeight = 15;
        int x = halfWidth+90;
        double y = getRowHeight(1)-25;
        buttonList.add(new ButtonNormal(x, y, boxWidth, boxHeight, Message.TAB_GENERAL_SETTINGS.getMessage(), main, Feature.GENERAL_SETTINGS));
    }


    private void addFeaturedBanner() {
        if (main.getUtils().getFeaturedURL() != null) {
            int halfWidth = width / 2;
            buttonList.add(new ButtonBanner(halfWidth - 170, 15, main));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (featureSearchBar.isFocused()) {
            featureSearchBar.textboxKeyTyped(typedChar, keyCode);
            searchString = featureSearchBar.getText();

            main.getUtils().setFadingIn(false);
            buttonList.clear();

            page = 1;
            initGui();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        featureSearchBar.mouseClicked(mouseX, mouseY, mouseButton);
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
        if (!cancelClose) {
            if (tab == EnumUtils.GuiTab.GENERAL_SETTINGS) {
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
            }
            main.getConfigValues().saveConfig();
            Keyboard.enableRepeatEvents(false);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        featureSearchBar.updateCursorCounter();
    }
}
