package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.GuiIngameForge;

import java.awt.*;

public class SkyblockAddonsGui extends GuiScreen {

    static int WIDTH_LIMIT = 140;

    private SkyblockAddons main;

    private long timeOpened = System.currentTimeMillis();

    public SkyblockAddonsGui(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public void initGui() {
        addButton(height*0.25, ConfigValues.Message.SETTING_MAGMA_BOSS_WARNING, Feature.MAGMA_WARNING, 1);
        addButton(height*0.25, ConfigValues.Message.SETTING_ITEM_DROP_CONFIRMATION, Feature.DROP_CONFIRMATION, 2);
        addButton(height*0.33, ConfigValues.Message.SETTING_MANA_BAR, Feature.MANA_BAR, 1);
        addButton(height*0.33, ConfigValues.Message.SETTING_HIDE_SKELETON_HAT_BONES, Feature.HIDE_BONES, 2);
        addButton(height*0.41, ConfigValues.Message.SETTING_SKELETON_HAT_BONES_BAR, Feature.SKELETON_BAR, 1);
        addButton(height*0.41, ConfigValues.Message.SETTING_HIDE_FOOD_AND_ARMOR, Feature.HIDE_FOOD_ARMOR_BAR, 2);
        addButton(height*0.49, ConfigValues.Message.SETTING_FULL_INVENTORY_WARNING, Feature.FULL_INVENTORY_WARNING, 1);
        addButton(height*0.25, ConfigValues.Message.SETTING_MAGMA_BOSS_HEALTH_BAR, Feature.MAGMA_BOSS_BAR, 3);
        addButton(height*0.49, ConfigValues.Message.SETTING_DISABLE_EMBER_ROD_ABILITY, Feature.DISABLE_EMBER_ROD,2);
        addButton(height*0.33, ConfigValues.Message.SETTING_HIDE_DURABILITY, Feature.HIDE_DURABILITY,3);
        addButton(height*0.41, ConfigValues.Message.SETTING_ENCHANTS_AND_REFORGES, Feature.SHOW_ENCHANTMENTS_REFORGES,3);
        addButton(height*0.49, ConfigValues.Message.SETTING_MINION_STOP_WARNING, Feature.MINION_STOP_WARNING, 3);
        addButton(height*0.86, ConfigValues.Message.SETTING_EDIT_SETTINGS, Feature.SETTINGS, 4);
        addButton(height*0.57, ConfigValues.Message.SETTING_AUCTION_HOUSE_PLAYERS, Feature.HIDE_AUCTION_HOUSE_PLAYERS, 1);
        addButton(height*0.86, ConfigValues.Message.LANGUAGE, Feature.LANGUAGE, 5);
        addButton(height*0.57, ConfigValues.Message.SETTING_HEALTH_BAR, Feature.HEALTH_BAR, 2);
        addButton(height*0.57, ConfigValues.Message.SETTING_DEFENCE_ICON, Feature.DEFENCE_ICON, 3);
        addButton(height*0.65, ConfigValues.Message.SETTING_SHOW_BACKPACK_PREVIEW, Feature.SHOW_BACKPACK_PREVIEW, 1);
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long timeSinceOpen = System.currentTimeMillis() - timeOpened;
        float alphaMultiplier;
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
        int defaultBlue = new Color(189,236,252, alpha*2).getRGB();

        drawScaledString("SkyblockAddons", 0.12, defaultBlue, 2.5F);
        drawScaledString("by Biscut", 0.12, defaultBlue, 1.3, 50, 17);
        drawScaledString(main.getConfigValues().getMessage(ConfigValues.Message.SETTING_SETTINGS), 0.8, defaultBlue, 1.5);
        if (main.isUsingLabymod() || (boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
            drawScaledString(main.getConfigValues().getMessage(ConfigValues.Message.MESSAGE_LABYMOD), 0.75, defaultBlue, 1);
        }
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }


    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonRegular) {
            Feature feature = ((ButtonRegular) abstractButton).getFeature();
            if (feature.getButtonType() == Feature.ButtonType.REGULAR) {
                if (feature == Feature.MANA_BAR) {
                    main.getConfigValues().setManaBarType(main.getConfigValues().getManaBarType().getNextType());
                } if (feature == Feature.HEALTH_BAR) {
                    main.getConfigValues().setHealthBarType(main.getConfigValues().getHealthBarType().getNextType());
                } if (feature == Feature.DEFENCE_ICON) {
                    main.getConfigValues().setDefenceIconType(main.getConfigValues().getDefenceIconType().getNextType());
                } else {
                    if (main.getConfigValues().getDisabledFeatures().contains(feature)) {
                        main.getConfigValues().getDisabledFeatures().remove(feature);
                    } else {
                        main.getConfigValues().getDisabledFeatures().add(feature);
                        if (feature == Feature.HIDE_FOOD_ARMOR_BAR) {
                            GuiIngameForge.renderArmor = true;
                        }
                    }
                }
            } else if (feature == Feature.SETTINGS) {
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main));
            } else if (feature == Feature.LANGUAGE) {
                main.getConfigValues().setLanguage(main.getConfigValues().getLanguage().getNextLanguage());
                main.getConfigValues().loadLanguageFile();
                main.getUtils().setFadingIn(false);
                Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main));
            }
        }
    }

    private void drawScaledString(String text, double yMultiplier, int color, double scale) {
        drawScaledString(text, yMultiplier, color, scale, 0, 0);
    }

    private void drawScaledString(String text, double yMultiplier, int color, double scale, int xOff, int yOff) {
        double x = width/2;
        double y = height*yMultiplier;
        GlStateManager.pushMatrix();
        double scaleMultiplier = 1F/scale;
        GlStateManager.scale(scale, scale, 1);
        drawCenteredString(fontRendererObj, text,
                (int)(x*scaleMultiplier)+xOff, (int)(y*scaleMultiplier)+yOff, color);
        GlStateManager.popMatrix();
    }

    private void addButton(double y, ConfigValues.Message message, Feature feature, int collumn) {
        String text = null;
        if (message != null) {
            text = main.getConfigValues().getMessage(message);
        }
        int halfWidth = width/2;
        int oneThird = width/3;
        int twoThirds = oneThird*2;
        int boxWidth = fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > WIDTH_LIMIT) boxWidth = WIDTH_LIMIT;
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
        buttonList.add(new ButtonRegular(0, x, y, text, main, feature,
                boxWidth, boxHeight));
    }


    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
    }
}
