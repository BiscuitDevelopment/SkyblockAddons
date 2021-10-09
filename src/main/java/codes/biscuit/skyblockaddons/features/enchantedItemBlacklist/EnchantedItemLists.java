package codes.biscuit.skyblockaddons.features.enchantedItemBlacklist;

import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.utils.DataUtils;
import lombok.Data;

import java.util.List;

/**
 * This is the blacklist and whitelist used by the "Avoid Placing Enchanted Items" feature to determine which items to block.
 * This list is loaded from a file in {@link DataUtils}.
 *
 * @see EnchantedItemPlacementBlocker
 */
@Data
public class EnchantedItemLists {
    /** This is the list of all the item IDs of the enchanted items that the player will not be allowed to place on their island. */
    List<String> blacklistedIDs;
    /**
     * This is the list of all the item IDs of the enchanted items above the rarity limit that the player will be allowed
     * to place on their island.
     *  */
    List<String> whitelistedIDs;
    /** This is the minimum rarity to block for enchanted items that aren't yet on one of the lists. */
    ItemRarity rarityLimit;

    /**
     * Creates a new instance of {@code EnchantedItemLists} with variables set to the values given.
     *
     * @param blacklistedIDs the list of item IDs of the enchanted items to blacklist
     * @param rarityLimit the minimum rarity to blacklist
     */
    public EnchantedItemLists(List<String> blacklistedIDs, List<String> whitelistedIDs, ItemRarity rarityLimit) {
        this.blacklistedIDs = blacklistedIDs;
        this.whitelistedIDs = whitelistedIDs;
        this.rarityLimit = rarityLimit;
    }
}