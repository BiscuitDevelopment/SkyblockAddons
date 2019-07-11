package codes.biscuit.skyblockaddons.utils;

import com.google.gson.*;

import java.io.*;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ConfigValues {

    private File configFile;
    private JsonObject loadedConfig = new JsonObject();

    private Set<Feature> disabledFeatures = EnumSet.noneOf(Feature.class);
    private Map<Feature, ConfigColor> featureColors = new EnumMap<>(Feature.class);
    private Feature.ManaBarType manaBarType = Feature.ManaBarType.BAR_TEXT;
    private int warningSeconds = 4;
//    private int inventoryWarningSeconds = 3;
    private float manaBarX = 0.45F;
    private float manaBarY = 0.83F;
    private float skeletonBarX = 0.68F;
    private float skeletonBarY = 0.93F;

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
                JsonElement fileElement = new JsonParser().parse(complete);
                if (fileElement == null || fileElement.isJsonNull()) {
                    addDefaultsAndSave();
                    return;
                }
                loadedConfig = fileElement.getAsJsonObject();
            } catch (JsonParseException | IllegalStateException | IOException ex) {
                ex.printStackTrace();
                System.out.println("SkyblockAddons: There was an error loading the config. Resetting to defaults.");
                addDefaultsAndSave();
                return;
            }
            for (JsonElement element : loadedConfig.getAsJsonArray("disabledFeatures")) {
                disabledFeatures.add(Feature.fromId(element.getAsInt()));
            }
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
            if (loadedConfig.has("warningColor")) { // migrate from old config
                featureColors.put(Feature.WARNING_COLOR, ConfigColor.values()[loadedConfig.get("warningColor").getAsInt()]);
            } else {
                featureColors.put(Feature.WARNING_COLOR, ConfigColor.RED);
            }
            if (loadedConfig.has("confirmationColor")) { // migrate from old config
                featureColors.put(Feature.CONFIRMATION_COLOR, ConfigColor.values()[loadedConfig.get("confirmationColor").getAsInt()]);
            } else {
                featureColors.put(Feature.CONFIRMATION_COLOR, ConfigColor.RED);
            }
            if (loadedConfig.has("manaBarColor")) { // migrate from old config
                featureColors.put(Feature.MANA_BAR_COLOR, ConfigColor.values()[loadedConfig.get("manaBarColor").getAsInt()]);
            } else {
                featureColors.put(Feature.MANA_BAR_COLOR, ConfigColor.BLUE);
            }
            if (loadedConfig.has("manaBarTextColor")) { // migrate from old config
                featureColors.put(Feature.MANA_TEXT_COLOR, ConfigColor.values()[loadedConfig.get("manaBarTextColor").getAsInt()]);
            } else {
                featureColors.put(Feature.MANA_TEXT_COLOR, ConfigColor.BLUE);
            }
//            if (loadedConfig.has("inventoryWarningSeconds")) {
//                inventoryWarningSeconds = loadedConfig.get("inventoryWarningSeconds").getAsInt();
//            }
        } else {
            addDefaultsAndSave();
        }
    }

    private void addDefaultsAndSave() {
        featureColors.put(Feature.CONFIRMATION_COLOR, ConfigColor.RED);
        featureColors.put(Feature.WARNING_COLOR, ConfigColor.RED);
        featureColors.put(Feature.MANA_TEXT_COLOR, ConfigColor.BLUE);
        featureColors.put(Feature.MANA_BAR_COLOR, ConfigColor.BLUE);
        saveConfig();
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
            loadedConfig.addProperty("warningColor", getColor(Feature.WARNING_COLOR).ordinal());
            loadedConfig.addProperty("confirmationColor", getColor(Feature.CONFIRMATION_COLOR).ordinal());
            loadedConfig.addProperty("manaBarColor", getColor(Feature.MANA_BAR_COLOR).ordinal());
            loadedConfig.addProperty("manaBarTextColor", getColor(Feature.MANA_TEXT_COLOR).ordinal());
            loadedConfig.addProperty("manaBarType", manaBarType.ordinal());
            loadedConfig.addProperty("warningSeconds", warningSeconds);
//            loadedConfig.addProperty("inventoryWarningSeconds", inventoryWarningSeconds);
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

    public void setManaBarType(Feature.ManaBarType manaBarType) {
        this.manaBarType = manaBarType;
    }

    public void setColor(Feature feature, ConfigColor color) {
        featureColors.put(feature, color);
    }

    public ConfigColor getColor(Feature feature) {
        return featureColors.getOrDefault(feature, ConfigColor.RED);
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
