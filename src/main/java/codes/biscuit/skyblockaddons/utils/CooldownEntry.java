package codes.biscuit.skyblockaddons.utils;

import net.minecraft.item.Item;

public class CooldownEntry {

    private Item item;
    private String itemName;
    private int cooldownSeconds;

    private long lastUse = System.currentTimeMillis();

    CooldownEntry(Item item, String itemName, int cooldownSeconds) {
        this.item = item;
        this.itemName = itemName;
        this.cooldownSeconds = cooldownSeconds;
    }

    public Item getItem() {
        return item;
    }

    String getItemName() {
        return itemName;
    }

    void setLastUse() {
        this.lastUse = System.currentTimeMillis();
    }

    public double getCooldown() {
        double cooldown = (double)(System.currentTimeMillis()-lastUse)/getCooldownMillis();
        if (cooldown > 1) cooldown = 1;
        return cooldown;
    }

    public long getLastUse() {
        return lastUse;
    }

    public int getCooldownMillis() {
        return cooldownSeconds*1000;
    }
}
