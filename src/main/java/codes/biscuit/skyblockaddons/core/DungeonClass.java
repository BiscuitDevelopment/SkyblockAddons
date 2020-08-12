package codes.biscuit.skyblockaddons.core;

import lombok.Getter;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum DungeonClass {
    HEALER(Items.potionitem),
    ARCHER(Items.bow),
    TANK(Items.leather_chestplate),
    MAGE(Items.blaze_rod),
    BERSERKER(Items.iron_sword);

    @Getter private String firstLetter;
    @Getter private ItemStack item;

    DungeonClass(Item item) {
        this.firstLetter = this.name().substring(0, 1);
        this.item = new ItemStack(item);
    }

    public static DungeonClass fromFirstLetter(String firstLetter) {
        for (DungeonClass dungeonClass : DungeonClass.values()) {
            if (dungeonClass.firstLetter.equals(firstLetter)) {
                return dungeonClass;
            }
        }
        return null;
    }
}
