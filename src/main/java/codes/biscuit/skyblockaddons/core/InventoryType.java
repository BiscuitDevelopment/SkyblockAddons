package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPattern;
import lombok.Getter;

import static codes.biscuit.skyblockaddons.core.Message.*;

//TODO Fix for Hypixel localization

/**
 * This is an enum containing different menus in Skyblock. It's used in logic where the menu the player is in matters.
 */
public enum InventoryType {
    ENCHANTMENT_TABLE(INVENTORY_TYPE_ENCHANTS, "Enchant Item"),
    BASIC_REFORGING(INVENTORY_TYPE_REFORGES, "Reforge Item"),
    ADVANCED_REFORGING(INVENTORY_TYPE_REFORGES, "Reforge Item"),
    BAKER(null, "Baker"),
    CRAFTING_TABLE(INVENTORY_TYPE_CRAFTING, CraftingPattern.CRAFTING_TABLE_DISPLAYNAME);

    private final Message message;
    @Getter
    private final String inventoryName;

    InventoryType(Message message, String inventoryName) {
        this.message = message;
        this.inventoryName = inventoryName;
    }

    public String getMessage() {
        return message.getMessage();
    }
}
