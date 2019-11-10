package codes.biscuit.skyblockaddons.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.FMLLog;

public enum PowerOrb {

    RADIANT(0, "§aRadiant", 0.01, 0, 0, 0, 18*18),
    MANA_FLUX(1, "§9Mana Flux", 0.02, 0.5, 10, 0, 18*18),
    OVERFLUX(2, "§5Overflux", 0.025, 1, 25, 0.05, 18*18);

    /**
     * Higher priority takes effect over lower priority
     */
    public final int priority;
    /**
     * Start of the display name of the actual floating orb.
     */
    public final String display;
    public final double healthRegen;
    public final double manaRegen;
    public final int strength;
    public final double healIncrease;
    public final int radiusSquared;

    PowerOrb(int priority, String display, double healthRegen, double manaRegen, int strength, double healIncrease, int radiusSquared) {
        this.priority = priority;
        this.display = display;
        this.healthRegen = healthRegen;
        this.manaRegen = manaRegen;
        this.strength = strength;
        this.healIncrease = healIncrease;
        this.radiusSquared = radiusSquared;
    }

    public boolean isInRadius(double distanceSquared) {
        return distanceSquared <= radiusSquared;
    }

    public static PowerOrb getByDisplayname(String displayName) {
        for (PowerOrb powerOrb : values()) {
//            if(displayName.startsWith(powerOrb.display)) {
            if(displayName.startsWith(powerOrb.display.replaceAll("§\\w", ""))) {
                return powerOrb;
            }
        }
        return null;
    }

    public static class ActivePowerOrb {
        private PowerOrb powerOrb;
        private int seconds;

        public ActivePowerOrb(PowerOrb powerOrb, int seconds) {
            this.powerOrb = powerOrb;
            this.seconds = seconds;
        }

        public PowerOrb getPowerOrb() {
            return powerOrb;
        }

        public int getSeconds() {
            return seconds;
        }

        public void setPowerOrb(PowerOrb powerOrb) {
            this.powerOrb = powerOrb;
        }

        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        @Override
        public String toString() {
            return "ActivePowerOrb{" +
                    "powerOrb=" + powerOrb +
                    ", seconds=" + seconds +
                    '}';
        }
    }
}
