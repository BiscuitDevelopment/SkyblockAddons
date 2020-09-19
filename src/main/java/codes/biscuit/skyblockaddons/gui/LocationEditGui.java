package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonColorWheel;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonResize;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSolid;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.objects.IntPair;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui.BUTTON_MAX_WIDTH;

public class LocationEditGui extends GuiScreen {

    private EditMode editMode = EditMode.RESCALE;
    private boolean showColorIcons = true;
    private boolean enableSnapping = true;

    private SkyblockAddons main = SkyblockAddons.getInstance();
    // The feature that is currently being dragged, or null for nothing.
    private Feature dragging;

    private boolean resizing;
    private ButtonResize.Corner resizingCorner;

    private int originalHeight;
    private int originalWidth;

    private float xOffset;
    private float yOffset;

    private int lastPage;
    private EnumUtils.GuiTab lastTab;

    private Map<Feature, ButtonLocation> buttonLocations = new EnumMap<>(Feature.class);

    private boolean closing = false;

    private static final int SNAPPING_RADIUS = 120;
    private static final int SNAP_PULL = 1;

    public LocationEditGui(int lastPage, EnumUtils.GuiTab lastTab) {
        this.lastPage = lastPage;
        this.lastTab = lastTab;
    }

    @Override
    public void initGui() {
        // Add all gui elements that can be edited to the gui.
        for (Feature feature : Feature.getGuiFeatures()) {
            if ((feature.getGuiFeatureData() == null || feature.getGuiFeatureData().getDrawType() != EnumUtils.DrawType.TEXT) &&
                    !main.getConfigValues().isDisabled(feature)) { // Don't display features that have been disabled
                ButtonLocation buttonLocation = new ButtonLocation(feature);
                buttonList.add(buttonLocation);
                buttonLocations.put(feature, buttonLocation);
            }
        }
        for (Feature feature : Feature.getGuiFeatures()) {
            if (feature.getGuiFeatureData() != null && feature.getGuiFeatureData().getDrawType() == EnumUtils.DrawType.TEXT &&
                    !main.getConfigValues().isDisabled(feature)) { // Don't display features that have been disabled
                ButtonLocation buttonLocation = new ButtonLocation(feature);
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
        int boxHeight = 20;
        int numButtons = 5;
        int y = scaledResolution.getScaledHeight()/2;
        if (numButtons % 2 == 0) {
            y -= Math.round((numButtons/2F) * (boxHeight+5)) - 2.5;
        } else {
            y -= Math.round(((numButtons-1)/2F) * (boxHeight+5)) + 10;
        }

        String text = Message.SETTING_RESET_LOCATIONS.getMessage();
        int boxWidth = mc.fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        int x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.RESET_LOCATION));

        text = Feature.RESCALE_FEATURES.getMessage();
        boxWidth = mc.fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        y += boxHeight + 5;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.RESCALE_FEATURES));

        text = Feature.RESIZE_BARS.getMessage();
        boxWidth = mc.fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        y += boxHeight + 5;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.RESIZE_BARS));

        text = Feature.SHOW_COLOR_ICONS.getMessage();
        boxWidth = mc.fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        y += boxHeight + 5;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.SHOW_COLOR_ICONS));

        text = Feature.ENABLE_FEATURE_SNAPPING.getMessage();
        boxWidth = mc.fontRendererObj.getStringWidth(text)+10;
        if (boxWidth > BUTTON_MAX_WIDTH) boxWidth = BUTTON_MAX_WIDTH;
        x = scaledResolution.getScaledWidth()/2-boxWidth/2;
        y += boxHeight + 5;
        buttonList.add(new ButtonSolid(x, y, boxWidth, boxHeight, text, main, Feature.ENABLE_FEATURE_SNAPPING));
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

            float y = buttonLocation.getBoxYOne() + (buttonLocation.getBoxYTwo()-buttonLocation.getBoxYOne())/2F - ButtonColorWheel.getSize()/2F;
            float x;

            if (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.BOTTOM_LEFT) {
                x = buttonLocation.getBoxXOne() + (buttonLocation.getBoxXTwo()-buttonLocation.getBoxXOne());
            } else {
                x = buttonLocation.getBoxXOne() - ButtonColorWheel.getSize();
            }

            buttonList.add(new ButtonColorWheel(Math.round(x), Math.round(y), feature));
        }
    }

    private void addResizeCorners(Feature feature) {
        buttonList.removeIf((button) -> button instanceof ButtonResize && ((ButtonResize)button).getFeature() == feature);

        ButtonLocation buttonLocation = buttonLocations.get(feature);
        if (buttonLocation == null) {
            return;
        }

        float boxXOne = buttonLocation.getBoxXOne();
        float boxXTwo = buttonLocation.getBoxXTwo();
        float boxYOne = buttonLocation.getBoxYOne();
        float boxYTwo = buttonLocation.getBoxYTwo();
        buttonList.add(new ButtonResize(boxXOne, boxYOne, feature, ButtonResize.Corner.TOP_LEFT));
        buttonList.add(new ButtonResize(boxXTwo, boxYOne, feature, ButtonResize.Corner.TOP_RIGHT));
        buttonList.add(new ButtonResize(boxXOne, boxYTwo, feature, ButtonResize.Corner.BOTTOM_LEFT));
        buttonList.add(new ButtonResize(boxXTwo, boxYTwo, feature, ButtonResize.Corner.BOTTOM_RIGHT));
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

                float boxXOne = buttonLocation.getBoxXOne();
                float boxXTwo = buttonLocation.getBoxXTwo();
                float boxYOne = buttonLocation.getBoxYOne();
                float boxYTwo = buttonLocation.getBoxYTwo();

                if (feature == Feature.DEFENCE_ICON) {
                    boxXOne /= main.getConfigValues().getGuiScale(feature);
                    boxXTwo /= main.getConfigValues().getGuiScale(feature);
                    boxYOne /= main.getConfigValues().getGuiScale(feature);
                    boxYTwo /= main.getConfigValues().getGuiScale(feature);
                }

                if (corner == ButtonResize.Corner.TOP_LEFT) {
                    buttonResize.x = boxXOne;
                    buttonResize.y = boxYOne;
                } else if (corner == ButtonResize.Corner.TOP_RIGHT) {
                    buttonResize.x = boxXTwo;
                    buttonResize.y = boxYOne;
                } else if (corner == ButtonResize.Corner.BOTTOM_LEFT) {
                    buttonResize.x = boxXOne;
                    buttonResize.y = boxYTwo;
                } else if (corner == ButtonResize.Corner.BOTTOM_RIGHT) {
                    buttonResize.x = boxXTwo;
                    buttonResize.y = boxYTwo;
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
                float y = buttonLocation.getBoxYOne() + (buttonLocation.getBoxYTwo()-buttonLocation.getBoxYOne())/2F - ButtonColorWheel.getSize()/2F;
                float x;

                if (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.BOTTOM_LEFT) {
                    x = buttonLocation.getBoxXTwo() + 2;
                } else {
                    x = buttonLocation.getBoxXOne() - ButtonColorWheel.getSize() - 2;
                }

                buttonColorWheel.x = x;
                buttonColorWheel.y = y;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Snap[] snaps = checkSnapping();

        onMouseMove(mouseX, mouseY, snaps);

        if (this.editMode == EditMode.RESCALE) {
            recalculateResizeButtons();
        }
        recalculateColorWheels();

        int startColor = new Color(0,0, 0, 64).getRGB();
        int endColor = new Color(0,0, 0, 128).getRGB();
        drawGradientRect(0, 0, width, height, startColor, endColor);
        for (EnumUtils.AnchorPoint anchorPoint : EnumUtils.AnchorPoint.values()) {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int x = anchorPoint.getX(sr.getScaledWidth());
            int y = anchorPoint.getY(sr.getScaledHeight());
            int color = ColorCode.RED.getColor(127).getRGB();
            Feature lastHovered = ButtonLocation.getLastHoveredFeature();
            if (lastHovered != null && main.getConfigValues().getAnchorPoint(lastHovered) == anchorPoint) {
                color = ColorCode.YELLOW.getColor(127).getRGB();
            }
            Gui.drawRect(x-4, y-4, x+4, y+4, color);
        }
        super.drawScreen(mouseX, mouseY, partialTicks); // Draw buttons.

        if (snaps != null) {
            for (Snap snap : snaps) {
                if (snap != null) {
                    float left = snap.getRectangle().get(Edge.LEFT);
                    float top = snap.getRectangle().get(Edge.TOP);
                    float right = snap.getRectangle().get(Edge.RIGHT);
                    float bottom = snap.getRectangle().get(Edge.BOTTOM);

                    if (snap.getWidth() < 0.5) {
                        float averageX = (left+right)/2;
                        left = averageX-0.25F;
                        right = averageX+0.25F;
                    }
                    if (snap.getHeight() < 0.5) {
                        float averageY = (top+bottom)/2;
                        top = averageY-0.25F;
                        bottom = averageY+0.25F;
                    }

                    if ((right-left) == 0.5 || (bottom-top) == 0.5) {
                        main.getUtils().drawRect(left, top, right, bottom, 0xFF00FF00);
                    } else {
                        main.getUtils().drawRect(left, top, right, bottom, 0xFFFF0000);
                    }
                }
            }
        }
    }

    public Snap[] checkSnapping() {
        if (!enableSnapping) return null;

        if (dragging != null) {
            ButtonLocation thisButton = buttonLocations.get(dragging);
            if (thisButton == null) {
                return null;
            }

            Snap horizontalSnap = null;
            Snap verticalSnap = null;

            for (Map.Entry<Feature, ButtonLocation> buttonLocationEntry : this.buttonLocations.entrySet()) {
                ButtonLocation otherButton = buttonLocationEntry.getValue();

                if (otherButton == thisButton) continue;

                for (Edge otherEdge : Edge.getHorizontalEdges()) {
                    for (Edge thisEdge : Edge.getHorizontalEdges()) {

                        float deltaX = otherEdge.getCoordinate(otherButton) - thisEdge.getCoordinate(thisButton);

                        if (Math.abs(deltaX) <= SNAP_PULL) {
                            float deltaY = Edge.TOP.getCoordinate(otherButton) - Edge.TOP.getCoordinate(thisButton);

                            float topY;
                            float bottomY;
                            if (deltaY > 0) {
                                topY = Edge.BOTTOM.getCoordinate(thisButton);
                                bottomY = Edge.TOP.getCoordinate(otherButton);
                            } else {
                                topY = Edge.BOTTOM.getCoordinate(otherButton);
                                bottomY = Edge.TOP.getCoordinate(thisButton);
                            }

                            float snapX = otherEdge.getCoordinate(otherButton);
                            Snap thisSnap = new Snap(otherEdge.getCoordinate(otherButton), topY, thisEdge.getCoordinate(thisButton), bottomY, thisEdge, otherEdge, snapX);

                            if (thisSnap.getHeight() < SNAPPING_RADIUS) {
                                if (horizontalSnap == null || thisSnap.getHeight() < horizontalSnap.getHeight()) {
                                    if (main.isDevMode()) {
                                        main.getUtils().drawRect(snapX - 0.5, 0, snapX + 0.5, mc.displayHeight, 0xFF0000FF);
                                    }
                                    horizontalSnap = thisSnap;
                                }
                            }
                        }
                    }
                }

                for (Edge otherEdge : Edge.getVerticalEdges()) {
                    for (Edge thisEdge : Edge.getVerticalEdges()) {

                        float deltaY = otherEdge.getCoordinate(otherButton) - thisEdge.getCoordinate(thisButton);

                        if (Math.abs(deltaY) <= SNAP_PULL) {
                            float deltaX = Edge.LEFT.getCoordinate(otherButton) - Edge.LEFT.getCoordinate(thisButton);

                            float leftX;
                            float rightX;
                            if (deltaX > 0) {
                                leftX = Edge.RIGHT.getCoordinate(thisButton);
                                rightX = Edge.LEFT.getCoordinate(otherButton);
                            } else {
                                leftX = Edge.RIGHT.getCoordinate(otherButton);
                                rightX = Edge.LEFT.getCoordinate(thisButton);
                            }
                            float snapY = otherEdge.getCoordinate(otherButton);
                            Snap thisSnap = new Snap(leftX, otherEdge.getCoordinate(otherButton), rightX, thisEdge.getCoordinate(thisButton), thisEdge, otherEdge, snapY);

                            if (thisSnap.getWidth() < SNAPPING_RADIUS) {
                                if (verticalSnap == null || thisSnap.getWidth() < verticalSnap.getWidth()) {
                                    if (main.isDevMode()) {
                                        main.getUtils().drawRect(0, snapY - 0.5, mc.displayWidth, snapY + 0.5, 0xFF0000FF);
                                    }
                                    verticalSnap = thisSnap;
                                }
                            }
                        }
                    }
                }
            }

            return new Snap[] {horizontalSnap, verticalSnap};
        }

        return null;
    }

    enum Edge {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,

        HORIZONTAL_MIDDLE,
        VERTICAL_MIDDLE,
        ;

        @Getter private static final Set<Edge> verticalEdges = Sets.newHashSet(TOP, BOTTOM, HORIZONTAL_MIDDLE);
        @Getter private static final Set<Edge> horizontalEdges = Sets.newHashSet(LEFT, RIGHT, VERTICAL_MIDDLE);

        public float getCoordinate(ButtonLocation button) {
            switch (this) {
                case LEFT:
                    return button.getBoxXOne() * button.getScale();
                case TOP:
                    return button.getBoxYOne() * button.getScale();
                case RIGHT:
                    return button.getBoxXTwo() * button.getScale();
                case BOTTOM:
                    return button.getBoxYTwo() * button.getScale();
                case HORIZONTAL_MIDDLE:
                    return TOP.getCoordinate(button)+(BOTTOM.getCoordinate(button)-TOP.getCoordinate(button))/2F;
                case VERTICAL_MIDDLE:
                    return LEFT.getCoordinate(button)+(RIGHT.getCoordinate(button)-LEFT.getCoordinate(button))/2F;
                default:
                    return 0;
            }
        }
    }

    @Getter
    static class Snap {

        private Edge thisSnapEdge;
        private Edge otherSnapEdge;
        private float snapValue;
        private Map<Edge, Float> rectangle = new EnumMap<>(Edge.class);

        public Snap(float left, float top, float right, float bottom, Edge thisSnapEdge, Edge otherSnapEdge, float snapValue) {
            rectangle.put(Edge.LEFT, left);
            rectangle.put(Edge.TOP, top);
            rectangle.put(Edge.RIGHT, right);
            rectangle.put(Edge.BOTTOM, bottom);

            rectangle.put(Edge.HORIZONTAL_MIDDLE, top+getHeight()/2);
            rectangle.put(Edge.VERTICAL_MIDDLE, left+getWidth()/2);

            this.otherSnapEdge = otherSnapEdge;
            this.thisSnapEdge = thisSnapEdge;
            this.snapValue = snapValue;
        }

        public float getHeight() {
            return rectangle.get(Edge.BOTTOM) - rectangle.get(Edge.TOP);
        }

        public float getWidth() {
            return rectangle.get(Edge.RIGHT) - rectangle.get(Edge.LEFT);
        }
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

            ScaledResolution sr = new ScaledResolution(mc);
            float minecraftScale = sr.getScaleFactor();
            float floatMouseX = Mouse.getX() / minecraftScale;
            float floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale;

            xOffset = floatMouseX-main.getConfigValues().getActualX(buttonLocation.getFeature());
            yOffset = floatMouseY-main.getConfigValues().getActualY(buttonLocation.getFeature());
        } else if (abstractButton instanceof ButtonSolid) {
            ButtonSolid buttonSolid = (ButtonSolid)abstractButton;
            Feature feature = buttonSolid.getFeature();
            if (feature == Feature.RESET_LOCATION) {
                main.getConfigValues().setAllCoordinatesToDefault();
                main.getConfigValues().putDefaultBarSizes();
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
            } else if (feature == Feature.ENABLE_FEATURE_SNAPPING) {
                enableSnapping = !enableSnapping;
            }
        } else if (abstractButton instanceof ButtonResize) {
            ButtonResize buttonResize = (ButtonResize)abstractButton;
            dragging = buttonResize.getFeature();
            resizing = true;

            ScaledResolution sr = new ScaledResolution(mc);
            float minecraftScale = sr.getScaleFactor();
            float floatMouseX = Mouse.getX() / minecraftScale;
            float floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale;

            float scale = SkyblockAddons.getInstance().getConfigValues().getGuiScale(buttonResize.getFeature());
            if (editMode == EditMode.RESCALE) {
                xOffset = (floatMouseX-buttonResize.getX()*scale)/scale;
                yOffset = (floatMouseY-buttonResize.getY()*scale)/scale;
            } else {
                xOffset = floatMouseX;
                yOffset = floatMouseY;
            }

            resizingCorner = buttonResize.getCorner();

            if (this.editMode == EditMode.RESIZE_BARS) {
                IntPair sizes = main.getConfigValues().getSizes(dragging);
                originalWidth = sizes.getX();
                originalHeight = sizes.getY();
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
    protected void onMouseMove(int mouseX, int mouseY, Snap[] snaps) {
        ScaledResolution sr = new ScaledResolution(mc);
        float minecraftScale = sr.getScaleFactor();
        float floatMouseX = Mouse.getX() / minecraftScale;
        float floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale;

        if (resizing) {
            float x = mouseX - xOffset;
            float y = mouseY - yOffset;
            if (this.editMode == EditMode.RESIZE_BARS) {
                x /= 15;
                y /= -7;
                x += originalWidth;
                y += originalHeight;
                if (x > 0) {
                    main.getConfigValues().setSizeX(dragging, Math.round(x)); // TODO Make sure this works fine
                }
                if (y > 0) {
                    main.getConfigValues().setSizeY(dragging, Math.round(y));
                }
                addResizeCorners(dragging);
            } else if (this.editMode == EditMode.RESCALE) {
                ButtonLocation buttonLocation = buttonLocations.get(dragging);
                if (buttonLocation == null) {
                    return;
                }

                float scale = buttonLocation.getScale();
                float scaledX1 = buttonLocation.getBoxXOne()*buttonLocation.getScale();
                float scaledY1 = buttonLocation.getBoxYOne()*buttonLocation.getScale();
                float scaledX2 = buttonLocation.getBoxXTwo()*buttonLocation.getScale();
                float scaledY2 = buttonLocation.getBoxYTwo()*buttonLocation.getScale();
                float scaledWidth = scaledX2-scaledX1;
                float scaledHeight = scaledY2-scaledY1;

                float width = (buttonLocation.getBoxXTwo() - buttonLocation.getBoxXOne());
                float height = (buttonLocation.getBoxYTwo() - buttonLocation.getBoxYOne());

                float middleX = scaledX1+scaledWidth/2F;
                float middleY = scaledY1+scaledHeight/2F;

                float xOffset = floatMouseX-this.xOffset*scale-middleX;
                float yOffset = floatMouseY-this.yOffset*scale-middleY;

                if (resizingCorner == ButtonResize.Corner.TOP_LEFT) {
                    xOffset *= -1;
                    yOffset *= -1;
                } else if (resizingCorner == ButtonResize.Corner.TOP_RIGHT) {
                    yOffset *= -1;
                } else if (resizingCorner == ButtonResize.Corner.BOTTOM_LEFT) {
                    xOffset *= -1;
                }

                float newWidth = xOffset * 2F;
                float newHeight = yOffset * 2F;

                float scaleX = newWidth / width;
                float scaleY = newHeight / height;

                float newScale = Math.max(scaleX, scaleY);

                float normalizedScale = ConfigValues.normalizeValueNoStep(newScale);
                main.getConfigValues().setGuiScale(dragging, normalizedScale);
                buttonLocation.drawButton(mc, mouseX, mouseY);
                recalculateResizeButtons();
            }
        } else if (dragging != null) {
            ButtonLocation buttonLocation = buttonLocations.get(dragging);
            if (buttonLocation == null) {
                return;
            }

            Snap horizontalSnap = null;
            Snap verticalSnap = null;
            if (snaps != null) {
                horizontalSnap = snaps[0];
                verticalSnap = snaps[1];
            }

            float x = floatMouseX-main.getConfigValues().getAnchorPoint(dragging).getX(sr.getScaledWidth());
            float y = floatMouseY-main.getConfigValues().getAnchorPoint(dragging).getY(sr.getScaledHeight());

            float scaledX1 = buttonLocation.getBoxXOne()*buttonLocation.getScale();
            float scaledY1 = buttonLocation.getBoxYOne()*buttonLocation.getScale();
            float scaledX2 = buttonLocation.getBoxXTwo()*buttonLocation.getScale();
            float scaledY2 = buttonLocation.getBoxYTwo()*buttonLocation.getScale();
            float scaledWidth = scaledX2-scaledX1;
            float scaledHeight = scaledY2-scaledY1;

            boolean xSnapped = false;
            boolean ySnapped = false;

            if (horizontalSnap != null) {
                float snapX = horizontalSnap.getSnapValue();

                if (horizontalSnap.getThisSnapEdge() == Edge.LEFT) {
                    float snapOffset = Math.abs((floatMouseX-this.xOffset) - (snapX + scaledWidth/2F));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        xSnapped = true;
                        x = snapX - main.getConfigValues().getAnchorPoint(dragging).getX(sr.getScaledWidth()) + scaledWidth/2F;
                    }

                } else if (horizontalSnap.getThisSnapEdge() == Edge.RIGHT) {
                    float snapOffset = Math.abs((floatMouseX-this.xOffset) - (snapX - scaledWidth/2F));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        xSnapped = true;
                        x = snapX - main.getConfigValues().getAnchorPoint(dragging).getX(sr.getScaledWidth()) - scaledWidth/2F;
                    }

                } else if (horizontalSnap.getThisSnapEdge() == Edge.VERTICAL_MIDDLE) {
                    float snapOffset = Math.abs((floatMouseX-this.xOffset) - (snapX));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        xSnapped = true;
                        x = snapX - main.getConfigValues().getAnchorPoint(dragging).getX(sr.getScaledWidth());
                    }
                }
            }

            if (verticalSnap != null) {
                float snapY = verticalSnap.getSnapValue();

                if (verticalSnap.getThisSnapEdge() == Edge.TOP) {
                    float snapOffset = Math.abs((floatMouseY-this.yOffset) - (snapY + scaledHeight/2F));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        ySnapped = true;
                        y = snapY - main.getConfigValues().getAnchorPoint(dragging).getY(sr.getScaledHeight()) + scaledHeight/2F;
                    }

                } else if (verticalSnap.getThisSnapEdge() == Edge.BOTTOM) {
                    float snapOffset = Math.abs((floatMouseY-this.yOffset) - (snapY - scaledHeight/2F));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        ySnapped = true;
                        y = snapY - main.getConfigValues().getAnchorPoint(dragging).getY(sr.getScaledHeight()) - scaledHeight/2F;
                    }
                } else if (verticalSnap.getThisSnapEdge() == Edge.HORIZONTAL_MIDDLE) {
                    float snapOffset = Math.abs((floatMouseY-this.yOffset) - (snapY));
                    if (snapOffset <= SNAP_PULL*minecraftScale) {
                        ySnapped = true;
                        y = snapY - main.getConfigValues().getAnchorPoint(dragging).getY(sr.getScaledHeight());
                    }
                }
            }

            if (!xSnapped) {
                x -= xOffset;
            }

            if (!ySnapped) {
                y -= yOffset;
            }

            if (xSnapped || ySnapped) {
                float xChange = Math.abs(main.getConfigValues().getRelativeCoords(dragging).getX() - x);
                float yChange = Math.abs(main.getConfigValues().getRelativeCoords(dragging).getY() - y);
                if (xChange < 0.001 && yChange < 0.001) {
                    return;
                }
            }

            main.getConfigValues().setCoords(dragging, x, y);
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
