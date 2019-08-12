package codes.biscuit.skyblockaddons.utils;

import net.minecraft.util.EnumChatFormatting;

import java.awt.*;

public enum ConfigColor {

    BLACK(EnumChatFormatting.BLACK, 0,0,0),
    DARK_BLUE(EnumChatFormatting.DARK_BLUE, 0,0,170),
    DARK_GREEN(EnumChatFormatting.DARK_GREEN, 0,170,0),
    DARK_AQUA(EnumChatFormatting.DARK_AQUA, 0,170,170),
    DARK_RED(EnumChatFormatting.DARK_RED, 170,0,0),
    DARK_PURPLE(EnumChatFormatting.DARK_PURPLE, 170,0,170),
    GOLD(EnumChatFormatting.GOLD, 255,170,0),
    GRAY(EnumChatFormatting.GRAY, 170,170,170),
    DARK_GRAY(EnumChatFormatting.DARK_GRAY, 85,85,85),
    BLUE(EnumChatFormatting.BLUE, 85,85,255),
    GREEN(EnumChatFormatting.GREEN, 85,255,85),
    AQUA(EnumChatFormatting.AQUA, 85,255,255),
    RED(EnumChatFormatting.RED, 255,85,85),
    LIGHT_PURPLE(EnumChatFormatting.LIGHT_PURPLE, 255,85,255),
    YELLOW(EnumChatFormatting.YELLOW, 255,255,85),
    WHITE(EnumChatFormatting.WHITE, 255,255,255);

    private EnumChatFormatting chatFormatting;
    private int r;
    private int g;
    private int b;

    ConfigColor(EnumChatFormatting chatFormatting, int r, int g, int b) {
        this.chatFormatting = chatFormatting;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public EnumChatFormatting getChatFormatting() {
        return chatFormatting;
    }

    /**
     *
     * @param alpha Alpha value from 0 to 255
     */
    public int getColor(float alpha) {
        return new Color(r,g,b,(int)alpha).getRGB();
    }

    public ConfigColor getNextColor() {
        int nextColor = ordinal()+1;
        if (nextColor > values().length-1) {
            nextColor = 0;
        }
        return values()[nextColor];
    }
}
