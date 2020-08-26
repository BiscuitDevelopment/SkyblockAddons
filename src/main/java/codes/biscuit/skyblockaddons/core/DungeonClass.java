package codes.biscuit.skyblockaddons.core;

import lombok.Getter;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum DungeonClass {
    HEALER(Items.potionitem, "Healer"),
    ARCHER(Items.bow, "Archer"),
    TANK(Items.leather_chestplate, "Tank"),
    MAGE(Items.blaze_rod, "Mage"),
    BERSERKER(Items.iron_sword, "Berserk");

    @Getter private String firstLetter;
    @Getter private ItemStack item;
    @Getter private String chatDisplayName;

    DungeonClass(Item item, String chatDisplayName) {
        this.firstLetter = this.name().substring(0, 1);
        this.item = new ItemStack(item);
        this.chatDisplayName = chatDisplayName;
    }

    public static DungeonClass fromFirstLetter(String firstLetter) {
        for (DungeonClass dungeonClass : DungeonClass.values()) {
            if (dungeonClass.firstLetter.equals(firstLetter)) {
                return dungeonClass;
            }
        }
        return null;
    }

    public static DungeonClass fromDisplayName(String name) {
        for (DungeonClass dungeonClass : DungeonClass.values()) {
            if (dungeonClass.getChatDisplayName().equals(name)) {
                return dungeonClass;
            }
        }
        return null;
    }
}
