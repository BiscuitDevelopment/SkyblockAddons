package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

public class ConfigValues {

    private static final int CONFIG_VERSION = 6;
    private static final Feature[] GUI_FEATURES = {Feature.SKELETON_BAR, Feature.DEFENCE_ICON, Feature.DEFENCE_TEXT,
            Feature.DEFENCE_PERCENTAGE, Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.MANA_BAR, Feature.MANA_TEXT, Feature.HEALTH_UPDATES,
            Feature.ITEM_PICKUP_LOG, Feature.MAGMA_BOSS_TIMER, Feature.DARK_AUCTION_TIMER};

    private final static float GUI_SCALE_MINIMUM = 0.5F;
    private final static float GUI_SCALE_MAXIMUM = 5;
    private final static float GUI_SCALE_STEP = 0.1F;

    private SkyblockAddons main;
    private File settingsConfigFile;
    private JsonObject settingsConfig = new JsonObject();

    private JsonObject languageConfig = new JsonObject();

    private Set<Feature> disabledFeatures = EnumSet.noneOf(Feature.class);
    private Map<Feature, ConfigColor> featureColors = new EnumMap<>(Feature.class);
    private Map<Feature, MutableFloat> guiScales = new EnumMap<>(Feature.class);
    private int warningSeconds = 4;
    private Map<Feature, CoordsPair> coordinates = new EnumMap<>(Feature.class);
    private Map<Feature, EnumUtils.AnchorPoint> anchorPoints = new EnumMap<>(Feature.class);
    private Language language = Language.ENGLISH;
    private EnumUtils.BackpackStyle backpackStyle = EnumUtils.BackpackStyle.GUI;
    private EnumUtils.TextStyle textStyle = EnumUtils.TextStyle.REGULAR;
    private Set<Feature> remoteDisabledFeatures = EnumSet.noneOf(Feature.class);
    private Set<Integer> lockedSlots = new HashSet<>();

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
            if (settingsConfig.has("lockedSlots")) {
                for (JsonElement element : settingsConfig.getAsJsonArray("lockedSlots")) {
                    lockedSlots.add(element.getAsInt());
                }
            }

            warningSeconds = settingsConfig.get("warningSeconds").getAsInt();
            if (settingsConfig.has("manaBarX")) {
                coordinates.put(Feature.MANA_BAR, new CoordsPair(settingsConfig.get("manaBarX").getAsInt(), settingsConfig.get("manaBarY").getAsInt()));
            }
            if (settingsConfig.has("skeletonBarX")) {
                coordinates.put(Feature.SKELETON_BAR, new CoordsPair(settingsConfig.get("skeletonBarX").getAsInt(), settingsConfig.get("skeletonBarY").getAsInt()));
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
            if (settingsConfig.has("itemPickupLogX")) {
                coordinates.put(Feature.ITEM_PICKUP_LOG, new CoordsPair(settingsConfig.get("itemPickupLogX").getAsInt(), settingsConfig.get("itemPickupLogY").getAsInt()));
            }
            if (settingsConfig.has("magmaBossTimerX")) {
                coordinates.put(Feature.MAGMA_BOSS_TIMER, new CoordsPair(settingsConfig.get("magmaBossTimerX").getAsInt(), settingsConfig.get("magmaBossTimerY").getAsInt()));
            }
            if (settingsConfig.has("darkAuctionTimerX")) {
                coordinates.put(Feature.DARK_AUCTION_TIMER, new CoordsPair(settingsConfig.get("darkAuctionTimerX").getAsInt(), settingsConfig.get("darkAuctionTimerY").getAsInt()));
            }

            if (settingsConfig.has("anchorPoints")) {
                for (Map.Entry<String, JsonElement> element : settingsConfig.getAsJsonObject("anchorPoints").entrySet()) {
                    Feature feature = Feature.fromId(Integer.valueOf(element.getKey()));
                    anchorPoints.put(feature, EnumUtils.AnchorPoint.fromId(element.getValue().getAsInt()));
                }
            }

            if (settingsConfig.has("guiScales")) {
                for (Map.Entry<String, JsonElement> element : settingsConfig.getAsJsonObject("guiScales").entrySet()) {
                    Feature feature = Feature.fromId(Integer.parseInt(element.getKey()));
                    guiScales.put(feature, new MutableFloat(element.getValue().getAsFloat()));
                }
            }

            loadLegacyColor("warningColor", Feature.MAGMA_WARNING);
            loadLegacyColor("confirmationColor", Feature.DROP_CONFIRMATION);
            loadLegacyColor("manaBarColor", Feature.MANA_BAR);
            loadLegacyColor("manaBarTextColor", Feature.MANA_TEXT);
            loadLegacyColor("defencePercentageColor", Feature.DEFENCE_PERCENTAGE);
            loadLegacyColor("defenceTextColor", Feature.DEFENCE_TEXT);
            loadLegacyColor("healthBarColor", Feature.HEALTH_BAR);
            loadLegacyColor("healthTextColor", Feature.HEALTH_TEXT);
            loadLegacyColor("magmaBossTimerColor", Feature.MAGMA_BOSS_TIMER);
            loadLegacyColor("darkAuctionTimerColor", Feature.DARK_AUCTION_TIMER);

            if (settingsConfig.has("featureColors")) {
                for (Map.Entry<String, JsonElement> element : settingsConfig.getAsJsonObject("featureColors").entrySet()) {
                    Feature feature = Feature.fromId(Integer.parseInt(element.getKey()));
                    int ordinal = element.getValue().getAsInt();
                    if (ConfigColor.values().length > ordinal) {
                        featureColors.put(feature, ConfigColor.values()[ordinal]);
                    }
                }
            }

            setDefaultColorIfNotSet(ConfigColor.BLUE, Feature.MANA_BAR, Feature.MANA_TEXT);
            setDefaultColorIfNotSet(ConfigColor.GREEN, Feature.DEFENCE_TEXT, Feature.DEFENCE_PERCENTAGE);
            setDefaultColorIfNotSet(ConfigColor.GOLD, Feature.MAGMA_BOSS_TIMER, Feature.DARK_AUCTION_TIMER);

            if (settingsConfig.has("textStyle")) {
                int ordinal = settingsConfig.get("textStyle").getAsInt();
                if (EnumUtils.TextStyle.values().length > ordinal) {
                    textStyle = EnumUtils.TextStyle.values()[ordinal];
                }
            }

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
            } else if (configVersion <= 1) {
                disabledFeatures.add(Feature.USE_VANILLA_TEXTURE_DEFENCE);
                disabledFeatures.add(Feature.IGNORE_ITEM_FRAME_CLICKS);
                disabledFeatures.add(Feature.SHOW_BACKPACK_HOLDING_SHIFT);
            } else if (configVersion <= 2) {
                disabledFeatures.add(Feature.HEALTH_BAR);
                disabledFeatures.add(Feature.DEFENCE_PERCENTAGE);
                disabledFeatures.add(Feature.HIDE_PLAYERS_IN_LOBBY);
                disabledFeatures.add(Feature.SHOW_MAGMA_TIMER_IN_OTHER_GAMES);
                setAllCoordinatesToDefault();
            } else if (configVersion <= 3) {
                disabledFeatures.add(Feature.SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES);
                disabledFeatures.add(Feature.PREVENT_MOVEMENT_ON_DEATH);
            } else if (configVersion <= 4) {
                if (disabledFeatures.contains(Feature.DOUBLE_DROP_IN_OTHER_GAMES)) { // I inverted this feature thats why
                    disabledFeatures.remove(Feature.DOUBLE_DROP_IN_OTHER_GAMES);
                } else {
                    disabledFeatures.add(Feature.DOUBLE_DROP_IN_OTHER_GAMES);
                }
            } else if (configVersion <= 5) {
                disabledFeatures.add(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS);
            }
        } else {
            addDefaultsAndSave();
        }
        loadLanguageFile();
    }

    private void setDefaultColorIfNotSet(ConfigColor color, Feature... features) {
        for (Feature feature : features) {
            if (!featureColors.containsKey(feature)) featureColors.put(feature, color);
        }
    }

    @Deprecated()
    private void loadLegacyColor(String memberName, Feature feature) {
        if (settingsConfig.has(memberName)) {
            int ordinal = settingsConfig.get(memberName).getAsInt();
            if (ConfigColor.values().length > ordinal) {
                featureColors.put(feature, ConfigColor.values()[ordinal]);
            }
        }
    }

    private void addDefaultsAndSave() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            if (mc.getLanguageManager() != null && mc.getLanguageManager().getCurrentLanguage().getLanguageCode() != null) {
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
            }
        }
        featureColors.put(Feature.DROP_CONFIRMATION, ConfigColor.RED);
        featureColors.put(Feature.MAGMA_WARNING, ConfigColor.RED);
        featureColors.put(Feature.MANA_TEXT, ConfigColor.BLUE);
        featureColors.put(Feature.MANA_BAR, ConfigColor.BLUE);
        featureColors.put(Feature.HEALTH_BAR, ConfigColor.RED);
        featureColors.put(Feature.HEALTH_TEXT, ConfigColor.RED);
        featureColors.put(Feature.DEFENCE_TEXT, ConfigColor.GREEN);
        featureColors.put(Feature.DEFENCE_PERCENTAGE, ConfigColor.GREEN);
        featureColors.put(Feature.MAGMA_BOSS_TIMER, ConfigColor.GOLD);
        featureColors.put(Feature.DARK_AUCTION_TIMER, ConfigColor.GOLD);

        Feature[] toDisable = {Feature.DROP_CONFIRMATION, Feature.MINION_STOP_WARNING, Feature.HIDE_HEALTH_BAR,
            Feature.USE_VANILLA_TEXTURE_DEFENCE, Feature.IGNORE_ITEM_FRAME_CLICKS, Feature.SHOW_BACKPACK_HOLDING_SHIFT,
            Feature.HEALTH_BAR, Feature.DEFENCE_PERCENTAGE, Feature.HIDE_PLAYERS_IN_LOBBY, Feature.SHOW_MAGMA_TIMER_IN_OTHER_GAMES,
            Feature.SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES, Feature.PREVENT_MOVEMENT_ON_DEATH, Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS};
        disabledFeatures.addAll(Arrays.asList(toDisable));
        setAllCoordinatesToDefault();
        saveConfig();
    }

    public void setAllCoordinatesToDefault() {
        setAnchorPointsToDefault();
        for (Feature feature : GUI_FEATURES) {
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
        anchorPoints.put(Feature.ITEM_PICKUP_LOG, EnumUtils.AnchorPoint.TOP_LEFT);
        features = new Feature[]{Feature.MAGMA_BOSS_TIMER, Feature.DARK_AUCTION_TIMER};
        for (Feature feature : features) {
            anchorPoints.put(feature, EnumUtils.AnchorPoint.TOP_RIGHT);
        }
    }

    private void putDefaultCoordinates(Feature feature) {
        int x = 0;
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
            case ITEM_PICKUP_LOG:
                x = 86;
                y = 17;
                break;
            case MAGMA_BOSS_TIMER:
                x = -18;
                y = 13;
                break;
            case DARK_AUCTION_TIMER:
                x = -18;
                y = 29;
                break;
        }
        coordinates.put(feature, new CoordsPair(x, y));
    }

    public void loadLanguageFile() {
        loadLanguageFile(language);
    }

    public void loadLanguageFile(Language language) {
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

            jsonArray = new JsonArray();
            for (int slot : lockedSlots) {
                jsonArray.add(new GsonBuilder().create().toJsonTree(slot));
            }
            settingsConfig.add("lockedSlots", jsonArray);

            JsonObject anchorObject = new JsonObject();
            for (Feature feature : GUI_FEATURES) {
                anchorObject.addProperty(String.valueOf(feature.getId()), getAnchorPoint(feature).getId());
            }
            settingsConfig.add("anchorPoints", anchorObject);

            JsonObject scalesObject = new JsonObject();
            for (Feature feature : guiScales.keySet()) {
                scalesObject.addProperty(String.valueOf(feature.getId()), guiScales.get(feature).getValue());
            }
            settingsConfig.add("guiScales", scalesObject);

            JsonObject colorsObject = new JsonObject();
            for (Feature feature : featureColors.keySet()) {
                ConfigColor featureColor = featureColors.get(feature);
                if (featureColor != ConfigColor.RED) { // red is default, no need to save
                    colorsObject.addProperty(String.valueOf(feature.getId()), featureColor.ordinal());
                }
            }
            settingsConfig.add("featureColors", colorsObject);

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
            settingsConfig.addProperty("healthUpdatesX", getRelativeCoords(Feature.HEALTH_UPDATES).getX());
            settingsConfig.addProperty("healthUpdatesY", getRelativeCoords(Feature.HEALTH_UPDATES).getY());
            settingsConfig.addProperty("itemPickupLogX", getRelativeCoords(Feature.ITEM_PICKUP_LOG).getX());
            settingsConfig.addProperty("itemPickupLogY", getRelativeCoords(Feature.ITEM_PICKUP_LOG).getY());
            settingsConfig.addProperty("magmaBossTimerX", getRelativeCoords(Feature.MAGMA_BOSS_TIMER).getX());
            settingsConfig.addProperty("magmaBossTimerY", getRelativeCoords(Feature.MAGMA_BOSS_TIMER).getY());
            settingsConfig.addProperty("darkAuctionTimerX", getRelativeCoords(Feature.DARK_AUCTION_TIMER).getX());
            settingsConfig.addProperty("darkAuctionTimerY", getRelativeCoords(Feature.DARK_AUCTION_TIMER).getY());
            settingsConfig.addProperty("textStyle", textStyle.ordinal());
//            settingsConfig.addProperty("nextMagmaTimestamp", nextMagmaTimestamp);

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

    /**
     * @param feature The feature to check.
     * @return Whether the feature is remotely disabled.
     */
    public boolean isRemoteDisabled(Feature feature) {
        return remoteDisabledFeatures.contains(feature);
    }

    /**
     * @param feature The feature to check.
     * @return Whether the feature is disabled.
     */
    public boolean isDisabled(Feature feature) {
        return disabledFeatures.contains(feature) || isRemoteDisabled(feature);
    }

    /**
     * @param feature The feature to check.
     * @return Whether the feature is enabled.
     */
    public boolean isEnabled(Feature feature) {
        return !isDisabled(feature);
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

    public void setNextColor(Feature feature) {
        featureColors.put(feature, main.getConfigValues().getColor(feature).getNextColor());
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

    public void setClosestAnchorPoint(Feature feature) {
        int x1 = getActualX(feature);
        int y1 = getActualY(feature);
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int maxX = sr.getScaledWidth();
        int maxY = sr.getScaledHeight();
        double shortestDistance = -1;
        EnumUtils.AnchorPoint closestAnchorPoint = EnumUtils.AnchorPoint.HEALTH_BAR; // default
        for (EnumUtils.AnchorPoint point : EnumUtils.AnchorPoint.values()) {
            double distance = Point2D.distance(x1, y1, point.getX(maxX), point.getY(maxY));
            if (shortestDistance == -1 || distance < shortestDistance) {
                closestAnchorPoint = point;
                shortestDistance = distance;
            }
        }
        int targetX = getActualX(feature);
        int targetY = getActualY(feature);
        int x = targetX-closestAnchorPoint.getX(sr.getScaledWidth());
        int y = targetY-closestAnchorPoint.getY(sr.getScaledHeight());
        anchorPoints.put(feature, closestAnchorPoint);
        setCoords(feature, x, y);
    }

//    public void setNextAnchorPoint(Feature feature) {
//        EnumUtils.AnchorPoint nextPoint = getAnchorPoint(feature).getNextType();
//        int targetX = getActualX(feature);
//        int targetY = getActualY(feature);
//        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
//        int x = targetX-nextPoint.getX(sr.getScaledWidth());
//        int y = targetY-nextPoint.getY(sr.getScaledHeight());
//        anchorPoints.put(feature, nextPoint);
//        setCoords(feature, x, y);
//    }

    public EnumUtils.BackpackStyle getBackpackStyle() {
        return backpackStyle;
    }

    public void setBackpackStyle(EnumUtils.BackpackStyle backpackStyle) {
        this.backpackStyle = backpackStyle;
    }

    public EnumUtils.AnchorPoint getAnchorPoint(Feature feature) {
        return anchorPoints.getOrDefault(feature, EnumUtils.AnchorPoint.HEALTH_BAR);
    }

    JsonObject getLanguageConfig() {
        return languageConfig;
    }

    public EnumUtils.TextStyle getTextStyle() {
        return textStyle;
    }

    public void setTextStyle(EnumUtils.TextStyle textStyle) {
        this.textStyle = textStyle;
    }

    Set<Feature> getRemoteDisabledFeatures() {
        return remoteDisabledFeatures;
    }

    public Set<Integer> getLockedSlots() {
        return lockedSlots;
    }

    public void setGuiScale(Feature feature, float scale) {
        if (guiScales.containsKey(feature)) {
            guiScales.get(feature).setValue(scale);
        } else {
            guiScales.put(feature, new MutableFloat(scale));
        }
    }

    public float getGuiScale(Feature feature) {
        return getGuiScale(feature, true);
    }

    public float getGuiScale(Feature feature, boolean denormalized) {
        float value = 0.11F; //default scale (1.0)
        if (guiScales.containsKey(feature)) {
            value = guiScales.get(feature).getValue();
        }
        if (denormalized) value = denormalizeScale(value);
        return value;
    }

    // these are taken from GuiOptionSlider
    private float denormalizeScale(float value) {
        return snapToStepClamp(ConfigValues.GUI_SCALE_MINIMUM + (ConfigValues.GUI_SCALE_MAXIMUM - ConfigValues.GUI_SCALE_MINIMUM) *
                MathHelper.clamp_float(value, 0.0F, 1.0F));
    }
    private float snapToStepClamp(float value) {
        value = ConfigValues.GUI_SCALE_STEP * (float) Math.round(value / ConfigValues.GUI_SCALE_STEP);
        return MathHelper.clamp_float(value, ConfigValues.GUI_SCALE_MINIMUM, ConfigValues.GUI_SCALE_MAXIMUM);
    }



//    private float getRoundedValue(float value) {
//        return new BigDecimal(String.valueOf(value)).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
//    }
}
