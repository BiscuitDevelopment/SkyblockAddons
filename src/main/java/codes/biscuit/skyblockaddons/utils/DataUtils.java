package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.OnlineData;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreatureManager;
import codes.biscuit.skyblockaddons.features.EnchantManager;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemLists;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemPlacementBlocker;
import codes.biscuit.skyblockaddons.utils.pojo.SkyblockAddonsAPIResponse;
import codes.biscuit.skyblockaddons.utils.skyblockdata.CompactorItem;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerData;
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
import java.util.HashMap;
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

        // TODO Let's leave this enabled so we can see errors with the endpoints.
        //  If you need to change any files locally, just disable this manually.
//        if (!SkyblockAddonsTransformer.isDeobfuscated()) {
            fetchFromOnline();
//        }
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
            SkyblockAddons.getLogger().error("An error occurred while reading the local enchanted item lists!", ex);
        }

        // Containers
        inputStream = DataUtils.class.getResourceAsStream("/containers.json");
        try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))) {
            ItemUtils.setContainers(GSON.fromJson(jsonReader, new TypeToken<HashMap<String, ContainerData>>() {}.getType()));
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while reading the containers map!", ex);
        }

        // Compactor Items
        inputStream = DataUtils.class.getResourceAsStream("/compactorItems.json");
        try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))) {
            ItemUtils.setCompactorItems(GSON.fromJson(jsonReader, new TypeToken<HashMap<String, CompactorItem>>() {}.getType()));
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while reading the compactor items map!", ex);
        }

        // Enchantment data
        inputStream = DataUtils.class.getResourceAsStream("/enchants.json");
        try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8)))){
            EnchantManager.setEnchants(GSON.fromJson(jsonReader, new TypeToken<EnchantManager.Enchants>() {}.getType()));
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while reading the enchantments file!", ex);
        }

        // Online Data
        inputStream = DataUtils.class.getResourceAsStream("/data.json");
        try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8)))){
            main.setOnlineData(GSON.fromJson(jsonReader, OnlineData.class));
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while reading the local data file!", ex);
        }

        // TODO: pull from online as well
        // Cooldown Data
        inputStream = DataUtils.class.getResourceAsStream("/cooldowns.json");
        try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8)))){
            CooldownManager.setItemCooldowns(GSON.fromJson(jsonReader, new TypeToken<HashMap<String, Integer>>() {}.getType()));
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while reading the local data file!", ex);
        }
    }

    /*
     This method fetches copies of all the data files from the server and checks if they are newer than the local copies.
     If an online copy is newer, the local copy is overwritten.
     */
    private static void fetchFromOnline() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setUserAgent(Utils.USER_AGENT).build()) {
            // Enchanted Item Blacklist
            LOGGER.info("Trying to fetch enchanted item lists from the server...");
            EnchantedItemLists receivedBlacklist = httpClient.execute(new HttpGet("https://api.skyblockaddons.com/api/v1/skyblockaddons/enchantedItemLists"), response -> {
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

                    SkyblockAddonsAPIResponse apiResponse = SkyblockAddons.getGson().fromJson(jsonReader, SkyblockAddonsAPIResponse.class);
                    return GSON.fromJson(apiResponse.getResponse(), EnchantedItemLists.class);
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            });
            if (receivedBlacklist != null) {
                LOGGER.info("Successfully fetched enchanted item lists!");
                EnchantedItemPlacementBlocker.setItemLists(receivedBlacklist);
            }

            // Containers
            LOGGER.info("Trying to fetch containers from the server...");
            Map<String, ContainerData> receivedContainers = httpClient.execute(new HttpGet("https://api.skyblockaddons.com/api/v1/skyblock/containers"), response -> {
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

                    SkyblockAddonsAPIResponse apiResponse = SkyblockAddons.getGson().fromJson(jsonReader, SkyblockAddonsAPIResponse.class);
                    return GSON.fromJson(apiResponse.getResponse(), new TypeToken<HashMap<String, ContainerData>>() {}.getType());
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            });
            if (receivedContainers != null) {
                LOGGER.info("Successfully fetched containers!");
                ItemUtils.setContainers(receivedContainers);
            }

            // Compactor Items
            LOGGER.info("Trying to fetch compactor items from the server...");
            Map<String, CompactorItem> receivedCompactorItems = httpClient.execute(new HttpGet("https://api.skyblockaddons.com/api/v1/skyblock/compactorItems"), response -> {
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

                    SkyblockAddonsAPIResponse apiResponse = SkyblockAddons.getGson().fromJson(jsonReader, SkyblockAddonsAPIResponse.class);
                    return GSON.fromJson(apiResponse.getResponse(), new TypeToken<HashMap<String, CompactorItem>>() {}.getType());
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            });
            if (receivedCompactorItems != null) {
                LOGGER.info("Successfully fetched compactor items!");
                ItemUtils.setCompactorItems(receivedCompactorItems);
            }

            // Online Data
            LOGGER.info("Trying to fetch online data from the server...");
            OnlineData receivedOnlineData = httpClient.execute(new HttpGet("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons/development/src/main/resources/test-data.json"), response -> {
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

            // Sea Creatures
            SeaCreatureManager.getInstance().pullSeaCreatures();

        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("There was an error fetching data from the server. The bundled version of the file will be used instead. ");
            LOGGER.catching(e);
        }
    }
}
