package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.WIDTH_LIMIT;

public class LocationEditGui extends GuiScreen {

    private SkyblockAddons main;
    private Feature dragging = null;

    LocationEditGui(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public void initGui() {
        int boxWidth = 120;
        int boxHeight = 20;
        buttonList.add(new ButtonLocation(main, boxWidth, boxHeight, Feature.MANA_BAR));
        buttonList.add(new ButtonLocation(main, boxWidth, boxHeight, Feature.HEALTH_BAR));
        boxWidth = 50;
        buttonList.add(new ButtonLocation(main, boxWidth, boxHeight, Feature.SKELETON_BAR));
        buttonList.add(new ButtonLocation(main, boxWidth, boxHeight, Feature.MANA_TEXT));
        buttonList.add(new ButtonLocation(main, boxWidth, boxHeight, Feature.HEALTH_TEXT));
        buttonList.add(new ButtonLocation(main, boxWidth, boxHeight, Feature.DEFENCE_ICON));
        buttonList.add(new ButtonLocation(main, boxWidth, boxHeight, Feature.DEFENCE_TEXT));
        buttonList.add(new ButtonLocation(main, boxWidth, boxHeight, Feature.DEFENCE_PERCENTAGE));

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        String text = main.getConfigValues().getMessage(Message.SETTING_RESET_LOCATIONS);
        boxWidth = fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > WIDTH_LIMIT) boxWidth = WIDTH_LIMIT;
        int x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        int y = scaledResolution.getScaledHeight()/2-boxHeight/2;
        buttonList.add(new ButtonRegular(0, x, y, text, main, Feature.RESET_LOCATION, boxWidth, boxHeight));
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
            dragging = buttonLocation.getFeature();
        } else {
            main.getConfigValues().setAllCoordinatesToDefault();
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        ScaledResolution sr = new ScaledResolution(mc);
        if (dragging != null) {
            if (dragging == Feature.MANA_BAR || dragging == Feature.HEALTH_BAR) {
                main.getConfigValues().setCoords(dragging, mouseX, sr.getScaledWidth(), mouseY, sr.getScaledHeight());
            } else if (dragging == Feature.DEFENCE_ICON) {
                main.getConfigValues().setCoords(dragging, mouseX, sr.getScaledWidth()+5, mouseY, sr.getScaledHeight()+5);
            } else {
                main.getConfigValues().setCoords(dragging, mouseX-25, sr.getScaledWidth(),mouseY-10, sr.getScaledHeight());
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = null;
    }

    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
        main.getPlayerListener().setOpenMainGUI(true);
    }
}
