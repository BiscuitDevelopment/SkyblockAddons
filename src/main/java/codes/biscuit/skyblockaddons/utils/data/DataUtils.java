package codes.biscuit.skyblockaddons.utils.data;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.core.OnlineData;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreature;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreatureManager;
import codes.biscuit.skyblockaddons.exceptions.DataLoadingException;
import codes.biscuit.skyblockaddons.features.SkillXpManager;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemLists;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemPlacementBlocker;
import codes.biscuit.skyblockaddons.features.enchants.EnchantManager;
import codes.biscuit.skyblockaddons.misc.scheduler.ScheduledTask;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import codes.biscuit.skyblockaddons.utils.data.requests.*;
import codes.biscuit.skyblockaddons.utils.skyblockdata.CompactorItem;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerData;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import net.minecraft.crash.CrashReport;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.*;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.http.client.config.RequestConfig;
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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This class reads data from the JSON files in the mod's resources or on the mod's Github repo and loads it into memory.
 */
public class DataUtils {

    private static final Gson gson = SkyblockAddons.getGson();

    private static final Logger logger = SkyblockAddons.getLogger();

    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    private static final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(120 * 1000)
            .setConnectionRequestTimeout(120 * 1000)
            .setSocketTimeout(30 * 1000).build();

    private static final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    private static final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setUserAgent(Utils.USER_AGENT)
            .setDefaultRequestConfig(requestConfig)
            .setConnectionManager(connectionManager)
            .setConnectionReuseStrategy(new NoConnectionReuseStrategy())
            .setRetryHandler(new RequestRetryHandler()).build();

    private static final ThreadFactory threadFactory =
            new ThreadFactoryBuilder().setNameFormat("SBA DataUtils Thread %d")
                    .setUncaughtExceptionHandler(new UncaughtFetchExceptionHandler()).build();

    private static final ExecutorService executorService = Executors.newCachedThreadPool(threadFactory);

    private static final FutureRequestExecutionService futureRequestExecutionService =
            new FutureRequestExecutionService(httpClient, executorService);

    private static final ArrayList<RemoteFileRequest<?>> remoteRequests = new ArrayList<>();

    @Getter
    private static final ArrayList<HttpRequestFutureTask<?>> httpRequestFutureTasks = new ArrayList<>();

    @Getter
    private static final HashMap<RemoteFileRequest<?>, Throwable> failedRequests = new HashMap<>();

    // Whether the failed requests error was shown in chat, used to make it show only once per session
    private static boolean failureMessageShown = false;

    /**
     * The mod uses the online data files if this is {@code true} and local data if this is {@code false}.
     * This is set to {@code true} if the mod is running in production or if it's running in a dev environment that has
     * the environment variable {@code FETCH_DATA_ONLINE}.
     */
    public static final boolean USE_ONLINE_DATA = !SkyblockAddonsTransformer.isDeobfuscated() ||
            System.getenv().containsKey("FETCH_DATA_ONLINE");

    private static String path;

    private static LocalizedStringsRequest localizedStringsRequest = null;

    private static ScheduledTask languageLoadingTask = null;

    static {
        connectionManager.setMaxTotal(5);
        connectionManager.setDefaultMaxPerRoute(5);
        registerRemoteRequests();
    }

    //TODO: Migrate all data file loading to this class

    /**
     * This method reads the data files from the mod's resources and fetches copies of
     * the same files from a server, which replaces the local ones. If the mod is running in a development environment,
     * local files will be used, unless the environment variable "FETCH_DATA_ONLINE" is present.
     */
    public static void readLocalAndFetchOnline() {
        readLocalFileData();

        if (USE_ONLINE_DATA) {
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
        for (RemoteFileRequest<?> request : remoteRequests) {
            request.execute(futureRequestExecutionService);
        }
    }

    /**
     * Loads the received online data files into the mod.
     *
     * @see SkyblockAddons#preInit(FMLPreInitializationEvent)
     */
    public static void loadOnlineData() {
        Iterator<RemoteFileRequest<?>> requestIterator = remoteRequests.iterator();

        while (requestIterator.hasNext()) {
            RemoteFileRequest<?> request = requestIterator.next();

            if (!request.isDone()) {
                handleOnlineFileLoadException(request,
                        new RuntimeException(String.format("Request for \"%s\" didn't finish in time for mod init.",
                                getFileNameFromUrlString(request.getUrl()))));
            }

            try {
                loadOnlineFile(request);
                requestIterator.remove();
            } catch (InterruptedException | ExecutionException | NullPointerException | IllegalArgumentException e) {
                handleOnlineFileLoadException(Objects.requireNonNull(request), e);
            }
        }
    }

    /**
     * Loads a received online data file into the mod.
     *
     * @param request the {@code RemoteFileRequest} for the file
     */
    public static void loadOnlineFile(RemoteFileRequest<?> request) throws ExecutionException, InterruptedException {
        request.load();
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
     * Loads the localized strings for the given {@link Language} with the choice of loading only local strings or local
     * and online strings. Languages are handled separately from other files because they may need to be loaded multiple
     * times in-game instead of just on startup. Online strings will never be loaded for English, regardless of the value
     * of {@code loadOnlineStrings}.
     *
     * @param language the {@code Language} to load strings for
     * @param loadOnlineStrings Loads local and online strings if {@code true}, loads only local strings if {@code false},
     *                          does not override {@link DataUtils#USE_ONLINE_DATA}
     */
    public static void loadLocalizedStrings(Language language, boolean loadOnlineStrings) {
        // logger.info("Loading localized strings for " + language.name() + "...");

        path = "lang/" + language.getPath() + ".json";
        try (   InputStream inputStream = DataUtils.class.getClassLoader().getResourceAsStream(path);
                InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(inputStream),
                        StandardCharsets.UTF_8)){
            main.getConfigValues().setLanguageConfig(gson.fromJson(inputStreamReader, JsonObject.class));
            main.getConfigValues().setLanguage(language);
        } catch (Exception ex) {
            handleLocalFileReadException(path,ex);
        }

        if (USE_ONLINE_DATA && loadOnlineStrings && language != Language.ENGLISH) {
            if (localizedStringsRequest != null) {
                HttpRequestFutureTask<JsonObject> futureTask = localizedStringsRequest.getFutureTask();
                if (!futureTask.isDone()) {
                    futureTask.cancel(false);
                }
            } else if (languageLoadingTask != null) {
                languageLoadingTask.cancel();
            }

            localizedStringsRequest = new LocalizedStringsRequest(language);
            localizedStringsRequest.execute(futureRequestExecutionService);
            languageLoadingTask = main.getNewScheduler().scheduleLimitedRepeatingTask(new SkyblockRunnable() {
                @Override
                public void run() {
                    if (localizedStringsRequest != null) {
                        if (localizedStringsRequest.isDone()) {
                            try {
                                loadOnlineFile(localizedStringsRequest);
                            } catch (InterruptedException | ExecutionException | NullPointerException | IllegalArgumentException e) {
                                handleOnlineFileLoadException(Objects.requireNonNull(localizedStringsRequest), e);
                            }
                            cancel();
                        }
                    } else {
                        cancel();
                    }
                }
            }, 10, 20, 8);
        }

        // logger.info("Finished loading localized strings.");
    }

    // TODO: Shut it down and restart it as needed?
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
     * Displays a message when the player first joins Skyblock asking them to report failed requests to our Discord server.
     */
    public static void onSkyblockJoined() {
        if (!failureMessageShown && !failedRequests.isEmpty()) {
            StringBuilder errorMessageBuilder = new StringBuilder("Failed Requests:\n");

            for (Map.Entry<RemoteFileRequest<?>, Throwable> failedRequest : failedRequests.entrySet()) {
                errorMessageBuilder.append(failedRequest.getKey().getUrl()).append("\n");
                errorMessageBuilder.append(failedRequest.getValue().toString()).append("\n");
            }

            ChatComponentText failureMessageComponent = new ChatComponentText(
                    Translations.getMessage("messages.fileFetchFailed", EnumChatFormatting.AQUA
                                    + SkyblockAddons.MOD_NAME + EnumChatFormatting.RED,
                            failedRequests.size()));
            IChatComponent buttonRowComponent = new ChatComponentText("[" +
                    Translations.getMessage("messages.copy") + "]").setChatStyle(
                            new ChatStyle().setColor(EnumChatFormatting.WHITE).setBold(true).setChatClickEvent(
                                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/sba internal copy %s",
                                            errorMessageBuilder))));
            buttonRowComponent.appendText("  ");
            buttonRowComponent.appendSibling(new ChatComponentText("[Discord]").setChatStyle(new ChatStyle()
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/PqTAEek"))));
            failureMessageComponent.appendText("\n").appendSibling(buttonRowComponent);

            main.getUtils().sendMessage(failureMessageComponent, false);
            failureMessageShown = true;
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

    private static void registerRemoteRequests() {
        remoteRequests.add(new OnlineDataRequest());
        if (SkyblockAddons.getInstance().getConfigValues().getLanguage() != Language.ENGLISH) {
            remoteRequests.add(new LocalizedStringsRequest(SkyblockAddons.getInstance().getConfigValues().getLanguage()));
        }
        remoteRequests.add(new EnchantedItemListsRequest());
        remoteRequests.add(new ContainersRequest());
        remoteRequests.add(new CompactorItemsRequest());
        remoteRequests.add(new SeaCreaturesRequest());
        remoteRequests.add(new EnchantmentsRequest());
        remoteRequests.add(new CooldownsRequest());
        remoteRequests.add(new SkillXpRequest());
    }

    /**
     * This method handles errors that can occur when reading the local configuration files.
     * If the game is still initializing, it displays an error screen and prints the stacktrace of the given
     * {@code Throwable} in the console.
     * If the game is initialized, it crashes the game with a crash report containing the file path and the stacktrace
     * of the given {@code Throwable}.
     *
     * @param filePath the path to the file that caused the exception
     * @param exception the exception that occurred
     */
    private static void handleLocalFileReadException(String filePath, Throwable exception) {
        if (FMLClientHandler.instance().isLoading()) {
            throw new DataLoadingException(filePath, exception);
        } else {
            CrashReport crashReport = CrashReport.makeCrashReport(exception, String.format("Loading data file at %s",
                    filePath));
            throw new ReportedException(crashReport);
        }
    }

    /**
     * This method handles errors that can occur when reading the online configuration files.
     * If the game is still initializing, it displays an error screen and prints the stacktrace of the given
     * {@code Throwable} in the console.
     * If the game is initialized, it crashes the game with a crash report containing the file name and the stacktrace
     * of the given {@code Throwable}.
     *
     * @param request the {@code RemoteFileRequest} for the file that failed to load
     * @param exception the exception that occurred
     */
    private static void handleOnlineFileLoadException(RemoteFileRequest<?> request, Throwable exception) {
        String url = request.getUrl();
        String fileName = getFileNameFromUrlString(url);
        failedRequests.put(request, exception);

        // The loader encountered a file name it didn't expect.
        if (exception instanceof IllegalArgumentException) {
            logger.error(exception.getMessage());
            return;
        }

        if (request.isEssential()) {
            if (FMLClientHandler.instance().isLoading()) {
                throw new DataLoadingException(url, exception);
            } else {
                // Don't include URL because Fire strips URLs.
                CrashReport crashReport = CrashReport.makeCrashReport(exception, String.format("Loading online data file" +
                                " at %s",
                        fileName));
                throw new ReportedException(crashReport);
            }
        } else {
            logger.error("Failed to load \"{}\" from the server. The local copy will be used instead.", fileName);
            if (!(exception == null)) {
                logger.error(exception.getMessage());
            }
        }
    }
}
