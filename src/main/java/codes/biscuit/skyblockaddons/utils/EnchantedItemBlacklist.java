package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public enum EnchantedItemBlacklist {

    LAVA(Items.lava_bucket, false, Feature.AVOID_PLACING_ENCHANTED_ITEMS, true),
    DIAMOND(Item.getItemFromBlock(Blocks.diamond_block), false, Feature.AVOID_PLACING_ENCHANTED_ITEMS,true),
    STRING(Items.string, false, Feature.AVOID_PLACING_ENCHANTED_ITEMS, true),
    BLAZE_ROD(Items.blaze_rod, true, Feature.DISABLE_EMBER_ROD, false);

    private Item item;
    private boolean onlyOnIsland;
    private Feature feature;
    private boolean onlyBlockPlacement;

    EnchantedItemBlacklist(Item item, boolean onlyOnIsland, Feature feature, boolean onlyBlockPlacement) {
        this.item = item;
        this.onlyOnIsland = onlyOnIsland;
        this.feature = feature;
        this.onlyBlockPlacement = onlyBlockPlacement;
    }

    public static boolean shouldBlockUsage(ItemStack itemStack, PlayerInteractEvent.Action action) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        boolean blockUsage = false;
        if (itemStack != null && itemStack.isItemEnchanted()) {
            for (EnchantedItemBlacklist blacklistItem : EnchantedItemBlacklist.values()) {
                if (main.getConfigValues().isEnabled(blacklistItem.feature) && blacklistItem.item.equals(itemStack.getItem()) &&
                        (!blacklistItem.onlyOnIsland || main.getUtils().getLocation() == EnumUtils.Location.ISLAND) && !main.getUtils().isMaterialForRecipe(itemStack)) {

                    if (action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR && blacklistItem.onlyBlockPlacement) continue;

                    blockUsage = true; // ^ if the item is a recipe it'll be blocked server-side
                    break;
                }
            }
        }
        return blockUsage;
    }
}