package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.WIDTH_LIMIT;

public class LocationEditGui extends GuiScreen {

    private SkyblockAddons main;
    private boolean draggingManaBar = false;
    private boolean draggingManaText = false;
    private boolean draggingSkeleton = false;

    LocationEditGui(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public void initGui() {
        int boxWidth = 120;
        int boxHeight = 20;
        buttonList.add(new ButtonLocation(0, main, boxWidth, boxHeight, Feature.MANA_BAR));
        boxWidth = 50;
        buttonList.add(new ButtonLocation(1, main, boxWidth, boxHeight, Feature.SKELETON_BAR));
        buttonList.add(new ButtonLocation(2, main, boxWidth, boxHeight, Feature.MANA_TEXT));

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        String text = main.getConfigValues().getMessage(ConfigValues.Message.SETTING_RESET_LOCATIONS);
        boxWidth = fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > WIDTH_LIMIT) boxWidth = WIDTH_LIMIT;
        int x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        int y = scaledResolution.getScaledHeight()/2-boxHeight/2;
        buttonList.add(new ButtonRegular(0, x, y, text, main, Feature.RESET_LOCATION, boxWidth, boxHeight));
//        buttonList.add(new ButtonRegular(0, scaledResolution.getScaledWidth()/2-boxWidth/2, scaledResolution.getScaledHeight()/2-boxHeight/2,
//                main.getConfigValues().getMessage(ConfigValues.Message.SETTING_RESET_LOCATIONS), main, Feature.RESET_LOCATION, boxWidth, boxHeight));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float alphaMultiplier = 0.5F;
        int alpha = (int)(255*alphaMultiplier); // Alpha of the text will increase from 0 to 127 over 500ms.

        int startColor = new Color(0,0, 0, alpha).getRGB();
        int endColor = new Color(0,0, 0, (int)(alpha*1.5)).getRGB();
        drawGradientRect(0, 0, width, height, startColor, endColor);

        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }


    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonLocation) {
            ButtonLocation buttonLocation = (ButtonLocation)abstractButton;
            if (buttonLocation.getFeature() == Feature.MANA_TEXT) {
                draggingManaText = true;
            } else if (buttonLocation.getFeature() == Feature.MANA_BAR) {
                draggingManaBar = true;
            } else if (buttonLocation.getFeature() == Feature.SKELETON_BAR) {
                draggingSkeleton = true;
            }
        } else {
            ScaledResolution sr = new ScaledResolution(mc);
            main.getConfigValues().setCoords(Feature.MANA_BAR, width/2, sr.getScaledWidth(), height/2-40, sr.getScaledHeight());
//            main.getConfigValues().setManaBarX(width/2, sr.getScaledWidth());
//            main.getConfigValues().setManaBarY(height/2-40, sr.getScaledHeight());
            main.getConfigValues().setCoords(Feature.SKELETON_BAR, width/2-25, sr.getScaledWidth(), height/2-40, sr.getScaledHeight());
            main.getConfigValues().setCoords(Feature.MANA_TEXT, width/2-25, sr.getScaledWidth(), height/2-40, sr.getScaledHeight());
//            main.getConfigValues().setSkeletonBarX(width/2-25, sr.getScaledWidth());
//            main.getConfigValues().setSkeletonBarY(height/2-40, sr.getScaledHeight());
//            main.getConfigValues().setManaTextX(width/2-25, sr.getScaledWidth());
//            main.getConfigValues().setManaTextY(height/2-40, sr.getScaledHeight());
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        ScaledResolution sr = new ScaledResolution(mc);
        if (draggingManaText) {
            main.getConfigValues().setCoords(Feature.MANA_TEXT, mouseX-25, sr.getScaledWidth(),mouseY-10, sr.getScaledHeight());
//            main.getConfigValues().setManaTextX(mouseX-25, sr.getScaledWidth());
//            main.getConfigValues().setManaTextY(mouseY-10, sr.getScaledHeight());
        } else if (draggingManaBar) {
            main.getConfigValues().setCoords(Feature.MANA_BAR, mouseX, sr.getScaledWidth(),mouseY, sr.getScaledHeight());
//            main.getConfigValues().setManaBarX(mouseX, sr.getScaledWidth());
//            main.getConfigValues().setManaBarY(mouseY, sr.getScaledHeight());
        } else if (draggingSkeleton) {
            main.getConfigValues().setCoords(Feature.SKELETON_BAR, mouseX-25, sr.getScaledWidth(),mouseY-10, sr.getScaledHeight());
//            main.getConfigValues().setSkeletonBarX(mouseX-25, sr.getScaledWidth());
//            main.getConfigValues().setSkeletonBarY(mouseY-10, sr.getScaledHeight());
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        draggingManaBar = false;
        draggingSkeleton = false;
        draggingManaText = false;
    }

    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
        main.getUtils().setFadingIn(false);
        main.getPlayerListener().setOpenSettingsGUI(true);
    }
}
