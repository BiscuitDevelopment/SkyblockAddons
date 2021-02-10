package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.events.SkyblockJoinedEvent;
import codes.biscuit.skyblockaddons.events.SkyblockLeftEvent;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreview;
import codes.biscuit.skyblockaddons.features.itemdrops.ItemDropChecker;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class Utils {

    /**
     * Added to the beginning of messages.
     */
    public static final String MESSAGE_PREFIX =
            ColorCode.GRAY + "[" + ColorCode.AQUA + SkyblockAddons.MOD_NAME + ColorCode.GRAY + "] ";

    private static final Pattern SERVER_REGEX = Pattern.compile("(?<serverType>[Mm])(?<serverCode>[0-9]+[A-Z])$");
    private static final Pattern PURSE_REGEX = Pattern.compile("(?:Purse|Piggy): (?<coins>[0-9.]*)(?: .*)?");
    private static final Pattern SLAYER_TYPE_REGEX = Pattern.compile("(?<type>Tarantula Broodfather|Revenant Horror|Sven Packmaster) (?<level>[IV]+)");
    private static final Pattern SLAYER_PROGRESS_REGEX = Pattern.compile("(?<progress>[0-9.k]*)/(?<total>[0-9.k]*) (?:Kills|Combat XP)$");

    /**
     * In English, Chinese Simplified, Traditional Chinese.
     */
    private static final Set<String> SKYBLOCK_IN_ALL_LANGUAGES = Sets.newHashSet("SKYBLOCK", "\u7A7A\u5C9B\u751F\u5B58", "\u7A7A\u5CF6\u751F\u5B58");

    private static final WorldClient DUMMY_WORLD = new WorldClient(null, new WorldSettings(0L, WorldSettings.GameType.SURVIVAL,
            false, false, WorldType.DEFAULT), 0, null, null);

    /**
     * Used for web requests.
     */
    public static final String USER_AGENT = "SkyblockAddons/" + SkyblockAddons.VERSION;

    // I know this is messy af, but frustration led me to take this dark path - said someone not biscuit
    public static boolean blockNextClick;

    /**
     * Get a player's attributes. This includes health, mana, and defence.
     */
    private Map<Attribute, MutableInt> attributes = new EnumMap<>(Attribute.class);

    /**
     * This is the item checker that makes sure items being dropped or sold are allowed to be dropped or sold.
     */
    private final ItemDropChecker itemDropChecker = new ItemDropChecker();

    /**
     * List of enchantments that the player is looking to find.
     */
    private List<String> enchantmentMatches = new LinkedList<>();

    /**
     * List of enchantment substrings that the player doesn't want to match.
     */
    private List<String> enchantmentExclusions = new LinkedList<>();

    /**
     * Whether the player is on skyblock.
     */
    private boolean onSkyblock;

    /**
     * The player's current location in Skyblock
     */
    @Getter private Location location = Location.UNKNOWN;

    /**
     * The skyblock profile that the player is currently on. Ex. "Grapefruit"
     */
    private String profileName = "Unknown";

    /**
     * Whether or not a loud sound is being played by the mod.
     */
    private boolean playingSound;

    /**
     * The current serverID that the player is on.
     */
    private String serverID = "";
    private int lastHoveredSlot = -1;

    /**
     * Whether the player is using the old style of bars packaged into Imperial's Skyblock Pack.
     */
    private boolean usingOldSkyBlockTexture;

    /**
     * Whether the player is using the default bars packaged into the mod.
     */
    private boolean usingDefaultBarTextures = true;

    private SkyblockDate currentDate = new SkyblockDate(SkyblockDate.SkyblockMonth.EARLY_WINTER, 1, 1, 1, "am");
    private double purse = 0;
    private int jerryWave = -1;

    private boolean alpha;
    private boolean inDungeon;

    private boolean fadingIn;

    private long lastDamaged = -1;

    private EnumUtils.SlayerQuest slayerQuest;
    private int slayerQuestLevel = 1;
    private boolean slayerBossAlive;

    private SkyblockAddons main = SkyblockAddons.getInstance();
    private Logger logger = SkyblockAddons.getLogger();

    public Utils() {
        addDefaultStats();
    }

    private void addDefaultStats() {
        for (Attribute attribute : Attribute.values()) {
            attributes.put(attribute, new MutableInt(attribute.getDefaultValue()));
        }
    }

    public void sendMessage(String text, boolean prefix) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte) 1, new ChatComponentText((prefix ? MESSAGE_PREFIX : "") + text));
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message); // Just for logs
        }
    }

    public void sendMessage(String text) {
        sendMessage(text, true);
    }

    public void sendMessage(ChatComponentText text, boolean prefix) {
        if (prefix) { // Add the prefix in front.
            ChatComponentText newText = new ChatComponentText(MESSAGE_PREFIX);
            newText.appendSibling(text);
            text = newText;
        }

        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte) 1, text);
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message); // Just for logs
        }
    }

    public void sendErrorMessage(String errorText) {
        sendMessage(ColorCode.RED + "Error: " + errorText);
    }

    /**
     * Checks if the player is on the Hypixel Network
     *
     * @return {@code true} if the player is on Hypixel, {@code false} otherwise
     */
    public boolean isOnHypixel() {
        return ScoreboardManager.hasScoreboard() && ScoreboardManager.getStrippedScoreboardLines().contains("www.hypixel.net");
    }

    public void parseSidebar() {
        boolean foundScoreboard = false;

        boolean foundLocation = false;
        boolean foundJerryWave = false;
        boolean foundAlphaIP = false;
        boolean foundInDungeon = false;
        boolean foundSlayerQuest = false;
        boolean foundBossAlive = false;
        boolean foundSkyblockTitle = false;

        if (isOnHypixel()) {
            foundScoreboard = true;

            // Check title for skyblock
            String strippedScoreboardTitle = ScoreboardManager.getStrippedScoreboardTitle();
            for (String skyblock : SKYBLOCK_IN_ALL_LANGUAGES) {
                if (strippedScoreboardTitle.startsWith(skyblock)) {
                    foundSkyblockTitle = true;
                    break;
                }
            }

            // If it's a Skyblock scoreboard and the player has not joined Skyblock yet,
            // this indicates that he did so.
            if (foundSkyblockTitle && !this.isOnSkyblock()) {
                MinecraftForge.EVENT_BUS.post(new SkyblockJoinedEvent());
            }

            String timeString = null;
            String dateString = null;

            for (int lineNumber = 0; lineNumber < ScoreboardManager.getNumberOfLines(); lineNumber++) {
                String scoreboardLine = ScoreboardManager.getScoreboardLines().get(lineNumber);
                String strippedScoreboardLine = ScoreboardManager.getStrippedScoreboardLines().get(lineNumber);

                if (strippedScoreboardLine.endsWith("am") || strippedScoreboardLine.endsWith("pm")) {
                    timeString = strippedScoreboardLine;
                }

                if (strippedScoreboardLine.endsWith("st") || strippedScoreboardLine.endsWith("nd") || strippedScoreboardLine.endsWith("rd") || strippedScoreboardLine.endsWith("th")) {
                    dateString = strippedScoreboardLine;
                }

                Matcher matcher = PURSE_REGEX.matcher(strippedScoreboardLine);
                if (matcher.matches()) {
                    try {
                        double oldCoins = purse;
                        purse = Double.parseDouble(matcher.group("coins"));

                        if (oldCoins != purse) {
                            onCoinsChange(purse - oldCoins);
                        }
                    } catch (NumberFormatException ignored) {
                        purse = 0;
                    }
                }

                if ((matcher = SERVER_REGEX.matcher(strippedScoreboardLine)).find()) {
                    String serverType = matcher.group("serverType");
                    if (serverType.equals("m")) {
                        serverID = "mini" + matcher.group("serverCode");
                    } else if (serverType.equals("M")) {
                        serverID = "mega" + matcher.group("serverCode");
                    }
                }

                if (strippedScoreboardLine.endsWith("Combat XP") || strippedScoreboardLine.endsWith("Kills")) {
                    parseSlayerProgress(strippedScoreboardLine);
                }

                if (!foundLocation) {
                    // Catacombs contains the floor number so it's a special case...
                    if (strippedScoreboardLine.contains(Location.THE_CATACOMBS.getScoreboardName())) {
                        location = Location.THE_CATACOMBS;
                        foundLocation = true;
                    } else {
                        for (Location loopLocation : Location.values()) {
                            if (strippedScoreboardLine.endsWith(loopLocation.getScoreboardName())) {
                                if (loopLocation == Location.BLAZING_FORTRESS && location != Location.BLAZING_FORTRESS) {
                                    sendInventiveTalentPingRequest(EnumUtils.MagmaEvent.PING); // going into blazing fortress
                                    fetchMagmaBossEstimate();
                                }
                                // TODO: Special case causes Dwarven Village to map to Village since endsWith...idk if
                                //  changing to "equals" will mess it up for other locations
                                if (loopLocation == Location.VILLAGE && strippedScoreboardLine.contains("Dwarven")) {
                                    continue;
                                }
                                location = loopLocation;
                                foundLocation = true;
                                break;
                            }
                        }
                    }
                }

                if (!foundJerryWave && (location == Location.JERRYS_WORKSHOP || location == Location.JERRY_POND)) {
                    if (strippedScoreboardLine.startsWith("Wave")) {
                        foundJerryWave = true;

                        int newJerryWave;
                        try {
                            newJerryWave = Integer.parseInt(TextUtils.keepIntegerCharactersOnly(strippedScoreboardLine));
                        } catch (NumberFormatException ignored) {
                            newJerryWave = 0;
                        }
                        if (jerryWave != newJerryWave) {
                            jerryWave = newJerryWave;
                        }
                    }
                }

                if (!foundAlphaIP && strippedScoreboardLine.contains("alpha.hypixel.net")) {
                    foundAlphaIP = true;
                    alpha = true;
                    profileName = "Alpha";
                }

                if (!foundInDungeon && strippedScoreboardLine.contains("Dungeon Cleared: ")) {
                    foundInDungeon = true;
                    inDungeon = true;

                    String lastServer = main.getDungeonManager().getLastServerId();
                    if (lastServer != null && !lastServer.equals(serverID)) {
                        main.getDungeonManager().reset();
                    }
                    main.getDungeonManager().setLastServerId(serverID);
                }

                matcher = SLAYER_TYPE_REGEX.matcher(strippedScoreboardLine);
                if (matcher.matches()) {
                    String type = matcher.group("type");
                    String levelRomanNumeral = matcher.group("level");

                    EnumUtils.SlayerQuest detectedSlayerQuest = EnumUtils.SlayerQuest.fromName(type);
                    if (detectedSlayerQuest != null) {
                        try {
                            int level = RomanNumeralParser.parseNumeral(levelRomanNumeral);
                            slayerQuest = detectedSlayerQuest;
                            slayerQuestLevel = level;
                            foundSlayerQuest = true;

                        } catch (IllegalArgumentException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                if (strippedScoreboardLine.equals("Slay the boss!")) {
                    foundBossAlive = true;
                    slayerBossAlive = true;
                }

                if (inDungeon) {
                    try {
                        main.getDungeonManager().updateDungeonPlayer(scoreboardLine);
                    } catch (NumberFormatException ex) {
                        logger.error("Failed to update a dungeon player from the line " + scoreboardLine + ".", ex);
                    }
                }
            }

            currentDate = SkyblockDate.parse(dateString, timeString);
        }
        if (!foundLocation) {
            location = Location.UNKNOWN;
        }
        if (!foundJerryWave) {
            jerryWave = -1;
        }
        if (!foundAlphaIP) {
            alpha = false;
        }
        if (!foundInDungeon) {
            inDungeon = false;
        }
        if (!foundSlayerQuest) {
            slayerQuestLevel = 1;
            slayerQuest = null;
        }
        if (!foundBossAlive) {
            slayerBossAlive = false;
        }

        // If it's not a Skyblock scoreboard, the player must have left Skyblock and
        // be in some other Hypixel lobby or game.
        if (!foundSkyblockTitle && this.isOnSkyblock()) {

            // Check if we found a scoreboard in general. If not, its possible they are switching worlds.
            // If we don't find a scoreboard for 10s, then we know they actually left the server.
            if (foundScoreboard || System.currentTimeMillis() - ScoreboardManager.getLastFoundScoreboard() > 10000) {
                MinecraftForge.EVENT_BUS.post(new SkyblockLeftEvent());
            }
        }
    }

    private boolean triggeredSlayerWarning = false;
    private float lastCompletion;

    private void parseSlayerProgress(String line) {
        if (!main.getConfigValues().isEnabled(Feature.BOSS_APPROACH_ALERT)) return;

        Matcher matcher = SLAYER_PROGRESS_REGEX.matcher(line);
        if (matcher.find()) {
            String progressString = matcher.group("progress");
            String totalString = matcher.group("total");

            float progress = Float.parseFloat(TextUtils.keepFloatCharactersOnly(progressString));
            float total = Float.parseFloat(TextUtils.keepFloatCharactersOnly(totalString));

            if (progressString.contains("k")) progress *= 1000;
            if (totalString.contains("k")) total *= 1000;

            float completion = progress / total;

            if (completion > 0.85) {
                if (!triggeredSlayerWarning || (main.getConfigValues().isEnabled(Feature.REPEAT_SLAYER_BOSS_WARNING) && completion != lastCompletion)) {
                    triggeredSlayerWarning = true;
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setTitleFeature(Feature.BOSS_APPROACH_ALERT);
                    main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                }
            } else {
                triggeredSlayerWarning = false; // Reset warning flag when completion is below 85%, meaning they started a new quest.
            }

            lastCompletion = completion;
        }
    }

    private void onCoinsChange(double coinsChange) {
    }

    public int getDefaultColor(float alphaFloat) {
        int alpha = (int) alphaFloat;
        return new Color(150, 236, 255, alpha).getRGB();
    }

    /**
     * When you use this function, any sound played will bypass the player's
     * volume setting, so make sure to only use this for like warnings or stuff like that.
     */
    public void playLoudSound(String sound, double pitch) {
        playingSound = true;
        Minecraft.getMinecraft().thePlayer.playSound(sound, 1, (float) pitch);
        playingSound = false;
    }

    /**
     * This one plays the sound normally. See {@link Utils#playLoudSound(String, double)} for playing
     * a sound that bypasses the user's volume settings.
     */
    public void playSound(String sound, double pitch) {
        Minecraft.getMinecraft().thePlayer.playSound(sound, 1, (float) pitch);
    }

    public void playSound(String sound, double volume, double pitch) {
        Minecraft.getMinecraft().thePlayer.playSound(sound, (float) volume, (float) pitch);
    }

    public boolean enchantReforgeMatches(String text) {
        text = text.toLowerCase(Locale.US);
        for (String enchant : enchantmentMatches) {
            enchant = enchant.trim().toLowerCase(Locale.US);
            if (StringUtils.isNotEmpty(enchant) && text.contains(enchant)) {
                boolean foundExclusion = false;
                for (String exclusion : enchantmentExclusions) {
                    exclusion = exclusion.trim().toLowerCase(Locale.US);
                    if (StringUtils.isNotEmpty(exclusion) && text.contains(exclusion)) {
                        foundExclusion = true;
                        break;
                    }
                }
                if (!foundExclusion) {
                    return true;
                }
            }
        }
        return false;
    }

    public void fetchMagmaBossEstimate() {
        SkyblockAddons.runAsync(() -> {
            boolean magmaTimerEnabled = main.getConfigValues().isEnabled(Feature.MAGMA_BOSS_TIMER);
            if (!magmaTimerEnabled) {
                logger.info("Getting magma boss spawn estimate from server...");
            }
            try {
                URL url = new URL("https://hypixel-api.inventivetalent.org/api/skyblock/bosstimer/magma/estimatedSpawn");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);

                if (!magmaTimerEnabled) {
                    logger.info("Got response code " + connection.getResponseCode());
                }

                JsonObject responseJson = SkyblockAddons.getGson().fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
                connection.disconnect();

                long estimate = responseJson.get("estimate").getAsLong();
                long currentTime = responseJson.get("queryTime").getAsLong();
                int magmaSpawnTime = (int) ((estimate - currentTime) / 1000);

                if (!magmaTimerEnabled) {
                    logger.info("Query time was " + currentTime + ", server time estimate is " +
                            estimate + ". Updating magma boss spawn to be in " + magmaSpawnTime + " seconds.");
                }

                main.getPlayerListener().setMagmaTime(magmaSpawnTime);
                main.getPlayerListener().setMagmaAccuracy(EnumUtils.MagmaTimerAccuracy.ABOUT);
            } catch (IOException ex) {
                if (!magmaTimerEnabled) {
                    logger.warn("Failed to get magma boss spawn estimate from server");
                }
            }
        });
    }

    public void sendInventiveTalentPingRequest(EnumUtils.MagmaEvent event) {
        SkyblockAddons.runAsync(() -> {
            boolean magmaTimerEnabled = main.getConfigValues().isEnabled(Feature.MAGMA_BOSS_TIMER);
            if (!magmaTimerEnabled) {
                logger.info("Posting event " + event.getInventiveTalentEvent() + " to InventiveTalent API");
            }

            try {
                String urlString = "https://hypixel-api.inventivetalent.org/api/skyblock/bosstimer/magma/addEvent";
                if (event == EnumUtils.MagmaEvent.PING) {
                    urlString = "https://hypixel-api.inventivetalent.org/api/skyblock/bosstimer/magma/ping";
                }
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("User-Agent", USER_AGENT);

                Minecraft mc = Minecraft.getMinecraft();
                if (mc != null && mc.thePlayer != null) {
                    String postString;
                    if (event == EnumUtils.MagmaEvent.PING) {
                        postString = "isModRequest=true&minecraftUser=" + mc.thePlayer.getName() + "&lastFocused=" + System.currentTimeMillis() / 1000 + "&serverId=" + serverID;
                    } else {
                        postString = "type=" + event.getInventiveTalentEvent() + "&isModRequest=true&minecraftUser=" + mc.thePlayer.getName() + "&serverId=" + serverID;
                    }
                    connection.setDoOutput(true);
                    try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                        out.writeBytes(postString);
                        out.flush();
                    }

                    if (!magmaTimerEnabled) {
                        logger.info("Got response code " + connection.getResponseCode());
                    }
                    connection.disconnect();
                }
            } catch (IOException ex) {
                if (!magmaTimerEnabled) {
                    logger.warn("Failed to post event to server");
                }
            }
        });
    }

    /**
     * Returns the folder that SkyblockAddons is located in.
     *
     * @return the folder the SkyblockAddons jar is located in
     */
    public File getSBAFolder() {
        return Loader.instance().activeModContainer().getSource().getParentFile();
    }

    /**
     * Checks if it is currently Halloween according to the system calendar.
     *
     * @return {@code true} if it is Halloween, {@code false} otherwise
     */
    public boolean isHalloween() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) == Calendar.OCTOBER && calendar.get(Calendar.DAY_OF_MONTH) == 31;
    }

    public int getDefaultBlue(int alpha) {
        return new Color(160, 225, 229, alpha).getRGB();
    }

    public float normalizeValueNoStep(float value, float min, float max) {
        return MathHelper.clamp_float((snapNearDefaultValue(value) - min) / (max - min), 0.0F, 1.0F);
    }

    public float snapNearDefaultValue(float value) {
        if (value != 1 && value > 1 - 0.05 && value < 1 + 0.05) {
            return 1;
        }

        return value;
    }

    /**
     * Rounds a float value for when it is being displayed as a string.
     * <p>
     * For example, if the given value is 123.456789 and the decimal places is 2, this will round
     * to 1.23.
     *
     * @param value         The value to round
     * @param decimalPlaces The decimal places to round to
     * @return A string representation of the value rounded
     */
    public static String roundForString(float value, int decimalPlaces) {
        return String.format("%." + decimalPlaces + "f", value);
    }

    public String[] wrapSplitText(String text, int wrapLength) {
        return WordUtils.wrap(text, wrapLength).replace("\r", "").split(Pattern.quote("\n"));
    }

    public boolean itemIsInHotbar(ItemStack itemStack) {
        ItemStack[] inventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory;

        for (int slot = 0; slot < 9; slot++) {
            if (inventory[slot] == itemStack) {
                return true;
            }
        }
        return false;
    }

    public void loadLanguageFile(boolean pullOnline) {
        loadLanguageFile(main.getConfigValues().getLanguage());
        if (pullOnline) {
            main.getUtils().tryPullingLanguageOnline(main.getConfigValues().getLanguage()); // Try getting an updated version online after loading the local one.
        }
    }

    public void loadLanguageFile(Language language) {
        try {
            InputStream fileStream = getClass().getClassLoader().getResourceAsStream("lang/" + language.getPath() + ".json");
            if (fileStream != null) {
                try (InputStreamReader inputStreamReader = new InputStreamReader(fileStream, StandardCharsets.UTF_8)) {
                    JsonObject languageConfig = SkyblockAddons.getGson().fromJson(inputStreamReader, JsonObject.class);
                    main.getConfigValues().setLanguageConfig(languageConfig);
                } finally {
                    fileStream.close();
                }
            }
        } catch (Exception ex) {
            logger.error("There was an error loading the language json file!", ex);
        }
    }

    public void tryPullingLanguageOnline(Language language) {
        logger.info("Attempting to pull updated language files from online.");
        SkyblockAddons.runAsync(() -> {
            try {
                URL url = new URL(String.format(main.getOnlineData().getLanguageJSONFormat(), language.getPath()));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", Utils.USER_AGENT);

                logger.info("Got response code " + connection.getResponseCode());

                JsonObject onlineMessages = SkyblockAddons.getGson().fromJson(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8), JsonObject.class);
                connection.disconnect();

                overwriteCommonJsonMembers(main.getConfigValues().getLanguageConfig(), onlineMessages);
            } catch (JsonParseException | IllegalStateException | IOException ex) {
                logger.error("There was an error loading the language file online");
                logger.catching(ex);
            }
        });
    }

    public static String getTranslatedString(String parentPath, String value) {
        String text;
        try {
            SkyblockAddons main = SkyblockAddons.getInstance();
            List<String> path = new LinkedList<>(Arrays.asList((parentPath).split(Pattern.quote("."))));
            JsonObject jsonObject = main.getConfigValues().getLanguageConfig();
            for (String part : path) {
                if (!part.equals("")) {
                    jsonObject = jsonObject.getAsJsonObject(part);
                }
            }
            text = jsonObject.get(value).getAsString();
            if (text != null && (main.getConfigValues().getLanguage() == Language.HEBREW || main.getConfigValues().getLanguage() == Language.ARABIC) && !Minecraft.getMinecraft().fontRendererObj.getBidiFlag()) {
                text = bidiReorder(text);
            }
        } catch (NullPointerException ex) {
            text = value; // In case of fire...
        }
        return text;
    }

    private static String bidiReorder(String text) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(ArabicShaping.LETTERS_SHAPE)).shape(text), Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
            bidi.setReorderingMode(Bidi.REORDER_DEFAULT);
            return bidi.writeReordered(Bidi.DO_MIRRORING);
        } catch (ArabicShapingException var3) {
            return text;
        }
    }

    /**
     * This is used to merge in the online language entries into the existing ones.
     * Using this method rather than an overwrite allows new entries in development to still exist.
     *
     * @param otherObject The object to be merged (online entries).
     * @param baseObject   The object to me merged in to (local entries).
     */
    private void overwriteCommonJsonMembers(JsonObject baseObject, JsonObject otherObject) {
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

    public boolean isAxe(Item item) {
        return item instanceof ItemAxe;
    }

    private boolean depthEnabled;
    private boolean blendEnabled;
    private boolean alphaEnabled;
    private int blendFunctionSrcFactor;
    private int blendFunctionDstFactor;

    public void enableStandardGLOptions() {
        depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        alphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        blendFunctionSrcFactor = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        blendFunctionDstFactor = GL11.glGetInteger(GL11.GL_BLEND_DST);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlpha();
        GlStateManager.color(1, 1, 1, 1);
    }

    public void restoreGLOptions() {
        if (depthEnabled) {
            GlStateManager.enableDepth();
        }
        if (!alphaEnabled) {
            GlStateManager.disableAlpha();
        }
        if (!blendEnabled) {
            GlStateManager.disableBlend();
        }
        GlStateManager.blendFunc(blendFunctionSrcFactor, blendFunctionDstFactor);
    }

    public boolean isModLoaded(String modId) {
        return isModLoaded(modId, null);
    }

    /**
     * Check if another mod is loaded.
     *
     * @param modId   The modid to check.
     * @param version The version of the mod to match (optional).
     */
    public boolean isModLoaded(String modId, String version) {
        boolean isLoaded = Loader.isModLoaded(modId); // Check for the modid...

        if (isLoaded && version != null) { // Check for the specific version...
            for (ModContainer modContainer : Loader.instance().getModList()) {
                if (modContainer.getModId().equals(modId) && modContainer.getVersion().equals(version)) {
                    return true;
                }
            }

            return false;
        }

        return isLoaded;
    }

    public static WorldClient getDummyWorld() {
        return DUMMY_WORLD;
    }

    public float[] getCurrentGLTransformations() {
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf);
        buf.rewind();
        Matrix4f mat = new Matrix4f();
        mat.load(buf);

        float x = mat.m30;
        float y = mat.m31;
        float z = mat.m32;

        float scale = (float) Math.sqrt(mat.m00 * mat.m00 + mat.m01 * mat.m01 + mat.m02 * mat.m02);

        return new float[]{x, y, z, scale};
    }

    public static EntityPlayer getPlayerFromName(String name) {
        return Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);
    }

    public boolean isEmptyGlassPane(ItemStack itemStack) {
        return itemStack != null && (itemStack.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)
                || itemStack.getItem() == Item.getItemFromBlock(Blocks.glass_pane)) && itemStack.hasDisplayName() && TextUtils.stripColor(itemStack.getDisplayName().trim()).isEmpty();
    }

    public boolean isGlassPaneColor(ItemStack itemStack, EnumDyeColor color) {
        return itemStack != null && itemStack.getMetadata() == color.getMetadata();
    }

    public static float getPartialTicks() {
        return Minecraft.getMinecraft().timer.renderPartialTicks;
    }

    public static long getCurrentTick() {
        return SkyblockAddons.getInstance().getNewScheduler().getTotalTicks();
    }

    private static final Vector3d interpolatedPlayerPosition = new Vector3d();
    private static long lastTick;
    private static float lastPartialTicks;

    public static Vector3d getPlayerViewPosition() {
        long currentTick = getCurrentTick();
        float currentPartialTicks = getPartialTicks();

        if (currentTick != lastTick || currentPartialTicks != lastPartialTicks) {
            Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
            interpolatedPlayerPosition.x = MathUtils.interpolateX(renderViewEntity, currentPartialTicks);
            interpolatedPlayerPosition.y = MathUtils.interpolateY(renderViewEntity, currentPartialTicks);
            interpolatedPlayerPosition.z = MathUtils.interpolateZ(renderViewEntity, currentPartialTicks);

            lastTick = currentTick;
            lastPartialTicks = currentPartialTicks;
        }

        return interpolatedPlayerPosition;
    }

    public static byte[] toByteArray(BufferedInputStream inputStream) throws IOException {
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(inputStream);
        } finally {
            inputStream.close();
        }
        return bytes;
    }

    public static Entity getEntityByUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        for (Entity entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid)) {
                return entity;
            }
        }

        return null;
    }
}
