package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.utils.ColorCode;
import lombok.Getter;

/**
 * Skyblock item rarity definitions
 */
@Getter
public enum ItemRarity {

    COMMON("COMMON", ColorCode.WHITE),
    UNCOMMON("UNCOMMON", ColorCode.GREEN),
    RARE("RARE", ColorCode.BLUE),
    EPIC("EPIC", ColorCode.DARK_PURPLE),
    LEGENDARY("LEGENDARY", ColorCode.GOLD),
    MYTHIC("MYTHIC", ColorCode.LIGHT_PURPLE),
    DIVINE("DIVINE", ColorCode.AQUA),

    SPECIAL("SPECIAL", ColorCode.RED),
    VERY_SPECIAL("VERY SPECIAL", ColorCode.RED);

    /** The name of the rarity as displayed in an item's lore */
    private final String loreName;
    /** The color code for the color of the rarity as it's displayed in an item's lore */
    private final ColorCode colorCode;

    ItemRarity(String loreName, ColorCode colorCode) {
        this.loreName = loreName;
        this.colorCode = colorCode;
    }
}
