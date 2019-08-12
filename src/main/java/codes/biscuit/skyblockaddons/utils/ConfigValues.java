package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.io.*;
import java.util.*;

public class ConfigValues {

    private static final int CONFIG_VERSION = 3;

    private SkyblockAddons main;
    private File settingsConfigFile;
    private JsonObject settingsConfig = new JsonObject();

    private JsonObject languageConfig = new JsonObject();

    private Set<Feature> disabledFeatures = EnumSet.noneOf(Feature.class);
    private Map<Feature, ConfigColor> featureColors = new EnumMap<>(Feature.class);
    private int warningSeconds = 4;
    private Map<Feature, CoordsPair> coordinates = new EnumMap<>(Feature.class);
    private Map<Feature, EnumUtils.AnchorPoint> anchorPoints = new EnumMap<>(Feature.class);
    private float guiScale = 0.11F;
    private Language language = Language.ENGLISH;
    private EnumUtils.BackpackStyle backpackStyle = EnumUtils.BackpackStyle.GUI;

    public ConfigValues(SkyblockAddons main, File settingsConfigFile) {
        this.main = main;
        this.settingsConfigFile = settingsConfigFile;
    }

    public void loadConfig() {
        if (settingsConfigFile.exists()) {
            try {
                FileReader reader = new FileReader(settingsConfigFile);
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
                settingsConfig = fileElement.getAsJsonObject();
            } catch (JsonParseException | IllegalStateException | IOException ex) {
                ex.printStackTrace();
                System.out.println("SkyblockAddons: There was an error loading the config. Resetting all settings to default.");
                addDefaultsAndSave();
                return;
            }
            for (JsonElement element : settingsConfig.getAsJsonArray("disabledFeatures")) {
                Feature feature = Feature.fromId(element.getAsInt());
                if (feature != null) {
                    disabledFeatures.add(feature);
                }
            }
            warningSeconds = settingsConfig.get("warningSeconds").getAsInt();
            if (settingsConfig.has("manaBarX")) {
                coordinates.put(Feature.MANA_BAR, new CoordsPair(settingsConfig.get("manaBarX").getAsInt(), settingsConfig.get("manaBarY").getAsInt()));
            }
            if (settingsConfig.has("skeletonBarX")) {
                coordinates.put(Feature.SKELETON_BAR, new CoordsPair(settingsConfig.get("skeletonBarX").getAsInt(), settingsConfig.get("skeletonBarY").getAsInt()));
            }
            if (settingsConfig.has("guiScale")) {
                guiScale = settingsConfig.get("guiScale").getAsFloat();
            }
            if (settingsConfig.has("manaTextX")) {
                coordinates.put(Feature.MANA_TEXT, new CoordsPair(settingsConfig.get("manaTextX").getAsInt(), settingsConfig.get("manaTextY").getAsInt()));
            }
            if (settingsConfig.has("language")) {
                Language configLanguage = Language.getFromPath(settingsConfig.get("language").getAsString().toLowerCase());
                if (configLanguage != null) {
                    language = configLanguage;
                }
            }
            if (settingsConfig.has("backpackStyle")) {
                int ordinal = settingsConfig.get("backpackStyle").getAsInt();
                if (EnumUtils.BackpackStyle.values().length > ordinal) {
                    backpackStyle = EnumUtils.BackpackStyle.values()[ordinal];
                }
            }
            if (settingsConfig.has("healthBarX")) {
                coordinates.put(Feature.HEALTH_BAR, new CoordsPair(settingsConfig.get("healthBarX").getAsInt(), settingsConfig.get("healthBarY").getAsInt()));
            }
            if (settingsConfig.has("healthTextX")) {
                coordinates.put(Feature.HEALTH_TEXT, new CoordsPair(settingsConfig.get("healthTextX").getAsInt(), settingsConfig.get("healthTextY").getAsInt()));
            }
            if (settingsConfig.has("defenceTextX")) {
                coordinates.put(Feature.DEFENCE_TEXT, new CoordsPair(settingsConfig.get("defenceTextX").getAsInt(), settingsConfig.get("defenceTextY").getAsInt()));
            }
            if (settingsConfig.has("defencePercentageX")) {
                coordinates.put(Feature.DEFENCE_PERCENTAGE, new CoordsPair(settingsConfig.get("defencePercentageX").getAsInt(), settingsConfig.get("defencePercentageY").getAsInt()));
            }
            if (settingsConfig.has("defenceIconX")) {
                coordinates.put(Feature.DEFENCE_ICON, new CoordsPair(settingsConfig.get("defenceIconX").getAsInt(), settingsConfig.get("defenceIconY").getAsInt()));
            }
            if (settingsConfig.has("healthUpdatesX")) {
                coordinates.put(Feature.HEALTH_UPDATES, new CoordsPair(settingsConfig.get("healthUpdatesX").getAsInt(), settingsConfig.get("healthUpdatesY").getAsInt()));
            }
            loadColor("warningColor", Feature.MAGMA_WARNING, ConfigColor.RED);
            loadColor("confirmationColor", Feature.DROP_CONFIRMATION, ConfigColor.RED);
            loadColor("manaBarColor", Feature.MANA_BAR, ConfigColor.BLUE);
            loadColor("manaBarTextColor", Feature.MANA_TEXT, ConfigColor.BLUE);
            loadColor("defencePercentageColor", Feature.DEFENCE_PERCENTAGE, ConfigColor.GREEN);
            loadColor("defenceTextColor", Feature.DEFENCE_TEXT, ConfigColor.GREEN);
            loadColor("healthBarColor", Feature.HEALTH_BAR, ConfigColor.RED);
            loadColor("healthTextColor", Feature.HEALTH_TEXT, ConfigColor.RED);
            int configVersion;
            if (settingsConfig.has("configVersion")) {
                configVersion = settingsConfig.get("configVersion").getAsInt();
            } else {
                configVersion = 0;
            }
            if (configVersion == 0) {
                disabledFeatures.add(Feature.HIDE_HEALTH_BAR);
                disabledFeatures.add(Feature.MINION_STOP_WARNING);
                disabledFeatures.add(Feature.MINION_FULL_WARNING);
                Feature[] newFeatures = {Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.DEFENCE_TEXT, Feature.DEFENCE_PERCENTAGE,
                        Feature.DEFENCE_ICON};
                for (Feature feature : newFeatures) {
                    putDefaultCoordinates(feature);
                }
                if (guiScale == 0) {
                    guiScale = 0.11F;
                }
            } else if (configVersion == 1) {
                disabledFeatures.add(Feature.USE_VANILLA_TEXTURE_DEFENCE);
                disabledFeatures.add(Feature.IGNORE_ITEM_FRAME_CLICKS);
                disabledFeatures.add(Feature.SHOW_BACKPACK_HOLDING_SHIFT);
            } else if (configVersion == 2) {
                disabledFeatures.add(Feature.HEALTH_BAR);
                disabledFeatures.add(Feature.DEFENCE_PERCENTAGE);
                setAnchorPointsToDefault();
            }
        } else {
            addDefaultsAndSave();
        }
        loadLanguageFile();
    }

    private void loadColor(String memberName, Feature feature, ConfigColor defaultColor) {
        if (settingsConfig.has(memberName)) { // migrate from old config
            int ordinal = settingsConfig.get(memberName).getAsInt();
            if (ConfigColor.values().length > ordinal) {
                featureColors.put(feature, ConfigColor.values()[ordinal]);
            }
        } else {
            featureColors.put(feature, defaultColor);
        }
    }

    private void addDefaultsAndSave() {
        String minecraftLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase();
        Language configLanguage = Language.getFromPath(minecraftLanguage);
        if (configLanguage != null) { // Check if we have the exact locale they are using for Minecraft
            language = configLanguage;
        } else { // Check if we at least have the same language (different locale)
            String languageCode = minecraftLanguage.split("_")[0];
            for (Language loopLanguage : Language.values()) {
                String loopLanguageCode = loopLanguage.getPath().split("_")[0];
                if (loopLanguageCode.equals(languageCode)) {
                    language = loopLanguage;
                    break;
                }
            }
        }
        Feature[] newFeatures = {Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.DEFENCE_TEXT, Feature.DEFENCE_PERCENTAGE,
                Feature.DEFENCE_ICON};
        for (Feature feature : newFeatures) {
            putDefaultCoordinates(feature);
        }
        featureColors.put(Feature.DROP_CONFIRMATION, ConfigColor.RED);
        featureColors.put(Feature.MAGMA_WARNING, ConfigColor.RED);
        featureColors.put(Feature.MANA_TEXT, ConfigColor.BLUE);
        featureColors.put(Feature.MANA_BAR, ConfigColor.BLUE);
        featureColors.put(Feature.HEALTH_BAR, ConfigColor.RED);
        featureColors.put(Feature.HEALTH_TEXT, ConfigColor.RED);
        featureColors.put(Feature.DEFENCE_TEXT, ConfigColor.GREEN);
        featureColors.put(Feature.DEFENCE_PERCENTAGE, ConfigColor.GREEN);
        disabledFeatures.add(Feature.DROP_CONFIRMATION);
        disabledFeatures.add(Feature.MINION_STOP_WARNING);
        disabledFeatures.add(Feature.HIDE_HEALTH_BAR);
        disabledFeatures.add(Feature.MINION_FULL_WARNING);
        disabledFeatures.add(Feature.USE_VANILLA_TEXTURE_DEFENCE);
        disabledFeatures.add(Feature.IGNORE_ITEM_FRAME_CLICKS);
        disabledFeatures.add(Feature.SHOW_BACKPACK_HOLDING_SHIFT);
        disabledFeatures.add(Feature.HEALTH_BAR);
        disabledFeatures.add(Feature.DEFENCE_PERCENTAGE);
        setAllCoordinatesToDefault();
        saveConfig();
    }

    public void setAllCoordinatesToDefault() {
        setAnchorPointsToDefault();
        Feature[] features = {Feature.SKELETON_BAR, Feature.DEFENCE_ICON, Feature.DEFENCE_TEXT,
                Feature.DEFENCE_PERCENTAGE, Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.MANA_BAR, Feature.MANA_TEXT, Feature.HEALTH_UPDATES};
        for (Feature feature : features) {
            putDefaultCoordinates(feature);
        }
    }

    private void setAnchorPointsToDefault() {
        Feature[] features = {Feature.SKELETON_BAR, Feature.DEFENCE_ICON, Feature.DEFENCE_TEXT,
                Feature.DEFENCE_PERCENTAGE, Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.MANA_BAR,
                Feature.MANA_TEXT, Feature.HEALTH_UPDATES};
        for (Feature feature : features) {
            anchorPoints.put(feature, EnumUtils.AnchorPoint.HEALTH_BAR);
        }
    }

    private void putDefaultCoordinates(Feature feature) {
        int x = 0; //TODO add default coordinates
        int y = 0;
        switch (feature) {
            case SKELETON_BAR:
                x = 211;
                y = 28;
                break;
            case DEFENCE_ICON:
                x = 90;
                y = -24;
                break;
            case DEFENCE_TEXT:
                x = 90;
                y = -22;
                break;
            case DEFENCE_PERCENTAGE:
                x = 92;
                y = -14;
                break;
            case HEALTH_BAR:
                x = 41;
                y = -4;
                break;
            case HEALTH_TEXT:
                x = 40;
                y = -4;
                break;
            case MANA_BAR:
                x = 141;
                y = -4;
                break;
            case MANA_TEXT:
                x = 143;
                y = -4;
                break;
            case HEALTH_UPDATES:
                x = 41;
                y = -13;
                break;
        }
        coordinates.put(feature, new CoordsPair(x, y));
    }

    public void loadLanguageFile() {
        try {
            InputStream fileStream = getClass().getClassLoader().getResourceAsStream("lang/" + language.getPath() + ".json");
            if (fileStream != null) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                String dataString = result.toString("UTF-8");
                languageConfig = new JsonParser().parse(dataString).getAsJsonObject();
                fileStream.close();
            }
        } catch (JsonParseException | IllegalStateException | IOException ex) {
            ex.printStackTrace();
            System.out.println("SkyblockAddons: There was an error loading the language file.");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveConfig() {
        settingsConfig = new JsonObject();
        try {
            settingsConfigFile.createNewFile();
            FileWriter writer = new FileWriter(settingsConfigFile);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            JsonArray jsonArray = new JsonArray();
            for (Feature element : disabledFeatures) {
                jsonArray.add(new GsonBuilder().create().toJsonTree(element.getId()));
            }
            settingsConfig.add("disabledFeatures", jsonArray);
            settingsConfig.addProperty("warningColor", getColor(Feature.MAGMA_WARNING).ordinal());
            settingsConfig.addProperty("confirmationColor", getColor(Feature.DROP_CONFIRMATION).ordinal());
            settingsConfig.addProperty("manaBarColor", getColor(Feature.MANA_BAR).ordinal());
            settingsConfig.addProperty("manaBarTextColor", getColor(Feature.MANA_TEXT).ordinal());
            settingsConfig.addProperty("warningSeconds", warningSeconds);
            settingsConfig.addProperty("manaBarX", getRelativeCoords(Feature.MANA_BAR).getX());
            settingsConfig.addProperty("manaBarY", getRelativeCoords(Feature.MANA_BAR).getY());
            settingsConfig.addProperty("manaTextX", getRelativeCoords(Feature.MANA_TEXT).getX());
            settingsConfig.addProperty("manaTextY", getRelativeCoords(Feature.MANA_TEXT).getY());
            settingsConfig.addProperty("skeletonBarX", getRelativeCoords(Feature.SKELETON_BAR).getX());
            settingsConfig.addProperty("skeletonBarY", getRelativeCoords(Feature.SKELETON_BAR).getY());
            settingsConfig.addProperty("healthBarX", getRelativeCoords(Feature.HEALTH_BAR).getX());
            settingsConfig.addProperty("healthBarY", getRelativeCoords(Feature.HEALTH_BAR).getY());
            settingsConfig.addProperty("healthTextX", getRelativeCoords(Feature.HEALTH_TEXT).getX());
            settingsConfig.addProperty("healthTextY", getRelativeCoords(Feature.HEALTH_TEXT).getY());
            settingsConfig.addProperty("configVersion", CONFIG_VERSION);
            settingsConfig.addProperty("defenceTextX", getRelativeCoords(Feature.DEFENCE_TEXT).getX());
            settingsConfig.addProperty("defenceTextY", getRelativeCoords(Feature.DEFENCE_TEXT).getY());
            settingsConfig.addProperty("defencePercentageX", getRelativeCoords(Feature.DEFENCE_PERCENTAGE).getX());
            settingsConfig.addProperty("defencePercentageY", getRelativeCoords(Feature.DEFENCE_PERCENTAGE).getY());
            settingsConfig.addProperty("defenceIconX", getRelativeCoords(Feature.DEFENCE_ICON).getX());
            settingsConfig.addProperty("defenceIconY", getRelativeCoords(Feature.DEFENCE_ICON).getY());
            settingsConfig.addProperty("defencePercentageColor", getColor(Feature.DEFENCE_PERCENTAGE).ordinal());
            settingsConfig.addProperty("defenceTextColor", getColor(Feature.DEFENCE_TEXT).ordinal());
            settingsConfig.addProperty("healthBarColor", getColor(Feature.HEALTH_BAR).ordinal());
            settingsConfig.addProperty("healthTextColor", getColor(Feature.HEALTH_TEXT).ordinal());
            settingsConfig.addProperty("healthUpdatesX", getRelativeCoords(Feature.HEALTH_UPDATES).getX());
            settingsConfig.addProperty("healthUpdatesY", getRelativeCoords(Feature.HEALTH_UPDATES).getY());

            settingsConfig.addProperty("guiScale", guiScale);
            settingsConfig.addProperty("language", language.getPath());
            settingsConfig.addProperty("backpackStyle", backpackStyle.ordinal());

            bufferedWriter.write(settingsConfig.toString());
            bufferedWriter.close();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("An error occurred while attempting to save the config!");
        }
    }

    public String getMessage(Message message, String... variables) {
        String text;
        try {
            List<String> path = message.getMessageObject().getPath();
            JsonObject jsonObject = languageConfig;
            for (String part : path) {
                if (!part.equals("")) {
                    jsonObject = jsonObject.getAsJsonObject(part);
                }
            }
            text = jsonObject.get(message.getMemberName()).getAsString();
            if (text != null) {
                if (message == Message.SETTING_WARNING_TIME) {
                    text = text.replace("%time%", String.valueOf(warningSeconds));
                } else if (message == Message.SETTING_GUI_SCALE) {
                    text = text.replace("%scale%", variables[0]);
                } else if (message == Message.MESSAGE_NEW_VERSION) {
                    text = text.replace("%newestVersion%", variables[0]);
                } else if (message == Message.SETTING_BACKPACK_STYLE) {
                    text = text.replace("%style%", backpackStyle.getDisplayText());
                } else if (message == Message.MESSAGE_DEVELOPMENT_VERSION) {
                    text = text.replace("%version%", variables[0]).replace("%newestVersion%", variables[1]);
                } else if (message == Message.LANGUAGE) {
                    text = "Language: " + text;
                } else if (message == Message.MESSAGE_MINION_CANNOT_REACH || message == Message.MESSAGE_TYPE_ENCHANTMENTS
                        || message == Message.MESSAGE_ENCHANTS_TO_MATCH || message == Message.MESSAGE_ENCHANTS_TO_EXCLUDE) {
                    text = text.replace("%type%", variables[0]);
                }
            }
            if (text != null && language == Language.HEBREW) {
                text = reverseText(text);
            }
        } catch (NullPointerException ex) { // In case I messed up some translation or something.
            ex.printStackTrace();
            text = "";
        }
        return text;
    }

    // This reverses the text while leaving the english parts intact and in order.
    // (Maybe its more complicated than it has to be, but it gets the job done.
    private String reverseText(String originalText) {
        StringBuilder newString = new StringBuilder();
        String[] parts = originalText.split(" ");
        for (int i = parts.length; i > 0; i--) {
            String textPart = parts[i-1];
            boolean foundCharacter = false;
            for (char letter : textPart.toCharArray()) {
                if (letter > 191) { // Found special character
                    foundCharacter = true;
                    newString.append(new StringBuilder(textPart).reverse().toString());
                    break;
                }
            }
            newString.append(" ");
            if (!foundCharacter) {
                newString.insert(0, textPart);
            }
            newString.insert(0, " ");
        }
        return main.getUtils().removeDuplicateSpaces(newString.toString().trim());
    }

    public Set<Feature> getDisabledFeatures() {
        return disabledFeatures;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Language getLanguage() {
        return language;
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

    public int getActualX(Feature feature) {
        int maxX = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
        return getAnchorPoint(feature).getX(maxX)+ getRelativeCoords(feature).getX();
    }

    public int getActualY(Feature feature) {
        int maxY = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
        return getAnchorPoint(feature).getY(maxY)+ getRelativeCoords(feature).getY();
    }

    public CoordsPair getRelativeCoords(Feature feature) {
        if (coordinates.containsKey(feature)) {
            return coordinates.get(feature);
        } else {
            putDefaultCoordinates(feature);
            if (coordinates.containsKey(feature)) {
                return coordinates.get(feature);
            } else {
                return new CoordsPair(0,0);
            }
        }
    }

    public void setCoords(Feature feature, int x, int y) {
        if (coordinates.containsKey(feature)) {
            coordinates.get(feature).setX(x);
            coordinates.get(feature).setY(y);
        } else {
            coordinates.put(feature, new CoordsPair(x, y));
        }
    }

    public void setNextAnchorPoint(Feature feature) {
        EnumUtils.AnchorPoint nextPoint = getAnchorPoint(feature).getNextType();
        int targetX = getActualX(feature);
        int targetY = getActualY(feature);
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int x = targetX-nextPoint.getX(sr.getScaledWidth());
        int y = targetY-nextPoint.getY(sr.getScaledHeight());
        anchorPoints.put(feature, nextPoint);
        setCoords(feature, x, y);
    }

    public float getGuiScale() {
        return guiScale;
    }

    public void setGuiScale(float guiScale) {
        this.guiScale = guiScale;
    }

    public EnumUtils.BackpackStyle getBackpackStyle() {
        return backpackStyle;
    }

    public void setBackpackStyle(EnumUtils.BackpackStyle backpackStyle) {
        this.backpackStyle = backpackStyle;
    }

    public EnumUtils.AnchorPoint getAnchorPoint(Feature feature) {
        return anchorPoints.getOrDefault(feature, EnumUtils.AnchorPoint.HEALTH_BAR);
    }
}
