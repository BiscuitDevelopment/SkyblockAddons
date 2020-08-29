package codes.biscuit.skyblockaddons.utils.nifty;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringUtilTest {

    @Test
    void isEmpty() {
        Assertions.assertTrue(StringUtil.isEmpty(null));
        Assertions.assertTrue(StringUtil.isEmpty(""));
        Assertions.assertFalse(StringUtil.isEmpty("SkyblockAddons"));
    }

    @Test
    void notEmpty() {
        Assertions.assertFalse(StringUtil.notEmpty(null));
        Assertions.assertFalse(StringUtil.notEmpty(""));
        Assertions.assertTrue(StringUtil.notEmpty("SkyblockAddons"));
    }

    @Test
    void split() {
        String[] result1 = StringUtil.split(" ", "Hypixel");
        Assertions.assertEquals(1, result1.length);
        Assertions.assertEquals("Hypixel", result1[0]);

        String[] result2 = StringUtil.split(" ", "Hypixel Skyblock");
        Assertions.assertEquals(2, result2.length);
        Assertions.assertEquals("Hypixel", result2[0]);
        Assertions.assertEquals("Skyblock", result2[1]);

        String[] result3 = StringUtil.split(" ", "Hypixel Skyblock Addons");
        Assertions.assertEquals(3, result3.length);
        Assertions.assertEquals("Hypixel", result3[0]);
        Assertions.assertEquals("Skyblock", result3[1]);
        Assertions.assertEquals("Addons", result3[2]);

        String[] result4 = StringUtil.split(" ", null);
        Assertions.assertEquals(0, result4.length);

        String[] result5 = StringUtil.split(" ", "");
        Assertions.assertEquals(0, result5.length);
    }

    @Test
    void repeat() {
        Assertions.assertEquals("aaa", StringUtil.repeat("a", 3));
        Assertions.assertEquals("abcabcabcabcabcabcabcabcabc", StringUtil.repeat("abc", 3));
    }
}