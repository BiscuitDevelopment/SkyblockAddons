package codes.biscuit.skyblockaddons.config;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.bosstracker.BossTrackerManager;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPattern;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@Setter
@Getter
public class PersistentValues {

    private final File persistentValuesFile;
    private final Logger logger;

    private int kills;
    private int totalKills;
    private int summoningEyeCount;
    @Getter
    private JsonObject slayerDrops, dragonStats;

    private boolean blockCraftingIncompletePatterns = true;
    private CraftingPattern selectedCraftingPattern = CraftingPattern.FREE;

    public PersistentValues(File configDir) {
        this.persistentValuesFile = new File(configDir.getAbsolutePath() + "/skyblockaddons_persistent.cfg");
        logger = SkyblockAddons.getInstance().getLogger();
    }

    public void loadValues() {
        if (this.persistentValuesFile.exists()) {
            try (FileReader reader = new FileReader(this.persistentValuesFile)) {
                JsonElement fileElement = new JsonParser().parse(reader);

                if (fileElement == null || fileElement.isJsonNull()) {
                    throw new JsonParseException("File is null!");
                }

                JsonObject valuesObject = fileElement.getAsJsonObject();

                this.kills = valuesObject.has("kills") ? valuesObject.get("kills").getAsInt() : 0;

                this.totalKills = valuesObject.has("totalKills") ? valuesObject.get("totalKills").getAsInt() : 0;

                this.summoningEyeCount = valuesObject.has("summoningEyeCount") ? valuesObject.get("summoningEyeCount").getAsInt() : 0;

                this.slayerDrops = valuesObject.has("slayerDrops") ? valuesObject.getAsJsonObject("slayerDrops") : new JsonObject();
                SlayerTracker.getInstance().LoadPersistentValues();
                this.dragonStats = valuesObject.has("dragonStats") ? valuesObject.getAsJsonObject("dragonStats") : new JsonObject();
                BossTrackerManager.getInstance().LoadPersistentValues();

            } catch (Exception ex) {
                logger.error("SkyblockAddons: There was an error while trying to load persistent values.");
                logger.error(ex.getMessage());
                this.saveValues();
            }

        } else {
            this.saveValues();
        }
    }

    public void saveValues() {
        JsonObject valuesObject = new JsonObject();

        try (FileWriter writer = new FileWriter(this.persistentValuesFile)) {
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            valuesObject.addProperty("kills", this.kills);
            valuesObject.addProperty("totalKills", this.totalKills);
            valuesObject.addProperty("summoningEyeCount", this.summoningEyeCount);
            valuesObject.add("slayerDrops", SlayerTracker.getInstance().SavePersistentValues());
            valuesObject.add("dragonStats", BossTrackerManager.getInstance().getDragon().SavePersistentValues());

            bufferedWriter.write(valuesObject.toString());
            bufferedWriter.close();
        } catch (Exception ex) {
            logger.error("SkyblockAddons: An error occurred while attempting to save persistent values!");
            logger.error(ex.getMessage());
        }
    }

    public void addKill() {
        this.kills++;
        this.saveValues();
    }

    public void addEyeResetKills() {
        this.summoningEyeCount++;
        this.totalKills += this.kills;
        this.kills = -1; // This is triggered before the death of the killed zealot, so the kills are set to -1 to account for that.
        this.saveValues();
    }

    public void setBlockCraftingIncompletePatterns(boolean blockCraftingIncompletePatterns) {
        this.blockCraftingIncompletePatterns = blockCraftingIncompletePatterns;
        this.saveValues();
    }

    public void setSelectedCraftingPattern(CraftingPattern selectedCraftingPattern) {
        this.selectedCraftingPattern = selectedCraftingPattern;
        this.saveValues();
    }
}
