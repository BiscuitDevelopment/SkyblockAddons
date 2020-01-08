package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;

public class GuiFeatureData {

    private ChatFormatting defaultColor = null;
    private CoordsPair defaultPos = null;
    private CoordsPair defaultBarSize = null;
    private EnumUtils.AnchorPoint defaultAnchor = null;
    private EnumUtils.DrawType drawType = null;

    /**
     * Whether the color selection is restricted to the minecraft color codes only.
     */
    private boolean colorsRestricted;

    GuiFeatureData(ChatFormatting defaultColor) {
        this(defaultColor, false);
    }

    GuiFeatureData(ChatFormatting defaultColor, boolean colorsRestricted) {
        this.defaultColor = defaultColor;
        this.colorsRestricted = colorsRestricted;
    }

    GuiFeatureData(EnumUtils.DrawType drawType, ChatFormatting defaultColor, EnumUtils.AnchorPoint defaultAnchor, int... positionThenSizes) {
        this(drawType, defaultColor, defaultAnchor, false, positionThenSizes);
    }

    GuiFeatureData(EnumUtils.DrawType drawType, ChatFormatting defaultColor, EnumUtils.AnchorPoint defaultAnchor, boolean colorsRestricted, int... positionThenSizes) {
        this.drawType = drawType;
        this.defaultColor = defaultColor;
        this.colorsRestricted = colorsRestricted;
        this.defaultPos = new CoordsPair(positionThenSizes[0], positionThenSizes[1]);
        if (positionThenSizes.length > 2) {
            this.defaultBarSize = new CoordsPair(positionThenSizes[2], positionThenSizes[3]);
        }
        this.defaultAnchor = defaultAnchor;
    }

    GuiFeatureData(EnumUtils.DrawType drawType, EnumUtils.AnchorPoint defaultAnchor, int... position) {
        this(drawType, defaultAnchor, false, position);
    }

    GuiFeatureData(EnumUtils.DrawType drawType, EnumUtils.AnchorPoint defaultAnchor, boolean colorsRestricted, int... position) {
        this.drawType = drawType;
        this.defaultAnchor = defaultAnchor;
        this.colorsRestricted = colorsRestricted;
        this.defaultPos = new CoordsPair(position[0], position[1]);

    }

    ChatFormatting getDefaultColor() {
        return defaultColor;
    }

    EnumUtils.DrawType getDrawType() {
        return drawType;
    }

    CoordsPair getDefaultPos() {
        return defaultPos;
    }

    EnumUtils.AnchorPoint getDefaultAnchor() {
        return defaultAnchor;
    }

    CoordsPair getDefaultBarSize() {
        return defaultBarSize;
    }

    /**
     * Get whether the color selection is restricted to the minecraft colors codes only.
     */
    public boolean isColorsRestricted() {
        return colorsRestricted;
    }
}
