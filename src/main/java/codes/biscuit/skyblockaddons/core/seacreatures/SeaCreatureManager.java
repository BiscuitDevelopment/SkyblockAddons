package codes.biscuit.skyblockaddons.core.seacreatures;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConnectUtils;
import codes.biscuit.skyblockaddons.utils.DataUtils;
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

public class SeaCreatureManager {

    private static final SeaCreatureManager INSTANCE = new SeaCreatureManager();

    private Map<String, SeaCreature> seaCreatures = new LinkedHashMap<>();
    @Getter private Set<String> allSeaCreatureSpawnMessages = new HashSet<>();
    @Getter private Set<String> legendarySeaCreatureSpawnMessages = new HashSet<>();

    public SeaCreatureManager() {
        pullSeaCreatures();
    }

    private void pullSeaCreatures() {
        InputStream localStream = DataUtils.class.getResourceAsStream("/seaCreatures.json");
        try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(localStream, StandardCharsets.UTF_8)))) {
            seaCreatures = SkyblockAddons.getGson().fromJson(jsonReader, new TypeToken<Map<String, SeaCreature>>() {}.getType());
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while reading local sea creatures!");
        }

        ConnectUtils.get("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons-Data/main/fishing/seaCreatures.json", onlineStream -> {
            try (JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(onlineStream, StandardCharsets.UTF_8)))) {
                seaCreatures = SkyblockAddons.getGson().fromJson(jsonReader, new TypeToken<Map<String, SeaCreature>>() {}.getType());

                for (Map.Entry<String, SeaCreature> entry : seaCreatures.entrySet()) {
                    allSeaCreatureSpawnMessages.add(entry.getValue().getSpawnMessage());
                    if (SkyblockAddons.getInstance().getOnlineData().getLegendarySeaCreatures().contains(entry.getKey())) {
                        legendarySeaCreatureSpawnMessages.add(entry.getValue().getSpawnMessage());
                    }
                }
            } catch (Exception ex) {
                SkyblockAddons.getLogger().error("An error occurred while pulling online sea creatures!");
            }
        });
    }

    public static SeaCreatureManager getInstance() {
        return INSTANCE;
    }
}
