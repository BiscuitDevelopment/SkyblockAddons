package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.beans.Introspector;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ConfigValues {

    private static final int CONFIG_VERSION = 7;

    private final static float GUI_SCALE_MINIMUM = 0.5F;
    private final static float GUI_SCALE_MAXIMUM = 5;
    private final static float GUI_SCALE_STEP = 0.1F;

    private SkyblockAddons main;

    private File settingsConfigFile;
    private JsonObject settingsConfig = new JsonObject();
    @Getter private JsonObject languageConfig = new JsonObject();

    @Getter private Set<Feature> disabledFeatures = EnumSet.noneOf(Feature.class);
//    @Deprecated private Map<Feature, ChatFormatting> featureColors = new EnumMap<>(Feature.class); // Replaced with colors
    private Map<Feature, Integer> colors = new HashMap<>();
    private Map<Feature, MutableFloat> guiScales = new EnumMap<>(Feature.class);
    private Map<Feature, CoordsPair> barSizes = new EnumMap<>(Feature.class);
    @Getter @Setter private int warningSeconds = 4;
    private Map<Feature, CoordsPair> coordinates = new EnumMap<>(Feature.class);
    private Map<Feature, EnumUtils.AnchorPoint> anchorPoints = new EnumMap<>(Feature.class);
    @Getter @Setter private Language language = Language.ENGLISH;
    @Getter @Setter private EnumUtils.BackpackStyle backpackStyle = EnumUtils.BackpackStyle.GUI;
    @Getter @Setter private EnumUtils.PowerOrbDisplayStyle powerOrbDisplayStyle = EnumUtils.PowerOrbDisplayStyle.COMPACT;
    @Getter @Setter private EnumUtils.TextStyle textStyle = EnumUtils.TextStyle.STYLE_ONE;
    @Getter @SuppressWarnings("deprecation") private Set<Feature> remoteDisabledFeatures = EnumSet.of(Feature.AVOID_BREAKING_BOTTOM_SUGAR_CANE);
    private Set<Integer> legacyLockedSlots = new HashSet<>();
    private Map<String, Set<Integer>> profileLockedSlots = new HashMap<>();
    private Set<Feature> chromaFeatures = new HashSet<>();
    private float chromaSpeed = 0.19354838F; // 2.0
    private EnumUtils.ChromaMode chromaMode = EnumUtils.ChromaMode.ALL_SAME_COLOR;

    public ConfigValues(SkyblockAddons main, File settingsConfigFile) {
        this.main = main;
        this.settingsConfigFile = settingsConfigFile;
    }

    @SuppressWarnings("deprecation")
    public void loadConfig() {
        if (settingsConfigFile.exists()) {
            try {
                FileReader reader = new FileReader(settingsConfigFile);
                JsonElement fileElement = new JsonParser().parse(reader);

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
            if (settingsConfig.has("disabledFeatures")) {
                for (JsonElement element : settingsConfig.getAsJsonArray("disabledFeatures")) {
                    Feature feature = Feature.fromId(element.getAsInt());
                    if (feature != null) {
                        disabledFeatures.add(feature);
                    }
                }
            }
            if (settingsConfig.has("lockedSlots")) {
                for (JsonElement element : settingsConfig.getAsJsonArray("lockedSlots")) {
                    legacyLockedSlots.add(element.getAsInt());
                }
            }

            if (settingsConfig.has("profileLockedSlots")) {
                JsonObject profileSlotsObject = settingsConfig.getAsJsonObject("profileLockedSlots");
                for (Map.Entry<String, JsonElement> entry : profileSlotsObject.entrySet()) {
                    Set<Integer> slots = new HashSet<>();
                    for (JsonElement element : entry.getValue().getAsJsonArray()) {
                        slots.add(element.getAsInt());
                    }
                    profileLockedSlots.put(entry.getKey(), slots);
                }
            }

            if (settingsConfig.has("warningSeconds")) {
                warningSeconds = settingsConfig.get("warningSeconds").getAsInt();
            }

            if (settingsConfig.has("language")) {
                String languageKey = settingsConfig.get("language").getAsString();
                Language configLanguage = Language.getFromPath(languageKey);
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

            if (settingsConfig.has("powerOrbStyle")) {
                int ordinal = settingsConfig.get("powerOrbStyle").getAsInt();
                if (EnumUtils.PowerOrbDisplayStyle.values().length > ordinal) {
                    powerOrbDisplayStyle = EnumUtils.PowerOrbDisplayStyle.values()[ordinal];
                }
            }


            if (settingsConfig.has("anchorPoints")) {
                for (Map.Entry<String, JsonElement> element : settingsConfig.getAsJsonObject("anchorPoints").entrySet()) {
                    Feature feature = Feature.fromId(Integer.parseInt(element.getKey()));
                    EnumUtils.AnchorPoint anchorPoint = EnumUtils.AnchorPoint.fromId(element.getValue().getAsInt());
                    if (feature != null && anchorPoint != null) {
                        anchorPoints.put(feature, anchorPoint);
                    }
                }
            }

            if (settingsConfig.has("guiScales")) {
                for (Map.Entry<String, JsonElement> element : settingsConfig.getAsJsonObject("guiScales").entrySet()) {
                    Feature feature = Feature.fromId(Integer.parseInt(element.getKey()));
                    if (feature != null) {
                        guiScales.put(feature, new MutableFloat(element.getValue().getAsFloat()));
                    }
                }
            }

            for (Feature feature : Feature.getGuiFeatures()) { // Deprecated - Legacy Loader
                String property = Introspector.decapitalize(WordUtils.capitalizeFully(feature.toString().replace("_", " "))).replace(" ", "");
                String x = property+"X";
                String y = property+"Y";
                if (settingsConfig.has(x)) {
                    coordinates.put(feature, new CoordsPair(settingsConfig.get(x).getAsInt(), settingsConfig.get(y).getAsInt()));
                }
            }
            loadFeatureArray("guiPositions", coordinates);
            loadFeatureArray("barSizes", barSizes);

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
                    if (feature != null) {
                        int ordinal = element.getValue().getAsInt();
                        if (ordinal < 16) {
                            ChatFormatting chatFormatting = ChatFormatting.values()[ordinal];
//                            featureColors.put(feature, chatFormatting);
                            if (chatFormatting != ChatFormatting.RED) { // It's default, no need.
                                colors.put(feature, chatFormatting.getRGB());
                            }
                        }
                    }
                }
            }

            if (settingsConfig.has("colors")) {
                for (Map.Entry<String, JsonElement> element : settingsConfig.getAsJsonObject("colors").entrySet()) {
                    Feature feature = Feature.fromId(Integer.parseInt(element.getKey()));
                    if (feature != null) {
                        int color = element.getValue().getAsInt();
                        colors.put(feature, color);
                    }
                }
            }

            if (settingsConfig.has("textStyle")) {
                int ordinal = settingsConfig.get("textStyle").getAsInt();
                if (EnumUtils.TextStyle.values().length > ordinal) {
                    textStyle = EnumUtils.TextStyle.values()[ordinal];
                }
            }

            if (settingsConfig.has("chromaFeatures")) {
                JsonArray chromaFeaturesArray = settingsConfig.get("chromaFeatures").getAsJsonArray();
                for (JsonElement element : chromaFeaturesArray) {
                    chromaFeatures.add(Feature.fromId(element.getAsInt()));
                }
            }

            if (settingsConfig.has("chromaSpeed")) {
                chromaSpeed = settingsConfig.get("chromaSpeed").getAsFloat();
            }

            if (settingsConfig.has("chromaMode")) {
                int ordinal = settingsConfig.get("chromaMode").getAsInt();
                if (EnumUtils.ChromaMode.values().length > ordinal) {
                    chromaMode = EnumUtils.ChromaMode.values()[ordinal];
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
            } else if (configVersion <= 6) {
                putDefaultBarSizes();
                for (Map.Entry<Feature, CoordsPair> entry : coordinates.entrySet()) {
                    if (getAnchorPoint(entry.getKey()) == EnumUtils.AnchorPoint.BOTTOM_MIDDLE) {
                        CoordsPair coords = entry.getValue();
                        coords.setX(coords.getX()-91);
                        coords.setY(coords.getY()-39);
                    }
                }
            }
        } else {
            addDefaultsAndSave();
        }
        loadLanguageFile(true);
    }

    private void loadFeatureArray(String memberName, Map<Feature, CoordsPair> targetObject) {
        if (settingsConfig.has(memberName)) {
            for (Map.Entry<String, JsonElement> element : settingsConfig.getAsJsonObject(memberName).entrySet()) {
                Feature feature = Feature.fromId(Integer.parseInt(element.getKey()));
                if (feature != null) {
                    JsonArray array = element.getValue().getAsJsonArray();
                    targetObject.put(feature, new CoordsPair(array.get(0).getAsInt(), array.get(1).getAsInt()));
                }
            }
        }
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated()
    private void loadLegacyColor(String memberName, Feature feature) {
        if (settingsConfig.has(memberName)) {
            int ordinal = settingsConfig.get(memberName).getAsInt();
            if (ordinal < 16) {
//                featureColors.put(feature, ChatFormatting.values()[ordinal]);
                colors.put(feature, ChatFormatting.values()[ordinal].getRGB());
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

        for (Feature feature : Feature.values()) {
            ChatFormatting color = feature.getDefaultColor();
            if (color != null) {
                colors.put(feature, color.getRGB());
            }
            if (feature.isDefaultDisabled()) {
                disabledFeatures.add(feature);
            }
        }

        setAllCoordinatesToDefault();
        putDefaultBarSizes();
        saveConfig();
    }

    public void setAllCoordinatesToDefault() {
        setAnchorPointsToDefault();
        putDefaultBarSizes();
        for (Feature feature : Feature.getGuiFeatures()) {
            putDefaultCoordinates(feature);
        }
    }

    private void setAnchorPointsToDefault() {
        for (Feature feature : Feature.getGuiFeatures()) {
            EnumUtils.AnchorPoint anchorPoint = feature.getAnchorPoint();
            if (anchorPoint != null) {
                anchorPoints.put(feature, anchorPoint);
            }
        }
    }

    private void putDefaultCoordinates(Feature feature) {
        CoordsPair coords = feature.getDefaultCoordinates();
        if (coords != null) {
            coordinates.put(feature, coords);
        }
    }

    private void putDefaultBarSizes() {
        for (Feature feature : Feature.getGuiFeatures()) {
            CoordsPair size = feature.getDefaultBarSize();
            if (size != null) {
                barSizes.put(feature, size);
            }
        }
    }

    public void loadLanguageFile(boolean pullOnline) {
        loadLanguageFile(language);
        if (pullOnline) tryPullingLanguageOnline(language); // try getting an updated version online after loading the local one
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

    private void tryPullingLanguageOnline(Language language) {
        FMLLog.info("[SkyblockAddons] Attempting to pull updated language files from online.");
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/biscuut/SkyblockAddons/master/src/main/resources/lang/" + language.getPath() + ".json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", Utils.USER_AGENT);

                FMLLog.info("[SkyblockAddons] Got response code " + connection.getResponseCode());

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }
                connection.disconnect();
                JsonObject onlineMessages = new Gson().fromJson(response.toString(), JsonObject.class);
                mergeLanguageJsonObject(onlineMessages, languageConfig);
            } catch (JsonParseException | IllegalStateException | IOException ex) {
                ex.printStackTrace();
                System.out.println("SkyblockAddons: There was an error loading the language file online");
            }
        }).start();
    }

    /**
     * This is used to merge in the online language entries into the existing ones.
     * Using this method rather than an overwrite allows new entries in development to still exist.
     *
     * @param jsonObject The object to be merged (online entries).
     * @param targetObject The object to me merged in to (local entries).
     */
    private void mergeLanguageJsonObject(JsonObject jsonObject, JsonObject targetObject) {
        for (Map.Entry<String, JsonElement> entry : targetObject.entrySet()) {
            String memberName = entry.getKey();
            JsonElement value = entry.getValue();
            if (jsonObject.has(memberName)) {
                if (value instanceof JsonObject) {
                    mergeLanguageJsonObject(jsonObject.getAsJsonObject(memberName), (JsonObject)value);
                } else {
                    targetObject.add(memberName, value);
                }
            }
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
            for (int slot : legacyLockedSlots) {
                jsonArray.add(new GsonBuilder().create().toJsonTree(slot));
            }
            settingsConfig.add("lockedSlots", jsonArray);

            JsonObject profileSlotsObject = new JsonObject();
            for (Map.Entry<String, Set<Integer>> entry : profileLockedSlots.entrySet()) {
                JsonArray lockedSlots = new JsonArray();
                for (int slot : entry.getValue()) {
                    lockedSlots.add(new GsonBuilder().create().toJsonTree(slot));
                }
                profileSlotsObject.add(entry.getKey(), lockedSlots);
            }
            settingsConfig.add("profileLockedSlots", profileSlotsObject);

            JsonObject anchorObject = new JsonObject();
            for (Feature feature : Feature.getGuiFeatures()) {
                anchorObject.addProperty(String.valueOf(feature.getId()), getAnchorPoint(feature).getId());
            }
            settingsConfig.add("anchorPoints", anchorObject);

            JsonObject scalesObject = new JsonObject();
            for (Feature feature : guiScales.keySet()) {
                scalesObject.addProperty(String.valueOf(feature.getId()), guiScales.get(feature).getValue());
            }
            settingsConfig.add("guiScales", scalesObject);

//            JsonObject oldColorsObject = new JsonObject();
//            for (Feature feature : featureColors.keySet()) {
//                ChatFormatting featureColor = featureColors.get(feature);
//                if (featureColor != ChatFormatting.RED) { // Red is default, no need to save it!
//                    oldColorsObject.addProperty(String.valueOf(feature.getId()), featureColor.ordinal());
//                }
//            }
//            settingsConfig.add("featureColors", oldColorsObject);

            JsonObject colorsObject = new JsonObject();
            for (Feature feature : colors.keySet()) {
                int featureColor = colors.get(feature);
                if (featureColor != ChatFormatting.RED.getRGB()) { // Red is default, no need to save it!
                    colorsObject.addProperty(String.valueOf(feature.getId()), colors.get(feature));
                }
            }
            settingsConfig.add("colors", colorsObject);

            JsonObject coordinatesObject = new JsonObject();
            for (Feature feature : coordinates.keySet()) {
                JsonArray coordinatesArray = new JsonArray();
                coordinatesArray.add(new GsonBuilder().create().toJsonTree(coordinates.get(feature).getX()));
                coordinatesArray.add(new GsonBuilder().create().toJsonTree(coordinates.get(feature).getY()));
                coordinatesObject.add(String.valueOf(feature.getId()), coordinatesArray);
            }
            settingsConfig.add("guiPositions", coordinatesObject);

            JsonObject barSizesObject = new JsonObject();
            for (Feature feature : barSizes.keySet()) {
                JsonArray sizesArray = new JsonArray();
                sizesArray.add(new GsonBuilder().create().toJsonTree(barSizes.get(feature).getX()));
                sizesArray.add(new GsonBuilder().create().toJsonTree(barSizes.get(feature).getY()));
                barSizesObject.add(String.valueOf(feature.getId()), sizesArray);
            }
            settingsConfig.add("barSizes", barSizesObject);

            settingsConfig.addProperty("warningSeconds", warningSeconds);

            settingsConfig.addProperty("textStyle", textStyle.ordinal());
            settingsConfig.addProperty("language", language.getPath());
            settingsConfig.addProperty("backpackStyle", backpackStyle.ordinal());
            settingsConfig.addProperty("powerOrbStyle", powerOrbDisplayStyle.ordinal());

            JsonArray chromaFeaturesArray = new JsonArray();
            for (Feature feature : chromaFeatures) {
                chromaFeaturesArray.add(new GsonBuilder().create().toJsonTree(feature.getId()));
            }
            settingsConfig.add("chromaFeatures", chromaFeaturesArray);
            settingsConfig.addProperty("chromaSpeed", chromaSpeed);
            settingsConfig.addProperty("chromaMode", chromaMode.ordinal());

            settingsConfig.addProperty("configVersion", CONFIG_VERSION);

            bufferedWriter.write(settingsConfig.toString());
            bufferedWriter.close();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("SkyblockAddons: An error occurred while attempting to save the config!");
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

    public Color getColor(Feature feature, int alpha) {
        if (chromaFeatures.contains(feature)) {
            return ChromaManager.getCurrentColor(getActualX(feature), getActualY(feature));
        }

        Color color = getColor(feature);

        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public Color getColor(Feature feature) {
        if (chromaFeatures.contains(feature)) {
            return ChromaManager.getCurrentColor(getActualX(feature), getActualY(feature));
        }

        ChatFormatting defaultColor = feature.getDefaultColor();
        return new Color(colors.getOrDefault(feature, defaultColor != null ? defaultColor.getRGB() : ChatFormatting.RED.getRGB()));
    }

    public ChatFormatting getRestrictedColor(Feature feature) {
        Integer featureColor = colors.get(feature);

        if (featureColor != null) {
            for (ChatFormatting chatFormatting : ChatFormatting.values()) {
                try {
                    if (chatFormatting.getRGB() == featureColor) {
                        return chatFormatting;
                    }
                }
                catch (IllegalArgumentException ignored) {
                    // This chat formatting has no color, let's ignore it.
                }
            }
        }

        return feature.getDefaultColor();
    }

    public void setColor(Feature feature, int color) {
        colors.put(feature, color);
    }

    public int getActualX(Feature feature) {
        int maxX = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
        return getAnchorPoint(feature).getX(maxX)+ getRelativeCoords(feature).getX();
    }

    public int getActualY(Feature feature) {
        int maxY = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
        return getAnchorPoint(feature).getY(maxY)+ getRelativeCoords(feature).getY();
    }

    public CoordsPair getSizes(Feature feature) {
        CoordsPair defaultSize = feature.getDefaultBarSize();
        return barSizes.getOrDefault(feature, defaultSize != null ? defaultSize : new CoordsPair(7,1));
    }

    public void setSizeX(Feature feature, int x) {
        CoordsPair coords = getSizes(feature);
        coords.setX(x);
        barSizes.put(feature, coords);
    }

    public void setSizeY(Feature feature, int y) {
        CoordsPair coords = getSizes(feature);
        coords.setY(y);
        barSizes.put(feature, coords);
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
        EnumUtils.AnchorPoint closestAnchorPoint = EnumUtils.AnchorPoint.BOTTOM_MIDDLE; // default
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

    public EnumUtils.AnchorPoint getAnchorPoint(Feature feature) {
        EnumUtils.AnchorPoint defaultPoint = feature.getAnchorPoint();

        return anchorPoints.getOrDefault(feature, defaultPoint != null ? defaultPoint : EnumUtils.AnchorPoint.BOTTOM_MIDDLE);
    }

    public Set<Integer> getLockedSlots() {
        String profile = main.getUtils().getProfileName();
        if (profile == null) return legacyLockedSlots;
        if (!profileLockedSlots.containsKey(profile)) {
            profileLockedSlots.put(profile, new HashSet<>(legacyLockedSlots));
        }
        return profileLockedSlots.get(profile);
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

    /** These two are taken from GuiOptionSlider. */
    public float denormalizeScale(float value) {
        return snapToStepClamp(ConfigValues.GUI_SCALE_MINIMUM + (ConfigValues.GUI_SCALE_MAXIMUM - ConfigValues.GUI_SCALE_MINIMUM) *
                MathHelper.clamp_float(value, 0.0F, 1.0F));
    }
    public float snapToStepClamp(float value) {
        value = ConfigValues.GUI_SCALE_STEP * (float) Math.round(value / ConfigValues.GUI_SCALE_STEP);
        return MathHelper.clamp_float(value, ConfigValues.GUI_SCALE_MINIMUM, ConfigValues.GUI_SCALE_MAXIMUM);
    }

    public Set<Feature> getChromaFeatures() {
        return chromaFeatures;
    }

    public float getChromaSpeed() {
        return chromaSpeed;
    }

    public void setChromaSpeed(float chromaSpeed) {
        this.chromaSpeed = chromaSpeed;
    }

    public EnumUtils.ChromaMode getChromaMode() {
        return chromaMode;
    }

    public void setChromaMode(EnumUtils.ChromaMode chromaMode) {
        this.chromaMode = chromaMode;
    }
}
