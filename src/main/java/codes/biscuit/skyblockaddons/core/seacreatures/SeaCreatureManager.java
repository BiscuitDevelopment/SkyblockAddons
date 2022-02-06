package codes.biscuit.skyblockaddons.core.seacreatures;

import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static codes.biscuit.skyblockaddons.core.ItemRarity.LEGENDARY;


public class SeaCreatureManager {

    private static final SeaCreatureManager INSTANCE = new SeaCreatureManager();

    @Getter
    private final Set<String> allSeaCreatureSpawnMessages = new HashSet<>();
    @Getter
    private final Set<String> legendarySeaCreatureSpawnMessages = new HashSet<>();

    /**
     * Populate sea creature information from local and online sources
     */
    public void setSeaCreatures(Map<String, SeaCreature> seaCreatures) {

        allSeaCreatureSpawnMessages.clear();
        legendarySeaCreatureSpawnMessages.clear();
        for (SeaCreature sc : seaCreatures.values()) {
            allSeaCreatureSpawnMessages.add(sc.getSpawnMessage());
            if (sc.getRarity() == LEGENDARY) {
                legendarySeaCreatureSpawnMessages.add(sc.getSpawnMessage());
            }
        }
    }

    public static SeaCreatureManager getInstance() {
        return INSTANCE;
    }
}
