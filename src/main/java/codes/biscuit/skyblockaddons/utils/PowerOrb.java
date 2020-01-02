package codes.biscuit.skyblockaddons.utils;

import net.minecraft.util.ResourceLocation;

/**
 * Represents the Power Orbs introduced with the Slayer Update and unlocked through the Wolf slayer quests.
 *
 * @author DidiSkywalker
 */
public enum PowerOrb {

    RADIANT(0, "§aRadiant", 0.01, 0, 0, 0, 18*18, new ResourceLocation("skyblockaddons", "powerorbs/radiant.png")),
    MANA_FLUX(1, "§9Mana Flux", 0.02, 0.5, 10, 0, 18*18, new ResourceLocation("skyblockaddons", "powerorbs/mana_flux.png")),
    OVERFLUX(2, "§5Overflux", 0.025, 1, 25, 0.05, 18*18, new ResourceLocation("skyblockaddons", "powerorbs/overflux.png"));

    /**
     * Higher priority takes effect over lower priority
     */
    public final int priority;
    /**
     * Start of the display name of the actual floating orb entity.
     */
    public final String display;
    /**
     * Percentage of max health that's regenerated every second
     */
    public final double healthRegen;
    /**
     * Percentage of mana regeneration increase given by the orb
     */
    public final double manaRegen;
    /**
     * Amount of strength given by the orb
     */
    public final int strength;
    /**
     * Percentage value that all healing is increased by within orb radius
     */
    public final double healIncrease;
    /**
     * The orbs effective radius - squared to compare with a squared distance
     */
    public final int radiusSquared;
    /**
     * Resource location to the icon used when displaying the orb
     */
    public final ResourceLocation resourceLocation;

    PowerOrb(int priority, String display, double healthRegen, double manaRegen, int strength, double healIncrease, int radiusSquared, ResourceLocation resourceLocation) {
        this.priority = priority;
        this.display = display;
        this.healthRegen = healthRegen;
        this.manaRegen = manaRegen;
        this.strength = strength;
        this.healIncrease = healIncrease;
        this.radiusSquared = radiusSquared;
        this.resourceLocation = resourceLocation;
    }

    /**
     * Check if a distance is within this orbs radius.
     *
     * @param distanceSquared Squared distance from orb entity to player
     * @return Whether that distance is within radius
     */
    public boolean isInRadius(double distanceSquared) {
        return distanceSquared <= radiusSquared;
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
