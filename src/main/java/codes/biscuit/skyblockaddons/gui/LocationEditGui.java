package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonResize;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSolid;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
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
    private boolean resizing = false;
    private int barOriginalHeight = 0;
    private int barOriginalWidth = 0;
    private Feature dragging = null;
    private int xOffset = 0;
    private int yOffset = 0;

    private int lastPage;
    private EnumUtils.GuiTab lastTab;
    private String lastText;

    public LocationEditGui(SkyblockAddons main, int lastPage, EnumUtils.GuiTab lastTab, String lastText) {
        this.main = main;
        this.lastPage = lastPage;
        this.lastTab = lastTab;
        this.lastText = lastText;
    }

    @Override
    public void initGui() {
        // Add all gui elements that can be edited to the gui.
        for (Feature feature : Feature.getGuiFeatures()) {
            if (!main.getConfigValues().isDisabled(feature)) { // Don't display features that have been disabled
                buttonList.add(new ButtonLocation(main, feature));
                if (feature == Feature.HEALTH_BAR || feature == Feature.MANA_BAR) {
                    addResizeButtons(feature);
                }
            }
        }

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        String text = Message.SETTING_RESET_LOCATIONS.getMessage();
        int boxWidth = MinecraftReflection.FontRenderer.getStringWidth(text)+10;
        int boxHeight = 20;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        int x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        int y = scaledResolution.getScaledHeight()/2-boxHeight/2;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.RESET_LOCATION));
    }

    private void addResizeButtons(Feature feature) {
        buttonList.removeIf((button) -> button instanceof ButtonResize && ((ButtonResize)button).getFeature() == feature);
        float scale = main.getConfigValues().getGuiScale(feature);
        int barHeightExpansion = 2*main.getConfigValues().getSizes(feature).getY();
        int height = 3+barHeightExpansion;
        int barWidthExpansion = 9*main.getConfigValues().getSizes(feature).getX();
        int width = 22+barWidthExpansion;
        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);
        x/=scale;
        y/=scale;
        x-=(float)width/2;
        y-=(float)height/2;
        int intX = Math.round(x);
        int intY = Math.round(y);
        int boxXOne = intX-4;
        int boxXTwo = intX+width+5;
        int boxYOne = intY-3;
        int boxYTwo = intY+height+4;
        buttonList.add(new ButtonResize(boxXOne, boxYOne, feature));
        buttonList.add(new ButtonResize(boxXTwo, boxYOne, feature));
        buttonList.add(new ButtonResize(boxXOne, boxYTwo, feature));
        buttonList.add(new ButtonResize(boxXTwo, boxYTwo, feature));
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
            int color = ChatFormatting.RED.getColor(127).getRGB();
            Feature lastHovered = ButtonLocation.getLastHoveredFeature();
            if (lastHovered != null && main.getConfigValues().getAnchorPoint(lastHovered) == anchorPoint) {
                color = ChatFormatting.YELLOW.getColor(127).getRGB();
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
        } else if (abstractButton instanceof ButtonSolid) {
            ButtonSolid buttonSolid = (ButtonSolid)abstractButton;
            if (buttonSolid.getFeature() == Feature.RESET_LOCATION) {
                main.getConfigValues().setAllCoordinatesToDefault();
                for (Feature feature : Feature.getGuiFeatures()) {
                    if (!main.getConfigValues().isDisabled(feature)) { // Don't display features that have been disabled
                        if (feature == Feature.HEALTH_BAR || feature == Feature.MANA_BAR) {
                            addResizeButtons(feature);
                        }
                    }
                }
            }
        } else if (abstractButton instanceof ButtonResize) {
            ButtonResize buttonResize = (ButtonResize)abstractButton;
            dragging = buttonResize.getFeature();
            xOffset = buttonResize.getLastMouseX();
            yOffset = buttonResize.getLastMouseY();
            CoordsPair sizes = main.getConfigValues().getSizes(dragging);
            barOriginalWidth = sizes.getX();
            barOriginalHeight = sizes.getY();
            resizing = true;
        }
    }

    /**
     * Set the coordinates when the mouse moves.
     */
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        ScaledResolution sr = new ScaledResolution(mc);
        if (resizing) {
            int x = (mouseX-xOffset)/15;
            int y = (mouseY-yOffset)/7;
            y = -y;
            x+=barOriginalWidth;
            y+=barOriginalHeight;
            if (x > 0) {
                main.getConfigValues().setSizeX(dragging, x);
            }
            if (y > 0) {
                main.getConfigValues().setSizeY(dragging, y);
            }

            addResizeButtons(dragging);
        } else if (dragging != null) {
            int x = mouseX-main.getConfigValues().getAnchorPoint(dragging).getX(sr.getScaledWidth());
            int y = mouseY-main.getConfigValues().getAnchorPoint(dragging).getY(sr.getScaledHeight());
            main.getConfigValues().setCoords(dragging, x-xOffset, y-yOffset);
            main.getConfigValues().setClosestAnchorPoint(dragging);
            if (dragging == Feature.HEALTH_BAR || dragging == Feature.MANA_BAR) {
                addResizeButtons(dragging);
            }
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
        resizing = false;
    }

    /**
     * Open up the last GUI (main), and save the config.
     */
    @Override
    public void onGuiClosed() {
        main.getConfigValues().saveConfig();
        if (lastTab != null) {
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab, lastText);
        }
    }
}
