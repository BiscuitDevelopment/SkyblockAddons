package codes.biscuit.skyblockaddons.utils;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for managing active PowerOrbs around the player.
 * {@link #put(PowerOrb, int) Insert} orbs that get detected and {@link #getActivePowerOrb() get} the
 * active orb with the highest priority (enum ordinal).
 *
 * @author DidiSkywalker
 */
public class PowerOrbManager {

    private static final Pattern POWER_ORB_PATTERN = Pattern.compile("[A-Za-z ]* (?<seconds>[0-9]*)s");

    /** The PowerOrbManager instance. */
    @Getter private static final PowerOrbManager instance = new PowerOrbManager();

    /**
     * Entry displaying {@link PowerOrb#RADIANT} at 20 seconds for the edit screen
     */
    public static final PowerOrbEntry DUMMY_POWER_ORB_ENTRY = new PowerOrbEntry(PowerOrb.RADIANT, 20);

    private Map<PowerOrb, PowerOrbEntry> powerOrbEntryMap = new HashMap<>();

    /**
     * Put any detected orb into the list of active orbs.
     *
     * @param powerOrb Detected PowerOrb type
     * @param seconds Seconds the orb has left before running out
     */
    private void put(PowerOrb powerOrb, int seconds) {
        powerOrbEntryMap.put(powerOrb, new PowerOrbEntry(powerOrb, seconds));
    }

    /**
     * Get the active orb with the highest priority. Priority is based on enum value's ordinal
     * and the returned orb is guaranteed to have been active at least 100ms ago.
     *
     * @return Highest priority orb or null if none is around
     */
    public PowerOrbEntry getActivePowerOrb() {
        Optional<Map.Entry<PowerOrb, PowerOrbEntry>> max = powerOrbEntryMap.entrySet().stream()
                .filter(powerOrbEntryEntry -> powerOrbEntryEntry.getValue().timestamp + 100 > System.currentTimeMillis())
                .max(Map.Entry.comparingByKey());

        return max.map(Map.Entry::getValue).orElse(null);
    }

    /**
     * Detects a power orb from an entity, and puts it in this manager.
     *
     * @param entity The entity to detect whether it is a power orb or not.
     */
    public void detectPowerOrb(Entity entity) {
        String customNameTag = entity.getCustomNameTag();
        PowerOrb powerOrb = PowerOrb.getByDisplayname(customNameTag);
        if (powerOrb != null && Minecraft.getMinecraft().thePlayer != null &&
                powerOrb.isInRadius(entity.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer))) { // TODO Make sure this works...
            customNameTag = TextUtils.stripColor(customNameTag);
            Matcher matcher = POWER_ORB_PATTERN.matcher(customNameTag);
            if (matcher.matches()) {
                String secondsString = matcher.group("seconds");
                try {
                    // Apparently they don't have a second count for moment after spawning, that's what this try-catch is for
                    put(powerOrb, Integer.parseInt(secondsString));
                } catch (NumberFormatException ex) {
                    // It's okay, just don't add the power orb I guess...
                }
            }
        }
    }

    @Getter
    public static class PowerOrbEntry {
        /** The PowerOrb type. */
        private final PowerOrb powerOrb;

        /** Seconds the orb has left before running out */
        private final int seconds;

        private final long timestamp;

        private PowerOrbEntry(PowerOrb powerOrb, int seconds) {
            this.powerOrb = powerOrb;
            this.seconds = seconds;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
