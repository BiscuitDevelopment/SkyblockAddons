package codes.biscuit.skyblockaddons.utils;

import lombok.Getter;

@Getter
public class EnchantPair {

    private float x;
    private float y;
    private String enchant;

    public EnchantPair(float x, float y, String enchant) {
        this.x = x;
        this.y = y;
        this.enchant = enchant;
    }
}
