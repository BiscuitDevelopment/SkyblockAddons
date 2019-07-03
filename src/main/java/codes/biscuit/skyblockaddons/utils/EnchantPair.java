package codes.biscuit.skyblockaddons.utils;

public class EnchantPair {

    private float x;
    private float y;
    private String enchant;


    public EnchantPair(float x, float y, String enchant) {
        this.x = x;
        this.y = y;
        this.enchant = enchant;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getEnchant() {
        return enchant;
    }
}
