package codes.biscuit.skyblockaddons.features.powerorbs;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;

/**
 * Represents the Power Orbs introduced with the Slayer Update and unlocked through the Wolf slayer quests.
 *
 * @author DidiSkywalker
 */
@Getter
public enum PowerOrb {

    RADIANT("§aRadiant", 0.01, 0, 0, 0, 18*18, "radiant"),
    MANA_FLUX("§9Mana Flux", 0.02, 0.5, 10, 0, 18*18, "manaflux"),
    OVERFLUX("§5Overflux", 0.025, 1, 25, 0.05, 18*18, "overflux"),
    PLASMAFLUX("§d§lPlasmaflux", 0.03, 1.25, 35, 0.075, 20*20, "plasmaflux");

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
     * The squared range of the orb effects
     */
    private int rangeSquared;
    /**
     * Resource location to the icon used when displaying the orb
     */
    private ResourceLocation resourceLocation;

    PowerOrb(String display, double healthRegen, double manaRegen, int strength, double healIncrease, int rangeSquared, String resourcePath) {
        this.display = display;
        this.healthRegen = healthRegen;
        this.manaRegen = manaRegen;
        this.strength = strength;
        this.healIncrease = healIncrease;
        this.rangeSquared = rangeSquared;
        this.resourceLocation = new ResourceLocation("skyblockaddons", "powerorbs/"+resourcePath+".png");
    }

    /**
     * Check if a distance is within this orbs radius.
     *
     * @param distanceSquared Squared distance from orb entity to player
     * @return Whether that distance is within radius
     */
    public boolean isInRadius(double distanceSquared) {
        return distanceSquared <= rangeSquared;
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
