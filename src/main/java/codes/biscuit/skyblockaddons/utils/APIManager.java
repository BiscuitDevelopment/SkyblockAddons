package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerBoss;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.utils.pojo.PlayerData;
import codes.biscuit.skyblockaddons.utils.pojo.Profile;
import codes.biscuit.skyblockaddons.utils.pojo.ProfileMembers;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class APIManager {

    private static final APIManager INSTANCE = new APIManager();

    private static final String BASE_URL = "https://api.slothpixel.me/api/";
    private static final String PLAYER = BASE_URL + "players/%s"; // UUID
    private static final String SKYBLOCK_PROFILES = BASE_URL + "skyblock/profiles/%s"; // UUID
    private static final String SKYBLOCK_PROFILE = BASE_URL + "skyblock/profile/%s/%s"; // UUID, Profile

    private SkyblockAddons main = SkyblockAddons.getInstance();
    private Logger logger = SkyblockAddons.getLogger();
    private boolean firstSwitch = true;

    public void onProfileSwitch() {
        String profileName = main.getUtils().getProfileName();

        if (profileName != null) {
            String uuid = Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", ""); // No dashes

            if (firstSwitch) {
                pullPlayer(uuid);
                firstSwitch = false;
            }

            pullProfiles(uuid, profileName);
        }
    }

    public void pullPlayer(String uuid) {
        SkyblockAddons.newThread(() -> {
            logger.info("Grabbing player API data for UUID " + uuid + "...");
            try {
                URL url = new URL(String.format(PLAYER, uuid));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", Utils.USER_AGENT);

                logger.info("Got response code " + connection.getResponseCode());

                PlayerData playerData = SkyblockAddons.getGson().fromJson(new InputStreamReader(connection.getInputStream()), PlayerData.class);
                connection.disconnect();

                if (playerData != null && playerData.getLanguage() != null) {
                    main.getPersistentValuesManager().getPersistentValues().setHypixelLanguage(playerData.getLanguage());
                }

            } catch (Exception ex) {
                logger.warn("Failed to grab player's profiles API data!");
                logger.catching(ex);
            }
        }).start();
    }

    public void pullProfiles(String uuid, String profileName) {
        SkyblockAddons.newThread(() -> {
            logger.info("Grabbing player's profiles API data for UUID " + uuid + " & profile name " + profileName + "...");
            try {
                URL url = new URL(String.format(SKYBLOCK_PROFILES, uuid));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", Utils.USER_AGENT);

                logger.info("Got response code " + connection.getResponseCode());

                Map<String, Profile> profiles = SkyblockAddons.getGson().fromJson(new InputStreamReader(connection.getInputStream()), new TypeToken<HashMap<String, Profile>>(){}.getType());
                connection.disconnect();

                for (Map.Entry<String, Profile> entry : profiles.entrySet()) {
                    String profileID = entry.getKey();
                    Profile profile = entry.getValue();

                    if (profileName.equals(profile.getCute_name())) {
                        logger.info("Found profile matching " + profileName + " with ID " + profileID + "! Pulling profile data...");
                        pullProfileData(uuid, profileID);
                        return;
                    }
                }

                logger.info("Did not find profile matching " + profileName + "!");

            } catch (Exception ex) {
                logger.warn("Failed to grab player's profiles API data!");
                logger.catching(ex);
            }
        }).start();
    }

    public void pullProfileData(String uuid, String profileID) {
        SkyblockAddons.newThread(() -> {
            logger.info("Grabbing profile API data for UUID " + uuid + " & profile ID " + profileID + "...");
            try {
                URL url = new URL(String.format(SKYBLOCK_PROFILE, uuid, profileID));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", Utils.USER_AGENT);

                logger.info("Got response code " + connection.getResponseCode());

                ProfileMembers profileMembers = SkyblockAddons.getGson().fromJson(new InputStreamReader(connection.getInputStream()), ProfileMembers.class);
                connection.disconnect();

                if (profileMembers.getMembers().containsKey(uuid)) {
                    ProfileMembers.MemberData memberData = profileMembers.getMembers().get(uuid);

                    ProfileMembers.Slayers slayers = memberData.getSlayer();
                    if (slayers != null) {
                        ProfileMembers.SlayerData zombie = memberData.getSlayer().getZombie();
                        ProfileMembers.SlayerData spider = memberData.getSlayer().getSpider();
                        ProfileMembers.SlayerData wolf = memberData.getSlayer().getWolf();

                        if (zombie != null && zombie.getKills_tier() != null) {
                            int total = 0;
                            for (Integer kills : zombie.getKills_tier().values()) {
                                total += kills;
                            }
                            SlayerTracker.getInstance().setKillCount(SlayerBoss.REVENANT, total);
                        }

                        if (spider != null && spider.getKills_tier() != null) {
                            int total = 0;
                            for (Integer kills : spider.getKills_tier().values()) {
                                total += kills;
                            }
                            SlayerTracker.getInstance().setKillCount(SlayerBoss.TARANTULA, total);
                        }

                        if (wolf != null && wolf.getKills_tier() != null) {
                            int total = 0;
                            for (Integer kills : wolf.getKills_tier().values()) {
                                total += kills;
                            }
                            SlayerTracker.getInstance().setKillCount(SlayerBoss.SVEN, total);
                        }
                    }

                    ProfileMembers.Stats stats = memberData.getStats();
                    if (stats != null) {
                        ProfileMembers.PetMilestones petMilestones = stats.getPet_milestones();
                        if (petMilestones != null) {
                            main.getPersistentValuesManager().getPersistentValues().setOresMined(petMilestones.getOre_mined());
                            main.getPersistentValuesManager().getPersistentValues().setSeaCreaturesKilled(petMilestones.getSea_creatures_killed());
                        }
                    }
                }
            } catch (Exception ex) {
                logger.warn("Failed to grab profile API data!");
                logger.catching(ex);
            }
        }).start();
    }

    public static APIManager getInstance() {
        return INSTANCE;
    }
}
