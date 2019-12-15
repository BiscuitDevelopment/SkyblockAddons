package codes.biscuit.skyblockaddons.utils.nifty.color;

import codes.biscuit.skyblockaddons.utils.nifty.StringUtil;
import com.google.common.base.Preconditions;

/**
 * A container for a color palette. This class is immutable; the color names listed as fields are HTML5 standards.
 *
 * @author Brian Graham (CraftedFury)
 */
public final class Color {

	private static final int BIT_MASK = 0xFF;

	/**
	 * White, or (0xFF,0xFF,0xFF) in (R,G,B)
	 */
	public static final Color WHITE = fromRGB(0xFFFFFF);
	/**
	 * Silver, or (0xC0,0xC0,0xC0) in (R,G,B)
	 */
	public static final Color SILVER = fromRGB(0xC0C0C0);
	/**
	 * Gray, or (0x80,0x80,0x80) in (R,G,B)
	 */
	public static final Color GRAY = fromRGB(0x808080);
	/**
	 * Black, or (0x00,0x00,0x00) in (R,G,B)
	 */
	public static final Color BLACK = fromRGB(0x000000);
	/**
	 * Red, or (0xFF,0x00,0x00) in (R,G,B)
	 */
	public static final Color RED = fromRGB(0xFF0000);
	/**
	 * Maroon, or (0x80,0x00,0x00) in (R,G,B)
	 */
	public static final Color MAROON = fromRGB(0x800000);
	/**
	 * Yellow, or (0xFF,0xFF,0x00) in (R,G,B)
	 */
	public static final Color YELLOW = fromRGB(0xFFFF00);
	/**
	 * Olive, or (0x80,0x80,0x00) in (R,G,B)
	 */
	public static final Color OLIVE = fromRGB(0x808000);
	/**
	 * Lime, or (0x00,0xFF,0x00) in (R,G,B)
	 */
	public static final Color LIME = fromRGB(0x00FF00);
	/**
	 * Green, or (0x00,0x80,0x00) in (R,G,B)
	 */
	public static final Color GREEN = fromRGB(0x008000);
	/**
	 * Aqua, or (0x00,0xFF,0xFF) in (R,G,B)
	 */
	public static final Color AQUA = fromRGB(0x00FFFF);
	/**
	 * Teal, or (0x00,0x80,0x80) in (R,G,B)
	 */
	public static final Color TEAL = fromRGB(0x008080);
	/**
	 * Blue, or (0x00,0x00,0xFF) in (R,G,B)
	 */
	public static final Color BLUE = fromRGB(0x0000FF);
	/**
	 * Navy, or (0x00,0x00,0x80) in (R,G,B)
	 */
	public static final Color NAVY = fromRGB(0x000080);
	/**
	 * Fuchsia, or (0xFF,0x00,0xFF) in (R,G,B)
	 */
	public static final Color FUCHSIA = fromRGB(0xFF00FF);
	/**
	 * Purple, or (0x80,0x00,0x80) in (R,G,B)
	 */
	public static final Color PURPLE = fromRGB(0x800080);
	/**
	 * Orange, or (0xFF,0xA5,0x00) in (R,G,B)
	 */
	public static final Color ORANGE = fromRGB(0xFFA500);

	private final int value;

	private Color(int red, int green, int blue) {
		this(red, green, blue, 255);
	}

	private Color(int red, int green, int blue, int alpha) {
		Preconditions.checkArgument((red >= 0 && red <= BIT_MASK), StringUtil.format("Red is not between 0-255: {0}", red));
		Preconditions.checkArgument((green >= 0 && green <= BIT_MASK), StringUtil.format("Green is not between 0-255: {0}", green));
		Preconditions.checkArgument((blue >= 0 && blue <= BIT_MASK), StringUtil.format("Blue is not between 0-255: {0}", blue));
		Preconditions.checkArgument((alpha >= 0 && alpha <= BIT_MASK), StringUtil.format("Alpha is not between 0-255: {0}", alpha));

		value = ((alpha & BIT_MASK) << 24) | ((red & BIT_MASK) << 16) | ((green & BIT_MASK) << 8) | (blue & BIT_MASK);
	}

	private Color(int rgba) {
		value = rgba;
	}

	/**
	 *
	 * @return An integer representation of this color, as 0xRRGGBB
	 */
	public int asRGB() {
		return value;
	}

	/**
	 * Creates a new Color object from an existing color.
	 *
	 * @param red integer from 0-255
	 * @param green integer from 0-255
	 * @param blue integer from 0-255
	 * @return a new Color object for the red, green, blue
	 * @throws IllegalArgumentException if any value is strictly {@literal >255 or <0}
	 */
	public static Color fromRGB(int red, int green, int blue) throws IllegalArgumentException {
		return fromRGB(red, green, blue, 255);
	}

	/**
	 * Creates a new Color object from a red, green, and blue
	 *
	 * @param red integer from 0-255
	 * @param green integer from 0-255
	 * @param blue integer from 0-255
	 * @param alpha integer from 0-255
	 * @return a new Color object for the red, green, blue, alpha
	 * @throws IllegalArgumentException if any value is strictly {@literal >255 or <0}
	 */
	public static Color fromRGB(int red, int green, int blue, int alpha) throws IllegalArgumentException {
		return new Color(red, green, blue, alpha);
	}

	/**
	 * Creates a new color object from an integer that contains the red,
	 * green and blue bytes in the lowest order 24 bits.
	 *
	 * @param rgb the integer storing the red, green, blue and alpha values
	 * @return a new color object for specified values
	 * @throws IllegalArgumentException if any value is strictly {@literal >255 or <0}
	 */
	public static Color fromRGB(int rgb) {
		return fromRGB(rgb, false);
	}

	/**
	 * Creates a new color object from an integer that contains the red,
	 * green, blue and alpha bytes in the lowest order 32 bits.
	 *
	 * @param rgba the integer storing the red, green, blue and alpha values
	 * @return a new color object for specified values
	 * @throws IllegalArgumentException if any value is strictly {@literal >255 or <0}
	 */
	public static Color fromRGB(int rgba, boolean hasAlpha) {
		return new Color(hasAlpha ? rgba : (0xff000000 | rgba));
	}

	/**
	 * Gets the alpha component.
	 *
	 * @return alpha component, from 0 to 255
	 */
	public int getAlpha() {
		return (this.asRGB() >> 24) & BIT_MASK;
	}

	/**
	 * Gets the blue component.
	 *
	 * @return blue component, from 0 to 255
	 */
	public int getBlue() {
		return this.asRGB() & BIT_MASK;
	}

	/**
	 * Gets the green component.
	 *
	 * @return green component, from 0 to 255
	 */
	public int getGreen() {
		return (this.asRGB() >> 8) & BIT_MASK;
	}

	/**
	 * Gets the red component
	 *
	 * @return red component, from 0 to 255
	 */
	public int getRed() {
		return (this.asRGB() >> 16) & BIT_MASK;
	}

	/**
	 * Creates a new {@link Color} with the provided alpha value.
	 *
	 * @param alpha alpha value, 0 to 255
	 * @return new color with alpha component
	 */
	public Color setAlpha(int alpha) {
		return fromRGB(this.getRed(), this.getGreen(), this.getBlue(), alpha);
	}

	/**
	 * Creates a new color with its RGB components changed as if it was dyed
	 * with the colors passed in, replicating vanilla workbench dyeing
	 *
	 * @param colors The colors to dye with
	 * @return A new color with the changed rgb components
	 */
	public Color mixColors(Color... colors) {
		Preconditions.checkArgument(colors != null && colors.length > 0, "Colors cannot be NULL!");

		int totalRed = this.getRed();
		int totalGreen = this.getGreen();
		int totalBlue = this.getBlue();
		int totalMax = Math.max(Math.max(totalRed, totalGreen), totalBlue);

		for (Color color : colors) {
			totalRed += color.getRed();
			totalGreen += color.getGreen();
			totalBlue += color.getBlue();
			totalMax += Math.max(Math.max(color.getRed(), color.getGreen()), color.getBlue());
		}

		float averageRed = totalRed / (colors.length + 1);
		float averageGreen = totalGreen / (colors.length + 1);
		float averageBlue = totalBlue / (colors.length + 1);
		float averageMax = totalMax / (colors.length + 1);

		float maximumOfAverages = Math.max(Math.max(averageRed, averageGreen), averageBlue);
		float gainFactor = averageMax / maximumOfAverages;

		return Color.fromRGB((int) (averageRed * gainFactor), (int) (averageGreen * gainFactor), (int) (averageBlue * gainFactor));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Color))
			return false;
		else {
			final Color that = (Color)obj;
			return this.getBlue() == that.getBlue() && this.getGreen() == that.getGreen() && this.getRed() == that.getRed();
		}
	}

	@Override
	public int hashCode() {
		return this.asRGB() ^ Color.class.hashCode();
	}

	@Override
	public String toString() {
		return "Color:[argb0x" + Integer.toHexString(this.getAlpha()).toUpperCase() + Integer.toHexString(this.getRed()).toUpperCase() + Integer.toHexString(this.getGreen()).toUpperCase() + Integer.toHexString(this.getBlue()).toUpperCase() + "]";
	}

}