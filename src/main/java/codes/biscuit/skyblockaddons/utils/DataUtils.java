package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.OnlineData;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreatureManager;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemLists;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemPlacementBlocker;
import codes.biscuit.skyblockaddons.features.enchants.EnchantManager;
import codes.biscuit.skyblockaddons.utils.pojo.SkyblockAddonsAPIResponse;
import codes.biscuit.skyblockaddons.utils.skyblockdata.CompactorItem;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerData;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
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

    private static final String NO_DATA_RECEIVED_ERROR = "No data received for get request to \"%s\"";

    private static String path;

    //TODO: Migrate all data file loading to this class

    /**
     * This method reads the data files from the mod's resources and fetches copies of
     * the same files from a server, which replaces the local ones. If the mod is running in a development environment,
     * local files will be used.
     */
    public static void readLocalAndFetchOnline() {
        readLocalFileData();

        // TODO Let's leave this enabled so we can see errors with the endpoints.
        //  If you need to change any files locally, just disable this manually.
//        if (!SkyblockAddonsTransformer.isDeobfuscated()) {
        fetchFromOnline();
/*        } else {
        SkyblockAddons.getInstance().getUpdater().checkForUpdate();
        }*/
    }

    /**
     * Reads local json files before pulling from online
     */
    public static void readLocalFileData() {
        // Online Data
        path = "/test-data.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            main.setOnlineData(gson.fromJson(inputStreamReader, OnlineData.class));
        } catch (Exception ex) {
            handleFileReadException(ex, "Failed to read the local data file");
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
            handleFileReadException(ex, "Failed to read the local enchanted item lists");
        }

        // Containers
        path = "/containers.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            ItemUtils.setContainers(gson.fromJson(inputStreamReader, new TypeToken<HashMap<String, ContainerData>>() {}.getType()));
        } catch (Exception ex) {
            handleFileReadException(ex, "Failed to read the containers map");
        }

        // Compactor Items
        path = "/compactorItems.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            ItemUtils.setCompactorItems(gson.fromJson(inputStreamReader, new TypeToken<HashMap<String, CompactorItem>>() {}.getType()));
        } catch (Exception ex) {
            handleFileReadException(ex, "Failed to read the compactor items map");
        }

        // Enchantment data
        path = "/enchants.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            EnchantManager.setEnchants(gson.fromJson(inputStreamReader, new TypeToken<EnchantManager.Enchants>() {}.getType()));
        } catch (Exception ex) {
            handleFileReadException(ex, "Failed to read the enchantments file");
        }

        // Cooldown Data
        path = "/cooldowns.json";
        try (   InputStream inputStream = DataUtils.class.getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            CooldownManager.setItemCooldowns(gson.fromJson(inputStreamReader, new TypeToken<HashMap<String, Integer>>() {}.getType()));
        } catch (Exception ex) {
            handleFileReadException(ex, "Failed to read the cool down data file");
        }
    }

    /*
     This method fetches copies of all the data files from the server and checks if they are newer than the local copies.
     If an online copy is newer, the local copy is overwritten.
     */
    private static void fetchFromOnline() {
        String requestUri;

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setUserAgent(Utils.USER_AGENT).build()) {

            // Online Data
            logger.info("Trying to fetch online data from the server...");
            requestUri = "https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons/development/src/main/resources/test-data.json";
            OnlineData receivedOnlineData = httpClient.execute(new HttpGet(requestUri),
                    createLegacyResponseHandler(OnlineData.class));
            if (receivedOnlineData != null) {
                logger.info("Successfully fetched online data!");
                main.setOnlineData(receivedOnlineData);
                main.getUpdater().checkForUpdate();
            } else {
                throw new NullPointerException(String.format(NO_DATA_RECEIVED_ERROR, requestUri));
            }

            // Localized Strings
            Language language = main.getConfigValues().getLanguage();
            overwriteCommonJsonMembers(main.getConfigValues().getLanguageConfig(), getOnlineLocalizedStrings(httpClient, language));

            // Enchanted Item Blacklist
            logger.info("Trying to fetch enchanted item lists from the server...");
            requestUri = "https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblockaddons/enchantedItemLists.json";
            EnchantedItemLists receivedBlacklist = httpClient.execute(new HttpGet(requestUri),
                    createLegacyResponseHandler(EnchantedItemLists.class));
            if (receivedBlacklist != null) {
                logger.info("Successfully fetched enchanted item lists!");
                EnchantedItemPlacementBlocker.setItemLists(receivedBlacklist);
            } else {
                throw new NullPointerException(String.format(NO_DATA_RECEIVED_ERROR, requestUri));
            }

            // Containers
            logger.info("Trying to fetch containers from the server...");
            requestUri = "https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/containers.json";
            Map<String, ContainerData> receivedContainers = httpClient.execute(new HttpGet(requestUri),
                    createLegacyResponseHandler(new TypeToken<HashMap<String, ContainerData>>() {
                    }.getType()));
            if (receivedContainers != null) {
                logger.info("Successfully fetched containers!");
                ItemUtils.setContainers(receivedContainers);
            } else {
                throw new NullPointerException(String.format(NO_DATA_RECEIVED_ERROR, requestUri));
            }

            // Compactor Items
            logger.info("Trying to fetch compactor items from the server...");
            requestUri = "https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/compactorItems.json";
            Map<String, CompactorItem> receivedCompactorItems = httpClient.execute(new HttpGet(requestUri),
                    createLegacyResponseHandler(new TypeToken<HashMap<String, CompactorItem>>() {
                    }.getType()));
            if (receivedCompactorItems != null) {
                logger.info("Successfully fetched compactor items!");
                ItemUtils.setCompactorItems(receivedCompactorItems);
            } else {
                throw new NullPointerException(String.format(NO_DATA_RECEIVED_ERROR, requestUri));
            }

            // Sea Creatures
            SeaCreatureManager.getInstance().pullSeaCreatures();

            // Enchantments
            logger.info("Trying to fetch item enchantments from the server...");
            requestUri = "https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/enchants.json";
            EnchantManager.Enchants receivedEnchants = httpClient.execute(new HttpGet(requestUri),
                    createLegacyResponseHandler(new TypeToken<EnchantManager.Enchants>() {
                    }.getType()));
            if (receivedEnchants != null) {
                logger.info("Successfully fetched item enchantments!");
                EnchantManager.setEnchants(receivedEnchants);
            } else {
                throw new NullPointerException(String.format(NO_DATA_RECEIVED_ERROR, requestUri));
            }

            // Cooldowns
            logger.info("Trying to fetch cooldowns from the server...");
            requestUri = "https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/skyblock/cooldowns.json";
            Map<String, Integer> receivedCooldowns = httpClient.execute(new HttpGet(requestUri),
                    createLegacyResponseHandler(new TypeToken<HashMap<String, Integer>>() {
                    }.getType()));
            if (receivedCooldowns != null) {
                logger.info("Successfully fetched cooldowns!");
                CooldownManager.setItemCooldowns(receivedCooldowns);
            } else {
                throw new NullPointerException(String.format(NO_DATA_RECEIVED_ERROR, requestUri));
            }

        } catch (IOException | JsonSyntaxException | NullPointerException e) {
            logger.error("There was an error fetching data from the server. The bundled version of the file will be used instead.");
            logger.catching(e);
        }
    }

    /**
     * Loads the localized strings for the given {@link Language} with the choice of loading only local strings or local
     * and online strings.
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
            handleFileReadException(ex, "Failed to read the local localized strings");
        }

        if (loadOnlineStrings) {
            loadOnlineLocalizedStrings(language);
        }

        // logger.info("Finished loading localized strings.");
    }

    /**
     * Loads the online localized strings for the given {@link Language}
     *
     * @param language the {@code Language} to load strings for
     */
    public static void loadOnlineLocalizedStrings(Language language) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setUserAgent(Utils.USER_AGENT).build()) {
            JsonObject localLanguageConfig = main.getConfigValues().getLanguageConfig();
            JsonObject onlineLanguageConfig = getOnlineLocalizedStrings(httpClient, language);

            if (localLanguageConfig != null) {
                overwriteCommonJsonMembers(localLanguageConfig, onlineLanguageConfig);
            } else {
                logger.warn("Local language configuration was null, it will be replaced with the online version.");
                main.getConfigValues().setLanguageConfig(onlineLanguageConfig);
            }
        } catch (IOException | JsonSyntaxException e) {
            logger.error("There was an error fetching data from the server. The bundled version of the file will be used instead.");
            logger.catching(e);
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
     * Gets the online localized strings for the given {@link Language} using the given {@code ClosableHttpClient}.
     * This exists so I don't need to create an unneeded HTTP client when one already exists in
     * {@link DataUtils#fetchFromOnline()}.
     *
     * @param httpClient the {@code ClosableHttpClient} used to execute the get request
     * @param language the {@link Language} of the localized strings to get
     * @return the localized strings for the given language; a {@code NullPointerException} is thrown if no data is
     * received.
     * @throws IOException thrown if an error occurs while executing the request
     */
    private static JsonObject getOnlineLocalizedStrings(CloseableHttpClient httpClient, Language language) throws IOException {
        logger.info("Trying to fetch localized strings from the server for " + language.name() + "...");
        String requestUri = String.format(main.getOnlineData().getLanguageJSONFormat(), language.getPath());
        JsonObject receivedLocalizedStrings = httpClient.execute(new HttpGet(requestUri),
                createLegacyResponseHandler(JsonObject.class));
        if (receivedLocalizedStrings != null) {
            logger.info("Successfully fetched localized strings!");
            return receivedLocalizedStrings;
        } else {
            throw new NullPointerException(String.format(NO_DATA_RECEIVED_ERROR, requestUri));
        }
    }

    /**
     * This method handles errors that can occur when reading the local configuration files.
     * It creates a crash report with the stacktrace of the given {@code Throwable}, the given description,
     * the path of the file being read, and any suppressed exceptions if present. Then it throws a {@code ReportedException}.
     *
     *
     * @param exception the exception that occurred
     * @param description the description for the crash report
     */
    private static void handleFileReadException(Throwable exception, String description) {
        CrashReport crashReport = CrashReport.makeCrashReport(exception, description);
        CrashReportCategory category = crashReport.makeCategory("File being read");

        category.addCrashSection("File Path", path);

        Throwable[] suppressedExceptions = exception.getSuppressed();

        if (suppressedExceptions.length > 0) {
            for (int i = 0; i < suppressedExceptions.length; i++) {
                category.addCrashSectionThrowable("Suppressed Exception " + i, suppressedExceptions[i]);
            }
        }

        // TODO: Can the stacktraces be made shorter?
        throw new ReportedException(crashReport);
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
