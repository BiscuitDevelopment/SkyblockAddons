package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.common.collect.Sets;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import java.util.Set;

public class Blacklist {

    public static Set<BlacklistedItem> DO_NOT_PLACE = Sets.newHashSet(
            new BlacklistedItem(Items.LAVA_BUCKET, true, false, false, Feature.AVOID_PLACING_ENCHANTED_ITEMS),
            new BlacklistedItem(Item.getItemFromBlock(Blocks.END_STONE), true, false, false, Feature.AVOID_PLACING_ENCHANTED_ITEMS),
            new BlacklistedItem(Items.STRING, true, false, true, Feature.AVOID_PLACING_ENCHANTED_ITEMS)
    );

    public static Set<BlacklistedItem> DO_NOT_RIGHT_CLICK = Sets.newHashSet(
            new BlacklistedItem(Items.BLAZE_ROD, true, true, false, Feature.DISABLE_EMBER_ROD)
    );

    public static class BlacklistedItem {

        private final Item item;
        private final boolean onlyEnchanted;
        private final boolean onlyOnIsland;
        private final boolean ctrlKeyBypass;
        private final Feature feature;

        BlacklistedItem(Item item, boolean onlyEnchanted, boolean onlyOnIsland, boolean ctrlKeyBypass, Feature feature) {
            this.item = item;
            this.onlyEnchanted = onlyEnchanted;
            this.onlyOnIsland = onlyOnIsland;
            this.ctrlKeyBypass = ctrlKeyBypass;
            this.feature = feature;
        }

        public boolean canCtrlKeyBypass() {
            return this.ctrlKeyBypass;
        }

        public Item getItem() {
            return this.item;
        }

        public Feature getFeature() {
            return this.feature;
        }

        public boolean isDisabled() {
            return this.feature != null && SkyblockAddons.getInstance().getConfigValues().isDisabled(this.feature);
        }

        public boolean isEnabled() {
            return !this.isDisabled();
        }

        public boolean isOnlyEnchanted() {
            return this.onlyEnchanted;
        }

        public boolean isOnlyOnIsland() {
            return this.onlyOnIsland;
        }

    }

}