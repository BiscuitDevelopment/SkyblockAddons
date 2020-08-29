package codes.biscuit.skyblockaddons.utils;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.text.WordUtils;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Collection of text/string related utility methods
 */
public class TextUtils {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private static final Pattern NUMBERS_SLASHES = Pattern.compile("[^0-9 /]");
    private static final Pattern SCOREBOARD_CHARACTERS = Pattern.compile("[^a-z A-Z:0-9_/'.!§\\[\\]❤]");
    private static final Pattern FLOAT_CHARACTERS = Pattern.compile("[^.0-9\\-]");
    private static final Pattern INTEGER_CHARACTERS = Pattern.compile("[^0-9]");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");

    private static final NavigableMap<Integer, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000, "k");
        suffixes.put(1_000_000, "M");
        suffixes.put(1_000_000_000, "G");
    }

    /**
     * Formats a double number to look better with commas every 3 digits and
     * one decimal point.
     * For example: {@code 1,006,789.5}
     *
     * @param number Number to format
     * @return Formatted string
     */
    public static String formatDouble(double number) {
        return DECIMAL_FORMAT.format(number);
    }

    /**
     * Strips color codes from a given text
     *
     * @param input Text to strip colors from
     * @return Text without color codes
     */
    public static String stripColor(final String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Removes any character that isn't a number, letter, or common symbol from a given text.
     *
     * @param text Input text
     * @return Input text with only letters and numbers
     */
    public static String keepScoreboardCharacters(String text) {
        return SCOREBOARD_CHARACTERS.matcher(text).replaceAll("");
    }

    /**
     * Removes any character that isn't a number, - or . from a given text.
     *
     * @param text Input text
     * @return Input text with only valid float number characters
     */
    public static String keepFloatCharactersOnly(String text) {
        return FLOAT_CHARACTERS.matcher(text).replaceAll("");
    }

    /**
     * Removes any character that isn't a number from a given text.
     *
     * @param text Input text
     * @return Input text with only valid integer number characters
     */
    public static String keepIntegerCharactersOnly(String text) {
        return INTEGER_CHARACTERS.matcher(text).replaceAll("");
    }

    /**
     * Removes any character that isn't a number from a given text.
     *
     * @param text Input text
     * @return Input text with only numbers
     */
    public static String getNumbersOnly(String text) {
        return NUMBERS_SLASHES.matcher(text).replaceAll("");
    }

    /**
     * Removes any duplicate spaces from a given text.
     *
     * @param text Input text
     * @return Input text without repeating spaces
     */
    public static String removeDuplicateSpaces(String text) {
        return text.replaceAll("\\s+", " ");
    }

    /**
     * Reverses a given text while leaving the english parts intact and in order.
     * (Maybe its more complicated than it has to be, but it gets the job done.)
     *
     * @param originalText Input text
     * @return Reversed input text
     */
    public static String reverseText(String originalText) {
        StringBuilder newString = new StringBuilder();
        String[] parts = originalText.split(" ");
        for (int i = parts.length; i > 0; i--) {
            String textPart = parts[i-1];
            boolean foundCharacter = false;
            for (char letter : textPart.toCharArray()) {
                if (letter > 191) { // Found special character
                    foundCharacter = true;
                    newString.append(new StringBuilder(textPart).reverse().toString());
                    break;
                }
            }
            newString.append(" ");
            if (!foundCharacter) {
                newString.insert(0, textPart);
            }
            newString.insert(0, " ");
        }
        return removeDuplicateSpaces(newString.toString().trim());
    }

    /**
     * Get the ordinal suffix of a number, meaning
     * <ul>
     *     <li>st - if n ends with 1 but isn't 11</li>
     *     <li>nd - if n ends with 2 but isn't 12</li>
     *     <li>rd - if n ends with 3 but isn't 13</li>
     *     <li>th - in all other cases</li>
     * </ul>
     */
    public static String getOrdinalSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }

    /**
     * Converts an enum name to camel case. For example, it will turn ONE_TWO_THREE into oneTwoThree.
     *
     * @param enumConstant The enum constant
     * @return The enum constant's name in camel case
     */
    public static String getEnumConstantNameInCamelCase(Enum<?> enumConstant) {
        String memberName =  WordUtils.capitalizeFully(enumConstant.name().toLowerCase(Locale.US).replace("_", " ")).replace(" ", "");
        return memberName.substring(0, 1).toLowerCase(Locale.US) + memberName.substring(1);
    }

    /**
     * @param textureURL The texture ID/hash that is in the texture URL (not including http://textures.minecraft.net/texture/)
     * @return A json string including the texture URL as a skin texture (used in NBT)
     */
    public static String encodeSkinTextureURL(String textureURL) {
        JsonObject skin = new JsonObject();
        skin.addProperty("url", "http://textures.minecraft.net/texture/" + textureURL);

        JsonObject textures = new JsonObject();
        textures.add("SKIN", skin);

        JsonObject root = new JsonObject();
        root.add("textures", textures);

        return Base64.getEncoder().encodeToString(Utils.getGson().toJson(root).getBytes(StandardCharsets.UTF_8));
    }

    public static String abbreviate(int number) {
        if (number < 0) {
            return "-" + abbreviate(-number);
        }
        if (number < 1000) {
            return Long.toString(number);
        }

        Map.Entry<Integer, String> entry = suffixes.floorEntry(number);
        Integer divideBy = entry.getKey();
        String suffix = entry.getValue();

        int truncated = number / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

}