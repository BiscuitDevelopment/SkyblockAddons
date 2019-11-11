package codes.biscuit.skyblockaddons.utils;

/**
 * Class for easy cooldown management
 */
public class CooldownEntry {

    /**
     * Entry with no cooldown
     */
    static final CooldownEntry NULL_ENTRY = new CooldownEntry(0);

    private long cooldown;
    private long lastUse;

    /**
     * Create a new CooldownEntry
     *
     * @param cooldown Cooldown in milliseconds
     */
    CooldownEntry(long cooldown) {
        this.cooldown = cooldown;
        this.lastUse = System.currentTimeMillis();
    }

    /**
     * Check whether this entry is on cooldown
     *
     * @return {@code true} if the cooldown is still active, {@code false} if it ran out
     */
    boolean isOnCooldown() {
        return System.currentTimeMillis() < (lastUse + cooldown);
    }

    /**
     * Get the remaining cooldown in milliseconds
     *
     * @return Milliseconds until the cooldown runs out
     */
    long getRemainingCooldown() {
        long diff = (lastUse + cooldown) - System.currentTimeMillis();
        return diff <= 0 ? 0 : diff;
    }

    /**
     * Get the remaining cooldown as a Percentage of the remaining time to the base cooldown
     *
     * @return Percentage between {@code 0 to 1} or {@code 0} if not on cooldown
     */
    double getRemainingCooldownPercent() {
        return isOnCooldown() ? ((double) getRemainingCooldown()) / ((double) cooldown) : 0;
    }

}
