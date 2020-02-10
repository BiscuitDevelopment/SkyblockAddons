package codes.biscuit.skyblockaddons.utils;

import lombok.Getter;

@Getter
public enum Attribute {

    DEFENCE(0),
    HEALTH(100),
    MAX_HEALTH(100),
    MANA(100),
    MAX_MANA(100);

    private int defaultValue;

    Attribute(int defaultValue) {
        this.defaultValue = defaultValue;
    }
}
