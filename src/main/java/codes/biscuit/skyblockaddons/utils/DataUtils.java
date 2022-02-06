package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.OnlineData;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreature;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreatureManager;
import codes.biscuit.skyblockaddons.exceptions.DataLoadingException;
import codes.biscuit.skyblockaddons.features.SkillXpManager;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemLists;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemPlacementBlocker;
import codes.biscuit.skyblockaddons.features.enchants.EnchantManager;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.utils.pojo.SkyblockAddonsAPIResponse;
import codes.biscuit.skyblockaddons.utils.skyblockdata.CompactorItem;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerData;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class reads data from the JSON files in the mod's resources or on the mod's Github repo and loads it into memory.
 */
public class DataUtils {

    private static final Gson gson = SkyblockAddons.getGson();

    private static final Logger logger = SkyblockAddons.getLogger();

    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    private static final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(3000)
            .setConnectionRequestTimeout(1500)
            .setSocketTimeout(3000).build();

    private static final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setUserAgent(Utils.USER_AGENT)
            .setDefaultRequestConfig(requestConfig).build();

    private static final String NO_DATA_RECEIVED_ERROR = "No data received for get request to \"%s\"";

    private static String path;

    //TODO: Migrate all data file loading to this class

    /**
     * This method reads the data files from the mod's resources and fetches copies of
     * the same files from a server, which replaces the local ones. If the mod is running in a development environment,
     * local files will be used, unless the environment variable "FETCH_DATA_ONLINE" is present.
     */
    public static void readLocalAndFetchOnline() {
        readLocalFileData();

        if (!SkyblockAddonsTransformer.isDeobfuscated() || System.getenv().containsKey("FETCH_DATA_ONLINE")) {
            fetchFromOnline();
        } else {
            SkyblockAddons.getInstance().getUpdater().checkForUpdate();
        }
    }

    /**
     * Reads local json files before pulling from online
     */
    public static void readLocalFileData() {
        // Online Data
        path = "/data.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            main.setOnlineData(gson.fromJson(inputStreamReader, OnlineData.class));
        } catch (Exception ex) {
            handleFileReadException(path, ex);
        }

        // Localized Strings
        loadLocalizedStrings(false);

        // Enchanted Item Blacklist
        path = "/enchantedItemLists.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            EnchantedItemPlacementBlocker.setItemLists(gson.fromJson(inputStreamReader, EnchantedItemLists.class));
        } catch (Exception ex) {
            handleFileReadException(path,ex);
        }

        // Containers
        path = "/containers.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            ItemUtils.setContainers(gson.fromJson(inputStreamReader, new TypeToken<HashMap<String, ContainerData>>() {}.getType()));
        } catch (Exception ex) {
            handleFileReadException(path,ex);
        }

        // Compactor Items
        path = "/compactorItems.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            ItemUtils.setCompactorItems(gson.fromJson(inputStreamReader, new TypeToken<HashMap<String, CompactorItem>>() {}.getType()));
        } catch (Exception ex) {
            handleFileReadException(path,ex);
        }

        // Sea Creatures
        path = "/seaCreatures.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)) {
            SeaCreatureManager.getInstance().setSeaCreatures(gson.fromJson(inputStreamReader, new TypeToken<Map<String, SeaCreature>>() {}.getType()));
        } catch (Exception ex) {
            handleFileReadException(path,ex);
        }

        // Enchantment data
        path = "/enchants.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            EnchantManager.setEnchants(gson.fromJson(inputStreamReader, new TypeToken<EnchantManager.Enchants>() {}.getType()));
        } catch (Exception ex) {
            handleFileReadException(path,ex);
        }

        // Cooldown Data
        path = "/cooldowns.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                     StandardCharsets.UTF_8)) {
            CooldownManager.setItemCooldowns(gson.fromJson(inputStreamReader, new TypeToken<HashMap<String, Integer>>() {
            }.getType()));
        } catch (Exception ex) {
            handleFileReadException(path,ex);
        }

        // Skill xp Data
        path = "/skillXp.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                     StandardCharsets.UTF_8)) {
            main.getSkillXpManager().initialize(gson.fromJson(inputStreamReader, SkillXpManager.JsonInput.class));
        } catch (Exception ex) {
            handleFileReadException(path,ex);
        }
    }

    /*
     This method fetches copies of all the data files from the server and checks if they are newer than the local copies.
     If an online copy is newer, the local copy is overwritten.
     */
    private static void fetchFromOnline() {

        /*
        Names of files the mod needs to successfully fetch to load
        The game will crash if these are not loaded successfully.
         */
        String[] essentialFileNames = {"data.json"};
        URI requestUrl = null;

        try {
            // Online Data
            requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons/development/src/main/resources/data.json");
            OnlineData receivedOnlineData = Objects.requireNonNull((OnlineData) fetchAndDeserialize(requestUrl, OnlineData.class),
                    String.format(NO_DATA_RECEIVED_ERROR, requestUrl));
            logger.info("Successfully fetched online data!");
            main.setOnlineData(receivedOnlineData);
            main.getUpdater().checkForUpdate();

            // Localized Strings
            Language language = main.getConfigValues().getLanguage();
            overwriteCommonJsonMembers(main.getConfigValues().getLanguageConfig(), getOnlineLocalizedStrings(language));

            // Enchanted Item Blacklist
            requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblockaddons/enchantedItemLists.json");
            EnchantedItemLists receivedBlacklist = Objects.requireNonNull(
                    (EnchantedItemLists) fetchAndDeserialize(requestUrl, EnchantedItemLists.class),
                    String.format(NO_DATA_RECEIVED_ERROR, requestUrl));
            logger.info("Successfully fetched enchanted item lists!");
            EnchantedItemPlacementBlocker.setItemLists(receivedBlacklist);

            // Containers
            requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/containers.json");
            @SuppressWarnings("unchecked")
            Map<String, ContainerData> receivedContainers = (Map<String, ContainerData>) Objects.requireNonNull(
                    fetchAndDeserialize(requestUrl,
                            new TypeToken<HashMap<String, ContainerData>>() {}.getType()),
                    String.format(NO_DATA_RECEIVED_ERROR, requestUrl));
            logger.info("Successfully fetched containers!");
            ItemUtils.setContainers(receivedContainers);

            // Compactor Items
            requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/compactorItems.json");
            @SuppressWarnings("unchecked")
            Map<String, CompactorItem> receivedCompactorItems = (Map<String, CompactorItem>) Objects.requireNonNull(
                    fetchAndDeserialize(requestUrl,
                            new TypeToken<HashMap<String, CompactorItem>>() {}.getType()),
                    String.format(NO_DATA_RECEIVED_ERROR, requestUrl));
            logger.info("Successfully fetched compactor items!");
            ItemUtils.setCompactorItems(receivedCompactorItems);

            // Sea Creatures
            requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/seaCreatures.json");
            @SuppressWarnings("unchecked")
            Map<String, SeaCreature> receivedSeaCreatures = (Map<String, SeaCreature>) Objects.requireNonNull(
                    fetchAndDeserialize(requestUrl, new TypeToken<HashMap<String, SeaCreature>>() {}.getType()),
                    String.format(NO_DATA_RECEIVED_ERROR, requestUrl));
            logger.info("Successfully fetched sea creature data!");
            SeaCreatureManager.getInstance().setSeaCreatures(receivedSeaCreatures);

            // Enchantments
            requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/enchants.json");
            EnchantManager.Enchants receivedEnchants = (EnchantManager.Enchants) Objects.requireNonNull(
                    fetchAndDeserialize(requestUrl, new TypeToken<EnchantManager.Enchants>() {
                    }.getType()),
                    String.format(NO_DATA_RECEIVED_ERROR, requestUrl));
            logger.info("Successfully fetched item enchantments!");
            EnchantManager.setEnchants(receivedEnchants);

            // Cooldowns
            requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/cooldowns.json");
            @SuppressWarnings("unchecked")
            Map<String, Integer> receivedCooldowns = (Map<String, Integer>) Objects.requireNonNull(
                    fetchAndDeserialize(requestUrl, new TypeToken<HashMap<String, Integer>>() {
                    }.getType()),
                    String.format(NO_DATA_RECEIVED_ERROR, requestUrl));
            logger.info("Successfully fetched cooldowns!");
            CooldownManager.setItemCooldowns(receivedCooldowns);

            // Skill xp
            requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/skillXp.json");
            SkillXpManager.JsonInput receivedSkillXp = (SkillXpManager.JsonInput) Objects.requireNonNull(
                    fetchAndDeserialize(requestUrl, SkillXpManager.JsonInput.class),
                    String.format(NO_DATA_RECEIVED_ERROR, requestUrl));
            logger.info("Successfully fetched skill xp!");
            main.getSkillXpManager().initialize(receivedSkillXp);

        } catch (IOException | JsonSyntaxException | NullPointerException e) {
            String finalRequestUrlString = requestUrl != null ? requestUrl.toString() : "null";
            String matchedFileName = null;

            for (String essentialFileName : essentialFileNames) {
                if (finalRequestUrlString.contains(essentialFileName)) {
                    matchedFileName = essentialFileName;
                }
            }

            if (matchedFileName != null) {
                throw new DataLoadingException(finalRequestUrlString);
            } else {
                logger.error("There was an error fetching data from the server. The bundled version of the file will be used instead.");
                logger.error("Failed to load URL \"{}\"", finalRequestUrlString);
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Loads the localized strings for the given {@link Language} with the choice of loading only local strings or local
     * and online strings. Languages are handled separately from other files because they may need to be loaded multiple
     * times in-game instead of just on startup.
     *
     * @param language the {@code Language} to load strings for
     * @param loadOnlineStrings Loads local and online strings if {@code true}, loads only local strings if {@code false}
     */
    public static void loadLocalizedStrings(Language language, boolean loadOnlineStrings) {
        // logger.info("Loading localized strings for " + language.name() + "...");

        path = "lang/" + language.getPath() + ".json";
        try (   InputStream inputStream = DataUtils.class.getClassLoader().getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            main.getConfigValues().setLanguageConfig(gson.fromJson(inputStreamReader, JsonObject.class));
        } catch (Exception ex) {
            handleFileReadException(path,ex);
        }

        if (loadOnlineStrings) {
            try {
                loadOnlineLocalizedStrings(language);
            } catch (IOException | JsonSyntaxException | NullPointerException e) {
                logger.error(e.getMessage());
                logger.error("There was an error fetching data from the server. The bundled version of the file will be used instead.");
            }
        }

        // logger.info("Finished loading localized strings.");
    }

    /**
     * Loads the online localized strings for the given {@link Language}
     *
     * @param language the {@code Language} to load strings for
     */
    public static void loadOnlineLocalizedStrings(Language language) throws IOException {
        JsonObject localLanguageConfig = main.getConfigValues().getLanguageConfig();
        JsonObject onlineLanguageConfig = getOnlineLocalizedStrings(language);

        if (localLanguageConfig != null) {
            overwriteCommonJsonMembers(localLanguageConfig, onlineLanguageConfig);
        } else {
            logger.warn("Local language configuration was null, it will be replaced with the online version.");
            main.getConfigValues().setLanguageConfig(onlineLanguageConfig);
        }
    }

    /**
     * Loads the localized strings for the current {@link Language} set in the mod settings with the choice of loading
     * only local strings or local and online strings.
     *
     * @param loadOnlineStrings Loads local and online strings if {@code true}, loads only local strings if {@code false}
     */
    public static void loadLocalizedStrings(boolean loadOnlineStrings) {
        loadLocalizedStrings(main.getConfigValues().getLanguage(), loadOnlineStrings);
    }

    /**
     * Creates a response handler to handle responses from the SkyblockAddons API
     */
    private static <T> ResponseHandler<T> createResponseHandler(Type T) {
        return response -> {
            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                HttpEntity entity = response.getEntity();
                try (    InputStream inputStream = entity.getContent();
                         InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                                 StandardCharsets.UTF_8)) {
                    SkyblockAddonsAPIResponse apiResponse = gson.fromJson(inputStreamReader, SkyblockAddonsAPIResponse.class);
                    return gson.fromJson(apiResponse.getResponse(), T);
                }
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
    }

    /**
     * Creates a response handler to handle responses from the SkyblockAddons Github repo
     */
    private static <T> ResponseHandler<T> createLegacyResponseHandler(Type T) {
        return response -> {
            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                HttpEntity entity = response.getEntity();
                try (    InputStream inputStream = entity.getContent();
                         InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                                 StandardCharsets.UTF_8)) {
                    return gson.fromJson(inputStreamReader, T);
                }
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
    }

    /**
     * Fetches a JSON file from the given URL and deserializes it to an object of the given type.
     *
     * @param url the URL to fetch the JSON file from
     * @param T the {@code Type} to deserialize the JSON to
     * @return an object of {@code clazz} deserialized from the JSON fetched from {@code url}
     */
    private static Object fetchAndDeserialize(URI url, Type T) throws IOException {
        return httpClient.execute(new HttpGet(url), createLegacyResponseHandler(T));
    }

    /**
     * Gets the online localized strings for the given {@link Language}.
     *
     * @param language the {@link Language} of the localized strings to get
     * @return the localized strings for the given language; a {@code NullPointerException} is thrown if no data is
     * received.
     * @throws IOException thrown if an error occurs while executing the request
     * @throws NullPointerException if the received localized strings object is {@code null}
     */
    private static JsonObject getOnlineLocalizedStrings(Language language) throws IOException {
        URI requestUrl = URI.create(String.format(main.getOnlineData().getLanguageJSONFormat(), language.getPath()));
        JsonObject receivedLocalizedStrings = Objects.requireNonNull((JsonObject) fetchAndDeserialize(requestUrl, JsonObject.class),
                String.format(NO_DATA_RECEIVED_ERROR, requestUrl));
        logger.info("Successfully fetched localized strings!");
        return receivedLocalizedStrings;
    }

    /**
     * This method handles errors that can occur when reading the local configuration files.
     * It displays an error screen and prints the stacktrace of the given {@code Throwable} in the console.
     *
     * @param filePath the path to the file that caused the exception
     * @param exception the exception that occurred
     */
    private static void handleFileReadException(String filePath, Throwable exception) {
        throw new DataLoadingException(filePath.substring(filePath.lastIndexOf('/')), exception);
    }

    /**
     * This is used to merge in the online language entries into the existing ones.
     * Using this method rather than an overwrite allows new entries in development to still exist.
     *
     * @param baseObject   The object to be merged in to (local entries).
     * @param otherObject The object to be merged (online entries).
     */
    private static void overwriteCommonJsonMembers(JsonObject baseObject, JsonObject otherObject) {
        for (Map.Entry<String, JsonElement> entry : otherObject.entrySet()) {
            String memberName = entry.getKey();
            JsonElement otherElement = entry.getValue();

            if (otherElement.isJsonObject()) {
                // If the base object already has this object, then recurse
                if (baseObject.has(memberName) && baseObject.get(memberName).isJsonObject()) {
                    JsonObject baseElementObject = baseObject.getAsJsonObject(memberName);
                    overwriteCommonJsonMembers(baseElementObject, otherElement.getAsJsonObject());

                    // Otherwise we have to add a new object first, then recurse
                } else {
                    JsonObject baseElementObject = new JsonObject();
                    baseObject.add(memberName, baseElementObject);
                    overwriteCommonJsonMembers(baseElementObject, otherElement.getAsJsonObject());
                }

                // If it's a string, then just add or overwrite the base version
            } else if (otherElement.isJsonPrimitive() && otherElement.getAsJsonPrimitive().isString()) {
                baseObject.add(memberName, otherElement);
            }
        }
    }
}
