package codes.biscuit.skyblockaddons.utils;

import com.google.gson.*;

import java.io.*;
import java.util.EnumSet;
import java.util.Set;

public class ConfigValues {

    private File configFile;
    private JsonObject loadedConfig = new JsonObject();

    private Set<Feature> disabledFeatures = EnumSet.noneOf(Feature.class);
    private ConfigColor warningColor = ConfigColor.RED;
    private ConfigColor confirmationColor = ConfigColor.GRAY;
    private int warningSeconds = 4;

    public ConfigValues(File configFile) {
        this.configFile = configFile;
    }

    public void loadConfig() {
        if (configFile.exists()) {
            try {
                FileReader reader = new FileReader(configFile);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder builder = new StringBuilder();
                String nextLine;
                while ((nextLine = bufferedReader.readLine()) != null) {
                    builder.append(nextLine);
                }
                String complete = builder.toString();
                loadedConfig = new JsonParser().parse(complete).getAsJsonObject();

                for (JsonElement element : loadedConfig.getAsJsonArray("disabledFeatures")) {
                    disabledFeatures.add(Feature.fromId(element.getAsInt()));
                }
                warningColor = ConfigColor.values()[loadedConfig.get("warningColor").getAsInt()];
                confirmationColor = ConfigColor.values()[loadedConfig.get("confirmationColor").getAsInt()];
                warningSeconds = loadedConfig.get("warningSeconds").getAsInt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            saveConfig();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveConfig() {
        loadedConfig = new JsonObject();
        try {
            configFile.createNewFile();
            FileWriter writer = new FileWriter(configFile);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            JsonArray jsonArray = new JsonArray();
            for (Feature element : disabledFeatures) {
                jsonArray.add(new GsonBuilder().create().toJsonTree(element.getId()));
            }
            loadedConfig.add("disabledFeatures", jsonArray);
            loadedConfig.addProperty("warningColor", warningColor.ordinal());
            loadedConfig.addProperty("confirmationColor", confirmationColor.ordinal());
            loadedConfig.addProperty("warningSeconds", warningSeconds);

            bufferedWriter.write(loadedConfig.toString());
            bufferedWriter.close();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("An error occurred while attempting to save the config!");
        }
    }

    public Set<Feature> getDisabledFeatures() {
        return disabledFeatures;
    }

    public ConfigColor getConfirmationColor() {
        return confirmationColor;
    }

    public ConfigColor getWarningColor() {
        return warningColor;
    }

    public void setConfirmationColor(ConfigColor confirmationColor) {
        this.confirmationColor = confirmationColor;
    }

    public void setWarningColor(ConfigColor warningColor) {
        this.warningColor = warningColor;
    }

    public int getWarningSeconds() {
        return warningSeconds;
    }

    public void setWarningSeconds(int warningSeconds) {
        this.warningSeconds = warningSeconds;
    }
}
