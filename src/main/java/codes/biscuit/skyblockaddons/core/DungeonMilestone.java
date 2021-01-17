package codes.biscuit.skyblockaddons.core;

import lombok.Getter;

@Getter
public class DungeonMilestone {

    public static DungeonMilestone getZeroMilestone(DungeonClass dungeonClass) {
        return new DungeonMilestone(dungeonClass, "⓿", "0");
    }

    private DungeonClass dungeonClass;
    private String level;
    private String value;

    public DungeonMilestone(DungeonClass dungeonClass, String level, String value) {
        this.dungeonClass = dungeonClass;
        this.level = level;
        this.value = value;
    }
}
