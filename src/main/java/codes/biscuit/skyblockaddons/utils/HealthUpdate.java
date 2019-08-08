package codes.biscuit.skyblockaddons.utils;

public class HealthUpdate {

    public static final int LIFESPAN = 1000;

    private final int healthChange;
    private final long timestamp;

    public HealthUpdate(int healthChange, long timestamp) {
        this.healthChange = healthChange;
        this.timestamp = timestamp;
    }

    public int getHealthChange() {
        return healthChange;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLifetime() {
        return System.currentTimeMillis() - timestamp;
    }
}
