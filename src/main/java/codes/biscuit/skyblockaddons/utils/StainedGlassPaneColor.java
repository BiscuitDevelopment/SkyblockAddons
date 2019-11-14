package codes.biscuit.skyblockaddons.utils;

public enum StainedGlassPaneColor {
    // Same order as in Minecraft, don't change! Starts from 0

    WHITE,
    ORANGE,
    MAGENTA,
    LIGHT_BLUE,
    YELLOW,
    LIME,
    PINK,
    GRAY,
    LIGHT_GRAY,
    CYAN,
    PURPLE,
    BLUE,
    BROWN,
    GREEN,
    RED,
    BLACK;

    private static final StainedGlassPaneColor[] enumValues = StainedGlassPaneColor.values();

    public static StainedGlassPaneColor getStainedGlassPaneColor(int color) {
        return enumValues[color];
    }
}
