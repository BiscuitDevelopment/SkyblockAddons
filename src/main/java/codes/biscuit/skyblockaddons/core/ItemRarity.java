package codes.biscuit.skyblockaddons.core;

import lombok.Getter;

/**
 * Skyblock item rarity definitions
 */
@Getter
public enum ItemRarity {

    COMMON("§f§lCOMMON"),
    UNCOMMON("§a§lUNCOMMON"),
    RARE("§9§lRARE"),
    EPIC("§5§lEPIC"),
    LEGENDARY("§6§lLEGENDARY"),
    SPECIAL("§d§lSPECIAL");

    private final String tag;

    ItemRarity(String s) {
        this.tag = s;
    }
}
