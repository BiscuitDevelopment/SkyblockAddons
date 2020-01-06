package codes.biscuit.skyblockaddons.utils;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

/**
 * Collection of text/string related utility methods
 */
public class TextUtils {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private static final Pattern NUMBERS_SLASHES = Pattern.compile("[^0-9 /]");
    private static final Pattern LETTERS_NUMBERS = Pattern.compile("[^a-z A-Z:0-9/']");
    private static final Pattern FLOAT_CHARACTERS = Pattern.compile("[^.0-9\\-]");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###.##");

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
     * Removes any character that isn't a number or letter from a given text.
     *
     * @param text Input text
     * @return Input text with only letters and numbers
     */
    public static String keepLettersAndNumbersOnly(String text) {
        return LETTERS_NUMBERS.matcher(text).replaceAll("");
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
}
