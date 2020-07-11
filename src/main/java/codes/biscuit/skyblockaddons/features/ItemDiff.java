package codes.biscuit.skyblockaddons.features;

import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class ItemDiff {

    /**
     * How long items in the log should be displayed before they are removed in ms
     */
    public static final long LIFESPAN = 5000;

    /** The item's display name. */
    private final String displayName;

    /** The changed amount. */
    private int amount;

    @Getter(AccessLevel.NONE) private long timestamp;

    /**
     * @param displayName The item's display name.
     * @param amount      The changed amount.
     */
    public ItemDiff(String displayName, int amount) {
        this.displayName = displayName;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
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
