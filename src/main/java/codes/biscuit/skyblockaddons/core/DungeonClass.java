package codes.biscuit.skyblockaddons.core;

import lombok.Getter;

public enum DungeonClass {

    HEALER,
    ARCHER,
    TANK,
    MAGE,
    BERSERKER;

    @Getter private String firstLetter;

    DungeonClass() {
        this.firstLetter = this.name().substring(0, 1);
    }

    public static DungeonClass fromFirstLetter(String firstLetter) {
        for (DungeonClass dungeonClass : DungeonClass.values()) {
            if (dungeonClass.firstLetter.equals(firstLetter)) {
                return dungeonClass;
            }
        }
        return null;
    }
}
