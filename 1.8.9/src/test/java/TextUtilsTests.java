import static org.junit.jupiter.api.Assertions.assertEquals;

import codes.biscuit.skyblockaddons.utils.TextUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TextUtils}
 */
public class TextUtilsTests {
    @Test
    void formatDouble() {
        assertEquals("1", TextUtils.formatDouble(1));
        assertEquals("1.57", TextUtils.formatDouble(1.57));
        assertEquals("-5.2", TextUtils.formatDouble(-5.2));
        assertEquals("1,000", TextUtils.formatDouble(1000));
        assertEquals("2,000.95", TextUtils.formatDouble(2000.95));
        assertEquals("10,000", TextUtils.formatDouble(10000));
        assertEquals("1,000,000.9", TextUtils.formatDouble(1000000.90));
    }
}
