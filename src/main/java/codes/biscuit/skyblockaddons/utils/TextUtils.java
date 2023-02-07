package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.gson.JsonObject;
import net.minecraft.util.IChatComponent;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of text/string related utility methods
 */
public class TextUtils {
    /**
     * Hypixel uses US number format.
     */
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORZ]");
    private static final Pattern STRIP_ICONS_PATTERN = Pattern.compile("[♲Ⓑ⚒ቾ]+");
    private static final Pattern STRIP_PREFIX_PATTERN = Pattern.compile("\\[[^\\[\\]]*\\]");
    private static final Pattern REPEATED_COLOR_PATTERN = Pattern.compile("(?i)(§[0-9A-FK-ORZ])+");
    private static final Pattern NUMBERS_SLASHES = Pattern.compile("[^0-9 /]");
    private static final Pattern SCOREBOARD_CHARACTERS = Pattern.compile("[^a-z A-Z:0-9_/'.!§\\[\\]❤]");
    private static final Pattern FLOAT_CHARACTERS = Pattern.compile("[^.0-9\\-]");
    private static final Pattern INTEGER_CHARACTERS = Pattern.compile("[^0-9]");
    private static final Pattern TRIM_WHITESPACE_RESETS = Pattern.compile("^(?:\\s|§r)*|(?:\\s|§r)*$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_]+");
    private static final Pattern RESET_CODE_PATTERN = Pattern.compile("(?i)§R");
    private static final Pattern MAGNITUDE_PATTERN = Pattern.compile("(\\d[\\d,.]*\\d*)+([kKmMbBtT])");

    private static final NavigableMap<Integer, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000, "k");
        suffixes.put(1_000_000, "M");
        suffixes.put(1_000_000_000, "B");
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    /**
     * Formats a double number to look better with commas every 3 digits and up to two decimal places.
     * For example: {@code 1,006,789.5}
     *
     * @param number Number to format
     * @return Formatted string
     */
    public static String formatDouble(double number) {
        return NUMBER_FORMAT.format(number);
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
     * Strips icons from player names
     * @param input Text to strip icons from
     * @return Text without icons
     */
    public static String stripIcons(String input) {
        return STRIP_ICONS_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Strips icons and colors and trims spaces from a potential username
     * @param input Text to strip from
     * @return Stripped Text
     */
    public static String stripUsername(String input) {
        return trimWhitespaceAndResets(stripIcons(stripColor(stripPrefix((input)))));
    }

    public static String stripPrefix(String input) {
        return STRIP_PREFIX_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Computationally efficient way to test if a given string has a rendered length of 0
     * @param input string to test
     * @return {@code true} if the input string is length 0 or only contains repeated formatting codes
     */
    public static boolean isZeroLength(String input) {
        return input.length() == 0 || REPEATED_COLOR_PATTERN.matcher(input).matches();
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
     * Converts all numbers with magnitudes in a given string, e.g. "10k" -> "10000" and "10M" -> "10000000." Magnitudes
     * are not case-sensitive.
     *
     * <b>Supported magnitudes:</b>
     * <p>k - thousand</p>
     * <p>m - million</p>
     * <p>b - billion</p>
     * <p>t - trillion</p>
     * <p>
     * <p>
     * <b>Examples:</b>
     * <p>1k -> 1,000</p>
     * <p>2.5K -> 2,500</p>
     * <p>100M -> 100,000,000</p>
     *
     * @param text - Input text
     * @return Input text with converted magnitudes
     */
    public static String convertMagnitudes(String text) throws ParseException {
        Matcher matcher = MAGNITUDE_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            double parsedDouble = NUMBER_FORMAT.parse(matcher.group(1)).doubleValue();
            String magnitude = matcher.group(2).toLowerCase(Locale.ROOT);

            switch (magnitude) {
                case "k":
                    parsedDouble *= 1_000;
                    break;
                case "m":
                    parsedDouble *= 1_000_000;
                    break;
                case "b":
                    parsedDouble *= 1_000_000_000;
                    break;
                case "t":
                    parsedDouble *= 1_000_000_000_000L;
            }

            matcher.appendReplacement(sb, NUMBER_FORMAT.format(parsedDouble));
        }
        matcher.appendTail(sb);

        return sb.toString();
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
                    newString.append(new StringBuilder(textPart).reverse());
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

        return Base64.getEncoder().encodeToString(SkyblockAddons.getGson().toJson(root).getBytes(StandardCharsets.UTF_8));
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
        //noinspection IntegerDivisionInFloatingPointContext
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }


    /**
     * Removes all leading or trailing reset color codes and whitespace from a string.
     *
     * @param input Text to trim
     * @return Text without leading or trailing reset color codes and whitespace
     */
    public static String trimWhitespaceAndResets(String input) {
        return TRIM_WHITESPACE_RESETS.matcher(input).replaceAll("");
    }

    /**
     * Checks if text matches a Minecraft username
     *
     * @param input Text to check
     * @return Whether this input can be Minecraft username or not
     */
    public static boolean isUsername(String input) {
        return USERNAME_PATTERN.matcher(input).matches();
    }

    /**
     * Removes all reset color codes from a given text
     *
     * @param input Text to strip
     * @return Text with all reset color codes removed
     */
    public static String stripResets(String input) {
        return RESET_CODE_PATTERN.matcher(input).replaceAll("");
    }


    /**
     * Converts a string into proper case (Source: <a href="https://dev-notes.com">Dev Notes</a>)
     * @param inputString a string
     * @return a new string in which the first letter of each word is capitalized
     */
    public static String toProperCase(String inputString) {
        String ret;
        StringBuffer sb = new StringBuffer();
        Matcher match = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(inputString);
        while (match.find()) {
            match.appendReplacement(sb, match.group(1).toUpperCase() + match.group(2).toLowerCase());
        }
        ret = match.appendTail(sb).toString();
        return ret;
    }


    /**
     * Calculates and returns the first formatted substring that matches the unformatted string
     * <p>
     * Used for color/style compatibility mode.
     *
     * @param unformattedSubstring the uncolored/unstyled substring of which we request a match
     * @param formatted            the colored string, from which we request a substring
     * @return {@code null} if {@param unformattedSubstring} is not found in {@param formatted}, or the colored/styled substring.
     */
    public static String getFormattedString(String formatted, String unformattedSubstring) {
        if (unformattedSubstring.length() == 0) {
            return "";
        }
        String styles = "kKlLmMnNoO";
        StringBuilder preEnchantFormat = new StringBuilder();
        StringBuilder formattedEnchant = new StringBuilder();

        int i = -2;
        int len = formatted.length();
        int unformattedEnchantIdx = 0;
        int k = 0;
        while (true) {
            i = formatted.indexOf('§', i + 2);
            // No more formatting codes were found in the string
            if (i == -1) {
                // Test if there is an instance of the formatted enchant in the rest of the string
                for (; k < len; k++) {
                    // Enchant string matches at position k
                    if (formatted.charAt(k) == unformattedSubstring.charAt(unformattedEnchantIdx)) {
                        formattedEnchant.append(formatted.charAt(k));
                        unformattedEnchantIdx++;
                        // We have matched the entire enchant. Return the current format + the formatted enchant
                        if (unformattedEnchantIdx == unformattedSubstring.length()) {
                            return preEnchantFormat.append(formattedEnchant).toString();
                        }
                    }
                    // Enchant string doesn't match at position k
                    else {
                        unformattedEnchantIdx = 0;
                        // Transfer formats from formatted enchant to format
                        preEnchantFormat = new StringBuilder(mergeFormats(preEnchantFormat.toString(), formattedEnchant.toString()));
                        formattedEnchant = new StringBuilder();
                    }
                }
                // No matching enchant found
                return null;
            } else {
                for (; k < i; k++) {
                    if (formatted.charAt(k) == unformattedSubstring.charAt(unformattedEnchantIdx)) {
                        formattedEnchant.append(formatted.charAt(k));
                        unformattedEnchantIdx++;
                        // We have matched the entire enchant. Return the current format + the formatted enchant
                        if (unformattedEnchantIdx == unformattedSubstring.length()) {
                            return preEnchantFormat.append(formattedEnchant).toString();
                        }
                    } else {
                        unformattedEnchantIdx = 0;
                        // Transfer formats from formatted enchant to format
                        preEnchantFormat = new StringBuilder(mergeFormats(preEnchantFormat.toString(), formattedEnchant.toString()));
                        formattedEnchant = new StringBuilder();
                    }
                }
                // Add the format code if present
                if (i + 1 < len) {
                    char formatChar = formatted.charAt(i + 1);
                    // If not parsing an enchant, alter the pre enchant format
                    if (unformattedEnchantIdx == 0) {
                        // Restart format at a new color
                        if (styles.indexOf(formatChar) == -1) {
                            preEnchantFormat = new StringBuilder();
                        }
                        // Append the new format code to the formatter
                        preEnchantFormat.append("§").append(formatChar);
                    }
                    // If parsing an enchant, alter the current enchant format and the formatted enchant
                    else {
                        // Restart format at a new color
                        formattedEnchant.append("§").append(formatChar);
                    }
                    // Skip the formatting code "§[0-9a-zA-Z]" on the next round
                    k = i + 2;
                }
            }
        }
    }

    /**
     * Calculate the color/style formatting after first and second format strings
     * <p>
     * Used for: Given the color/style formatting before an enchantment. as well as the enchantment itself,
     * Calculate the color/style formatting after the enchantment
     *
     * @param firstFormat  the color/style formatting before the string
     * @param secondFormat the string that may have formatting codes within it
     * @return the relevant formatting codes in effect after {@param secondFormat}
     */
    private static String mergeFormats(String firstFormat, String secondFormat) {
        if (secondFormat == null || secondFormat.length() == 0) {
            return firstFormat;
        }
        String styles = "kKlLmMnNoO";
        StringBuilder builder = new StringBuilder(firstFormat);
        int i = -2;
        while ((i = secondFormat.indexOf('§', i + 2)) != -1) {
            if (i + 1 < secondFormat.length()) {
                char c = secondFormat.charAt(i + 1);
                // If it's not a style then it's a color code
                if (styles.indexOf(c) == -1) {
                    builder = new StringBuilder();
                }
                builder.append("§").append(c);
            }
        }
        return builder.toString();
    }
    /**
     * Recursively performs an action upon a chat component and its siblings
     * This code is adapted from Skytils
     * <p>
     * https://github.com/Skytils/SkytilsMod/commit/35b1fbed1613f07bd422c61dbe3d261218b8edc6
     * <p>
     * I, Sychic, the author of this code grant usage under the terms of the MIT License.
     * @param chatComponent root chat component
     * @param action action to be performed
     * @author Sychic
     */
    public static void transformAllChatComponents(IChatComponent chatComponent, Consumer<IChatComponent> action) {
        action.accept(chatComponent);
        for (IChatComponent sibling : chatComponent.getSiblings()) {
            transformAllChatComponents(sibling, action);
        }
    }

    /**
     * Recursively searches for a chat component to transform based on a given Predicate.
     *
     * Important to note that this function will stop on the first successful transformation, unlike {@link #transformAllChatComponents(IChatComponent, Consumer)}
     * @param chatComponent root chat component
     * @param action predicate that transforms a component and reports a successful transformation
     * @return Whether any transformation occurred
     */
    public static boolean transformAnyChatComponent(IChatComponent chatComponent, Predicate<IChatComponent> action) {
        if(action.test(chatComponent))
            return true;
        for (IChatComponent sibling : chatComponent.getSiblings()) {
            if(transformAnyChatComponent(sibling, action))
                return true;
        }
        return false;
    }
}