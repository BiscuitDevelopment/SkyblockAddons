package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import lombok.Getter;

@Getter
public class GuiFeatureData {

    private ColorCode defaultColor = null;
    private EnumUtils.DrawType drawType = null;

    /**
     * This represents whether the color selection is restricted to the minecraft color codes only
     * such as &f, &a, and &b (white, green, and blue respectively).<br>
     *
     * Colors that cannot be used include other hex colors such as #FF00FF.
     */
    private boolean colorsRestricted;

    public GuiFeatureData(ColorCode defaultColor) {
        this(defaultColor, false);
    }

    public GuiFeatureData(ColorCode defaultColor, boolean colorsRestricted) {
        this.defaultColor = defaultColor;
        this.colorsRestricted = colorsRestricted;
    }

    public GuiFeatureData(EnumUtils.DrawType drawType) {
        this(drawType, false);
    }

    public GuiFeatureData(EnumUtils.DrawType drawType, ColorCode defaultColor) {
        this(drawType, defaultColor, false);
    }

    private GuiFeatureData(EnumUtils.DrawType drawType, boolean colorsRestricted) {
        this.drawType = drawType;
        this.colorsRestricted = colorsRestricted;
    }

    public GuiFeatureData(EnumUtils.DrawType drawType, ColorCode defaultColor, boolean colorsRestricted) {
        this.drawType = drawType;
        this.defaultColor = defaultColor;
        this.colorsRestricted = colorsRestricted;
    }
}
