package codes.biscuit.skyblockaddons.utils.data;

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
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import codes.biscuit.skyblockaddons.utils.pojo.SkyblockAddonsAPIResponse;
import codes.biscuit.skyblockaddons.utils.skyblockdata.CompactorItem;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerData;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

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

    private static final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    private static final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setUserAgent(Utils.USER_AGENT)
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(connectionManager)
            .setConnectionReuseStrategy(new NoConnectionReuseStrategy()).build();

    private static final ThreadFactory threadFactory =
            new ThreadFactoryBuilder().setNameFormat("SBA DataUtils Thread %d")
                    .setUncaughtExceptionHandler(new UncaughtFetchExceptionHandler()).build();

    private static final ExecutorService executorService = Executors.newCachedThreadPool(threadFactory);

    private static final FutureRequestExecutionService futureRequestExecutionService =
            new FutureRequestExecutionService(httpClient, executorService);

    @Getter
    private static final ArrayList<HttpRequestFutureTask<?>> httpRequestFutureTasks = new ArrayList<>();

    /*
    URLs of files the mod needs to successfully fetch to load
    The game will not start if these are not loaded successfully.
     */
    static final String[] ESSENTIAL_FILE_URLS = {
            "https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons/development/src/main/resources/data.json"
    };

    private static final String NO_DATA_RECEIVED_ERROR = "No data received for get request to \"%s\"";

    private static String path;

    static {
        connectionManager.setMaxTotal(5);
        connectionManager.setDefaultMaxPerRoute(5);
    }

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
            handleLocalFileReadException(path, ex);
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
            handleLocalFileReadException(path,ex);
        }

        // Containers
        path = "/containers.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            ItemUtils.setContainers(gson.fromJson(inputStreamReader, new TypeToken<HashMap<String, ContainerData>>() {}.getType()));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Compactor Items
        path = "/compactorItems.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            ItemUtils.setCompactorItems(gson.fromJson(inputStreamReader, new TypeToken<HashMap<String, CompactorItem>>() {}.getType()));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Sea Creatures
        path = "/seaCreatures.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)) {
            SeaCreatureManager.getInstance().setSeaCreatures(gson.fromJson(inputStreamReader, new TypeToken<Map<String, SeaCreature>>() {}.getType()));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Enchantment data
        path = "/enchants.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            EnchantManager.setEnchants(gson.fromJson(inputStreamReader, new TypeToken<EnchantManager.Enchants>() {}.getType()));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Cooldown Data
        path = "/cooldowns.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                     StandardCharsets.UTF_8)) {
            CooldownManager.setItemCooldowns(gson.fromJson(inputStreamReader, new TypeToken<HashMap<String, Integer>>() {
            }.getType()));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        // Skill xp Data
        path = "/skillXp.json";
        try (InputStream inputStream = DataUtils.class.getResourceAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                     StandardCharsets.UTF_8)) {
            main.getSkillXpManager().initialize(gson.fromJson(inputStreamReader, SkillXpManager.JsonInput.class));
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }
    }

    /*
     This method fetches copies of all the data files from the server and checks if they are newer than the local copies.
     If an online copy is newer, the local copy is overwritten.
     */
    private static void fetchFromOnline() {
        URI requestUrl;

        // Online Data
        requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons/development/src/main/resources/datat.json");
        httpRequestFutureTasks.add(futureRequestExecutionService.execute(new HttpGet(requestUrl), null,
                new JSONResponseHandler<>(OnlineData.class), new DataFetchCallback<OnlineData>(requestUrl)));

        // Enchanted Item Blacklist
        requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblockaddons/enchantedItemLists.json");
        httpRequestFutureTasks.add(futureRequestExecutionService.execute(new HttpGet(requestUrl), null,
                new JSONResponseHandler<>(EnchantedItemLists.class), new DataFetchCallback<>(requestUrl)));

        // Containers
        requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/containers.json");
        httpRequestFutureTasks.add(futureRequestExecutionService.execute(new HttpGet(requestUrl), null,
                new JSONResponseHandler<>(new TypeToken<HashMap<String, ContainerData>>() {}.getType()),
                new DataFetchCallback<>(requestUrl)));

        // Compactor Items
        requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/compactorItems.json");
        httpRequestFutureTasks.add(futureRequestExecutionService.execute(new HttpGet(requestUrl), null,
                new JSONResponseHandler<>(new TypeToken<HashMap<String, CompactorItem>>() {}.getType()),
                new DataFetchCallback<>(requestUrl)));

        // Sea Creatures
        requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/seaCreatures.json");
        httpRequestFutureTasks.add(futureRequestExecutionService.execute(new HttpGet(requestUrl), null,
                new JSONResponseHandler<>(new TypeToken<HashMap<String, SeaCreature>>() {}.getType()),
                new DataFetchCallback<>(requestUrl)));

        // Enchantments
        requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/enchants.json");
        httpRequestFutureTasks.add(futureRequestExecutionService.execute(new HttpGet(requestUrl), null,
                new JSONResponseHandler<>(new TypeToken<EnchantManager.Enchants>() {}.getType()),
                new DataFetchCallback<>(requestUrl)));

        // Cooldowns
        requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/cooldowns.json");
        httpRequestFutureTasks.add(futureRequestExecutionService.execute(new HttpGet(requestUrl), null,
                new JSONResponseHandler<>(new TypeToken<Map<String, Integer>>() {}.getType()),
                new DataFetchCallback<>(requestUrl)));

        // Skill xp
        requestUrl = URI.create("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/skillXp.json");
        httpRequestFutureTasks.add(futureRequestExecutionService.execute(new HttpGet(requestUrl), null,
                new JSONResponseHandler<>(SkillXpManager.JsonInput.class),
                new DataFetchCallback<>(requestUrl)));

        // Localized Strings
        Language language = main.getConfigValues().getLanguage();

        // TODO: Figure out how to do languages in Async
        try {
            overwriteCommonJsonMembers(main.getConfigValues().getLanguageConfig(), getOnlineLocalizedStrings(language));
        } catch (IOException | JsonSyntaxException | NullPointerException e) {
            logger.info("Failed to load language file for language \"{}\"", language);
        }
    }

    /**
     * Loads the received online data into the mod.
     *
     * @see SkyblockAddons#init(FMLInitializationEvent)
     */
    public static void loadOnlineData() {
        ArrayList<String> loadedFileUrls = new ArrayList<>();
        String urlString;

        for (HttpRequestFutureTask<?> futureTask : httpRequestFutureTasks) {
            urlString = futureTask.toString();
            String fileName = getFileNameFromUrlString(urlString);
            String noDataError = String.format(NO_DATA_RECEIVED_ERROR, urlString);

            // TODO: See if this can be made less copy-pasty with a callback or something
            try {
                switch (fileName) {
                    case "data.json":
                        main.setOnlineData(Objects.requireNonNull((OnlineData) futureTask.get(),
                                noDataError));
                        main.getUpdater().checkForUpdate();
                        break;
                    case "enchantedItemLists.json":
                        EnchantedItemPlacementBlocker.setItemLists(Objects.requireNonNull((EnchantedItemLists) futureTask.get(),
                                noDataError));
                        break;
                    case "containers.json":
                        //noinspection unchecked
                        ItemUtils.setContainers(Objects.requireNonNull((HashMap<String, ContainerData>) futureTask.get(),
                                noDataError));
                        break;
                    case "compactorItems.json":
                        //noinspection unchecked
                        ItemUtils.setCompactorItems(Objects.requireNonNull((HashMap<String, CompactorItem>) futureTask.get(),
                                noDataError));
                        break;
                    case "seaCreatures.json":
                        //noinspection unchecked
                        SeaCreatureManager.getInstance().setSeaCreatures(Objects.requireNonNull((HashMap<String, SeaCreature>) futureTask.get(),
                                noDataError));
                        break;
                    case "enchants.json":
                        EnchantManager.setEnchants(Objects.requireNonNull((EnchantManager.Enchants) futureTask.get(),
                                noDataError));
                        break;
                    case "cooldowns.json":
                        //noinspection unchecked
                        CooldownManager.setItemCooldowns(Objects.requireNonNull((Map<String, Integer>) futureTask.get(),
                                noDataError));
                        break;
                    case "skillXp.json":
                        main.getSkillXpManager().initialize((SkillXpManager.JsonInput) futureTask.get());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown data file " + urlString + "\nDid you forget something?");
                }

                logger.info("Successfully loaded {}.", fileName);
                loadedFileUrls.add(urlString);
            } catch (InterruptedException | ExecutionException | NullPointerException | IllegalArgumentException e) {
                handleOnlineFileLoadException(urlString, e);
            }
        }

        for (String essentialFileUrl : ESSENTIAL_FILE_URLS) {
            if (!loadedFileUrls.contains(essentialFileUrl)) {
                handleOnlineFileLoadException(essentialFileUrl, null);
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
            handleLocalFileReadException(path,ex);
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
     * Shuts down {@link DataUtils#futureRequestExecutionService} and the underlying {@code ExecutorService} and
     * {@code ClosableHttpClient}.
     */
    public static void shutdownExecutorService() {
        try {
            futureRequestExecutionService.close();
            logger.debug("Executor service shut down.");
        } catch (IOException e) {
            logger.error("Failed to shut down executor service.", e);
        }
    }

    /**
     * Returns the file name from the end of a given URL string.
     * This does not check if the URL has a valid file name at the end.
     *
     * @param url the URL string to get the file name from
     * @return the file name from the end of the URL string
     */
    static String getFileNameFromUrlString(String url) {
        int fileNameIndex = url.lastIndexOf('/') + 1;
        int queryParamIndex = url.indexOf('?', fileNameIndex);
        return url.substring(fileNameIndex, queryParamIndex > fileNameIndex ? queryParamIndex : url.length());
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
    private static void handleLocalFileReadException(String filePath, Throwable exception) {
        throw new DataLoadingException(filePath, exception);
    }

    private static void handleOnlineFileLoadException(String url, Throwable exception) {
        String fileName = getFileNameFromUrlString(url);

        // The loader encountered a file name it didn't expect.
        if (exception instanceof IllegalArgumentException) {
            logger.error(exception.getMessage());
        }

        if (Arrays.stream(ESSENTIAL_FILE_URLS).anyMatch(s -> s.contains(fileName))) {
            throw new DataLoadingException(url, exception);
        } else {
            logger.error("Failed to load \"{}\" from the server. The local copy will be used instead.", fileName);
            if (!(exception == null)) {
                logger.error(exception.getMessage());
            }
        }
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
