package codes.biscuit.skyblockaddons.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class RomanNumeralsParserTest {
    @Test
    public void replaceNumeralsWithIntegers() throws Exception {
        String input = "Sharpness VIII, Vampirism V, Cubism X, Impaling III, Something elsevi";
        String expected = "Sharpness 8, Vampirism 5, Cubism 10, Impaling 3, Something elsevi";
        String result = RomanNumeralsParser.replaceNumeralsWithIntegers(input);
        assertEquals(expected, result);
    }

    @Test
    public void isNumeralValid() {
        // valid
        testValid("I", true);
        testValid("II", true);
        testValid("IV", true);
        testValid("V", true);
        testValid("VI", true);
        testValid("IX", true);
        testValid("X", true);
        testValid("L", true);
        testValid("C", true);
        testValid("D", true);
        testValid("M", true);
        testValid("MMCLXXXIX", true);

        // invalid numerals
        testValid("IIII", false);
        testValid("XXXX", false);

        // lowercase
        testValid("i", false);
        testValid("ii", false);
        testValid("iv", false);
        testValid("v", false);

        // invalid words starting with valid letters
        testValid("Impaling", false);
        testValid("Vampirism", false);

        // irrelevant words
        testValid("no numeral letters", false);
        testValid("", false);
    }

    @Test
    public void parseNumeral_success() {
        testParse("I", 1);
        testParse("II", 2);
        testParse("III", 3);
        testParse("IV", 4);
        testParse("V", 5);
        testParse("VI", 6);
        testParse("VII", 7);
        testParse("VIII", 8);
        testParse("IX", 9);
        testParse("X", 10);
        testParse("XX", 20);
        testParse("XXIV", 24);
        testParse("XXIX", 29);
        testParse("LXIX", 69);
        testParse("L", 50);
        testParse("C", 100);
        testParse("D", 500);
        testParse("M", 1000);
        testParse("MMCLXXXIX", 2189);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseNumeral_failure() {
        RomanNumeralsParser.parseNumeral("Invalid");
    }

    private void testParse(String input, int expected) {
        assertEquals(expected, RomanNumeralsParser.parseNumeral(input));
    }

    private void testValid(String input, boolean expected) {
        assertEquals(expected, RomanNumeralsParser.isNumeralValid(input));
    }

}