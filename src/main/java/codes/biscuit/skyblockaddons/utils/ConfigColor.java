package codes.biscuit.skyblockaddons.utils;

import com.mojang.realmsclient.gui.ChatFormatting;

import java.awt.Color;

public enum ConfigColor {

    BLACK(ChatFormatting.BLACK, 0,0,0),
    DARK_BLUE(ChatFormatting.DARK_BLUE, 0,0,170),
    DARK_GREEN(ChatFormatting.DARK_GREEN, 0,170,0),
    DARK_AQUA(ChatFormatting.DARK_AQUA, 0,170,170),
    DARK_RED(ChatFormatting.DARK_RED, 170,0,0),
    DARK_PURPLE(ChatFormatting.DARK_PURPLE, 170,0,170),
    GOLD(ChatFormatting.GOLD, 255,170,0),
    GRAY(ChatFormatting.GRAY, 170,170,170),
    DARK_GRAY(ChatFormatting.DARK_GRAY, 85,85,85),
    BLUE(ChatFormatting.BLUE, 85,85,255),
    GREEN(ChatFormatting.GREEN, 85,255,85),
    AQUA(ChatFormatting.AQUA, 85,255,255),
    RED(ChatFormatting.RED, 255,85,85),
    LIGHT_PURPLE(ChatFormatting.LIGHT_PURPLE, 255,85,255),
    YELLOW(ChatFormatting.YELLOW, 255,255,85),
    WHITE(ChatFormatting.WHITE, 255,255,255);

    private final ChatFormatting chatFormatting;
    private final int r;
    private final int g;
    private final int b;

    ConfigColor(ChatFormatting chatFormatting, int r, int g, int b) {
        this.chatFormatting = chatFormatting;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public ChatFormatting getChatFormatting() {
        return chatFormatting;
    }

    public int getColor() {
        return new Color(r,g,b,255).getRGB();
    }

    /**
     *
     * @param alpha Alpha value from 0 to 255
     */
    public int getColor(float alpha) {
        return new Color(r,g,b,(int)alpha).getRGB();
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public ConfigColor getNextColor() {
        int nextColor = ordinal()+1;
        if (nextColor > values().length-1) {
            nextColor = 0;
        }
        return values()[nextColor];
    }

}
