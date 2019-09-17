package codes.biscuit.skyblockaddons.utils;

import java.awt.*;
import java.util.EnumSet;
import java.util.Set;

public enum BackpackColor {

    BLACK(29, 29, 33),
    RED(176, 46, 38),
    GREEN(94, 124, 22),
    BROWN(131, 84, 50),
    BLUE(60, 68, 170),
    PURPLE(137, 50, 184),
    CYAN(22, 156, 156),
    LIGHT_GREY(157, 157, 151),
    GREY(71, 79, 82),
    PINK(243, 139, 170),
    LIME(128, 199, 31),
    YELLOW(254, 216, 61),
    LIGHT_BLUE(58, 179, 218),
    MAGENTA(199, 78, 189),
    ORANGE(249, 128, 29),
    WHITE(255, 255, 255);

    private static Set<BackpackColor> darkColors = EnumSet.of(BackpackColor.BLACK, BackpackColor.PURPLE, BackpackColor.GREEN,
            BackpackColor.MAGENTA, BackpackColor.RED, BackpackColor.BROWN, BackpackColor.BLUE, BackpackColor.GREY);
    private int r;
    private int g;
    private int b;

    BackpackColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public float getR() {
        return (float) r / 255;
    }

    public float getG() {
        return (float) g / 255;
    }

    public float getB() {
        return (float) b / 255;
    }

    public int getTextColor() {
        int rgb = 4210752;
        if (darkColors.contains(this)) {
            rgb = new Color(219, 219, 219, 255).getRGB();
        }
        return rgb;
    }
}
