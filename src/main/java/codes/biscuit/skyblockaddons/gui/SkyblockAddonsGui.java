package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonFeature;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSolid;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonToggle;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.GuiIngameForge;

import java.awt.*;

public class SkyblockAddonsGui extends GuiScreen {

    public static final int BUTTON_MAX_WIDTH = 140;

    private SkyblockAddons main;
    private int page;

    private long timeOpened = System.currentTimeMillis();

    /**
     * The main gui, opened with /sba.
     */
    public SkyblockAddonsGui(SkyblockAddons main, int page) {
        this.main = main;
        this.page = page;
    }

    @Override
    public void initGui() {
        // Add the buttons for each page.
        if (page == 1) {
            addButton(1, Feature.SHOW_ENCHANTMENTS_REFORGES, 1, EnumUtils.ButtonType.TOGGLE);
            addButton(1, Feature.SHOW_BACKPACK_PREVIEW, 2, EnumUtils.ButtonType.TOGGLE);
            addButton(1, Feature.MINION_FULL_WARNING, 3, EnumUtils.ButtonType.TOGGLE);
            addButton(2, Feature.HIDE_BONES, 1, EnumUtils.ButtonType.TOGGLE);
            addButton(2, Feature.FULL_INVENTORY_WARNING, 2, EnumUtils.ButtonType.TOGGLE);
            addButton(2, Feature.DISABLE_EMBER_ROD, 3, EnumUtils.ButtonType.TOGGLE);
            addButton(3, Feature.HIDE_AUCTION_HOUSE_PLAYERS, 1, EnumUtils.ButtonType.TOGGLE);
            addButton(3, Feature.IGNORE_ITEM_FRAME_CLICKS, 2, EnumUtils.ButtonType.TOGGLE);
            addButton(3, Feature.HIDE_FOOD_ARMOR_BAR, 3, EnumUtils.ButtonType.TOGGLE);
            addButton(4, Feature.HIDE_HEALTH_BAR, 1, EnumUtils.ButtonType.TOGGLE);
            addButton(4, Feature.MINION_STOP_WARNING, 2, EnumUtils.ButtonType.TOGGLE);
            addButton(4, Feature.MAGMA_BOSS_TIMER, 3, EnumUtils.ButtonType.TOGGLE);
            addButton(5, Feature.DROP_CONFIRMATION, 1, EnumUtils.ButtonType.TOGGLE);
            addButton(5, Feature.MAGMA_WARNING, 2, EnumUtils.ButtonType.TOGGLE);
            addButton(5, Feature.HIDE_DURABILITY, 3, EnumUtils.ButtonType.TOGGLE);

            addButton(6, Feature.NEXT_PAGE, 2, EnumUtils.ButtonType.SOLID);
        } else if (page == 2) {

            addButton(1, Feature.HIDE_PLAYERS_IN_LOBBY, 1, EnumUtils.ButtonType.TOGGLE);
            addButton(2, Feature.DEFENCE_TEXT, 1, EnumUtils.ButtonType.TOGGLE);
            addButton(2, Feature.MANA_BAR, 2, EnumUtils.ButtonType.TOGGLE);
            addButton(2, Feature.HEALTH_BAR, 3, EnumUtils.ButtonType.TOGGLE);
            addButton(3, Feature.DEFENCE_ICON, 1, EnumUtils.ButtonType.TOGGLE);
            addButton(3, Feature.MANA_TEXT, 2, EnumUtils.ButtonType.TOGGLE);
            addButton(3, Feature.HEALTH_TEXT, 3, EnumUtils.ButtonType.TOGGLE);
            addButton(4, Feature.DEFENCE_PERCENTAGE, 1, EnumUtils.ButtonType.TOGGLE);
            addButton(4, Feature.SKELETON_BAR, 2, EnumUtils.ButtonType.TOGGLE);
            addButton(4, Feature.HEALTH_UPDATES, 3, EnumUtils.ButtonType.TOGGLE);
            addButton(5, Feature.ITEM_PICKUP_LOG, 1, EnumUtils.ButtonType.TOGGLE);

            addButton(6, Feature.PREVIOUS_PAGE, 2, EnumUtils.ButtonType.SOLID);
        }
        addButton(8.5, Feature.EDIT_LOCATIONS, 1, EnumUtils.ButtonType.SOLID);
        addButton(8.5, Feature.SETTINGS, 2, EnumUtils.ButtonType.SOLID);
        addButton(8.5, Feature.LANGUAGE, 3, EnumUtils.ButtonType.SOLID);
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
        drawScaledString("SkyblockAddons", 0.12, defaultBlue, 2.5F);
        drawScaledString("v" + SkyblockAddons.VERSION + " by Biscut", 0.12, defaultBlue, 1.3, 50, 17);
        drawScaledString(Message.SETTING_SETTINGS.getMessage(), 0.8, defaultBlue, 1.5);
        if (page == 1) {
            if (main.isUsingLabymod() || (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) { // Show the labymod message also when i'm testing it to make sure it looks fine.
                drawScaledString(Message.MESSAGE_LABYMOD.getMessage(), 0.75, defaultBlue, 1);
            }
        } else if (page == 2) {
            if (main.isUsingOofModv1() || (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) { // Show the labymod message also when i'm testing it to make sure it looks fine.
                drawScaledString("", 0.75, defaultBlue, 1);
            }
//            drawScaledString("GUI Items", 0.26, defaultBlue, 1.8F);
        }
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
                main.getConfigValues().setLanguage(main.getConfigValues().getLanguage().getNextLanguage());
                main.getConfigValues().loadLanguageFile();
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, page));
            }  else if (feature == Feature.EDIT_LOCATIONS) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main));
            } else if (feature == Feature.NEXT_PAGE) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, 2));
            } else if (feature == Feature.PREVIOUS_PAGE) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, 1));
            } else {
                if (main.getConfigValues().getDisabledFeatures().contains(feature)) {
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
        drawCenteredString(fontRenderer, text,
                (int)(x/scale)+xOff, (int)(y/scale)+yOff, color);
        GlStateManager.popMatrix();
    }

    /**
     * Adds a button, limiting its width and setting the correct position.
     */
    private void addButton(double row, Feature feature, int collumn, EnumUtils.ButtonType buttonType) {
        String text = feature.getMessage();
        int halfWidth = width/2;
        int oneThird = width/3;
        int twoThirds = oneThird*2;
        int boxWidth = fontRenderer.getStringWidth(text) + 10;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        int boxHeight = 20;
        int x = 0;
        if (collumn == 1) {
            x = oneThird-boxWidth-30;
        } else if (collumn == 2) {
            x = halfWidth-(boxWidth/2);
        } else if (collumn == 3) {
            x = twoThirds+30;
        } else if (collumn == 4) {
            x = oneThird-(boxWidth/2);
        } else if (collumn == 5) {
            x = twoThirds-(boxWidth/2);
        }
        if (buttonType == EnumUtils.ButtonType.TOGGLE) {
            buttonList.add(new ButtonToggle(x, getRowHeight(row), boxWidth, boxHeight, text, main, feature));
        } else if (buttonType == EnumUtils.ButtonType.SOLID) {
            buttonList.add(new ButtonSolid(x, getRowHeight(row), boxWidth, boxHeight, text, main, feature));
        }
    }

    // Each row is spaced 0.08 apart, starting at 0.17.
    private double getRowHeight(double row) {
        return height * (0.17+(row*0.08));
    }

    /**
     * Save the config when exiting.
     */
    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
    }
}
