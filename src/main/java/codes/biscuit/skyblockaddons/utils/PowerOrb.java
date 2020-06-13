package codes.biscuit.skyblockaddons.utils;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;

/**
 * Represents the Power Orbs introduced with the Slayer Update and unlocked through the Wolf slayer quests.
 *
 * @author DidiSkywalker
 */
@Getter
public enum PowerOrb {

    RADIANT("§aRadiant", 0.01, 0, 0, 0, "radiant"),
    MANA_FLUX("§9Mana Flux", 0.02, 0.5, 10, 0, "manaflux"),
    OVERFLUX("§5Overflux", 0.025, 1, 25, 0.05, "overflux");

    /**
     * The orbs effective radius - squared to compare with a squared distance
     */
    private static final int RADIUS_SQUARED = 18*18;

    /**
     * Start of the display name of the actual floating orb entity.
     */
    private String display;
    /**
     * Percentage of max health that's regenerated every second
     */
    private double healthRegen;
    /**
     * Percentage of mana regeneration increase given by the orb
     */
    private double manaRegen;
    /**
     * Amount of strength given by the orb
     */
    private int strength;
    /**
     * Percentage value that all healing is increased by within orb radius
     */
    private double healIncrease;
    /**
     * Resource location to the icon used when displaying the orb
     */
    private ResourceLocation resourceLocation;

    PowerOrb(String display, double healthRegen, double manaRegen, int strength, double healIncrease, String resourcePath) {
        this.display = display;
        this.healthRegen = healthRegen;
        this.manaRegen = manaRegen;
        this.strength = strength;
        this.healIncrease = healIncrease;
        this.resourceLocation = new ResourceLocation("skyblockaddons", "powerorbs/"+resourcePath+".png");
    }

    /**
     * Check if a distance is within this orbs radius.
     *
     * @param distanceSquared Squared distance from orb entity to player
     * @return Whether that distance is within radius
     */
    public boolean isInRadius(double distanceSquared) {
        return distanceSquared <= RADIUS_SQUARED;
    }

    /**
     * Match an entity display name against Power Orb entity names to get the corresponding type.
     *
     * @param displayName Entity display name
     * @return The matching type or null if none was found
     */
    public static PowerOrb getByDisplayname(String displayName) {
        for (PowerOrb powerOrb : values()) {
            if(displayName.startsWith(powerOrb.display)) {
                return powerOrb;
            }
        }
        return null;
    }
}
