package codes.biscuit.skyblockaddons.utils.data;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.OnlineData;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemBlacklist;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemPlacementBlocker;
import codes.biscuit.skyblockaddons.utils.Utils;
import codes.biscuit.skyblockaddons.utils.data.responseHandlers.EnchantedItemBlacklistResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.responseHandlers.OnlineDataResponseHandler;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class reads data from the JSON files in the mod's resources or on the mod's Github repo and loads it into memory.
 */
public class DataReader {
    private static final SkyblockAddons MAIN = SkyblockAddons.getInstance();
    private static final Logger LOGGER = MAIN.getLogger();
    private static final Gson GSON = new Gson();

    private static EnchantedItemBlacklist blacklist;
    private static OnlineData onlineData;

    //TODO: Migrate all data file loading to this class

    /**
     * This method reads the data files from the mod's resources and fetches copies of the same files from a server.
     * If the server's copies are newer, they are loaded into memory. Otherwise, the local ones are loaded.
     *
     * @throws URISyntaxException if the path to one of the data files is not a valid {@code URI}
     * @throws IOException if an error occurred while reading one of the data files.
     */
    public static void readAndLoadAll() throws IOException, URISyntaxException {
        readOnlineData();
        readEnchantedItemBlacklist();
        fetchAll();
        loadAll();
    }

    /**
     * Reads the enchanted item blacklist from {@code enchantedItemBlacklist.json} in the mod's resources
     *
     * @throws URISyntaxException if the path to the file is not a valid {@code URI}
     * @throws IOException if there was an error reading the file
     */
    public static void readEnchantedItemBlacklist() throws URISyntaxException, IOException {
        Path enchantedItemBlacklistPath = Paths.get(DataReader.class.getResource("/enchantedItemBlacklist.json").toURI());
        JsonReader jsonReader = new JsonReader(Files.newBufferedReader(enchantedItemBlacklistPath, StandardCharsets.UTF_8));

        DataReader.blacklist = GSON.fromJson(jsonReader, EnchantedItemBlacklist.class);
    }

    /**
     * Reads the online data from {@code data.json} in the mod's resources
     *
     * @throws URISyntaxException if the path to the file is not a valid {@code URI}
     * @throws IOException if there was an error reading the file
     */
    public static void readOnlineData() throws URISyntaxException, IOException {
        Path onlineDataPath = Paths.get(DataReader.class.getResource("/data.json").toURI());
        JsonReader jsonReader = new JsonReader(Files.newBufferedReader(onlineDataPath, StandardCharsets.UTF_8));

        DataReader.onlineData = GSON.fromJson(jsonReader, OnlineData.class);
    }

    /*
     This method fetches copies of all the data files from the server and checks if they are newer than the local copies.
     If an online copy is newer, the local copy is overwritten.
     */
    private static void fetchAll() {
        try (CloseableHttpClient HTTP_CLIENT = HttpClientBuilder.create().setUserAgent(Utils.USER_AGENT).build()) {
            HttpGet enchantedItemBlacklistGet = new HttpGet("https://raw.githubusercontent.com/BiscuitDevelopment/" +
                    "SkyblockAddons/development/src/main/resources/enchantedItemBlacklist.json");
            HttpGet onlineDataGet = new HttpGet("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons/"
                    + (SkyblockAddons.VERSION.contains("beta") ? "development" : "master") + "/src/main/resources/data.json");

            /*
            Enchanted Item Blacklist
             */

            // Attempt to fetch the data from the server.
            LOGGER.info("Trying to fetch enchanted item blacklist from the server...");
            EnchantedItemBlacklist receivedBlacklist = HTTP_CLIENT.execute(enchantedItemBlacklistGet, new EnchantedItemBlacklistResponseHandler());
            LOGGER.info("Success!");

            // Check hashes and overwrite the local version if the online version has a different hash.
            if (DataReader.blacklist.hashCode() != receivedBlacklist.hashCode()) {
                LOGGER.info("A newer version was found on the server! It will be used instead of the local version.");
                DataReader.blacklist = receivedBlacklist;
            } else {
                LOGGER.info("No newer version was found on the server! The local copy will be used.");
            }

            /*
            Online Data
             */

            // Attempt to fetch the data from the server.
            LOGGER.info("Trying to fetch online data from the server...");
            OnlineData receivedOnlineData = HTTP_CLIENT.execute(onlineDataGet, new OnlineDataResponseHandler());
            LOGGER.info("Success!");

            // Check hashes and overwrite the local version if the online version has a different hash.
            if (DataReader.onlineData.hashCode() != receivedBlacklist.hashCode()) {
                LOGGER.info("A newer version was found on the server! It will be used instead of the local version.");
                DataReader.onlineData = receivedOnlineData;
            } else {
                LOGGER.info("No newer version was found on the server! The local copy will be used.");
            }
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("There was an error fetching data from the server. " +
                    "The bundled version of the file will be used instead. ");
            LOGGER.catching(e);
        }
    }

    // This method loads the values that were read into the mod.
    private static void loadAll() {
        EnchantedItemPlacementBlocker.setBlacklist(DataReader.blacklist);
        MAIN.setOnlineData(DataReader.onlineData);
    }
}
