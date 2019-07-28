package codes.biscuit.skyblockaddons.utils;

import net.minecraft.item.ItemStack;

public class BackpackInfo {

    private int x;
    private int y;
    private ItemStack[] items;
    private Feature.Backpack backpack;

    public BackpackInfo(int x, int y, ItemStack[] items, Feature.Backpack backpack) {
        this.x = x;
        this.y = y;
        this.items = items;
        this.backpack = backpack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public Feature.Backpack getBackpack() {
        return backpack;
    }
}
