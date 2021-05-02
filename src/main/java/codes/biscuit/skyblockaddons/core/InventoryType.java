package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPattern;
import lombok.Getter;

import java.util.regex.Pattern;

//TODO Fix for Hypixel localization

/**
 * This is an enum containing different menus in Skyblock. It's used in logic where the menu the player is in matters.
 */
public enum InventoryType {
    ENCHANTMENT_TABLE("Enchant Item", "Enchant Item"),
    BASIC_REFORGING("Reforge Item", "Reforge Item"),
    ADVANCED_REFORGING("Reforge Item", "Reforge Item"),
    BASIC_ACCESSORY_BAG_REFORGING("Reforge Accessory Bag", "Reforge Accessory Bag"),
    ADVANCED_ACCESSORY_BAG_REFORGING("Reforge Accessory Bag", "Reforge Accessory Bag"),
    BAKER("Baker", "Baker"),
    CRAFTING_TABLE(CraftingPattern.CRAFTING_TABLE_DISPLAYNAME, CraftingPattern.CRAFTING_TABLE_DISPLAYNAME),
    SALVAGING("Salvage Dungeon Item", "Salvage Dungeon Item"),
    ULTRASEQUENCER("Ultrasequencer", "Ultrasequencer \\((?<type>[a-zA-Z]+)\\)"),
    CHRONOMATRON("Chronomatron", "Chronomatron \\((?<type>[a-zA-Z]+)\\)"),
    SUPERPAIRS("Superpairs", "Superpairs \\((?<type>[a-zA-Z]+)\\)"),
    STORAGE("Storage", "Storage"),
    STORAGE_BACKPACK("BackpackStorage", "(?<type>[a-zA-Z]+) Backpack \\((?<page>[0-9]+)/[0-9]+\\)"),
    SKILL_TYPE_MENU("Skill Type Menu", "(?<type>[a-zA-Z]+) Skill");

    @Getter
    private final String inventoryName;
    @Getter
    private final Pattern inventoryPattern;

    InventoryType(String inventoryName, String regex) {
        this.inventoryName = inventoryName;
        this.inventoryPattern = Pattern.compile(regex);
    }
}
