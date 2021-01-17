package codes.biscuit.skyblockaddons.core;

import lombok.Getter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum SkillType {
    FARMING("Farming", Items.golden_hoe),
    MINING("Mining", Items.diamond_pickaxe),
    COMBAT("Combat", Items.iron_sword),
    FORAGING("Foraging", Item.getItemFromBlock(Blocks.sapling)),
    FISHING("Fishing", Items.fishing_rod),
    ENCHANTING("Enchanting", Item.getItemFromBlock(Blocks.enchanting_table)),
    ALCHEMY("Alchemy", Items.brewing_stand),
    CARPENTRY("Carpentry", Item.getItemFromBlock(Blocks.crafting_table)),
    RUNECRAFTING("Runecrafting", Items.magma_cream),
    TAMING("Taming", Items.spawn_egg),
    DUNGEONEERING("Dungeoneering", Item.getItemFromBlock(Blocks.deadbush));

    private String skillName;
    @Getter
    private ItemStack item;

    SkillType(String skillName, Item item) {
        this.skillName = skillName;
        this.item = new ItemStack(item);
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
