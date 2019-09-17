package codes.biscuit.skyblockaddons.utils;

public class ItemDiff {

    /**
     * How long items in the log should be displayed before they are removed in ms
     */
    static final long LIFESPAN = 5000;

    private final String displayName;
    private int amount;
    private long timestamp;

    /**
     * @param displayName The item's display name
     * @param amount      The changed amount
     */
    public ItemDiff(String displayName, int amount) {
        this.displayName = displayName;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Update the changed amount of the item
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
     * @return The item's display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return The changed amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @return Amount of ms since the ItemDiff was created
     */
    long getLifetime() {
        return System.currentTimeMillis() - timestamp;
    }
}
