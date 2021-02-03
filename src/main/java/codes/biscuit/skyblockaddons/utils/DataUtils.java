package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.OnlineData;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreature;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreatureManager;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemLists;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemPlacementBlocker;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ItemMap;
import codes.biscuit.skyblockaddons.utils.skyblockdata.SkyblockItem;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
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
import java.util.Map;

/**
 * This class reads data from the JSON files in the mod's resources or on the mod's Github repo and loads it into memory.
 */
public class DataUtils {

    private static final Gson GSON = SkyblockAddons.getGson();

    private static final Logger LOGGER = SkyblockAddons.getLogger();

    //TODO: Migrate all data file loading to this class

    /**
     * This method reads the data files from the mod's resources and fetches copies of
     * the same files from a server, which replaces the local ones. If the mod is running in a development environment,
     * local files will be used.
     */
    public static void readLocalAndFetchOnline() {
        readLocalFileData();

        if (!SkyblockAddonsTransformer.isDeobfuscated()) {
            fetchFromOnline();
        }
    }

    /**
     * Reads local json files before pulling from online
     */
    public static void readLocalFileData() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        // Enchanted Item Blacklist
        InputStream inputStream = DataUtils.class.getResourceAsStream("/enchantedItemLists.json");
        try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))){
            EnchantedItemPlacementBlocker.setItemLists(GSON.fromJson(jsonReader, EnchantedItemLists.class));
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while reading local enchanted item lists!");
        }

        // Item Map
        inputStream = DataUtils.class.getResourceAsStream("/itemMap.json");
        try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))) {
            ItemUtils.itemMap = GSON.fromJson(jsonReader, ItemMap.class);
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while reading local item map!");
        }
        //SkyblockAddons.getLogger().info(ItemUtils.itemMap.toString());

        // Online Data
        inputStream = DataUtils.class.getResourceAsStream("/data.json");
        try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8)))){
            main.setOnlineData(GSON.fromJson(jsonReader, OnlineData.class));
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while reading local data!");
        }
    }

    /*
     This method fetches copies of all the data files from the server and checks if they are newer than the local copies.
     If an online copy is newer, the local copy is overwritten.
     */
    private static void fetchFromOnline() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setUserAgent(Utils.USER_AGENT).build()) {
            HttpGet enchantedItemListsGet = new HttpGet("https://raw.githubusercontent.com/BiscuitDevelopment/" +
                    "SkyblockAddons/development/src/main/resources/enchantedItemLists.json");
            HttpGet itemMapGet = new HttpGet("https://raw.githubusercontent.com/BiscuitDevelopment/" +
                    "SkyblockAddons/development/src/main/resources/itemMap.json");
            HttpGet onlineDataGet = new HttpGet("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons/"
                    + (SkyblockAddons.VERSION.contains("b") ? "development" : "master") + "/src/main/resources/data.json");
            @Deprecated
            HttpGet seaCreaturesGet = new HttpGet("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/fishing/seaCreatures.json");

            // Enchanted Item Blacklist
            LOGGER.info("Trying to fetch enchanted item lists from the server...");
            EnchantedItemLists receivedBlacklist = httpClient.execute(enchantedItemListsGet, response -> {
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

                    return GSON.fromJson(jsonReader, EnchantedItemLists.class);
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            });
            if (receivedBlacklist != null) {
                LOGGER.info("Successfully fetched enchanted item lists!");
                EnchantedItemPlacementBlocker.setItemLists(receivedBlacklist);
            }

            // Item Map
            LOGGER.info("Trying to fetch item map from the server...");
            ItemMap receivedItemMap = httpClient.execute(itemMapGet, response -> {
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

                    return GSON.fromJson(jsonReader, ItemMap.class);
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            });
            if (receivedItemMap != null) {
                LOGGER.info("Successfully fetched item map!");
                // TODO: Link back up to online
                //ItemUtils.itemMap = receivedItemMap;
            }

            // Online Data
            LOGGER.info("Trying to fetch online data from the server...");
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
                LOGGER.info("Successfully fetched online data!");
                main.setOnlineData(receivedOnlineData);
                main.getUpdater().processUpdateCheckResult();
            }

        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("There was an error fetching data from the server. The bundled version of the file will be used instead. ");
            LOGGER.catching(e);
        }
    }
}
