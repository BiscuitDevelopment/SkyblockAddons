package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.utils.ColorCode;

/**
 * Skyblock item rarity definitions
 */
public enum ItemRarity {

    COMMON("COMMON", ColorCode.WHITE),
    UNCOMMON("UNCOMMON", ColorCode.GREEN),
    RARE("RARE", ColorCode.BLUE),
    EPIC("EPIC", ColorCode.DARK_PURPLE),
    LEGENDARY("LEGENDARY", ColorCode.GOLD),
    MYTHIC("MYTHIC", ColorCode.LIGHT_PURPLE),
    SPECIAL("SPECIAL", ColorCode.RED),
    VERY_SPECIAL("VERY SPECIAL", ColorCode.RED);

    public final String TAG;
    public final ColorCode COLOR_CODE;

    ItemRarity(String loreName, ColorCode colorCode) {
        this.TAG = loreName;
        this.COLOR_CODE = colorCode;
    }
}
