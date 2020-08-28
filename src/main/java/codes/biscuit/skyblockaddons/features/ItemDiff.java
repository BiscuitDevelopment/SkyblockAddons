package codes.biscuit.skyblockaddons.features;

import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;

@Getter
public class ItemDiff {

    /**
     * How long items in the log should be displayed before they are removed in ms
     */
    public static final long LIFESPAN = 5000;

    /** The item's display name. */
    private final String displayName;

    /**
     * The item's ExtraAttributes from the NBT
     */
    @Getter
    private final NBTTagCompound extraAttributes;

    /** The changed amount. */
    private int amount;

    @Getter(AccessLevel.NONE) private long timestamp;

    /**
     * @param displayName The item's display name.
     * @param amount      The changed amount.
     */
    public ItemDiff(String displayName, int amount) {
        this(displayName, amount, null);
    }

    /**
     * @param displayName The item's display name.
     * @param amount      The changed amount.
     * @param extraAttributes The Skyblock NBT data of the first item detected
     */
    public ItemDiff(String displayName, int amount, NBTTagCompound extraAttributes) {
        this.displayName = displayName;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.extraAttributes = extraAttributes;
    }

    /**
     * Update the changed amount of the item.
     *
     * @param amount Amount to be added
     */
    public void add(int amount) {
        this.amount += amount;
        if (this.amount == 0) {
            this.timestamp -= LIFESPAN;
        } else {
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * @return Amount of time in ms since the ItemDiff was created.
     */
    public long getLifetime() {
        return System.currentTimeMillis() - timestamp;
    }
}
