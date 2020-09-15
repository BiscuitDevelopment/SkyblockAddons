package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPattern;
import lombok.Getter;

import static codes.biscuit.skyblockaddons.core.Message.*;

//TODO Fix for Hypixel localization

/**
 * This is an enum containing different menus in Skyblock. It's used in logic where the menu the player is in matters.
 */
public enum InventoryType {
    ENCHANTMENT_TABLE("Enchant Item"),
    BASIC_REFORGING("Reforge Item"),
    ADVANCED_REFORGING("Reforge Item"),
    BASIC_ACCESSORY_BAG_REFORGING("Reforge Accessory Bag"),
    ADVANCED_ACCESSORY_BAG_REFORGING("Reforge Accessory Bag"),
    BAKER("Baker"),
    CRAFTING_TABLE(CraftingPattern.CRAFTING_TABLE_DISPLAYNAME);

    @Getter
    private final String inventoryName;

    InventoryType(String inventoryName) {
        this.inventoryName = inventoryName;
    }
}
