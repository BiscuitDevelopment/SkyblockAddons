package codes.biscuit.skyblockaddons.utils.objects;

import lombok.Getter;

@Getter
public class FloatPairString {

    private FloatPair floatPair;
    private String enchant;

    public FloatPairString(float x, float y, String enchant) {
        this.floatPair = new FloatPair(x, y);
        this.enchant = enchant;
    }

    public float getX() {
        return floatPair.getX();
    }

    public float getY() {
        return floatPair.getY();
    }
}
