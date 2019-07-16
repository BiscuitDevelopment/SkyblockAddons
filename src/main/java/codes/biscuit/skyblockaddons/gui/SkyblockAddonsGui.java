package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.GuiIngameForge;

import java.awt.*;

public class SkyblockAddonsGui extends GuiScreen {

    private SkyblockAddons main;

    private long timeOpened = System.currentTimeMillis();

    public SkyblockAddonsGui(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public void initGui() {
        int halfWidth = width/2;
        int oneThird = width/3;
        int twoThirds = oneThird*2;
        int boxWidth = 130;
        int boxHeight = 20;
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.25, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_MAGMA_BOSS_WARNING), main, Feature.MAGMA_WARNING, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, halfWidth-(boxWidth/2), height*0.25, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_ITEM_DROP_CONFIRMATION), main, Feature.DROP_CONFIRMATION, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.33, null, main, Feature.MANA_BAR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, halfWidth-(boxWidth/2), height*0.33, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_HIDE_SKELETON_HAT_BONES), main, Feature.HIDE_BONES, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.41, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_SKELETON_HAT_BONES_BAR), main, Feature.SKELETON_BAR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, halfWidth-(boxWidth/2), height*0.41, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_HIDE_FOOD_AND_ARMOR), main, Feature.HIDE_FOOD_ARMOR_BAR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.49, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_FULL_INVENTORY_WARNING), main, Feature.FULL_INVENTORY_WARNING, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, twoThirds+30, height*0.25, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_MAGMA_BOSS_HEALTH_BAR), main, Feature.MAGMA_BOSS_BAR, boxWidth, boxHeight));
//        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.25, "Magma Boss Warning", main, Feature.MAGMA_WARNING, boxWidth, boxHeight));
//        buttonList.add(new ButtonRegular(0, twoThirds+30, height*0.25, "Item Drop Confirmation", main, Feature.DROP_CONFIRMATION, boxWidth, boxHeight));
//        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.33, null, main, Feature.MANA_BAR, boxWidth, boxHeight));
//        buttonList.add(new ButtonRegular(0, twoThirds+30, height*0.33, "Hide Skeleton Hat Bones", main, Feature.HIDE_BONES, boxWidth, boxHeight));
//        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.41, "Skeleton Hat Bones Bar", main, Feature.SKELETON_BAR, boxWidth, boxHeight));
//        buttonList.add(new ButtonRegular(0, twoThirds+30, height*0.41, "Hide Food & Armor Bar", main, Feature.HIDE_FOOD_ARMOR_BAR, boxWidth, boxHeight));
//        buttonList.add(new ButtonRegular(0, twoThirds+30, height*0.49, "Full Inventory Warning", main, Feature.FULL_INVENTORY_WARNING, boxWidth, boxHeight));
        boxWidth = 200;
        buttonList.add(new ButtonRegular(0, halfWidth-(boxWidth/2), height*0.49, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_DISABLE_EMBER_ROD_ABILITY), main, Feature.DISABLE_EMBER_ROD, boxWidth, boxHeight));
        boxWidth = 100;
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.65, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_WARNING_COLOR), main, Feature.WARNING_COLOR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, halfWidth-(boxWidth/2), height*0.65, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_CONFIRMATION_COLOR), main, Feature.CONFIRMATION_COLOR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.73, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_MANA_TEXT_COLOR), main, Feature.MANA_TEXT_COLOR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, halfWidth-(boxWidth/2), height*0.73, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_MANA_BAR_COLOR), main, Feature.MANA_BAR_COLOR, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-30, height*0.81, null, main, Feature.WARNING_TIME, boxWidth, boxHeight));
//        buttonList.add(new ButtonRegular(0, halfWidth-boxWidth-20, height*0.81, null, main, Feature.WARNING_TIME, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, halfWidth-(boxWidth/2), height*0.81, main.getConfigValues().getMessage(ConfigValues.Message.SETTING_EDIT_LOCATIONS), main, Feature.EDIT_LOCATIONS, boxWidth, boxHeight));
        buttonList.add(new ButtonSlider(0, twoThirds+30, height*0.65, boxWidth, boxHeight, main));//twoThirds+30, height*33));
        boxWidth = 20;
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-135, height*0.81, "+", main, Feature.ADD, boxWidth, boxHeight));
        buttonList.add(new ButtonRegular(0, oneThird-boxWidth-5, height*0.81, "-", main, Feature.SUBTRACT, boxWidth, boxHeight));
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long timeSinceOpen = System.currentTimeMillis() - timeOpened;
        float alphaMultiplier = 0.5F;
        int fadeMilis = 500;
        if (timeSinceOpen <= fadeMilis) {
            alphaMultiplier = (float)timeSinceOpen/(fadeMilis*2);
        }
        int alpha = (int)(255*alphaMultiplier); // Alpha of the text will increase from 0 to 127 over 500ms.

        int startColor = new Color(0,0, 0, alpha).getRGB(); // Black
        int endColor = new Color(0,0, 0, (int)(alpha*1.5)).getRGB(); // Orange
        drawGradientRect(0, 0, width, height, startColor, endColor);
        GlStateManager.enableBlend();

        GlStateManager.pushMatrix();
        float scale = 2.5F;
        float scaleMultiplier = 1F/scale; // Keeps the proportions of the text intact.
        GlStateManager.scale(scale, scale, scale);
        if (alpha < 4) alpha = 4; // Text under 4 alpha appear 100% transparent for some reason o.O
        drawCenteredString(fontRendererObj, EnumChatFormatting.WHITE+"SkyblockAddons",
                (int)(width/2*scaleMultiplier), (int)(height*0.12*scaleMultiplier), new Color(255,255,255, alpha*2).getRGB());
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        scale = 1.5F;
        scaleMultiplier = 1F/scale;
        GlStateManager.scale(scale, scale, scale);
        drawCenteredString(fontRendererObj, EnumChatFormatting.WHITE+"Settings",
                (int)(width/2*scaleMultiplier), (int)(height*0.58*scaleMultiplier), new Color(255,255,255, alpha*2).getRGB());
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }


    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonRegular) {
            Feature feature = ((ButtonRegular) abstractButton).getFeature();
            if (feature.getButtonType() == Feature.ButtonType.REGULAR) {
                if (feature == Feature.MANA_BAR) {
                    main.getConfigValues().setManaBarType(main.getConfigValues().getManaBarType().getNextType());
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
            } else if (feature.getButtonType() == Feature.ButtonType.COLOR) {
                main.getConfigValues().setColor(feature, main.getConfigValues().getColor(feature).getNextColor());
            } else if (feature.getButtonType() == Feature.ButtonType.MODIFY) {
                if (feature == Feature.ADD) {
                    if (main.getConfigValues().getWarningSeconds() < 99) {
                        main.getConfigValues().setWarningSeconds(main.getConfigValues().getWarningSeconds() + 1);
                    }
                } else {
                    if (main.getConfigValues().getWarningSeconds() > 1) {
                        main.getConfigValues().setWarningSeconds(main.getConfigValues().getWarningSeconds() - 1);
                    }
                }
            } else if (feature == Feature.EDIT_LOCATIONS) {
                Minecraft.getMinecraft().displayGuiScreen(null);
                Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main));
            }
        }
    }

    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
    }
}
