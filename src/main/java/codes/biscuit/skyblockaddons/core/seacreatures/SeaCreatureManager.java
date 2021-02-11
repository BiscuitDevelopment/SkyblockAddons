package codes.biscuit.skyblockaddons.core.seacreatures;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConnectUtils;
import codes.biscuit.skyblockaddons.utils.DataUtils;
import codes.biscuit.skyblockaddons.utils.pojo.SkyblockAddonsAPIResponse;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static codes.biscuit.skyblockaddons.core.ItemRarity.LEGENDARY;


public class SeaCreatureManager {

    private static final SeaCreatureManager INSTANCE = new SeaCreatureManager();

    private Map<String, SeaCreature> seaCreatures = new LinkedHashMap<>();
    @Getter private Set<String> allSeaCreatureSpawnMessages = new HashSet<>();
    @Getter private Set<String> legendarySeaCreatureSpawnMessages = new HashSet<>();

    /**
     * Populate sea creature information from local and online sources
     */
    public void pullSeaCreatures() {
        InputStream localStream = DataUtils.class.getResourceAsStream("/seaCreatures.json");
        try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(localStream, StandardCharsets.UTF_8)))) {
            seaCreatures = SkyblockAddons.getGson().fromJson(jsonReader, new TypeToken<Map<String, SeaCreature>>() {}.getType());

            for (SeaCreature sc : seaCreatures.values()) {
                allSeaCreatureSpawnMessages.add(sc.getSpawnMessage());
                if (sc.getRarity() == LEGENDARY) {
                    legendarySeaCreatureSpawnMessages.add(sc.getSpawnMessage());
                }
            }

        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while reading local sea creatures!");
        }

        // Assume online asset has most up-to-date information
        ConnectUtils.get("https://api.skyblockaddons.com/api/v1/skyblock/seaCreatures", onlineStream -> {
            try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(onlineStream, StandardCharsets.UTF_8)))) {
                SkyblockAddonsAPIResponse apiResponse = SkyblockAddons.getGson().fromJson(jsonReader, SkyblockAddonsAPIResponse.class);
                seaCreatures = SkyblockAddons.getGson().fromJson(apiResponse.getResponse(), new TypeToken<Map<String, SeaCreature>>() {}.getType());

                allSeaCreatureSpawnMessages.clear();
                legendarySeaCreatureSpawnMessages.clear();
                for (SeaCreature sc : seaCreatures.values()) {
                    allSeaCreatureSpawnMessages.add(sc.getSpawnMessage());
                    if (sc.getRarity() == LEGENDARY) {
                        legendarySeaCreatureSpawnMessages.add(sc.getSpawnMessage());
                    }
                }
                SkyblockAddons.getLogger().info("Successfully fetched sea creatures!");
            } catch (Exception ex) {
                SkyblockAddons.getLogger().error("An error occurred while pulling online sea creatures!", ex);
            }
        });
    }

    public static SeaCreatureManager getInstance() {
        return INSTANCE;
    }
}
