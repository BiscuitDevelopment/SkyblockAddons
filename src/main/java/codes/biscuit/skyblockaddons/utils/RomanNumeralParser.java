package codes.biscuit.skyblockaddons.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for working with Roman numerals
 * @author DidiSkywalker
 */
public class RomanNumeralParser {

    /**
     * Pattern that validates a string as a correct Roman numeral
     */
    private static final Pattern NUMERAL_VALIDATION_PATTERN = Pattern.compile("^(?=[MDCLXVI])M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
    /**
     * Pattern that finds words that begin with a Roman numeral
     */
    private static final Pattern NUMERAL_FINDING_PATTERN = Pattern.compile(" ((?=[MDCLXVI])M*(C[MD]|D?C{0,3})(X[CL]|L?X{0,3})(I[XV]|V?I{0,3}))\\w*");

    private enum Numeral {

        I(1),
        V(5),
        X(10),
        L(50),
        C(100),
        D(500),
        M(1000);

        private final int value;

        Numeral(int value) {
            this.value = value;
        }

        private static Numeral getFromChar(char c) {
            try {
                return Numeral.valueOf(Character.toString(c));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Expected valid Roman numeral, received " + c);
            }
        }
    }

    /**
     * Replaces all occurrences of Roman numerals in an input string with their integer values.
     * For example: VI -> 6, X -> 10, etc
     *
     * @param input Input string to replace numerals in
     * @return The input string with all numerals replaced by integers
     */
    public static String replaceNumeralsWithIntegers(String input) {
        StringBuilder result = new StringBuilder(input);
        int offsetByReplacement = 0; // Because integers and numerals can be of different lengths, keep track of the produced offset from replacing
        Matcher matcher = NUMERAL_FINDING_PATTERN.matcher(input);
        while (matcher.find()) {
            // The matcher finds all words that begin with a Roman numeral as groups
            String group = matcher.group().trim(); // trim to remove the required leading space
            if (isNumeralValid(group)) { // check if that word is actually a valid numeral in itself (to catch things like Vampirism)
                // capturing group 1 matches the actual numeral
                int startIndex = matcher.start(1);
                int endIndex = matcher.end(1);
                int parsedInteger = parseNumeral(group);
                String parsedIntegerString = String.valueOf(parsedInteger);
                int lengthDiff = group.length() - parsedIntegerString.length();
                result.replace(startIndex + offsetByReplacement, endIndex + offsetByReplacement, parsedIntegerString);
                offsetByReplacement -= lengthDiff;
            }
        }
        return result.toString();
    }

    /**
     * Tests whether an input string is a valid Roman numeral.
     * To be valid the numerals must be either {@code I, V, X, L, C, D, M} and in upper case
     * and in correct format (meaning {@code IIII} is invalid as it should be {@code IV})
     *
     * @param romanNumeral String to test
     * @return Whether that string represents a valid Roman numeral
     */
    private static boolean isNumeralValid(String romanNumeral) {
        return NUMERAL_VALIDATION_PATTERN.matcher(romanNumeral).matches();
    }

    /**
     * Parses a valid Roman numeral string to its integer value.
     * Use {@link #isNumeralValid(String)} to check.
     *
     * @param numeralString Numeral to parse
     * @return Parsed value
     * @throws IllegalArgumentException If the input is malformed
     */
    private static int parseNumeral(String numeralString) {
        int value = 0; // parsed value
        char[] charArray = numeralString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            Numeral numeral = Numeral.getFromChar(c);
            if (i + 1 < charArray.length) {
                // check next numeral to correctly evaluate IV, IX and so forth
                Numeral nextNumeral = Numeral.getFromChar(charArray[i + 1]);
                int diff = nextNumeral.value - numeral.value;
                if (diff > 0) {
                    // if the next numeral is of higher value, it means their difference should be added instead
                    value += diff;
                    i++; // skip next char
                    continue;
                }
            }
            value += numeral.value;
        }
        return value;
    }

}