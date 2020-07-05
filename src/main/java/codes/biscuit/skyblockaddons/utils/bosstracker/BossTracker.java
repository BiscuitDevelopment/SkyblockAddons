package codes.biscuit.skyblockaddons.utils.bosstracker;

import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.Utils;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;

public abstract class BossTracker {
    @Getter @Setter
    public Date stopAcceptingTimestamp, secondPriorTimestamp;
    /**
     * The "feature" setting that determines if this boss' stats should be rendered
     */
    @Getter
    private Feature feature;
    /**
     * The name used in storing/loading the boss stats
     */
    @Getter
    private String bossName;
    @Getter @Setter
    private int kills = 0;

    public BossTracker(Feature feature, String bossName) {
        this.feature = feature;
        this.bossName = bossName;
    }

    public String getTranslatedStat(String stat) {
        return Utils.getTranslatedString("settings." + bossName, stat);
    }

    public abstract ArrayList<Stat> getStats();

    public abstract void LoadPersistentValues();

    public abstract JsonObject SavePersistentValues();

    public class Stat {
        @Getter
        private String name;
        @Getter @Setter
        private int count = 0;
        @Getter
        private Rarity rarity;

        public Stat(String name, Rarity rarity) {
            this.name = name;
            this.rarity = rarity;
        }
    }
}
