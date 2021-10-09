package codes.biscuit.skyblockaddons.features.slayertracker;

import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

@Getter
public class SlayerTrackerData {

    private Map<SlayerBoss, Integer> slayerKills = new EnumMap<>(SlayerBoss.class);
    private Map<SlayerDrop, Integer> slayerDropCounts = new EnumMap<>(SlayerDrop.class);
    @Setter private SlayerBoss lastKilledBoss;
}
