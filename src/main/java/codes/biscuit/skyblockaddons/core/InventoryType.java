package codes.biscuit.skyblockaddons.core;

import lombok.Getter;

import java.util.regex.Pattern;

/**
 * This is an enum containing different menus in Skyblock. It's used in logic where the menu the player is in matters.
 */
public enum InventoryType {
    ENCHANTMENT_TABLE("Enchant Item", "Enchant Item"),
    BASIC_REFORGING("Reforge Item", "Reforge Item"),
    ADVANCED_REFORGING("Reforge Item (Advanced)", "Reforge Item (Advanced)"),
    BAKER("Baker", "Baker"),
    CRAFTING_TABLE("Craft Item", "Craft Item"),
    SALVAGING("Salvage Dungeon Item", "Salvage Dungeon Item"),
    ULTRASEQUENCER("Ultrasequencer", "Ultrasequencer \\((?<type>[a-zA-Z]+)\\)"),
    CHRONOMATRON("Chronomatron", "Chronomatron \\((?<type>[a-zA-Z]+)\\)"),
    SUPERPAIRS("Superpairs", "Superpairs \\((?<type>[a-zA-Z]+)\\)"),
    STORAGE("Storage", "Storage"),
    STORAGE_BACKPACK("BackpackStorage", "(?<type>[a-zA-Z]+) Backpack ?✦? \\((?<page>\\d+)/\\d+\\)"),
    SKILL_TYPE_MENU("Skill Type Menu", "(?<type>[a-zA-Z]+) Skill"),
    ENDER_CHEST("EnderChest", "Ender Chest \\((?<page>\\d+)/\\d+\\)");

    @Getter
    private final String inventoryName;
    @Getter
    private final Pattern inventoryPattern;

    InventoryType(String inventoryName, String regex) {
        this.inventoryName = inventoryName;
        this.inventoryPattern = Pattern.compile(regex);
    }
}
