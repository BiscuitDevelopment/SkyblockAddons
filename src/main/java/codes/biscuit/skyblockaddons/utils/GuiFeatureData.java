package codes.biscuit.skyblockaddons.utils;

class GuiFeatureData {

    private ConfigColor defaultColor = null;
    private CoordsPair defaultPos = null;
    private CoordsPair defaultBarSize = null;
    private EnumUtils.AnchorPoint defaultAnchor = null;
    private EnumUtils.DrawType drawType = null;

    GuiFeatureData(ConfigColor defaultColor) {
        this.defaultColor = defaultColor;
    }

    GuiFeatureData(EnumUtils.DrawType drawType, ConfigColor defaultColor, EnumUtils.AnchorPoint defaultAnchor, int... positionThenSizes) {
        this.drawType = drawType;
        this.defaultColor = defaultColor;
        this.defaultPos = new CoordsPair(positionThenSizes[0], positionThenSizes[1]);
        if (positionThenSizes.length > 2) {
            this.defaultBarSize = new CoordsPair(positionThenSizes[2], positionThenSizes[3]);
        }
        this.defaultAnchor = defaultAnchor;
    }

    GuiFeatureData(EnumUtils.DrawType drawType, EnumUtils.AnchorPoint defaultAnchor, int... position) {
        this.drawType = drawType;
        this.defaultAnchor = defaultAnchor;
        this.defaultPos = new CoordsPair(position[0], position[1]);

    }

    ConfigColor getDefaultColor() {
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
}
