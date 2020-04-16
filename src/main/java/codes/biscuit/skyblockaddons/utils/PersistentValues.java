package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@Getter
public class PersistentValues {

    private final File persistentValuesFile;
    private final Logger logger;

    private int kills;

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

            } catch (Exception ex) {
                logger.error("SkyblockAddons: There was an error loading persistent values.");
                logger.error(ex.getMessage());
                this.saveCounter();
            }

        } else {
            this.saveCounter();
        }
    }

    private void saveCounter() {
        JsonObject valuesObject = new JsonObject();

        try (FileWriter writer = new FileWriter(this.persistentValuesFile)) {
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            valuesObject.addProperty("kills", this.kills);

            bufferedWriter.write(valuesObject.toString());

        } catch (Exception ex) {
            logger.error("SkyblockAddons: An error occurred while attempted to save values!");
            logger.error(ex.getMessage());
        }
    }

    public void addKill() {
        this.kills++;
        this.saveCounter();
    }

    public void setKills(int kills) {
        this.kills = kills;
        this.saveCounter();
    }
}
