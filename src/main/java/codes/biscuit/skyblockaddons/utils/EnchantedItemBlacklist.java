package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum EnchantedItemBlacklist {

    LAVA(Items.lava_bucket, false, Feature.AVOID_PLACING_ENCHANTED_ITEMS),
    DIAMOND(Item.getItemFromBlock(Blocks.diamond_block), false, Feature.AVOID_PLACING_ENCHANTED_ITEMS),
    STRING(Items.string, false, Feature.AVOID_PLACING_ENCHANTED_ITEMS),
    BLAZE_ROD(Items.blaze_rod, true, Feature.DISABLE_EMBER_ROD);

    private Item item;
    private boolean onlyOnIsland;
    private Feature feature;

    EnchantedItemBlacklist(Item item, boolean onlyOnIsland, Feature feature) {
        this.item = item;
        this.onlyOnIsland = onlyOnIsland;
        this.feature = feature;
    }

    public static boolean shouldBlockUsage(ItemStack itemStack) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        boolean blockUsage = false;
        if (itemStack != null && itemStack.isItemEnchanted()) {
            for (EnchantedItemBlacklist blacklistItem : EnchantedItemBlacklist.values()) {
                if (main.getConfigValues().isEnabled(blacklistItem.feature) && blacklistItem.item.equals(itemStack.getItem()) &&
                        (!blacklistItem.onlyOnIsland || main.getUtils().getLocation() == EnumUtils.Location.ISLAND) && !main.getUtils().isMaterialForRecipe(itemStack)) {
                    blockUsage = true; // ^ if the item is a recipe it'll be blocked server-side
                    break;
                }
            }
        }
        return blockUsage;
    }
}