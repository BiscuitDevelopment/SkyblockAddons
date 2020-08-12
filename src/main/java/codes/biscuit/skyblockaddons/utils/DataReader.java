package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.OnlineData;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemBlacklist;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemPlacementBlocker;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class reads data from the JSON files in the mod's resources or on the mod's Github repo and loads it into memory.
 */
public class DataReader {
    private static final Gson GSON = new Gson();
    private static final SkyblockAddons MAIN = SkyblockAddons.getInstance();
    private static final Logger LOGGER = MAIN.getLogger();

    //TODO: Migrate all data file loading to this class

    /**
     * Reads all the data files and loads the data that was read into the mod
     */
    public static void readAndLoadAll() throws IOException, URISyntaxException {
        readAndLoadOnlineData();
        readAndLoadEnchantedItemBlacklist();
    }

    public static void readAndLoadEnchantedItemBlacklist() throws URISyntaxException, IOException {
        Path enchantedItemBlacklistPath = Paths.get(DataReader.class.getResource("/enchantedItemBlacklist.json").toURI());
        JsonReader jsonReader = new JsonReader(Files.newBufferedReader(enchantedItemBlacklistPath, StandardCharsets.UTF_8));

        EnchantedItemBlacklist blacklist = GSON.fromJson(jsonReader, EnchantedItemBlacklist.class);
        jsonReader.close();

        EnchantedItemPlacementBlocker.setBlacklist(blacklist);
    }

    public static void readAndLoadOnlineData() throws URISyntaxException, IOException {
        Path onlineDataPath = Paths.get(DataReader.class.getResource("/data.json").toURI());
        JsonReader jsonReader = new JsonReader(Files.newBufferedReader(onlineDataPath, StandardCharsets.UTF_8));
        AtomicReference<OnlineData> onlineData = new AtomicReference<>(GSON.fromJson(jsonReader, OnlineData.class));

        LOGGER.info("Attempting to grab data from online.");
        new Thread(() -> {
            try {
                boolean isCurrentBeta = SkyblockAddons.VERSION.contains("b");
                URL url = new URL("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons/"+(isCurrentBeta ? "development" : "master")+"/src/main/resources/data.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", Utils.USER_AGENT);

                LOGGER.info("Online data - Got response code " + connection.getResponseCode());

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }
                connection.disconnect();

                onlineData.set(GSON.fromJson(response.toString(), OnlineData.class));
                LOGGER.info("Successfully grabbed online data.");

                MAIN.getUpdater().processUpdateCheckResult();
            } catch (Exception ex) {
                LOGGER.warn("There was an error while trying to pull the online data...");
                LOGGER.catching(ex);
            }
        }).start();

        MAIN.setOnlineData(onlineData.get());
    }
}
