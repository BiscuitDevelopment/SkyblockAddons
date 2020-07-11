package codes.biscuit.skyblockaddons.features.bosstracker;

import lombok.Getter;

public class BossTrackerManager {

    @Getter
    private static final BossTrackerManager instance = new BossTrackerManager();
    @Getter
    private DragonBossTracker dragon;

    public BossTrackerManager()
    {
        dragon = new DragonBossTracker();
    }

    public void LoadPersistentValues() {
        dragon.LoadPersistentValues();
    }
}
