package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class LocationEditGui extends GuiScreen {

    private SkyblockAddons main;
    private boolean dragging = false;

    LocationEditGui(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public void initGui() {
        int halfWidth = width/2;
        int boxWidth = 120;
        int boxHeight = 20;
        buttonList.add(new ButtonLocation(0, halfWidth-boxWidth-30, height*0.25, "Move This", main, boxWidth, boxHeight));
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        buttonList.add(new ButtonRegular(0, scaledResolution.getScaledWidth()/2-boxWidth/2, scaledResolution.getScaledHeight()/2-boxHeight/2,
                "Reset Location", main, Feature.RESET_LOCATION, boxWidth, boxHeight));
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
            dragging = true;
        } else {
            ScaledResolution sr = new ScaledResolution(mc);
            main.getConfigValues().setManaBarX(width/2-60, sr.getScaledWidth());
            main.getConfigValues().setManaBarY(height/2-40, sr.getScaledWidth());
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (dragging) {
            ScaledResolution sr = new ScaledResolution(mc);
            main.getConfigValues().setManaBarX(mouseX-60, sr.getScaledWidth());
            main.getConfigValues().setManaBarY(mouseY-10, sr.getScaledHeight());
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = false;
    }

    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
    }
}
