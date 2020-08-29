package codes.biscuit.skyblockaddons.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextUtilsTest {

    @Test
    void stripColor() {
        Assertions.assertEquals("Hypixel", TextUtils.stripColor("§1Hypixel"));
        Assertions.assertEquals("Hypixel", TextUtils.stripColor("Hypixel"));
    }
}