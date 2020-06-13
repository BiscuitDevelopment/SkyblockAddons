package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import lombok.Getter;

@Getter
public class GuiFeatureData {

    private ChatFormatting defaultColor = null;
    private FloatPair defaultPos = null;
    private IntPair defaultBarSize = null;
    private EnumUtils.AnchorPoint defaultAnchor = null;
    private EnumUtils.DrawType drawType = null;

    /**
     * This represents whether the color selection is restricted to the minecraft color codes only
     * such as &f, &a, and &b (white, green, and blue respectively).<br>
     *
     * Colors that cannot be used include other hex colors such as #FF00FF.
     */
    private boolean colorsRestricted;

    public GuiFeatureData(ChatFormatting defaultColor) {
        this(defaultColor, false);
    }

    public GuiFeatureData(ChatFormatting defaultColor, boolean colorsRestricted) {
        this.defaultColor = defaultColor;
        this.colorsRestricted = colorsRestricted;
    }

    public GuiFeatureData(EnumUtils.DrawType drawType, ChatFormatting defaultColor, EnumUtils.AnchorPoint defaultAnchor, int... positionThenSizes) {
        this(drawType, defaultColor, defaultAnchor, false, positionThenSizes);
    }

    public GuiFeatureData(EnumUtils.DrawType drawType, ChatFormatting defaultColor, EnumUtils.AnchorPoint defaultAnchor, boolean colorsRestricted, int... positionThenSizes) {
        this.drawType = drawType;
        this.defaultColor = defaultColor;
        this.colorsRestricted = colorsRestricted;
        this.defaultPos = new FloatPair(positionThenSizes[0], positionThenSizes[1]);
        if (positionThenSizes.length > 2) {
            this.defaultBarSize = new IntPair(positionThenSizes[2], positionThenSizes[3]);
        }
        this.defaultAnchor = defaultAnchor;
    }

    public GuiFeatureData(EnumUtils.DrawType drawType, EnumUtils.AnchorPoint defaultAnchor, int... position) {
        this(drawType, defaultAnchor, false, position);
    }

    private GuiFeatureData(EnumUtils.DrawType drawType, EnumUtils.AnchorPoint defaultAnchor, boolean colorsRestricted, int... position) {
        this.drawType = drawType;
        this.defaultAnchor = defaultAnchor;
        this.colorsRestricted = colorsRestricted;
        this.defaultPos = new FloatPair(position[0], position[1]);
    }
}
