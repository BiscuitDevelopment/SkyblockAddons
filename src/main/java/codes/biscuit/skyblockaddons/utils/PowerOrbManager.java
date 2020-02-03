package codes.biscuit.skyblockaddons.utils;

import lombok.Getter;

import java.util.*;

/**
 * Class for managing active PowerOrbs around the player.
 * {@link #put(PowerOrb, int) Insert} orbs that get detected and {@link #get() get} the
 * active orb with the {@link PowerOrb#priority highest priority}.
 *
 * @author DidiSkywalker
 */
public class PowerOrbManager {

    /** The PowerOrbManager instance. */
    @Getter private static final PowerOrbManager instance = new PowerOrbManager();

    /**
     * Entry displaying {@link PowerOrb#RADIANT} at 20 seconds for the edit screen
     */
    public static final Entry DUMMY_ENTRY = new Entry(PowerOrb.RADIANT, 20);

    private Map<PowerOrb, Entry> powerOrbEntryMap = new HashMap<>();

    /**
     * Put any detected orb into the list of active orbs.
     *
     * @param powerOrb Detected PowerOrb type
     * @param seconds Seconds the orb has left before running out
     */
    public void put(PowerOrb powerOrb, int seconds) {
        powerOrbEntryMap.put(powerOrb, new Entry(powerOrb, seconds));
    }

    /**
     * Get the active orb with the highest priority. Priority is based on the value defined in
     * {@link PowerOrb#priority} and the returned orb is guaranteed to have been active at least 100ms ago.
     *
     * @return Highest priority orb or null if none is around
     */
    public Entry get() {
        Optional<Map.Entry<PowerOrb, Entry>> max = powerOrbEntryMap.entrySet().stream()
                .filter(powerOrbEntryEntry -> powerOrbEntryEntry.getValue().timestamp + 100 > System.currentTimeMillis())
                .max(Comparator.comparing(Map.Entry::getKey));

        return max.map(Map.Entry::getValue).orElse(null);
    }

    @Getter
    public static class Entry {
        /** The PowerOrb type. */
        private final PowerOrb powerOrb;

        /** Seconds the orb has left before running out */
        private final int seconds;

        private final long timestamp;

        private Entry(PowerOrb powerOrb, int seconds) {
            this.powerOrb = powerOrb;
            this.seconds = seconds;
            this.timestamp = System.currentTimeMillis();
        }
    }

}
