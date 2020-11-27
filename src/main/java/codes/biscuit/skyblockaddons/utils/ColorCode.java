package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;

/**
 * @author Brian Graham (CraftedFury)
 */
public enum ColorCode {

	BLACK('0', 0x000000),
	DARK_BLUE('1', 0x0000AA),
	DARK_GREEN('2', 0x00AA00),
	DARK_AQUA('3', 0x00AAAA),
	DARK_RED('4', 0xAA0000),
	DARK_PURPLE('5', 0xAA00AA),
	GOLD('6', 0xFFAA00),
	GRAY('7', 0xAAAAAA),
	DARK_GRAY('8', 0x555555),
	BLUE('9', 0x5555FF),
	GREEN('a', 0x55FF55),
	AQUA('b', 0x55FFFF),
	RED('c', 0xFF5555),
	LIGHT_PURPLE('d', 0xFF55FF),
	YELLOW('e', 0xFFFF55),
	WHITE('f', 0xFFFFFF),
	MAGIC('k', true, "obfuscated"),
	BOLD('l', true),
	STRIKETHROUGH('m', true),
	UNDERLINE('n', true, "underlined"),
	ITALIC('o', true),
	RESET('r');

	public static final char COLOR_CHAR = '\u00a7';
	private char code;
	private boolean isFormat;
	private String jsonName;
	private String toString;
	private int color;

	ColorCode(char code) {
		this(code, -1);
	}

	ColorCode(char code, int rgb) {
		this(code, false, rgb);
	}

	ColorCode(char code, boolean isFormat) {
		this(code, isFormat, -1);
	}

	ColorCode(char code, boolean isFormat, int rgb) {
		this(code, isFormat, null, rgb);
	}

	ColorCode(char code, boolean isFormat, String jsonName) {
		this(code, isFormat, jsonName, -1);
	}

	ColorCode(char code, boolean isFormat, String jsonName, int rgb) {
		this.code = code;
		this.isFormat = isFormat;
		this.jsonName = jsonName;
		this.toString = new String(new char[] { COLOR_CHAR, code });
		this.color = rgb;
	}

    /**
     * Get the color represented by the specified code.
     *
     * @param code The code to search for.
     * @return The mapped color, or null if non exists.
     */
	public static ColorCode getByChar(char code) {
		for (ColorCode color : values()) {
			if (color.code == code)
				return color;
		}

		return null;
	}

	public char getCode() {
		return this.code;
	}

	public Color getColorObject() {
		return new Color(this.color);
	}

	public int getColor(int alpha) {
		return SkyblockAddons.getInstance().getUtils().getColorWithAlpha(this.color, alpha);
	}

	public String getJsonName() {
		return StringUtils.isEmpty(this.jsonName) ? this.name().toLowerCase() : this.jsonName;
	}

	public int getColor() {
		return color;
	}

	public boolean isColor() {
		return !this.isFormat() && this != RESET;
	}

	public boolean isFormat() {
		return this.isFormat;
	}

	public ColorCode getNextFormat() {
		return this.getNextFormat(ordinal());
	}

	private ColorCode getNextFormat(int ordinal) {
		int nextColor = ordinal + 1;

		if (nextColor > values().length - 1) {
			return values()[0];
		} else if (!values()[nextColor].isColor()) {
			return getNextFormat(nextColor);
		}

		return values()[nextColor];
	}

	@Override
	public String toString() {
		return this.toString;
	}
}