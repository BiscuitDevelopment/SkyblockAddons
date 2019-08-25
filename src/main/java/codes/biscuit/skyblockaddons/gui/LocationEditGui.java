package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSolid;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.BUTTON_MAX_WIDTH;

public class LocationEditGui extends GuiScreen {

    private SkyblockAddons main;
    // The feature that is currently being dragged, or null for nothing.
    private Feature dragging = null;
    private int xOffset = 0;
    private int yOffset = 0;
    private boolean cancelScreenReturn = false;

    public LocationEditGui(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public void initGui() {
        // Add all gui elements that can be edited to the gui.
        Feature[] features = {Feature.MANA_BAR, Feature.HEALTH_BAR, Feature.SKELETON_BAR, Feature.MANA_TEXT,
                Feature.HEALTH_TEXT, Feature.DEFENCE_ICON, Feature.DEFENCE_TEXT, Feature.DEFENCE_PERCENTAGE,
                Feature.HEALTH_UPDATES, Feature.DARK_AUCTION_TIMER, Feature.MAGMA_BOSS_TIMER, Feature.ITEM_PICKUP_LOG};
        for (Feature feature : features) {
            if (!main.getConfigValues().isRemoteDisabled(feature)) { // Don't display features that I have disabled
                buttonList.add(new ButtonLocation(main, feature));
            }
        }

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        String text = Message.SETTING_RESET_LOCATIONS.getMessage();
        int boxWidth = fontRendererObj.getStringWidth(text)+10;
        int boxHeight = 20;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        int x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        int y = scaledResolution.getScaledHeight()/2-boxHeight/2;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.RESET_LOCATION));
//        text = Message.SETTING_ANCHOR_POINT.getMessage();
//        boxWidth = fontRendererObj.getStringWidth(text)+10;
//        boxHeight = 20;
//        x = scaledResolution.getScaledWidth()/2-boxWidth/2;
//        y = scaledResolution.getScaledHeight()/2-boxHeight/2;
////        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
//        y-=25;
//        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.ANCHOR_POINT));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int startColor = new Color(0,0, 0, 127).getRGB();
        int endColor = new Color(0,0, 0, 180).getRGB();
        drawGradientRect(0, 0, width, height, startColor, endColor);
        for (EnumUtils.AnchorPoint anchorPoint : EnumUtils.AnchorPoint.values()) {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int x = anchorPoint.getX(sr.getScaledWidth());
            int y = anchorPoint.getY(sr.getScaledHeight());
            int color = ConfigColor.RED.getColor(127);
            Feature lastHovered = ButtonLocation.getLastHoveredFeature();
            if (lastHovered != null && main.getConfigValues().getAnchorPoint(lastHovered) == anchorPoint) {
                color = ConfigColor.YELLOW.getColor(127);
            }
            Gui.drawRect(x-4, y-4, x+4, y+4, color);
        }
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.
    }

    /**
     * If button is pressed, update the currently dragged button.
     * Otherwise, they clicked the reset button, so reset the coordinates.
     */
    @Override
    protected void actionPerformed(GuiButton abstractButton) {
        if (abstractButton instanceof ButtonLocation) {
            ButtonLocation buttonLocation = (ButtonLocation)abstractButton;
            dragging = buttonLocation.getFeature();
            xOffset = buttonLocation.getLastMouseX()-main.getConfigValues().getActualX(buttonLocation.getFeature());
            yOffset = buttonLocation.getLastMouseY()-main.getConfigValues().getActualY(buttonLocation.getFeature());
        } else {
            ButtonSolid buttonSolid = (ButtonSolid)abstractButton;
            if (buttonSolid.getFeature() == Feature.RESET_LOCATION) {
                main.getConfigValues().setAllCoordinatesToDefault();
            }// else if (buttonSolid.getFeature() == Feature.ANCHOR_POINT) {
//                main.getConfigValues().setNextAnchorPoint(ButtonLocation.getLastHoveredFeature());
//                cancelScreenReturn = true;
//                Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main));
//                cancelScreenReturn = false;
//            }
        }
    }

    /**
     * Set the coordinates when the mouse moves.
     */
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        ScaledResolution sr = new ScaledResolution(mc);
        if (dragging != null) {
            int x = mouseX-main.getConfigValues().getAnchorPoint(dragging).getX(sr.getScaledWidth());
            int y = mouseY-main.getConfigValues().getAnchorPoint(dragging).getY(sr.getScaledHeight());
            main.getConfigValues().setCoords(dragging, x-xOffset, y-yOffset);
            main.getConfigValues().setClosestAnchorPoint(dragging);
        }
    }

    /**
     * Allow moving the last hovered feature with arrow keys.
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        Feature hoveredFeature = ButtonLocation.getLastHoveredFeature();
        if (hoveredFeature != null) {
            int xOffset = 0;
            int yOffset = 0;
            if (keyCode == Keyboard.KEY_LEFT) {
                xOffset--;
            } else if (keyCode == Keyboard.KEY_UP) {
                yOffset--;
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                xOffset++;
            } else if (keyCode == Keyboard.KEY_DOWN) {
                yOffset++;
            }
            if (keyCode == Keyboard.KEY_A) {
                xOffset-= 10;
            } else if (keyCode == Keyboard.KEY_W) {
                yOffset-= 10;
            } else if (keyCode == Keyboard.KEY_D) {
                xOffset+= 10;
            } else if (keyCode == Keyboard.KEY_S) {
                yOffset+= 10;
            }
            main.getConfigValues().setCoords(hoveredFeature, main.getConfigValues().getRelativeCoords(hoveredFeature).getX()+xOffset,
                    main.getConfigValues().getRelativeCoords(hoveredFeature).getY()+yOffset);
        }
    }

    /**
     * Reset the dragged feature when the mouse is released.
     */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = null;
    }

    /**
     * Open up the last GUI (main), and save the config.
     */
    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
        if (!cancelScreenReturn) {
            main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.MAIN);
        }
    }
}
