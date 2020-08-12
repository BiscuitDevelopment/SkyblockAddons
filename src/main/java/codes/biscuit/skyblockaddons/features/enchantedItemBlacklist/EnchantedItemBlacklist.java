package codes.biscuit.skyblockaddons.features.enchantedItemBlacklist;

import codes.biscuit.skyblockaddons.core.ItemRarity;

import java.util.List;

/**
 * This is the blacklist used by the "Avoid Placing Enchanted Items" feature to determine which items to block.
 * This list is loaded from a file in {@link codes.biscuit.skyblockaddons.utils.DataReader}.
 *
 * @see EnchantedItemPlacementBlocker
 */
public class EnchantedItemBlacklist {
    /** This is the list of all the item IDs of the enchanted items that the player will not be allowed to place on their island. */
    List<String> enchantedItemIds;
    /** This is the minimum rarity to block for enchanted blocks that aren't yet on the blacklist. */
    ItemRarity rarityLimit;

    /**
     * Creates a new instance of {@code EnchantedItemBlacklist} with variables set to the values given.
     *
     * @param enchantedItemIds the list of item IDs of the enchanted items to blacklist
     * @param rarityLimit the minimum rarity to blacklist
     */
    public EnchantedItemBlacklist(List<String> enchantedItemIds, ItemRarity rarityLimit) {
        this.enchantedItemIds = enchantedItemIds;
        this.rarityLimit = rarityLimit;
    }
}