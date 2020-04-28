package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.constants.game.Rarity;
import lombok.Getter;

import java.util.List;

/**
 * This is the list used by the Stop Dropping/Selling Rare Items feature to determine if items can be dropped or sold.
 * It is used by {@link ItemDropChecker} to check if items can be dropped/sold.
 *
 * @author ILikePlayingGames
 * @version 1.0
 */
@Getter
class ItemDropList {
    /** Items in the inventory (excluding the hotbar) that are at or above this rarity are prohibited from being dropped/sold */
    private Rarity minimumInventoryRarity;
    /** Items in the hotbar that are at or above this rarity are prohibited from being dropped/sold */
    private Rarity minimumHotbarRarity;
    /** Items with a rarity below the minimum that can't be dropped, takes precedence over the whitelist */
    private List<String> blacklist;
    /** Items with a rarity above the minimum that is allowed to be dropped */
    private List<String> whitelist;
}
