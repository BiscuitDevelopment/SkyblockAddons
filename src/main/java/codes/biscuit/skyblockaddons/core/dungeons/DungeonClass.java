package codes.biscuit.skyblockaddons.core.dungeons;

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

    @Getter private char firstLetter;
    @Getter private ItemStack item;
    @Getter private String chatDisplayName; // The way Hypixel writes it out in chat

    DungeonClass(Item item, String chatDisplayName) {
        this.firstLetter = this.name().charAt(0);
        this.item = new ItemStack(item);
        this.chatDisplayName = chatDisplayName;
    }

    public static DungeonClass fromFirstLetter(char firstLetter) {
        for (DungeonClass dungeonClass : DungeonClass.values()) {
            if (dungeonClass.firstLetter == firstLetter) {
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
