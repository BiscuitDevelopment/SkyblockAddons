package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.*;
import codes.biscuit.skyblockaddons.utils.CoordsPair;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.GuiIngameForge;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.regex.Pattern;

public class SkyblockAddonsGui extends GuiScreen {

    public static final int BUTTON_MAX_WIDTH = 140;
//    private static Feature tooltipFeature;

    private EnumUtils.GuiTab tab;
    private SkyblockAddons main;
    private int page;
    private GuiTextField magmaTextField = null;
    private int row = 1;
    private int collumn = 1;
    private int displayCount;

    private long timeOpened = System.currentTimeMillis();

    /**
     * The main gui, opened with /sba.
     */
    public SkyblockAddonsGui(SkyblockAddons main, int page, EnumUtils.GuiTab tab) {
        this.tab = tab;
        this.main = main;
        this.page = page;
    }

    @SuppressWarnings({"IntegerDivisionInFloatingPointContext"})
    @Override
    public void initGui() {
        row = 1;
        collumn = 1;
        displayCount = findDisplayCount();
        addLanguageButton();
        addEditLocationsButton();
        // Add the buttons for each page.
        Set<Feature> features = tab.getFeatures();
        if (features != null) {
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
                    if (feature == Feature.TEXT_STYLE || feature == Feature.WARNING_TIME) {
                        addButton(feature, EnumUtils.ButtonType.SOLID);
                    } else {
                        addButton(feature, EnumUtils.ButtonType.TOGGLE);
                    }
                } else {
                    skip--;
                }
            }

            addTabs();
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private void addTabs() {
        int collumn = 1;
        for (EnumUtils.GuiTab loopTab : EnumUtils.GuiTab.values()) {
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
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main, feature, 1, page, tab, feature.getSettings()));
                return;
            }
            if (feature == Feature.LANGUAGE) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main, Feature.LANGUAGE, 1, page,tab, null));
            }  else if (feature == Feature.EDIT_LOCATIONS) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main, page, tab));
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
        } else if (abstractButton instanceof ButtonSocial) {
            EnumUtils.Social social = ((ButtonSocial)abstractButton).getSocial();
            try {
                Desktop.getDesktop().browse(social.getUrl());
            } catch (Exception ignored) {}
        }
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
                buttonList.add(new ButtonCredit(coords.getX(), coords.getY(), text, main, credit, feature));
            }

            if (feature.getSettings().size() > 0) {
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
