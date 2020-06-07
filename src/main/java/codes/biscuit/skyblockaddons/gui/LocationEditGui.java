package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonColorWheel;
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
import java.util.EnumMap;
import java.util.Map;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.BUTTON_MAX_WIDTH;

public class LocationEditGui extends GuiScreen {

    private EditMode editMode = EditMode.RESCALE;
    private boolean showColorIcons = true;

    private SkyblockAddons main;
    // The feature that is currently being dragged, or null for nothing.
    private Feature dragging = null;

    private boolean resizing = false;
    private ButtonResize.Corner resizingCorner = null;

    private int originalHeight;
    private int originalWidth;

    private float originalScaleDenormalized;
    private int originalXOne;
    private int originalYOne;

    private int xOffset;
    private int yOffset;

    private int lastPage;
    private EnumUtils.GuiTab lastTab;

    private Map<Feature, ButtonLocation> buttonLocations = new EnumMap<>(Feature.class);

    private boolean closing = false;

    public LocationEditGui(SkyblockAddons main, int lastPage, EnumUtils.GuiTab lastTab) {
        this.main = main;
        this.lastPage = lastPage;
        this.lastTab = lastTab;
    }

    @Override
    public void initGui() {
        // Add all gui elements that can be edited to the gui.
        for (Feature feature : Feature.getGuiFeatures()) {
            if (!main.getConfigValues().isDisabled(feature)) { // Don't display features that have been disabled
                ButtonLocation buttonLocation = new ButtonLocation(main, feature);
                buttonList.add(buttonLocation);
                buttonLocations.put(feature, buttonLocation);
            }
        }

        if (this.editMode == EditMode.RESIZE_BARS) {
            addResizeButtonsToBars();
        } else if (this.editMode == EditMode.RESCALE) {
            addResizeButtonsToAllFeatures();
        }

        addColorWheelsToAllFeatures();

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        String text = Message.SETTING_RESET_LOCATIONS.getMessage();
        int boxWidth = MinecraftReflection.FontRenderer.getStringWidth(text)+10;
        int boxHeight = 20;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        int x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        int y = scaledResolution.getScaledHeight()/2-2-boxHeight-5-boxHeight;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.RESET_LOCATION));

        text = Feature.RESCALE_FEATURES.getMessage();
        boxWidth = MinecraftReflection.FontRenderer.getStringWidth(text)+10;
        boxHeight = 20;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        y = scaledResolution.getScaledHeight()/2-boxHeight;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.RESCALE_FEATURES));

        text = Feature.RESIZE_BARS.getMessage();
        boxWidth = MinecraftReflection.FontRenderer.getStringWidth(text)+10;
        boxHeight = 20;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        y = scaledResolution.getScaledHeight()/2+5;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.RESIZE_BARS));

        text = Feature.SHOW_COLOR_ICONS.getMessage();
        boxWidth = MinecraftReflection.FontRenderer.getStringWidth(text)+10;
        boxHeight = 20;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        y = scaledResolution.getScaledHeight()/2+5+boxHeight+5;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.SHOW_COLOR_ICONS));
    }

    private void clearAllResizeButtons() {
        buttonList.removeIf((button) -> button instanceof ButtonResize);
    }

    private void clearAllColorWheelButtons() {
        buttonList.removeIf((button) -> button instanceof ButtonColorWheel);
    }

    private void addResizeButtonsToAllFeatures() {
        clearAllResizeButtons();
        // Add all gui elements that can be edited to the gui.
        for (Feature feature : Feature.getGuiFeatures()) {
            if (!main.getConfigValues().isDisabled(feature)) { // Don't display features that have been disabled
                addResizeCorners(feature);
            }
        }
    }

    private void addResizeButtonsToBars() {
        clearAllResizeButtons();
        // Add all gui elements that can be edited to the gui.
        for (Feature feature : Feature.getGuiFeatures()) {
            if (!main.getConfigValues().isDisabled(feature)) { // Don't display features that have been disabled
                if (feature == Feature.HEALTH_BAR || feature == Feature.MANA_BAR) {
                    addResizeCorners(feature);
                }
            }
        }
    }

    private void addColorWheelsToAllFeatures() {
        for (ButtonLocation buttonLocation : buttonLocations.values()) {
            Feature feature = buttonLocation.getFeature();

            if (feature.getGuiFeatureData() == null || feature.getGuiFeatureData().getDefaultColor() == null) {
                continue;
            }

            EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(feature);

            int y = buttonLocation.getBoxYOne() + (buttonLocation.getBoxYTwo()-buttonLocation.getBoxYOne())/2 - ButtonColorWheel.getSize()/2;
            int x;

            if (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.BOTTOM_LEFT) {
                x = buttonLocation.getBoxXOne() + (buttonLocation.getBoxXTwo()-buttonLocation.getBoxXOne());
            } else {
                x = buttonLocation.getBoxXOne() - ButtonColorWheel.getSize();
            }

            buttonList.add(new ButtonColorWheel(x, y, feature));
        }
    }

    private void addResizeCorners(Feature feature) {
        buttonList.removeIf((button) -> button instanceof ButtonResize && ((ButtonResize)button).getFeature() == feature);

        float scale = main.getConfigValues().getGuiScale(feature);

        if (this.editMode == EditMode.RESIZE_BARS) {
            int barHeightExpansion = 2 * main.getConfigValues().getSizes(feature).getY();
            int height = 3 + barHeightExpansion;
            int barWidthExpansion = 9 * main.getConfigValues().getSizes(feature).getX();
            int width = 22 + barWidthExpansion;
            float x = main.getConfigValues().getActualX(feature);
            float y = main.getConfigValues().getActualY(feature);
            x /= scale;
            y /= scale;
            x -= (float) width / 2;
            y -= (float) height / 2;
            int intX = Math.round(x);
            int intY = Math.round(y);
            int boxXOne = intX - 4;
            int boxXTwo = intX + width + 5;
            int boxYOne = intY - 3;
            int boxYTwo = intY + height + 4;
            buttonList.add(new ButtonResize(boxXOne, boxYOne, feature, ButtonResize.Corner.TOP_LEFT));
            buttonList.add(new ButtonResize(boxXTwo, boxYOne, feature, ButtonResize.Corner.TOP_RIGHT));
            buttonList.add(new ButtonResize(boxXOne, boxYTwo, feature, ButtonResize.Corner.BOTTOM_LEFT));
            buttonList.add(new ButtonResize(boxXTwo, boxYTwo, feature, ButtonResize.Corner.BOTTOM_RIGHT));
        } else if (this.editMode == EditMode.RESCALE) {
            ButtonLocation buttonLocation = buttonLocations.get(feature);
            if (buttonLocation == null) {
                return;
            }

            int boxXOne = buttonLocation.getBoxXOne();
            int boxXTwo = buttonLocation.getBoxXTwo();
            int boxYOne = buttonLocation.getBoxYOne();
            int boxYTwo = buttonLocation.getBoxYTwo();
            buttonList.add(new ButtonResize(boxXOne, boxYOne, feature, ButtonResize.Corner.TOP_LEFT));
            buttonList.add(new ButtonResize(boxXTwo, boxYOne, feature, ButtonResize.Corner.TOP_RIGHT));
            buttonList.add(new ButtonResize(boxXOne, boxYTwo, feature, ButtonResize.Corner.BOTTOM_LEFT));
            buttonList.add(new ButtonResize(boxXTwo, boxYTwo, feature, ButtonResize.Corner.BOTTOM_RIGHT));
        }
    }

    private void recalculateResizeButtons() {
        for (GuiButton button : this.buttonList) {
            if (button instanceof ButtonResize) {
                ButtonResize buttonResize = (ButtonResize)button;
                ButtonResize.Corner corner = buttonResize.getCorner();
                Feature feature = buttonResize.getFeature();
                ButtonLocation buttonLocation = buttonLocations.get(feature);
                if (buttonLocation == null) {
                    continue;
                }

                int boxXOne = buttonLocation.getBoxXOne();
                int boxXTwo = buttonLocation.getBoxXTwo();
                int boxYOne = buttonLocation.getBoxYOne();
                int boxYTwo = buttonLocation.getBoxYTwo();

                if (feature == Feature.DEFENCE_ICON) {
                    boxXOne /= main.getConfigValues().getGuiScale(feature);
                    boxXTwo /= main.getConfigValues().getGuiScale(feature);
                    boxYOne /= main.getConfigValues().getGuiScale(feature);
                    boxYTwo /= main.getConfigValues().getGuiScale(feature);
                }

                if (corner == ButtonResize.Corner.TOP_LEFT) {
                    buttonResize.xPosition = boxXOne;
                    buttonResize.yPosition = boxYOne;
                } else if (corner == ButtonResize.Corner.TOP_RIGHT) {
                    buttonResize.xPosition = boxXTwo;
                    buttonResize.yPosition = boxYOne;
                } else if (corner == ButtonResize.Corner.BOTTOM_LEFT) {
                    buttonResize.xPosition = boxXOne;
                    buttonResize.yPosition = boxYTwo;
                } else if (corner == ButtonResize.Corner.BOTTOM_RIGHT) {
                    buttonResize.xPosition = boxXTwo;
                    buttonResize.yPosition = boxYTwo;
                }
            }
        }
    }

    private void recalculateColorWheels() {
        for (GuiButton button : this.buttonList) {
            if (button instanceof ButtonColorWheel) {
                ButtonColorWheel buttonColorWheel = (ButtonColorWheel)button;
                Feature feature = buttonColorWheel.getFeature();
                ButtonLocation buttonLocation = buttonLocations.get(feature);
                if (buttonLocation == null) {
                    continue;
                }

                EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(feature);
                int y = buttonLocation.getBoxYOne() + (buttonLocation.getBoxYTwo()-buttonLocation.getBoxYOne())/2 - ButtonColorWheel.getSize()/2;
                int x;

                if (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.BOTTOM_LEFT) {
                    x = buttonLocation.getBoxXOne() + (buttonLocation.getBoxXTwo()-buttonLocation.getBoxXOne());
                } else {
                    x = buttonLocation.getBoxXOne() - ButtonColorWheel.getSize();
                }

                buttonColorWheel.xPosition = x;
                buttonColorWheel.yPosition = y;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.editMode == EditMode.RESCALE) {
            recalculateResizeButtons();
        }
        recalculateColorWheels();

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
            Feature feature = buttonSolid.getFeature();
            if (feature == Feature.RESET_LOCATION) {
                main.getConfigValues().setAllCoordinatesToDefault();
                for (Feature guiFeature : Feature.getGuiFeatures()) {
                    if (!main.getConfigValues().isDisabled(guiFeature)) { // Don't display features that have been disabled
                        if (guiFeature == Feature.HEALTH_BAR || guiFeature == Feature.MANA_BAR) {
                            addResizeCorners(guiFeature);
                        }
                    }
                }
            } else if (feature == Feature.RESIZE_BARS) {
                if (editMode != EditMode.RESIZE_BARS) {
                    editMode = EditMode.RESIZE_BARS;
                    addResizeButtonsToBars();
                } else {
                    editMode = null;
                    clearAllResizeButtons();
                }

            } else if (feature == Feature.RESCALE_FEATURES) {
                if (editMode != EditMode.RESCALE) {
                    editMode = EditMode.RESCALE;
                    addResizeButtonsToAllFeatures();
                } else {
                    editMode = null;
                    clearAllResizeButtons();
                }
            } else if (feature == Feature.SHOW_COLOR_ICONS) {
                if (showColorIcons) {
                    showColorIcons = false;
                    clearAllColorWheelButtons();
                } else {
                    showColorIcons = true;
                    addColorWheelsToAllFeatures();
                }
            }
        } else if (abstractButton instanceof ButtonResize) {
            ButtonResize buttonResize = (ButtonResize)abstractButton;
            dragging = buttonResize.getFeature();
            resizing = true;
            xOffset = buttonResize.getLastMouseX();
            yOffset = buttonResize.getLastMouseY();
            resizingCorner = buttonResize.getCorner();

            if (this.editMode == EditMode.RESIZE_BARS) {
                CoordsPair sizes = main.getConfigValues().getSizes(dragging);
                originalWidth = sizes.getX();
                originalHeight = sizes.getY();
            } else if (this.editMode == EditMode.RESCALE) {
                ButtonLocation buttonLocation = buttonLocations.get(buttonResize.getFeature());
                if (buttonLocation == null) {
                    return;
                }

                float scale = main.getConfigValues().getGuiScale(dragging);

                originalXOne = Math.round(buttonLocation.getBoxXOne()*scale);
                int originalXTwo = Math.round(buttonLocation.getBoxXTwo() * scale);
                originalYOne = Math.round(buttonLocation.getBoxYOne()*scale);
                int originalYTwo = Math.round(buttonLocation.getBoxYTwo() * scale);

                if (dragging == Feature.DEFENCE_ICON) {
                    originalXOne /= main.getConfigValues().getGuiScale(dragging);
                    originalXTwo /= main.getConfigValues().getGuiScale(dragging);
                    originalYOne /= main.getConfigValues().getGuiScale(dragging);
                    originalYTwo /= main.getConfigValues().getGuiScale(dragging);
                }

                originalWidth = originalXTwo -originalXOne;
                originalHeight = originalYTwo -originalYOne;
                originalScaleDenormalized = main.getConfigValues().getGuiScale(dragging, true);
            }
        } else if (abstractButton instanceof ButtonColorWheel) {
            ButtonColorWheel buttonColorWheel = (ButtonColorWheel)abstractButton;

            closing = true;
            mc.displayGuiScreen(new ColorSelectionGui(buttonColorWheel.getFeature(), EnumUtils.GUIType.EDIT_LOCATIONS, lastTab, lastPage));
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
            int x = mouseX - xOffset;
            int y = mouseY - yOffset;
            if (this.editMode == EditMode.RESIZE_BARS) {
                x /= 15;
                y /= -7;
                x += originalWidth;
                y += originalHeight;
                if (x > 0) {
                    main.getConfigValues().setSizeX(dragging, x);
                }
                if (y > 0) {
                    main.getConfigValues().setSizeY(dragging, y);
                }
                addResizeCorners(dragging);
            } else if (this.editMode == EditMode.RESCALE) {
                ButtonLocation buttonLocation = buttonLocations.get(dragging);
                if (buttonLocation == null) {
                    return;
                }

                int middleX = originalXOne+originalWidth/2;
                int middleY = originalYOne+originalHeight/2;

                int xOffset = mouseX-middleX;
                int yOffset = mouseY-middleY;

                if (resizingCorner == ButtonResize.Corner.TOP_LEFT) {
                    xOffset *= -1;
                    yOffset *= -1;
                } else if (resizingCorner == ButtonResize.Corner.TOP_RIGHT) {
                    yOffset *= -1;
                } else if (resizingCorner == ButtonResize.Corner.BOTTOM_LEFT) {
                    xOffset *= -1;
                }

                float scaleX = xOffset/(originalWidth/2F);
                float scaleY = yOffset/(originalHeight/2F);

                float scalePercentage = Math.max(scaleX, scaleY);

                float newScale = originalScaleDenormalized *scalePercentage;

                float normalizedScale = ConfigValues.normalizeValueNoStep(newScale);

                main.getConfigValues().setGuiScale(dragging, normalizedScale);
                buttonLocation.drawButton(mc, mouseX, mouseY);
                recalculateResizeButtons();
            }
        } else if (dragging != null) {
            int x = mouseX-main.getConfigValues().getAnchorPoint(dragging).getX(sr.getScaledWidth());
            int y = mouseY-main.getConfigValues().getAnchorPoint(dragging).getY(sr.getScaledHeight());
            main.getConfigValues().setCoords(dragging, x-xOffset, y-yOffset);
            main.getConfigValues().setClosestAnchorPoint(dragging);
            if (dragging == Feature.HEALTH_BAR || dragging == Feature.MANA_BAR) {
                addResizeCorners(dragging);
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
        if (lastTab != null && !closing) {
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, lastPage, lastTab);
        }
    }

    private enum  EditMode {
        RESCALE,
        RESIZE_BARS
    }
}
