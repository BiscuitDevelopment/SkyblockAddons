package codes.biscuit.skyblockaddons;

import codes.biscuit.skyblockaddons.utils.TextUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link TextUtils}
 */
public class TextUtilsTests {

    @DisplayName("Action Bar Magnitude Conversion Tests")
    @ParameterizedTest()
    @CsvFileSource(resources = "/convert-magnitudes.csv", numLinesToSkip = 1)
    void testActionBarMagnitudeConversionS(String inputString, String expectedOutput) {
        try {
            assertEquals(expectedOutput, TextUtils.convertMagnitudes(inputString));
        } catch (ParseException e) {
            fail("Failed to parse number at offset " + e.getErrorOffset() + " in string \"" + e.getMessage() + "\".");
        }
    }
}
