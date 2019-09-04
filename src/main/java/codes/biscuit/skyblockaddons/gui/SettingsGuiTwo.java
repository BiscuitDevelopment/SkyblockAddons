package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonArrow;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLanguage;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSwitchTab;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Language;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class SettingsGuiTwo extends GuiScreen {

    private SkyblockAddons main;
    private int page;
    private int row = 1;
    private int collumn = 1;
    private int displayCount;
    private Feature feature;
    private int lastPage;
    private EnumUtils.SkyblockAddonsGuiTab lastTab;
    private boolean closingGui = false;

    private long timeOpened = System.currentTimeMillis();

    /**
     * The main gui, opened with /sba.
     */
    public SettingsGuiTwo(SkyblockAddons main, Feature feature, int page,
                          int lastPage, EnumUtils.SkyblockAddonsGuiTab lastTab) {
        this.main = main;
        this.feature = feature;
        this.page = page;
        this.lastPage = lastPage;
        this.lastTab = lastTab;
    }

    @Override
    public void initGui() {
        if (feature == Feature.LANGUAGE) {
            int scale = mc.gameSettings.guiScale;
            if (scale == 0) {
                displayCount = 12;
            } else if (scale == 1) {
                displayCount = 48;
            } else if (scale == 2) {
                displayCount = 30;
            } else if (scale == 3) {
                displayCount = 18;
            }
            // Add the buttons for each page.
            int skip = (page-1)*displayCount;

            boolean max = page == 1;
            buttonList.add(new ButtonArrow(width/2-15-50, height-70, main, ButtonArrow.ArrowType.LEFT, max));
            max = Language.values().length-skip-displayCount <= 0;
            buttonList.add(new ButtonArrow(width/2-15+50, height-70, main, ButtonArrow.ArrowType.RIGHT, max));

            for (Language language : Language.values()) {
                if (skip == 0) {
                    addLanguageButton(language);
                } else {
                    skip--;
                }
            }
        }
        ScaledResolution sr = new ScaledResolution(mc);
        float textScale = 1.4F;
        int x = sr.getScaledWidth()/2;
        int y = (int)(sr.getScaledHeight() * 0.21);
        String text = "Features";
        buttonList.add(new ButtonSwitchTab(x-180, y, (int)(fontRendererObj.getStringWidth(text)*textScale),
                14, text, main, EnumUtils.SkyblockAddonsGuiTab.FEATURES, null));
        text = "Fixes";
        buttonList.add(new ButtonSwitchTab(x-80, y, (int)(fontRendererObj.getStringWidth(text)*textScale),
                14, text, main, EnumUtils.SkyblockAddonsGuiTab.FIXES, null));
        text = "GUI Features";
        buttonList.add(new ButtonSwitchTab(x-20, y, (int)(fontRendererObj.getStringWidth(text)*textScale),
                14, text, main, EnumUtils.SkyblockAddonsGuiTab.GUI_FEATURES, null));
        text = "General Settings";
        buttonList.add(new ButtonSwitchTab(x+90, y, (int)(fontRendererObj.getStringWidth(text)*textScale),
                14, text, main, EnumUtils.SkyblockAddonsGuiTab.GENERAL_SETTINGS, null));
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
            returnToGui();
        } else if (abstractButton instanceof ButtonArrow) {
            ButtonArrow arrow = (ButtonArrow)abstractButton;
            if (!arrow.isMax()) {
                main.getUtils().setFadingIn(false);
                if (arrow.getArrowType() == ButtonArrow.ArrowType.RIGHT) {
                    mc.displayGuiScreen(new SettingsGuiTwo(main, Feature.LANGUAGE, ++page, lastPage, lastTab));
                } else {
                    mc.displayGuiScreen(new SettingsGuiTwo(main, Feature.LANGUAGE, --page, lastPage, lastTab));
                }
            }
        } else if (abstractButton instanceof ButtonSwitchTab) {
            ButtonSwitchTab tab = (ButtonSwitchTab)abstractButton;
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

    // Each row is spaced 0.08 apart, starting at 0.17.
    private double getRowHeight(double row) {
        row--;
        return height*0.28+(row*30); //height*(0.18+(row*0.08));
    }

    @Override
    public void onGuiClosed() {
        if (!closingGui) {
            returnToGui();
        }
    }

    private void returnToGui() {
        closingGui = true;
        mc.displayGuiScreen(new SkyblockAddonsGui(main, lastPage, lastTab));
    }
}
