package codes.biscuit.skyblockaddons.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.*;

public class PersistentValues {

	private File persistentValuesFile;
	private JsonObject valuesObject = new JsonObject();
	
	private int kills;
	
	public PersistentValues(File configDir) {
		this.persistentValuesFile = new File(configDir.getParentFile().getAbsolutePath()+"/skyblockaddons_persistent.cfg");
	}
	
	public void loadValues() {
		if(persistentValuesFile.exists()) {
			try {
				FileReader reader = new FileReader(persistentValuesFile);
				BufferedReader bufferedReader = new BufferedReader(reader);
				StringBuilder builder = new StringBuilder();
				String nextLine;
				while ((nextLine = bufferedReader.readLine()) != null) {
					builder.append(nextLine);
				}
				String complete = builder.toString();
				JsonElement fileElement = new JsonParser().parse(complete);
				if (fileElement == null || fileElement.isJsonNull()) {
					throw new JsonParseException("File is null!");
				}
				valuesObject = fileElement.getAsJsonObject();
			} catch (IOException | NumberFormatException ex) {
				ex.printStackTrace();
				System.out.println("SkyblockAddons: There was an error loading saved values.");
				saveCounter();
			}
		} else {
			saveCounter();
		}
	}
	
	public void saveCounter() {
		valuesObject = new JsonObject();
		try {
			persistentValuesFile.createNewFile();
			FileWriter writer = new FileWriter(persistentValuesFile);
			BufferedWriter bufferedWriter = new BufferedWriter(writer);

			valuesObject.addProperty("kills", kills);

			bufferedWriter.write(valuesObject.toString());
			bufferedWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("SkyblockAddons: An error occurred while attempted to save values!");
		}
	}
	
	public void addKill() {
		kills++;
		saveCounter();
	}
	
	public void setKills(int kills) {
		this.kills = kills;
		saveCounter();
	}
	
	public int getKills() {
		return kills;
	}

}
