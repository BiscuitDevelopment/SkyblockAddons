package codes.biscuit.skyblockaddons.constants.game;

import lombok.Getter;

/**
 * Skyblock item rarity definitions
 */
@Getter
public enum Rarity {
    COMMON("§f§lCOMMON"),
    UNCOMMON("§a§lUNCOMMON"),
    RARE("§9§lRARE"),
    EPIC("§5§lEPIC"),
    LEGENDARY("§6§lLEGENDARY"),
    SPECIAL("§d§lSPECIAL");

    private String tag;

    Rarity(String s) {
        this.tag = s;
    }
}
