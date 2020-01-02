package codes.biscuit.skyblockaddons.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PersistentValues {

	private File persistentValuesFile;

	private int kills;

	public PersistentValues(File configDir) {
		this.persistentValuesFile = new File(configDir.getParentFile().getAbsolutePath() + "/skyblockaddons_persistent.cfg");
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
				ex.printStackTrace();
				System.out.println("SkyblockAddons: There was an error loading saved values.");
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
			ex.printStackTrace();
			System.out.println("SkyblockAddons: An error occurred while attempted to save values!");
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

	public int getKills() {
		return this.kills;
	}
}
