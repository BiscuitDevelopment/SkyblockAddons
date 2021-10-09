package codes.biscuit.skyblockaddons.core.dungeons;

import lombok.Getter;

@Getter
public class DungeonMilestone {

    private DungeonClass dungeonClass;
    private String level;
    private String value;

    public DungeonMilestone(DungeonClass dungeonClass) {
        this(dungeonClass, "⓿", "0");
    }

    public DungeonMilestone(DungeonClass dungeonClass, String level, String value) {
        this.dungeonClass = dungeonClass;
        this.level = level;
        this.value = value;
    }
}
