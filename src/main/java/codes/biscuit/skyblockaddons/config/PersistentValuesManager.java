package codes.biscuit.skyblockaddons.config;

import codes.biscuit.hypixellocalizationlib.HypixelLanguage;
import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPattern;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTracker;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@Setter @Getter
public class PersistentValuesManager {

    private final File persistentValuesFile;

    private PersistentValues persistentValues = new PersistentValues();

    @Getter @Setter
    public static class PersistentValues {

        private int kills = 0; // Kills since last eye
        private int totalKills = 0; // Lifetime zealots killed
        private int summoningEyeCount = 0; // Lifetime summoning eyes

        private SlayerTracker slayerDrops = new SlayerTracker();
        private DragonTracker dragonTracker = new DragonTracker();

        private boolean blockCraftingIncompletePatterns = true;
        private CraftingPattern selectedCraftingPattern = CraftingPattern.FREE;

        private int oresMined = 0;
        private int seaCreaturesKilled = 0;

        private HypixelLanguage hypixelLanguage = HypixelLanguage.ENGLISH;
    }

    public PersistentValuesManager(File configDir) {
        this.persistentValuesFile = new File(configDir.getAbsolutePath() + "/skyblockaddons_persistent.cfg");
    }

    public void loadValues() {
        if (this.persistentValuesFile.exists()) {

            try (FileReader reader = new FileReader(this.persistentValuesFile)) {
                persistentValues = SkyblockAddons.getGson().fromJson(reader, PersistentValues.class);

            } catch (Exception ex) {
                SkyblockAddons.getLogger().error("There was an error while trying to load persistent values.");
                SkyblockAddons.getLogger().catching(ex);
                this.saveValues();
            }

        } else {
            this.saveValues();
        }
    }

    public void saveValues() {
        try {
            persistentValuesFile.createNewFile();

            try (FileWriter writer = new FileWriter(this.persistentValuesFile)) {
                SkyblockAddons.getGson().toJson(this.persistentValues, writer);
            }
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while attempting to save persistent values!");
            SkyblockAddons.getLogger().catching(ex);
        }
    }

    public void addEyeResetKills() {
        this.persistentValues.summoningEyeCount++;
        this.persistentValues.totalKills += this.persistentValues.kills;
        this.persistentValues.kills = -1; // This is triggered before the death of the killed zealot, so the kills are set to -1 to account for that.
        this.saveValues();
    }
}
