package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.OnlineData;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemBlacklist;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemPlacementBlocker;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This class reads data from the JSON files in the mod's resources or on the mod's Github repo and loads it into memory.
 */
public class DataUtils {

    private static final Gson GSON = new Gson();

    private static Logger logger = SkyblockAddons.getLogger();

    //TODO: Migrate all data file loading to this class

    /**
     * This method reads the data files from the mod's resources and fetches copies of
     * the same files from a server, which replaces the local ones.
     */
    public static void readLocalAndFetchOnline() {
        readLocalFileData();

        fetchFromOnline();
    }

    /**
     * Reads local json files before pulling from online
     */
    public static void readLocalFileData() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        // Enchanted Item Blacklist
        InputStream inputStream = DataUtils.class.getResourceAsStream("/enchantedItemBlacklist.json");
        JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));

        EnchantedItemPlacementBlocker.setBlacklist(GSON.fromJson(jsonReader, EnchantedItemBlacklist.class));

        // Online Data
        inputStream = DataUtils.class.getResourceAsStream("/data.json");
        jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));

        main.setOnlineData(GSON.fromJson(jsonReader, OnlineData.class));
    }

    /*
     This method fetches copies of all the data files from the server and checks if they are newer than the local copies.
     If an online copy is newer, the local copy is overwritten.
     */
    private static void fetchFromOnline() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setUserAgent(Utils.USER_AGENT).build()) {
            HttpGet enchantedItemBlacklistGet = new HttpGet("https://raw.githubusercontent.com/BiscuitDevelopment/" +
                    "SkyblockAddons/development/src/main/resources/enchantedItemBlacklist.json");
            HttpGet onlineDataGet = new HttpGet("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons/"
                    + (SkyblockAddons.VERSION.contains("b") ? "development" : "master") + "/src/main/resources/data.json");


            // Enchanted Item Blacklist
            logger.info("Trying to fetch enchanted item blacklist from the server...");
            EnchantedItemBlacklist receivedBlacklist = httpClient.execute(enchantedItemBlacklistGet, response -> {
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

                    return GSON.fromJson(jsonReader, EnchantedItemBlacklist.class);
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            });
            if (receivedBlacklist != null) {
                logger.info("Successfully fetched enchanted item blacklist!");
                EnchantedItemPlacementBlocker.setBlacklist(receivedBlacklist);
            }

            // Online Data
            logger.info("Trying to fetch online data from the server...");
            OnlineData receivedOnlineData = httpClient.execute(onlineDataGet, response -> {
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

                    return GSON.fromJson(jsonReader, OnlineData.class);
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            });
            if (receivedOnlineData != null) {
                logger.info("Successfully fetched online data!");
                main.setOnlineData(receivedOnlineData);
                main.getUpdater().processUpdateCheckResult();
            }

        } catch (IOException | JsonSyntaxException e) {
            logger.error("There was an error fetching data from the server. The bundled version of the file will be used instead. ");
            logger.catching(e);
        }
    }
}
