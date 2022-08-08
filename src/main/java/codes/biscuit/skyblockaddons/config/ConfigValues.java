package codes.biscuit.skyblockaddons.config;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.chroma.ManualChromaManager;
import codes.biscuit.skyblockaddons.features.discordrpc.DiscordStatus;
import codes.biscuit.skyblockaddons.features.enchants.EnchantListLayout;
import codes.biscuit.skyblockaddons.features.enchants.EnchantManager;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.objects.FloatPair;
import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Logger;

import java.awt.geom.Point2D;
import java.beans.Introspector;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ConfigValues {

    private static final int CONFIG_VERSION = 9;

    private final static float DEFAULT_GUI_SCALE = normalizeValueNoStep(1);
    private final static float GUI_SCALE_MINIMUM = 0.5F;
    private final static float GUI_SCALE_MAXIMUM = 5;

    private static final ReentrantLock SAVE_LOCK = new ReentrantLock();

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger logger = SkyblockAddons.getLogger();

    private final Map<Feature, FloatPair> defaultCoordinates = new EnumMap<>(Feature.class);
    private final Map<Feature, EnumUtils.AnchorPoint> defaultAnchorPoints = new EnumMap<>(Feature.class);
    private final Map<Feature, Float> defaultGuiScales = new EnumMap<>(Feature.class);
    private final Map<Feature, FloatPair> defaultBarSizes = new EnumMap<>(Feature.class);

    private final File settingsConfigFile;
    private JsonObject loadedConfig = new JsonObject();
    @Getter
    @Setter
    private JsonObject languageConfig = new JsonObject();

    @Getter
    private final Set<Feature> disabledFeatures = EnumSet.noneOf(Feature.class);
    private final Map<Feature, Integer> colors = new HashMap<>();
    private Map<Feature, Float> guiScales = new EnumMap<>(Feature.class);
    private final Map<Feature, FloatPair> barSizes = new EnumMap<>(Feature.class);
    private final MutableInt warningSeconds = new MutableInt(4);
    private final Map<Feature, FloatPair> coordinates = new EnumMap<>(Feature.class);
    private Map<Feature, EnumUtils.AnchorPoint> anchorPoints = new EnumMap<>(Feature.class);
    private final MutableObject<Language> language = new MutableObject<>(Language.ENGLISH);
    private final MutableObject<EnumUtils.BackpackStyle> backpackStyle = new MutableObject<>(EnumUtils.BackpackStyle.GUI);
    private final MutableObject<EnumUtils.PowerOrbDisplayStyle> powerOrbDisplayStyle = new MutableObject<>(EnumUtils.PowerOrbDisplayStyle.COMPACT);
    private final MutableObject<EnumUtils.TextStyle> textStyle = new MutableObject<>(EnumUtils.TextStyle.STYLE_ONE);
    private final Map<String, Set<Integer>> profileLockedSlots = new HashMap<>();
    @Getter
    private final Set<Feature> chromaFeatures = EnumSet.noneOf(Feature.class);
    @Deprecated
    private final MutableFloat oldChromaSpeed = new MutableFloat(0.19354838F); // 2.0
    private final MutableObject<EnumUtils.ChromaMode> chromaMode = new MutableObject<>(EnumUtils.ChromaMode.FADE);
    private final MutableFloat chromaFadeWidth = new MutableFloat(0.22580644F); // 10° Hue
    private final MutableObject<DiscordStatus> discordDetails = new MutableObject<>(DiscordStatus.LOCATION);
    private final MutableObject<DiscordStatus> discordStatus = new MutableObject<>(DiscordStatus.AUTO_STATUS);
    private final MutableObject<DiscordStatus> discordAutoDefault = new MutableObject<>(DiscordStatus.NONE);
    @Getter
    private final List<String> discordCustomStatuses = new ArrayList<>();
    @Getter
    private final MutableFloat mapZoom = new MutableFloat(0.18478261F); // 1.3
    @Getter
    private final MutableFloat healingCircleOpacity = new MutableFloat(0.4);
    @Setter
    @Getter
    private MutableFloat chromaSize = new MutableFloat(30);
    @Getter
    private final MutableFloat chromaSpeed = new MutableFloat(6);
    @Getter
    private final MutableFloat chromaSaturation = new MutableFloat(0.75F);
    @Getter
    private final MutableFloat chromaBrightness = new MutableFloat(0.9F);
    private final MutableObject<EnchantListLayout> enchantLayout = new MutableObject<>(EnchantListLayout.NORMAL);

    public ConfigValues(File settingsConfigFile) {
        this.settingsConfigFile = settingsConfigFile;
    }

    public void loadValues() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("default.json");
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                     StandardCharsets.UTF_8)) {
            JsonObject defaultValues = SkyblockAddons.getGson().fromJson(inputStreamReader, JsonObject.class);

            deserializeFeatureFloatCoordsMapFromID(defaultValues, defaultCoordinates, "coordinates");
            deserializeEnumEnumMapFromIDS(defaultValues, defaultAnchorPoints, "anchorPoints", Feature.class, EnumUtils.AnchorPoint.class);
            deserializeEnumNumberMapFromID(defaultValues, defaultGuiScales, "guiScales", Feature.class, float.class);
            deserializeFeatureIntCoordsMapFromID(defaultValues, defaultBarSizes, "barSizes");
        } catch (Exception ex) {
            CrashReport crashReport = CrashReport.makeCrashReport(ex, "Reading default settings file");
            throw new ReportedException(crashReport);
        }

        if (settingsConfigFile.exists()) {
            try (FileReader reader = new FileReader(settingsConfigFile)) {
                JsonElement fileElement = new JsonParser().parse(reader);

                if (fileElement == null || fileElement.isJsonNull()) {
                    throw new JsonParseException("File is null!");
                }
                loadedConfig = fileElement.getAsJsonObject();
            } catch (JsonParseException | IllegalStateException | IOException ex) {
                logger.error("There was an error loading the config. Resetting all settings to default.");
                logger.catching(ex);
                addDefaultsAndSave();
                return;
            }

            int configVersion;
            if (loadedConfig.has("configVersion")) {
                configVersion = loadedConfig.get("configVersion").getAsInt();
            } else {
                configVersion = ConfigValues.CONFIG_VERSION;
            }

            deserializeFeatureSetFromID(disabledFeatures, "disabledFeatures");
            deserializeStringIntSetMap(profileLockedSlots, "profileLockedSlots");
            deserializeNumber(warningSeconds, "warningSeconds", int.class);

            try {
                if (loadedConfig.has("language")) {
                    String languageKey = loadedConfig.get("language").getAsString();
                    Language configLanguage = Language.getFromPath(languageKey);
                    if (configLanguage != null) {
                        setLanguage(configLanguage); // TODO Will this crash?
//                        language.setValue(configLanguage);
                    }
                }
            } catch (Exception ex) {
                logger.error("Failed to deserialize path: language");
                logger.catching(ex);
            }

            deserializeEnumValueFromOrdinal(backpackStyle, "backpackStyle");
            deserializeEnumValueFromOrdinal(powerOrbDisplayStyle, "powerOrbStyle");
            deserializeEnumEnumMapFromIDS(anchorPoints, "anchorPoints", Feature.class, EnumUtils.AnchorPoint.class);
            deserializeEnumNumberMapFromID(guiScales, "guiScales", Feature.class, float.class);

            try {
                for (Feature feature : Feature.getGuiFeatures()) { // TODO Legacy format from 1.3.4, remove in the future.
                    String property = Introspector.decapitalize(WordUtils.capitalizeFully(feature.toString().replace("_", " "))).replace(" ", "");
                    String x = property+"X";
                    String y = property+"Y";
                    if (loadedConfig.has(x)) {
                        coordinates.put(feature, new FloatPair(loadedConfig.get(x).getAsFloat(), loadedConfig.get(y).getAsFloat()));
                    }
                }
            } catch (Exception ex) {
                logger.error("Failed to deserialize path: coordinates (legacy)");
                logger.catching(ex);
            }

            if (loadedConfig.has("coordinates")) {
                deserializeFeatureFloatCoordsMapFromID(coordinates, "coordinates");
            } else {
                deserializeFeatureFloatCoordsMapFromID(coordinates, "guiPositions"); // TODO Legacy format from 1.4.2/1.5-betas, remove in the future.
            }
            deserializeFeatureIntCoordsMapFromID(barSizes, "barSizes");

            if (loadedConfig.has("featureColors")) { // TODO Legacy format from 1.3.4, remove in the future.
                try {
                    for (Map.Entry<String, JsonElement> element : loadedConfig.getAsJsonObject("featureColors").entrySet()) {
                        Feature feature = Feature.fromId(Integer.parseInt(element.getKey()));
                        if (feature != null) {
                            ColorCode colorCode = ColorCode.values()[element.getValue().getAsInt()];
                            if (colorCode.isColor() && colorCode != ColorCode.RED) { // Red is default, no need to set it.
                                colors.put(feature, colorCode.getColor());
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.error("Failed to deserialize path: featureColors");
                    logger.catching(ex);
                }
            } else {
                deserializeEnumNumberMapFromID(colors, "colors", Feature.class, int.class);
            }

            deserializeEnumValueFromOrdinal(textStyle, "textStyle");
            deserializeFeatureSetFromID(chromaFeatures, "chromaFeatures");
            if (configVersion <= 8) {
                deserializeNumber(oldChromaSpeed, "chromaSpeed", float.class);
                chromaSpeed.setValue(MathUtils.denormalizeSliderValue(oldChromaSpeed.floatValue(), 0.1F, 10, 0.5F));
            } else {
                deserializeNumber(chromaSpeed, "chromaSpeed", float.class);
            }
            deserializeNumber(chromaSize, "chromaSize", float.class);
            deserializeEnumValueFromOrdinal(chromaMode, "chromaMode");
            deserializeNumber(chromaFadeWidth, "chromaFadeWidth", float.class);
            deserializeEnumValueFromOrdinal(discordStatus, "discordStatus");
            deserializeEnumValueFromOrdinal(discordDetails, "discordDetails");
            deserializeEnumValueFromOrdinal(discordAutoDefault, "discordAutoDefault");
            deserializeStringCollection(discordCustomStatuses, "discordCustomStatuses");
            deserializeEnumValueFromOrdinal(enchantLayout, "enchantLayout");

            deserializeNumber(mapZoom, "mapZoom", float.class);
            deserializeNumber(chromaSaturation, "chromaSaturation", float.class);
            deserializeNumber(chromaBrightness, "chromaBrightness", float.class);

            if (configVersion <= 5) {
                disabledFeatures.add(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS);
            } else if (configVersion <= 6) {
                putDefaultBarSizes();
                for (Map.Entry<Feature, FloatPair> entry : coordinates.entrySet()) {
                    if (getAnchorPoint(entry.getKey()) == EnumUtils.AnchorPoint.BOTTOM_MIDDLE) {
                        FloatPair coords = entry.getValue();
                        coords.setX(coords.getX()-91);
                        coords.setY(coords.getY()-39);
                    }
                }
            } else if (configVersion <= 7) {
                for (Map.Entry<Feature, FloatPair> entry : coordinates.entrySet()) {
                    Feature feature = entry.getKey();
                    FloatPair coords = entry.getValue();

                    if (feature == Feature.DARK_AUCTION_TIMER || feature == Feature.FARM_EVENT_TIMER ||feature == Feature.ZEALOT_COUNTER || feature == Feature.SKILL_DISPLAY
                            || feature == Feature.SHOW_TOTAL_ZEALOT_COUNT || feature == Feature.SHOW_SUMMONING_EYE_COUNT || feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE ||
                            feature == Feature.BIRCH_PARK_RAINMAKER_TIMER || feature == Feature.ENDSTONE_PROTECTOR_DISPLAY) {
                        coords.setY(coords.getY() + 2/2F);
                        coords.setX(coords.getX() - 18/2F);
                        coords.setY(coords.getY() - 9/2F);
                    }

                    if (feature.getGuiFeatureData() != null && feature.getGuiFeatureData().getDrawType() == EnumUtils.DrawType.BAR) {
                        coords.setY(coords.getY() + 1);
                    }
                }
            }

            int lastFeatureID;
            if (loadedConfig.has("lastFeatureID")) {
                lastFeatureID = loadedConfig.get("lastFeatureID").getAsInt();
            } else {
                // This system was added after this feature.
                lastFeatureID = Feature.SKYBLOCK_ADDONS_BUTTON_IN_PAUSE_MENU.getId();
            }
            // This will go through every feature, and if they are new features that didn't exist before
            // that should be disabled by default, and their coordinates are default, this will disable those features.
            for (Feature feature : Feature.values()) {
                if (feature.getId() > lastFeatureID && feature.isDefaultDisabled() && featureCoordinatesAreDefault(feature)) {
                    this.getDisabledFeatures().add(feature);
                }
            }
        } else {
            addDefaultsAndSave();
        }
    }

    private void addDefaultsAndSave() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            if (mc.getLanguageManager() != null && mc.getLanguageManager().getCurrentLanguage().getLanguageCode() != null) {
                String minecraftLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase(Locale.US);
                Language configLanguage = Language.getFromPath(minecraftLanguage);
                if (configLanguage != null) { // Check if we have the exact locale they are using for Minecraft
                    language.setValue(configLanguage);
                } else { // Check if we at least have the same language (different locale)
                    String languageCode = minecraftLanguage.split("_")[0];
                    for (Language loopLanguage : Language.values()) {
                        String loopLanguageCode = loopLanguage.getPath().split("_")[0];
                        if (loopLanguageCode.equals(languageCode)) {
                            language.setValue(loopLanguage);
                            break;
                        }
                    }
                }
            }
        }

        for (Feature feature : Feature.values()) {
            ColorCode color = feature.getDefaultColor();
            if (color != null) {
                colors.put(feature, color.getColor());
            }
            if (feature.isDefaultDisabled()) {
                disabledFeatures.add(feature);
            }
        }

        setAllCoordinatesToDefault();
        putDefaultBarSizes();
        saveConfig();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveConfig() {
        EnchantManager.markCacheDirty();
        SkyblockAddons.runAsync(() -> {
            if (!SAVE_LOCK.tryLock()) {
                return;
            }

            logger.info("Saving config");

            try {
                settingsConfigFile.createNewFile();

                JsonObject saveConfig = new JsonObject();

                JsonArray jsonArray = new JsonArray();
                for (Feature element : disabledFeatures) {
                    jsonArray.add(new GsonBuilder().create().toJsonTree(element.getId()));
                }
                saveConfig.add("disabledFeatures", jsonArray);

                JsonObject profileSlotsObject = new JsonObject();
                for (Map.Entry<String, Set<Integer>> entry : profileLockedSlots.entrySet()) {
                    JsonArray lockedSlots = new JsonArray();
                    for (int slot : entry.getValue()) {
                        lockedSlots.add(new GsonBuilder().create().toJsonTree(slot));
                    }
                    profileSlotsObject.add(entry.getKey(), lockedSlots);
                }
                saveConfig.add("profileLockedSlots", profileSlotsObject);

                JsonObject anchorObject = new JsonObject();
                for (Feature feature : Feature.getGuiFeatures()) {
                    anchorObject.addProperty(String.valueOf(feature.getId()), getAnchorPoint(feature).getId());
                }
                saveConfig.add("anchorPoints", anchorObject);

                JsonObject scalesObject = new JsonObject();
                for (Feature feature : guiScales.keySet()) {
                    scalesObject.addProperty(String.valueOf(feature.getId()), guiScales.get(feature));
                }
                saveConfig.add("guiScales", scalesObject);

                JsonObject colorsObject = new JsonObject();
                for (Feature feature : colors.keySet()) {
                    int featureColor = colors.get(feature);
                    if (featureColor != ColorCode.RED.getColor()) { // Red is default, no need to save it!
                        colorsObject.addProperty(String.valueOf(feature.getId()), colors.get(feature));
                    }
                }
                saveConfig.add("colors", colorsObject);

                // Old gui coordinates, for backwards compatibility...
                JsonObject coordinatesObject = new JsonObject();
                for (Feature feature : coordinates.keySet()) {
                    JsonArray coordinatesArray = new JsonArray();
                    coordinatesArray.add(new GsonBuilder().create().toJsonTree(Math.round(coordinates.get(feature).getX())));
                    coordinatesArray.add(new GsonBuilder().create().toJsonTree(Math.round(coordinates.get(feature).getY())));
                    coordinatesObject.add(String.valueOf(feature.getId()), coordinatesArray);
                }
                saveConfig.add("guiPositions", coordinatesObject);
                // New gui coordinates
                coordinatesObject = new JsonObject();
                for (Feature feature : coordinates.keySet()) {
                    JsonArray coordinatesArray = new JsonArray();
                    coordinatesArray.add(new GsonBuilder().create().toJsonTree(coordinates.get(feature).getX()));
                    coordinatesArray.add(new GsonBuilder().create().toJsonTree(coordinates.get(feature).getY()));
                    coordinatesObject.add(String.valueOf(feature.getId()), coordinatesArray);
                }
                saveConfig.add("coordinates", coordinatesObject);

                JsonObject barSizesObject = new JsonObject();
                for (Feature feature : barSizes.keySet()) {
                    JsonArray sizesArray = new JsonArray();
                    sizesArray.add(new GsonBuilder().create().toJsonTree(barSizes.get(feature).getX()));
                    sizesArray.add(new GsonBuilder().create().toJsonTree(barSizes.get(feature).getY()));
                    barSizesObject.add(String.valueOf(feature.getId()), sizesArray);
                }
                saveConfig.add("barSizes", barSizesObject);

                saveConfig.addProperty("warningSeconds", warningSeconds);

                saveConfig.addProperty("textStyle", textStyle.getValue().ordinal());
                saveConfig.addProperty("language", language.getValue().getPath());
                saveConfig.addProperty("backpackStyle", backpackStyle.getValue().ordinal());
                saveConfig.addProperty("powerOrbStyle", powerOrbDisplayStyle.getValue().ordinal());

                JsonArray chromaFeaturesArray = new JsonArray();
                for (Feature feature : chromaFeatures) {
                    chromaFeaturesArray.add(new GsonBuilder().create().toJsonTree(feature.getId()));
                }
                saveConfig.add("chromaFeatures", chromaFeaturesArray);
                saveConfig.addProperty("chromaSpeed", chromaSpeed);
                saveConfig.addProperty("chromaMode", chromaMode.getValue().ordinal());
                saveConfig.addProperty("chromaSize", chromaSize);

                saveConfig.addProperty("discordStatus", discordStatus.getValue().ordinal());
                saveConfig.addProperty("discordDetails", discordDetails.getValue().ordinal());
                saveConfig.addProperty("discordAutoDefault", discordAutoDefault.getValue().ordinal());
                saveConfig.addProperty("enchantLayout", enchantLayout.getValue().ordinal());

                JsonArray discordCustomStatusesArray = new JsonArray();
                for (String string : discordCustomStatuses) {
                    discordCustomStatusesArray.add(new GsonBuilder().create().toJsonTree(string));
                }
                saveConfig.add("discordCustomStatuses", discordCustomStatusesArray);

                saveConfig.addProperty("mapZoom", mapZoom);
                saveConfig.addProperty("chromaSaturation", chromaSaturation);
                saveConfig.addProperty("chromaBrightness", chromaBrightness);

                saveConfig.addProperty("configVersion", CONFIG_VERSION);
                int largestFeatureID = 0;
                for (Feature feature : Feature.values()) {
                    if (feature.getId() > largestFeatureID) largestFeatureID = feature.getId();
                }
                saveConfig.addProperty("lastFeatureID", largestFeatureID);

                try (FileWriter writer = new FileWriter(settingsConfigFile)) {
                    SkyblockAddons.getGson().toJson(saveConfig, writer);
                }
            } catch (Exception ex) {
                logger.error("An error occurred while attempting to save the config!");
                logger.catching(ex);
            }

            SAVE_LOCK.unlock();

            logger.info("Config saved");
        });
    }


    private void deserializeFeatureSetFromID(Collection<Feature> collection, String path) {
        try {
            if (loadedConfig.has(path)) {
                for (JsonElement element : loadedConfig.getAsJsonArray(path)) {
                    Feature feature = Feature.fromId(element.getAsInt());
                    if (feature != null) {
                        collection.add(feature);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to deserialize path: "+ path);
            logger.catching(ex);
        }
    }

    private void deserializeStringCollection(Collection<String> collection, String path) {
        try {
            if (loadedConfig.has(path)) {
                for (JsonElement element : loadedConfig.getAsJsonArray(path)) {
                    String string = element.getAsString();
                    if (string != null) {
                        collection.add(string);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to deserialize path: "+ path);
            logger.catching(ex);
        }
    }

    private void deserializeStringIntSetMap(Map<String, Set<Integer>> map, String path) {
        try {
            if (loadedConfig.has(path)) {
                JsonObject profileSlotsObject = loadedConfig.getAsJsonObject(path);
                for (Map.Entry<String, JsonElement> entry : profileSlotsObject.entrySet()) {
                    Set<Integer> slots = new HashSet<>();
                    for (JsonElement element : entry.getValue().getAsJsonArray()) {
                        slots.add(element.getAsInt());
                    }
                    map.put(entry.getKey(), slots);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to deserialize path: "+ path);
            logger.catching(ex);
        }
    }

    private <E extends Enum<?>, F extends Enum<?>> void deserializeEnumEnumMapFromIDS(Map<E, F> map, String path, Class<E> keyClass, Class<F> valueClass) {
        deserializeEnumEnumMapFromIDS(loadedConfig, map, path, keyClass, valueClass);
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<?>, F extends Enum<?>> void deserializeEnumEnumMapFromIDS(JsonObject jsonObject, Map<E, F> map, String path, Class<E> keyClass, Class<F> valueClass) {
        try {
            if (jsonObject.has(path)) {
                for (Map.Entry<String, JsonElement> element : jsonObject.getAsJsonObject(path).entrySet()) {

                    Method fromId = keyClass.getDeclaredMethod("fromId", int.class);
                    E key = (E)fromId.invoke(null, Integer.parseInt(element.getKey()));

                    fromId = valueClass.getDeclaredMethod("fromId", int.class);
                    F value = (F)fromId.invoke(null, element.getValue().getAsInt());

                    if (key != null && value != null) {
                        map.put(key, value);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to deserialize path: "+ path);
            logger.catching(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<?>, N extends Number> void deserializeEnumNumberMapFromID(Map<E, N> map, String path, Class<E> keyClass, Class<N> numberClass) {
        deserializeEnumNumberMapFromID(loadedConfig, map, path, keyClass, numberClass);
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<?>, N extends Number> void deserializeEnumNumberMapFromID(JsonObject jsonObject, Map<E, N> map, String path, Class<E> keyClass, Class<N> numberClass) {
        try {
            if (jsonObject.has(path)) {
                for (Map.Entry<String, JsonElement> element : jsonObject.getAsJsonObject(path).entrySet()) {
                    Method fromId = keyClass.getDeclaredMethod("fromId", int.class);
                    E key = (E)fromId.invoke(null, Integer.parseInt(element.getKey()));
                    if (key != null) {
                        map.put(key, (N)getNumber(element.getValue(), numberClass));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to deserialize path: "+ path);
            logger.catching(ex);
        }
    }

    private <N extends Number> void deserializeNumber(Mutable<Number> number, String path, Class<N> numberClass) {
        try {
            if (loadedConfig.has(path)) {
                number.setValue(getNumber(loadedConfig.get(path), numberClass));
            }
        } catch (Exception ex) {
            logger.error("Failed to deserialize path: "+ path);
            logger.catching(ex);
        }
    }

    private Number getNumber(JsonElement jsonElement, Class<? extends Number> numberClass) {
        if (numberClass == byte.class) { return jsonElement.getAsByte();
        } else if (numberClass == short.class) { return jsonElement.getAsShort();
        } else if (numberClass == int.class) { return jsonElement.getAsInt();
        } else if (numberClass == long.class) { return jsonElement.getAsLong();
        } else if (numberClass == float.class) { return jsonElement.getAsFloat();
        } else if (numberClass == double.class) { return jsonElement.getAsDouble(); }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<?>> void deserializeEnumValueFromOrdinal(MutableObject<E> value, String path) {
        try {
            Class<? extends Enum<?>> enumClass = value.getValue().getDeclaringClass();
            Method method = enumClass.getDeclaredMethod("values");
            Object valuesObject = method.invoke(null);
            E[] values = (E[])valuesObject;

            if (loadedConfig.has(path)) {
                int ordinal = loadedConfig.get(path).getAsInt();
                if (values.length > ordinal) {
                    E enumValue = values[ordinal];
                    if (enumValue != null) {
                        value.setValue(values[ordinal]);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to deserialize path: "+ path);
            logger.catching(ex);
        }
    }

    private void deserializeFeatureFloatCoordsMapFromID(Map<Feature, FloatPair> map, String path) {
        deserializeFeatureFloatCoordsMapFromID(loadedConfig, map, path);
    }

    private void deserializeFeatureFloatCoordsMapFromID(JsonObject jsonObject, Map<Feature, FloatPair> map, String path) {
        try {
            if (jsonObject.has(path)) {
                for (Map.Entry<String, JsonElement> element : jsonObject.getAsJsonObject(path).entrySet()) {
                    Feature feature = Feature.fromId(Integer.parseInt(element.getKey()));
                    if (feature != null) {
                        JsonArray coords = element.getValue().getAsJsonArray();
                        map.put(feature, new FloatPair(coords.get(0).getAsFloat(), coords.get(1).getAsFloat()));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to deserialize path: "+ path);
            logger.catching(ex);
        }
    }

    private void deserializeFeatureIntCoordsMapFromID(Map<Feature, FloatPair> map, String path) {
        deserializeFeatureIntCoordsMapFromID(loadedConfig, map, path);
    }

    private void deserializeFeatureIntCoordsMapFromID(JsonObject jsonObject, Map<Feature, FloatPair> map, String path) {
        try {
            if (jsonObject.has(path)) {
                for (Map.Entry<String, JsonElement> element : jsonObject.getAsJsonObject(path).entrySet()) {
                    Feature feature = Feature.fromId(Integer.parseInt(element.getKey()));
                    if (feature != null) {
                        JsonArray coords = element.getValue().getAsJsonArray();
                        map.put(feature, new FloatPair(coords.get(0).getAsFloat(), coords.get(1).getAsFloat()));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to deserialize path: "+ path);
            logger.catching(ex);
        }
    }

    public void setAllCoordinatesToDefault() {
        coordinates.clear();
        for (Map.Entry<Feature, FloatPair> entry : defaultCoordinates.entrySet()) {
            coordinates.put(entry.getKey(), entry.getValue().cloneCoords());
        }

        anchorPoints = new HashMap<>(defaultAnchorPoints);

        guiScales = new HashMap<>(defaultGuiScales);
    }

    private void putDefaultCoordinates(Feature feature) {
        FloatPair coords = defaultCoordinates.get(feature);
        if (coords != null) {
            coordinates.put(feature, coords);
        }
    }

    public void putDefaultBarSizes() {
        barSizes.clear();
        for (Map.Entry<Feature, FloatPair> entry : defaultBarSizes.entrySet()) {
            barSizes.put(entry.getKey(), entry.getValue().cloneCoords());
        }
    }

    public static float normalizeValueNoStep(float value) {
        return MathHelper.clamp_float((snapNearDefaultValue(value) - ConfigValues.GUI_SCALE_MINIMUM) /
                (ConfigValues.GUI_SCALE_MAXIMUM - ConfigValues.GUI_SCALE_MINIMUM), 0.0F, 1.0F);
    }

    /** These two are taken from GuiOptionSlider. */
    public static float denormalizeScale(float value) {
        return snapNearDefaultValue(ConfigValues.GUI_SCALE_MINIMUM + (ConfigValues.GUI_SCALE_MAXIMUM - ConfigValues.GUI_SCALE_MINIMUM) *
                MathHelper.clamp_float(value, 0.0F, 1.0F));
    }
    public static float snapNearDefaultValue(float value) {
        if (value != 1 && value > 1-0.05 && value < 1+0.05) {
            return 1;
        }

        return value;
    }

    /**
     * Checks the received {@code OnlineData} to determine if the given feature should be disabled.
     * This method checks the list of features to be disabled for all versions first and then checks the list of features that
     * should be disabled for this specific version.
     *
     * @param feature The feature to check
     * @return {@code true} if the feature should be disabled, {@code false} otherwise
     */
    public boolean isRemoteDisabled(Feature feature) {
        if (feature == null) return false;

        HashMap<String, List<Integer>> disabledFeatures = main.getOnlineData().getDisabledFeatures();

        if (disabledFeatures.containsKey("all")) {
            if (disabledFeatures.get("all") != null) {
                if (disabledFeatures.get("all").contains(feature.getId())) {
                    return true;
                }
            } else {
                logger.error("\"all\" key in disabled features map has value of null. Please fix online data.");
            }
        }

        /*
        Check for disabled features for this mod version. Pre-release versions will follow the disabled features
        list for their release version. For example, the version {@code 1.6.0-beta.10} will adhere to the list
        for version {@code 1.6.0}
         */
        String version = SkyblockAddons.VERSION;
        if (version.contains("-")) {
            version = version.split("-")[0];
        }
        if (disabledFeatures.containsKey(version)) {
            if (disabledFeatures.get(version) != null) {
                return disabledFeatures.get(version).contains(feature.getId());
            } else {
                logger.error("\"" + version + "\" key in disabled features map has value of null. Please fix online data.");
            }
        }

        return false;
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

    // TODO Don't force alpha in the future...
    public int getColor(Feature feature) {
        return this.getColor(feature, 255);
    }

    public int getColor(Feature feature, int alpha) {
        // If the minimum alpha value is being limited let's make sure we are a little higher than that
//        if (GlStateManager.alphaState.alphaTest && GlStateManager.alphaState.func == GL11.GL_GREATER && alpha / 255F <= GlStateManager.alphaState.ref) {
//            alpha = ColorUtils.getAlphaIntFromFloat( GlStateManager.alphaState.ref + 0.001F);
//        }

        if (chromaFeatures.contains(feature)) {
            return ManualChromaManager.getChromaColor(0, 0, alpha);
        }

        if (colors.containsKey(feature)) {
            return ColorUtils.setColorAlpha(colors.get(feature), alpha);
        }

        ColorCode defaultColor = feature.getDefaultColor();
        return ColorUtils.setColorAlpha(defaultColor != null ? defaultColor.getColor() : ColorCode.RED.getColor(), alpha);
    }

    /**
     * Return skyblock color compatible with new shaders. Can bind the color (white) unconditionally
     * @param feature the feature
     * @return the color
     */
    public SkyblockColor getSkyblockColor(Feature feature) {
        SkyblockColor color = ColorUtils.getDummySkyblockColor(getColor(feature), chromaFeatures.contains(feature));
        // If chroma is enabled, and we are using shaders, set color to white
        if (color.drawMulticolorUsingShader()) {
            color.setColor(0xFFFFFFFF);
        }
        return color;
    }

    public ColorCode getRestrictedColor(Feature feature) {
        Integer featureColor = colors.get(feature);

        if (featureColor != null) {
            for (ColorCode colorCode : ColorCode.values()) {
                if (!colorCode.isColor()) {
                    continue;
                }

                if (colorCode.getColor() == featureColor) {
                    return colorCode;
                }
            }
        }

        return feature.getDefaultColor();
    }

    private boolean featureCoordinatesAreDefault(Feature feature) {
        if (!defaultCoordinates.containsKey(feature)) {
            return true;
        }
        if (!coordinates.containsKey(feature)) {
            return true;
        }

        return coordinates.get(feature).equals(defaultCoordinates.get(feature));
    }

    public void setColor(Feature feature, int color) {
        colors.put(feature, color);
    }

    public float getActualX(Feature feature) {
        int maxX = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
        return getAnchorPoint(feature).getX(maxX) + getRelativeCoords(feature).getX();
    }

    public float getActualY(Feature feature) {
        int maxY = new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
        return getAnchorPoint(feature).getY(maxY) + getRelativeCoords(feature).getY();
    }

    public FloatPair getSizes(Feature feature) {
        return barSizes.getOrDefault(feature, defaultBarSizes.containsKey(feature) ? defaultBarSizes.get(feature).cloneCoords() : new FloatPair(1, 1));
    }

    public float getSizesX(Feature feature) {
        return Math.min(Math.max(getSizes(feature).getX(), .25F), 1);
    }

    public float getSizesY(Feature feature) {
        return Math.min(Math.max(getSizes(feature).getY(), .25F), 1);
    }

    public void setScaleX(Feature feature, float x) {
        FloatPair coords = getSizes(feature);
        coords.setX(x);
    }

    public void setScaleY(Feature feature, float y) {
        FloatPair coords = getSizes(feature);
        coords.setY(y);
    }

    public FloatPair getRelativeCoords(Feature feature) {
        if (coordinates.containsKey(feature)) {
            return coordinates.get(feature);
        } else {
            putDefaultCoordinates(feature);
            if (coordinates.containsKey(feature)) {
                return coordinates.get(feature);
            } else {
                return new FloatPair(0,0);
            }
        }
    }

    public void setCoords(Feature feature, float x, float y) {
        if (coordinates.containsKey(feature)) {
            coordinates.get(feature).setX(x);
            coordinates.get(feature).setY(y);
        } else {
            coordinates.put(feature, new FloatPair(x, y));
        }
    }

    public EnumUtils.AnchorPoint getClosestAnchorPoint(float x, float y) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int maxX = sr.getScaledWidth();
        int maxY = sr.getScaledHeight();
        double shortestDistance = -1;
        EnumUtils.AnchorPoint closestAnchorPoint = EnumUtils.AnchorPoint.BOTTOM_MIDDLE; // default
        for (EnumUtils.AnchorPoint point : EnumUtils.AnchorPoint.values()) {
            double distance = Point2D.distance(x, y, point.getX(maxX), point.getY(maxY));
            if (shortestDistance == -1 || distance < shortestDistance) {
                closestAnchorPoint = point;
                shortestDistance = distance;
            }
        }
        return closestAnchorPoint;
    }

    public void setClosestAnchorPoint(Feature feature) {
        float x1 = getActualX(feature);
        float y1 = getActualY(feature);
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
        if (this.getAnchorPoint(feature) == closestAnchorPoint) {
            return;
        }
        float targetX = getActualX(feature);
        float targetY = getActualY(feature);
        float x = targetX-closestAnchorPoint.getX(maxX);
        float y = targetY-closestAnchorPoint.getY(maxY);
        anchorPoints.put(feature, closestAnchorPoint);
        setCoords(feature, x, y);
    }

    public EnumUtils.AnchorPoint getAnchorPoint(Feature feature) {
        return anchorPoints.getOrDefault(feature, defaultAnchorPoints.getOrDefault(feature, EnumUtils.AnchorPoint.BOTTOM_MIDDLE));
    }

    public Set<Integer> getLockedSlots() {
        String profile = main.getUtils().getProfileName();
        if (!profileLockedSlots.containsKey(profile)) {
            profileLockedSlots.put(profile, new HashSet<>());
        }

        return profileLockedSlots.get(profile);
    }

    public void setGuiScale(Feature feature, float scale) {
        guiScales.put(feature, scale);
    }

    public float getGuiScale(Feature feature) {
        return getGuiScale(feature, true);
    }

    public float getGuiScale(Feature feature, boolean denormalized) {
        float value = ConfigValues.DEFAULT_GUI_SCALE;
        if (guiScales.containsKey(feature)) {
            value = guiScales.get(feature);
        }
        if (denormalized) {
            value = denormalizeScale(value);
        }
        return value;
    }

    public void setChroma(Feature feature, boolean enabled) {
        if (enabled) {
            chromaFeatures.add(feature);
        } else {
            chromaFeatures.remove(feature);
        }
    }

    public int getWarningSeconds() {
        return warningSeconds.getValue();
    }

    public void setWarningSeconds(int warningSeconds) {
        this.warningSeconds.setValue(warningSeconds);
    }

    public Language getLanguage() {
        return language.getValue();
    }

    public void setLanguage(Language language) {
        this.language.setValue(language);
    }

    public EnumUtils.BackpackStyle getBackpackStyle() {
        return backpackStyle.getValue();
    }

    public void setBackpackStyle(EnumUtils.BackpackStyle backpackStyle) {
        this.backpackStyle.setValue(backpackStyle);
    }

    public EnumUtils.PowerOrbDisplayStyle getPowerOrbDisplayStyle() {
        return powerOrbDisplayStyle.getValue();
    }

    public void setPowerOrbDisplayStyle(EnumUtils.PowerOrbDisplayStyle powerOrbDisplayStyle) {
        this.powerOrbDisplayStyle.setValue(powerOrbDisplayStyle);
    }

    public EnumUtils.TextStyle getTextStyle() {
        return textStyle.getValue();
    }

    public void setTextStyle(EnumUtils.TextStyle textStyle) {
        this.textStyle.setValue(textStyle);
    }

    public EnumUtils.ChromaMode getChromaMode() {
        return chromaMode.getValue();
    }

    public void setChromaMode(EnumUtils.ChromaMode chromaMode) {
        this.chromaMode.setValue(chromaMode);
    }

    public void setChromaFadeWidth(float chromaFadeWidth) {
        this.chromaFadeWidth.setValue(chromaFadeWidth);
    }

    public float getChromaFadeWidth() {
        return chromaFadeWidth.getValue();
    }

    public void setDiscordDetails(DiscordStatus discordDetails) {
        this.discordDetails.setValue(discordDetails);
    }

    public void setDiscordStatus(DiscordStatus discordStatus) {
        this.discordStatus.setValue(discordStatus);
    }

    public DiscordStatus getDiscordStatus() {
        return discordStatus != null ? discordStatus.getValue() : DiscordStatus.NONE;
    }

    public DiscordStatus getDiscordDetails() {
        return discordDetails != null ? discordDetails.getValue() : DiscordStatus.NONE;
    }

    public DiscordStatus getDiscordAutoDefault() {
        return discordAutoDefault != null ? discordAutoDefault.getValue() : DiscordStatus.NONE;
    }

    public void setDiscordAutoDefault(DiscordStatus discordAutoDefault) {
        this.discordAutoDefault.setValue(discordAutoDefault);
    }

    public String getCustomStatus(EnumUtils.DiscordStatusEntry statusEntry) {
        while (main.getConfigValues().getDiscordCustomStatuses().size() < 2) {
            main.getConfigValues().getDiscordCustomStatuses().add("");
        }

        return discordCustomStatuses.get(statusEntry.getId());
    }

    public String setCustomStatus(EnumUtils.DiscordStatusEntry statusEntry, String text) {
        while (main.getConfigValues().getDiscordCustomStatuses().size() < 2) {
            main.getConfigValues().getDiscordCustomStatuses().add("");
        }

        return discordCustomStatuses.set(statusEntry.getId(), text);
    }

    public EnchantListLayout getEnchantLayout() {
        return enchantLayout != null ? enchantLayout.getValue() : EnchantListLayout.NORMAL;
    }

    public void setEnchantLayout(EnchantListLayout enchantLayout) {
        this.enchantLayout.setValue(enchantLayout);
    }
}
