package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public enum EnchantedItemBlacklist {

    ENCHANTED_LAVA_BUCKET("ENCHANTED_LAVA_BUCKET", false, Feature.AVOID_PLACING_ENCHANTED_ITEMS, true),
    ENCHANTED_DIAMOND_BLOCK("ENCHANTED_DIAMOND_BLOCK", false, Feature.AVOID_PLACING_ENCHANTED_ITEMS,true),
    ENCHANTED_SNOW("ENCHANTED_SNOW_BLOCK", false, Feature.AVOID_PLACING_ENCHANTED_ITEMS, true),
    ENCHANTED_STRING("ENCHANTED_STRING", false, Feature.AVOID_PLACING_ENCHANTED_ITEMS, true),
    ENCHANTED_WOOL("ENCHANTED_WOOL", true, Feature.AVOID_PLACING_ENCHANTED_ITEMS, true),
    WEIRD_TUBA("WEIRD_TUBA", false, Feature.AVOID_PLACING_ENCHANTED_ITEMS, true),
    EMBER_ROD("EMBER_ROD", true, Feature.DISABLE_EMBER_ROD, false);

    private String itemId;
    private boolean onlyOnIsland;
    private Feature feature;
    private boolean onlyBlockPlacement;

    /**
     * Adds a new entry to the enchanted item blacklist.
     *
     * @param itemId the Skyblock Item ID of the item
     * @param onlyOnIsland block the item on the player's island only if true
     * @param feature the feature that controls the blocking of this item
     * @param onlyBlockPlacement stop the item from being placed, but not from being used.
     */
    EnchantedItemBlacklist(String itemId, boolean onlyOnIsland, Feature feature, boolean onlyBlockPlacement) {
        this.itemId = itemId;
        this.onlyOnIsland = onlyOnIsland;
        this.feature = feature;
        this.onlyBlockPlacement = onlyBlockPlacement;
    }

    /**
     * Determine if the usage of this item should be blocked.
     *
     * @param itemStack the item being used
     * @param action the action the player is doing with the item
     * @return true if the usage should be blocked, false otherwise.
     */
    public static boolean shouldBlockUsage(ItemStack itemStack, PlayerInteractEvent.Action action) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        boolean blockUsage = false;

        for (EnchantedItemBlacklist blacklistItem : EnchantedItemBlacklist.values()) {
            if (main.getConfigValues().isEnabled(blacklistItem.feature) && !blacklistItem.isBucket() &&
                    blacklistItem.itemId.equals(ItemUtils.getSkyBlockItemID(itemStack)) &&
                    (!blacklistItem.onlyOnIsland || main.getUtils().getLocation() == Location.ISLAND) &&
                    !main.getUtils().isMaterialForRecipe(itemStack)) {

                if (action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && blacklistItem.onlyBlockPlacement) continue;

                blockUsage = true; // ^ if the item is a recipe it'll be blocked server-side
                break;
            }
        }
        return blockUsage;
    }

    /**
     * Checks if the given item from this blacklist is a bucket.
     * This is used to handle buckets separately from other blocks and items.
     *
     * @return true if the item is a bucket, false otherwise
     * @see codes.biscuit.skyblockaddons.listeners.PlayerListener#onBucketEvent(FillBucketEvent)
     */
    private boolean isBucket() {
        return itemId.contains("BUCKET");
    }
}