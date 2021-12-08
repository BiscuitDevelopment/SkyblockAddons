package codes.biscuit.skyblockaddons.core;

import lombok.Getter;

/**
 * Skyblock item type definitions
 * <p>
 * Item types are displayed in the item's lore beside the rarity.
 */
@Getter
public enum ItemType {

    // Tools and Weapons
    AXE("AXE"),
    BOW("BOW"),
    DRILL("DRILL"),
    FISHING_ROD("FISHING ROD"),
    FISHING_WEAPON("FISHING WEAPON"),
    GAUNTLET("GAUNTLET"),
    HOE("HOE"),
    SHEARS("SHEARS"),
    SHOVEL("SHOVEL"),
    SWORD("SWORD"),
    PICKAXE("PICKAXE"),
    WAND("WAND"),

    // Armor
    HELMET("HELMET"),
    CHESTPLATE("CHESTPLATE"),
    LEGGINGS("LEGGINGS"),
    BOOTS("BOOTS"),

    // Other
    ACCESSORY("ACCESSORY"),
    COSMETIC("COSMETIC"),
    DUNGEON_ITEM("DUNGEON ITEM"),
    /** Used when the item has only a rarity and no item type */
    OTHER("");

    /** The name of this item type as shown in the item's lore */
    private final String loreName;

    ItemType(String loreName) {
        this.loreName = loreName;
    }
}
