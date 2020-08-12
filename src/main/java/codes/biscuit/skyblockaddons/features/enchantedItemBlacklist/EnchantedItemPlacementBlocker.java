package codes.biscuit.skyblockaddons.features.enchantedItemBlacklist;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * This class is the main class of the "Avoid Placing Enchanted Items" feature. Whenever a player tries to place an item
 * on their private island, this class is used to check if the action should be blocked or allowed.
 */
public class EnchantedItemPlacementBlocker {
    private static final SkyblockAddons MAIN = SkyblockAddons.getInstance();

    @Setter
    private static EnchantedItemBlacklist blacklist;

    /**
     * Determine if the placement of this item should be blocked.
     *
     * @param itemStack the item being placed
     * @param interactEvent the {@code PlayerInteractEvent} that was triggered when the player used the item
     * @return {@code true} if the usage should be blocked, {@code false} otherwise.
     */
    public static boolean shouldBlockPlacement(ItemStack itemStack, PlayerInteractEvent interactEvent) {
        if (itemStack == null) {
            throw new NullPointerException();
        }

        String skyblockItemID = ItemUtils.getSkyBlockItemID(itemStack);

        // Don't block non-Skyblock items.
        if (skyblockItemID == null) {
            return false;
        }

        /*
        Block placing blocks only on the private island since that's the only place players can place blocks at.
        Also block both actions RIGHT_CLICK_BLOCK and RIGHT_CLICK_AIR because 2 events are sent,
        one with the first action and one with the second.
         */
        if (MAIN.getUtils().getLocation() == Location.ISLAND &&
                interactEvent.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            for (String itemId : blacklist.enchantedItemIds) {
                if (skyblockItemID.equals(itemId)) {
                    // If the item is a material for a recipe, placement will be blocked server-side.
                    if (!ItemUtils.isMaterialForRecipe(itemStack)) {
                        return true;
                    }
                }
            }

            Item item = itemStack.getItem();

            /*
             If this item isn't found in the blacklist, check if it's an enchanted block with a rarity above the minimum.
             ItemReed is included because it's the class of some blocks like flowerpots and repeaters.
             */
            return Block.getBlockFromItem(item) != null || item instanceof ItemReed && itemStack.isItemEnchanted() &&
                    blacklist.rarityLimit.compareTo(ItemUtils.getRarity(itemStack)) <= 0;
        }

        return false;
    }
}