package codes.biscuit.skyblockaddons.core;

import lombok.Getter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum SkillType {
    FARMING("Farming", Items.golden_hoe, false),
    MINING("Mining", Items.diamond_pickaxe, false),
    COMBAT("Combat", Items.iron_sword, false),
    FORAGING("Foraging", Item.getItemFromBlock(Blocks.sapling), false),
    FISHING("Fishing", Items.fishing_rod, false),
    ENCHANTING("Enchanting", Item.getItemFromBlock(Blocks.enchanting_table), false),
    ALCHEMY("Alchemy", Items.brewing_stand, false),
    CARPENTRY("Carpentry", Item.getItemFromBlock(Blocks.crafting_table), true),
    RUNECRAFTING("Runecrafting", Items.magma_cream, true),
    TAMING("Taming", Items.spawn_egg, false),
    DUNGEONEERING("Dungeoneering", Item.getItemFromBlock(Blocks.deadbush), false),
    SOCIAL("Social", Items.cake, true);

    private final String skillName;
    @Getter
    private final ItemStack item;
    @Getter
    private final boolean cosmetic;

    SkillType(String skillName, Item item, boolean isCosmetic) {
        this.skillName = skillName;
        this.item = new ItemStack(item);
        this.cosmetic = isCosmetic;
    }

    public static SkillType getFromString(String text) {
        for (SkillType skillType : values()) {
            if (skillType.skillName != null && skillType.skillName.equals(text)) {
                return skillType;
            }
        }
        return null;
    }
}
