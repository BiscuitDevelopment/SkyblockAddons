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
    private Feature.ManaBarType manaBarType = Feature.ManaBarType.BAR_TEXT;
    private int warningSeconds = 4;
    private float manaBarX;
    private float manaBarY;
    private float skeletonBarX;
    private float skeletonBarY;

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
                if (loadedConfig.has("manaBarType")) {
                    manaBarType = Feature.ManaBarType.values()[loadedConfig.get("manaBarType").getAsInt()];
                }
                if (loadedConfig.has("manaBarX")) {
                    manaBarX = loadedConfig.get("manaBarX").getAsFloat();
                }
                if (loadedConfig.has("manaBarY")) {
                    manaBarY = loadedConfig.get("manaBarY").getAsFloat();
                }
                if (loadedConfig.has("skeletonBarX")) {
                    skeletonBarX = loadedConfig.get("skeletonBarX").getAsFloat();
                }
                if (loadedConfig.has("skeletonBarY")) {
                    skeletonBarY = loadedConfig.get("skeletonBarY").getAsFloat();
                }
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
            loadedConfig.addProperty("manaBarType", manaBarType.ordinal());
            loadedConfig.addProperty("warningSeconds", warningSeconds);
            loadedConfig.addProperty("manaBarX", manaBarX);
            loadedConfig.addProperty("manaBarY", manaBarY);
            loadedConfig.addProperty("skeletonBarX", skeletonBarX);
            loadedConfig.addProperty("skeletonBarY", skeletonBarY);

            bufferedWriter.write(loadedConfig.toString());
            bufferedWriter.close();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("An error occurred while attempting to save the config!");
        }
    }

    public Feature.ManaBarType getManaBarType() {
        return manaBarType;
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

    public void setManaBarType(Feature.ManaBarType manaBarType) {
        this.manaBarType = manaBarType;
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

    public float getManaBarX() {
        return manaBarX;
    }

    public void setManaBarX(int x, int maxX) {
        this.manaBarX = (float) x / maxX;
    }

    public float getManaBarY() {
        return manaBarY;
    }

    public void setManaBarY(int y, int maxY) {
        this.manaBarY = (float) y / maxY;
    }

    public float getSkeletonBarX() {
        return skeletonBarX;
    }

    public void setSkeletonBarX(int x, int maxX) {
        this.skeletonBarX = (float) x / maxX;
    }

    public float getSkeletonBarY() {
        return skeletonBarY;
    }

    public void setSkeletonBarY(int y, int maxY) {
        this.skeletonBarY = (float) y / maxY;
    }
}
