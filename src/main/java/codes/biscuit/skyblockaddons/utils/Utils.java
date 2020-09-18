package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.events.SkyblockJoinedEvent;
import codes.biscuit.skyblockaddons.events.SkyblockLeftEvent;
import codes.biscuit.skyblockaddons.features.backpacks.Backpack;
import codes.biscuit.skyblockaddons.features.dungeonmap.MapMarker;
import codes.biscuit.skyblockaddons.features.itemdrops.ItemDropChecker;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.misc.ChromaManager;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.*;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter @Setter
public class Utils {

    public static final Gson GSON = new Gson();

    /** Added to the beginning of messages. */
    public static final String MESSAGE_PREFIX =
            ColorCode.GRAY + "[" + ColorCode.AQUA + SkyblockAddons.MOD_NAME + ColorCode.GRAY + "] ";

    /** Enchantments listed by how good they are. May or may not be subjective lol. */
    private static final List<String> ORDERED_ENCHANTMENTS = Collections.unmodifiableList(Arrays.asList(
            "smite","bane of arthropods","knockback","fire aspect","venomous", // Sword Bad
            "thorns","growth","protection","depth strider","respiration","aqua affinity", // Armor
            "lure","caster","luck of the sea","blessing","angler","frail","magnet","spiked hook", // Fishing
            "dragon hunter","power","snipe","piercing","aiming","infinite quiver", // Bow Main
            "sharpness","critical","first strike","giant killer","execute","lethality","ender slayer","cubism","impaling", // Sword Damage
            "vampirism","life steal","looting","luck","scavenger","experience","cleave","thunderlord", // Sword Others
            "punch","flame", // Bow Others
            "telekinesis"
    ));

    private static final Pattern SERVER_REGEX = Pattern.compile("(?<serverType>[Mm])(?<serverCode>[0-9]+[A-Z])$");
    private static final Pattern PURSE_REGEX = Pattern.compile("(?:Purse|Piggy): (?<coins>[0-9.]*)(?: .*)?");
    private static final Pattern SLAYER_TYPE_REGEX = Pattern.compile("(?<type>Tarantula Broodfather|Revenant Horror|Sven Packmaster) (?<level>[IV]+)");
    private static final Pattern SLAYER_PROGRESS_REGEX = Pattern.compile("(?<progress>[0-9.k]*)/(?<total>[0-9.k]*) (?:Kills|Combat XP)$");

    /** In English, Chinese Simplified, Traditional Chinese. */
    private static final Set<String> SKYBLOCK_IN_ALL_LANGUAGES = Sets.newHashSet("SKYBLOCK","\u7A7A\u5C9B\u751F\u5B58", "\u7A7A\u5CF6\u751F\u5B58");

    private static final WorldClient DUMMY_WORLD = new WorldClient(null, new WorldSettings(0L, WorldSettings.GameType.SURVIVAL,
            false, false, WorldType.DEFAULT), 0, null, null);

    /** Used for web requests. */
    public static final String USER_AGENT = "SkyblockAddons/" + SkyblockAddons.VERSION;

    // I know this is messy af, but frustration led me to take this dark path - said someone not biscuit
    public static boolean blockNextClick;

    /** Get a player's attributes. This includes health, mana, and defence. */
    private Map<Attribute, MutableInt> attributes = new EnumMap<>(Attribute.class);

    /** This is the item checker that makes sure items being dropped or sold are allowed to be dropped or sold. */
    private final ItemDropChecker itemDropChecker = new ItemDropChecker();

    /** List of enchantments that the player is looking to find. */
    private List<String> enchantmentMatches = new LinkedList<>();

    /** List of enchantment substrings that the player doesn't want to match. */
    private List<String> enchantmentExclusions = new LinkedList<>();

    private Backpack backpackToPreview;

    /** Whether the player is on skyblock. */
    private boolean onSkyblock;

    /** The player's current location in Skyblock */
    private Location location = Location.UNKNOWN;

    /** The skyblock profile that the player is currently on. Ex. "Grapefruit" */
    private String profileName = "Unknown";

    /** Whether or not a loud sound is being played by the mod. */
    private boolean playingSound;

    /** The current serverID that the player is on. */
    private String serverID = "";
    private int lastHoveredSlot = -1;

    /** Whether the player is using the old style of bars packaged into Imperial's Skyblock Pack. */
    private boolean usingOldSkyBlockTexture;

    /** Whether the player is using the default bars packaged into the mod. */
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
    private Logger logger = SkyblockAddons.getInstance().getLogger();

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
        final Pattern SERVER_BRAND_PATTERN = Pattern.compile("(.+) <- (?:.+)");
        final String HYPIXEL_SERVER_BRAND = "BungeeCord (Hypixel)";

        Minecraft mc = Minecraft.getMinecraft();

        if (!mc.isSingleplayer() && mc.thePlayer.getClientBrand() != null) {
            Matcher matcher = SERVER_BRAND_PATTERN.matcher(mc.thePlayer.getClientBrand());

            if (matcher.find()) {
                // Group 1 is the server brand.
                return matcher.group(1).equals(HYPIXEL_SERVER_BRAND);
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    private long lastFoundScoreboard = -1;

    public void parseSidebar() {
        boolean foundScoreboard = false;

        boolean foundLocation = false;
        boolean foundJerryWave = false;
        boolean foundAlphaIP = false;
        boolean foundInDungeon = false;
        boolean foundSlayerQuest = false;
        boolean foundBossAlive = false;
        boolean foundSkyblockTitle = false;
        Minecraft mc = Minecraft.getMinecraft();

        if (mc != null && mc.theWorld != null && !mc.isSingleplayer() && isOnHypixel()) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective sidebarObjective = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);

            if (sidebarObjective != null) {
                foundScoreboard = true;
                lastFoundScoreboard = System.currentTimeMillis();

                String objectiveName = TextUtils.stripColor(sidebarObjective.getDisplayName());

                for (String skyblock : SKYBLOCK_IN_ALL_LANGUAGES) {
                    if (objectiveName.startsWith(skyblock)) {
                        foundSkyblockTitle = true;
                        break;
                    }
                }

                // If it's a Skyblock scoreboard and the player has not joined Skyblock yet,
                // this indicates that he did so.
                if (foundSkyblockTitle && !this.isOnSkyblock()) {
                    MinecraftForge.EVENT_BUS.post(new SkyblockJoinedEvent());
                }

                Collection<Score> scoreboardLines = scoreboard.getSortedScores(sidebarObjective);
                List<Score> list = scoreboardLines.stream().filter(p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")).collect(Collectors.toList());
                if (list.size() > 15) {
                    scoreboardLines = Lists.newArrayList(Iterables.skip(list, scoreboardLines.size() - 15));
                } else {
                    scoreboardLines = list;
                }

                String timeString = null;
                String dateString = null;

                for (Score line : scoreboardLines) {

                    ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(line.getPlayerName());
                    String strippedUnformatted = TextUtils.keepScoreboardCharacters(TextUtils.stripColor(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, line.getPlayerName()))).trim();
                    String strippedColored = TextUtils.keepScoreboardCharacters(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, line.getPlayerName())).trim();

                    if (strippedUnformatted.endsWith("am") || strippedUnformatted.endsWith("pm")) {
                        timeString = strippedUnformatted;
                    }

                    if (strippedUnformatted.endsWith("st") || strippedUnformatted.endsWith("nd") || strippedUnformatted.endsWith("rd") || strippedUnformatted.endsWith("th")) {
                        dateString = strippedUnformatted;
                    }

                    Matcher matcher = PURSE_REGEX.matcher(strippedUnformatted);
                    if (matcher.matches()) {
                        try {
                            double oldCoins = purse;
                            purse = Double.parseDouble(matcher.group("coins"));

                            if (oldCoins != purse) {
                                onCoinsChange(purse-oldCoins);
                            }
                        } catch(NumberFormatException ignored) {
                            purse = 0;
                        }
                    }

                    if ((matcher = SERVER_REGEX.matcher(strippedUnformatted)).find()) {
                        String serverType = matcher.group("serverType");
                        if (serverType.equals("m")) {
                            serverID = "mini";
                        } else if (serverType.equals("M")) {
                            serverID = "mega";
                        }
                        serverID += matcher.group("serverCode");
                    }

                    if (strippedUnformatted.endsWith("Combat XP") || strippedUnformatted.endsWith("Kills")) {
                        parseSlayerProgress(strippedUnformatted);
                    }

                    if (!foundLocation) {
                        for (Location loopLocation : Location.values()) {
                            if (strippedUnformatted.endsWith(loopLocation.getScoreboardName())) {
                                if (loopLocation == Location.BLAZING_FORTRESS && location != Location.BLAZING_FORTRESS) {
                                    sendInventiveTalentPingRequest(EnumUtils.MagmaEvent.PING); // going into blazing fortress
                                    fetchMagmaBossEstimate();
                                }

                                if (location != loopLocation) {
                                    location = loopLocation;
                                }

                                foundLocation = true;
                                break;
                            }
                        }
                    }

                    if (!foundJerryWave && (location == Location.JERRYS_WORKSHOP || location == Location.JERRY_POND)) {
                        if (strippedUnformatted.startsWith("Wave")) {
                            foundJerryWave = true;

                            int newJerryWave;
                            try {
                                newJerryWave = Integer.parseInt(TextUtils.keepIntegerCharactersOnly(strippedUnformatted));
                            } catch (NumberFormatException ignored) {
                                newJerryWave = 0;
                            }
                            if (jerryWave != newJerryWave) {
                                jerryWave = newJerryWave;
                            }
                        }
                    }

                    if (!foundAlphaIP && strippedUnformatted.contains("alpha.hypixel.net")) {
                        foundAlphaIP = true;
                        alpha = true;
                        profileName = "Alpha";
                    }

                    if (!foundInDungeon && strippedUnformatted.contains("Dungeon Cleared: ")) {
                        foundInDungeon = true;
                        inDungeon = true;

                        String lastServer = main.getDungeonUtils().getLastServerId();
                        if (lastServer != null && !lastServer.equals(serverID)) {
                            main.getDungeonUtils().reset();
                        }
                        main.getDungeonUtils().setLastServerId(serverID);
                    }

                    matcher = SLAYER_TYPE_REGEX.matcher(strippedUnformatted);
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

                    if (strippedUnformatted.equals("Slay the boss!")) {
                        foundBossAlive = true;
                        slayerBossAlive = true;
                    }

                    Map<String, DungeonPlayer> dungeonPlayers = main.getDungeonUtils().getPlayers();
                    if (inDungeon) {
                        DungeonPlayer dungeonPlayer = DungeonPlayer.fromScoreboardLine(strippedColored);
                        if (dungeonPlayer != null) {
                            if (dungeonPlayers.containsKey(dungeonPlayer.getName())) {
                                dungeonPlayers.get(dungeonPlayer.getName()).updateStatsFromOther(dungeonPlayer);
                            } else {
                                dungeonPlayers.put(dungeonPlayer.getName(), dungeonPlayer);
                            }
                        }
                    } else {
                        dungeonPlayers.clear();
                    }
                }
                currentDate = SkyblockDate.parse(dateString, timeString);
            }
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
            if (foundScoreboard || System.currentTimeMillis() - lastFoundScoreboard > 10000) {
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

            float completion = progress/total;

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
        Minecraft.getMinecraft().thePlayer.playSound(sound, (float)volume, (float) pitch);
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
        new Thread(() -> {
            final boolean magmaTimerEnabled = main.getConfigValues().isEnabled(Feature.MAGMA_BOSS_TIMER);
            if(!magmaTimerEnabled) {
                logger.info("Getting magma boss spawn estimate from server...");
            }
            try {
                URL url = new URL("https://hypixel-api.inventivetalent.org/api/skyblock/bosstimer/magma/estimatedSpawn");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);

                if(!magmaTimerEnabled) {
                    logger.info("Got response code " + connection.getResponseCode());
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }
                connection.disconnect();
                JsonObject responseJson = GSON.fromJson(response.toString(), JsonObject.class);
                long estimate = responseJson.get("estimate").getAsLong();
                long currentTime = responseJson.get("queryTime").getAsLong();
                int magmaSpawnTime = (int)((estimate-currentTime)/1000);

                if(!magmaTimerEnabled) {
                    logger.info("Query time was " + currentTime + ", server time estimate is " +
                            estimate + ". Updating magma boss spawn to be in " + magmaSpawnTime + " seconds.");
                }

                main.getPlayerListener().setMagmaTime(magmaSpawnTime);
                main.getPlayerListener().setMagmaAccuracy(EnumUtils.MagmaTimerAccuracy.ABOUT);
            } catch (IOException ex) {
                if(!magmaTimerEnabled) {
                    logger.warn("Failed to get magma boss spawn estimate from server");
                }
            }
        }).start();
    }

    public void sendInventiveTalentPingRequest(EnumUtils.MagmaEvent event) {
        new Thread(() -> {
            final boolean magmaTimerEnabled = main.getConfigValues().isEnabled(Feature.MAGMA_BOSS_TIMER);
            if(!magmaTimerEnabled) {
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

                    if(!magmaTimerEnabled) {
                        logger.info("Got response code " + connection.getResponseCode());
                    }
                    connection.disconnect();
                }
            } catch (IOException ex) {
                if(!magmaTimerEnabled) {
                    logger.warn("Failed to post event to server");
                }
            }
        }).start();
    }

    /**
     * Returns the folder that SkyblockAddons is located in.
     *
     * @return the folder the SkyblockAddons jar is located in
     */
    public File getSBAFolder() {
        return Loader.instance().activeModContainer().getSource().getParentFile();
    }

    public int getNBTInteger(ItemStack item, String... path) {
        if (item != null && item.hasTagCompound()) {
            NBTTagCompound tag = item.getTagCompound();
            for (String tagName : path) {
                if (path[path.length-1].equals(tagName)) continue;
                if (tag.hasKey(tagName)) {
                    tag = tag.getCompoundTag(tagName);
                } else {
                    return -1;
                }
            }
            return tag.getInteger(path[path.length-1]);
        }
        return -1;
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

    public void drawTextWithStyle(String text, float x, float y, int color) {
        if (main.getConfigValues().getTextStyle() == EnumUtils.TextStyle.STYLE_TWO) {
            int colorAlpha = Math.max(getAlpha(color), 4);
            int colorBlack = new Color(0, 0, 0, colorAlpha/255F).getRGB();
            String strippedText = TextUtils.stripColor(text);
            Minecraft.getMinecraft().fontRendererObj.drawString(strippedText,x+1, y+0, colorBlack, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(strippedText, x+-1, y+0, colorBlack, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(strippedText, x+0, y+1, colorBlack, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(strippedText, x+0, y+-1, colorBlack, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(text, x+0, y+0, color, false);
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString(text, x+0, y+0, color, true);
        }
    }

    public int getDefaultBlue(int alpha) {
        return new Color(160, 225, 229, alpha).getRGB();
    }

    public void reorderEnchantmentList(List<String> enchantments) {
        SortedMap<Integer, String> orderedEnchants = new TreeMap<>();
        for (int i = 0; i < enchantments.size(); i++) {
            int nameEnd = enchantments.get(i).lastIndexOf(' ');
            if (nameEnd < 0) nameEnd = enchantments.get(i).length();

            int key = ORDERED_ENCHANTMENTS.indexOf(enchantments.get(i).substring(0, nameEnd).toLowerCase(Locale.US));
            if (key < 0) key = 100 + i;
            orderedEnchants.put(key, enchantments.get(i));
        }

        enchantments.clear();
        enchantments.addAll(orderedEnchants.values());
    }

    public int getAlpha(int color) {
        return (color >> 24 & 255);
    }

    public float normalizeValueNoStep(float value, float min, float max) {
        return MathHelper.clamp_float((snapNearDefaultValue(value) - min) / (max - min), 0.0F, 1.0F);
    }

    public float snapNearDefaultValue(float value) {
        if (value != 1 && value > 1-0.05 && value < 1+0.05) {
            return 1;
        }

        return value;
    }

    public float denormalizeScale(float value, float min, float max, float step) {
        return snapToStepClamp(min + (max - min) *
                MathHelper.clamp_float(value, 0.0F, 1.0F), min, max, step);
    }

    private float snapToStepClamp(float value, float min, float max, float step) {
        value = step * (float) Math.round(value / step);
        return MathHelper.clamp_float(value, min, max);
    }

    public void bindRGBColor(int color) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = (float) (color >> 24 & 255) / 255.0F;

        GlStateManager.color(r, g, b, a);
    }

    public void bindColorInts(int r, int g, int b, int a) {
        GlStateManager.color(r/255F, g/255F, b/255F, a/255F);
}

    public String[] wrapSplitText(String text, int wrapLength) {
        return WordUtils.wrap(text, wrapLength).replace("\r", "").split(Pattern.quote("\n"));
    }

    public boolean itemIsInHotbar(ItemStack itemStack) {
        ItemStack[] inventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory;

        for (int slot = 0; slot < 9; slot ++) {
            if (inventory[slot] == itemStack) {
                return true;
            }
        }
        return false;
    }

    public int getColorWithAlpha(int color, int alpha) {
        return color + ((alpha << 24) & 0xFF000000);
    }

    public void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight) {
        drawModalRectWithCustomSizedTexture(x, y, u, v, width, height, textureWidth, textureHeight, false);
    }

    /**
     * Draws a textured rectangle at z = 0. Args: x, y, u, v, width, height, textureWidth, textureHeight
     */
    public void drawModalRectWithCustomSizedTexture(float x, float y, float u, float v, float width, float height, float textureWidth, float textureHeight, boolean linearTexture) {
        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + width) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();

        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    public void drawRect(double left, double top, double right, double bottom, int color) {
        drawRect(left, top, right, bottom, color, false);
    }

    /**
     * Draws a solid color rectangle with the specified coordinates and color (ARGB format). Args: x1, y1, x2, y2, color
     */
    public void drawRect(double left, double top, double right, double bottom, int color, boolean chroma) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        if (!chroma) {
            float f3 = (float) (color >> 24 & 255) / 255.0F;
            float f = (float) (color >> 16 & 255) / 255.0F;
            float f1 = (float) (color >> 8 & 255) / 255.0F;
            float f2 = (float) (color & 255) / 255.0F;
            GlStateManager.color(f, f1, f2, f3);
        }
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (chroma) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            posChromaColor(worldrenderer, left, bottom);
            posChromaColor(worldrenderer, right, bottom);
            posChromaColor(worldrenderer, right, top);
            posChromaColor(worldrenderer, left, top);
            tessellator.draw();
        } else {
            worldrenderer.begin(7, DefaultVertexFormats.POSITION);
            worldrenderer.pos(left, bottom, 0.0D).endVertex();
            worldrenderer.pos(right, bottom, 0.0D).endVertex();
            worldrenderer.pos(right, top, 0.0D).endVertex();
            worldrenderer.pos(left, top, 0.0D).endVertex();
            tessellator.draw();
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void posChromaColor(WorldRenderer worldRenderer, double x, double y) {
        int color = ChromaManager.getChromaColor((float) x, (float) y);
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color & 255) / 255.0F;
        float f2 = (float) (color >> 8 & 255) / 255.0F;
        worldRenderer.pos(x, y, 0.0D).color(f, f1, f2, f3).endVertex();
    }

    /**
     * Draws a solid color rectangle with the specified coordinates and color (ARGB format). Args: x1, y1, x2, y2, color
     */
    public void drawRectOutline(float x, float y, int w, int h, int thickness, int color, boolean chroma) {
        drawSegmentedLineVertical(x-thickness, y, thickness, h, color, chroma);
        drawSegmentedLineHorizontal(x-thickness, y-thickness, w+thickness*2, thickness, color, chroma);
        drawSegmentedLineVertical(x+w, y, thickness, h, color, chroma);
        drawSegmentedLineHorizontal(x-thickness, y+h, w+thickness*2, thickness, color, chroma);
    }

    public void drawSegmentedLineHorizontal(float x, float y, float w, float h, int color, boolean chroma) {
        int segments = (int) (w / 10);
        float length = w / segments;

        for (int segment = 0; segment < segments; segment++) {
            float start = x + length * segment;
            drawRect(start, y, start + length, y+h, color, chroma);
        }
    }

    public void drawSegmentedLineVertical(float x, float y, float w, float h, int color, boolean chroma) {
        int segments = (int) (h / 10);
        float length = h / segments;

        for (int segment = 0; segment < segments; segment++) {
            float start = y + length * segment;
            drawRect(x, start, x+w, start + length, color, chroma);
        }
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
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                String dataString = result.toString("UTF-8");
                main.getConfigValues().setLanguageConfig(new JsonParser().parse(dataString).getAsJsonObject());
                fileStream.close();
            }
        } catch (JsonParseException | IllegalStateException | IOException ex) {
            ex.printStackTrace();
            System.out.println("SkyblockAddons: There was an error loading the language file.");
        }
    }

    public void tryPullingLanguageOnline(Language language) {
        logger.info("Attempting to pull updated language files from online.");
        new Thread(() -> {
            try {
                URL url = new URL(String.format(main.getOnlineData().getLanguageJSONFormat(), language.getPath()));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", Utils.USER_AGENT);

                logger.info("Got response code " + connection.getResponseCode());

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }
                connection.disconnect();
                JsonObject onlineMessages = GSON.fromJson(response.toString(), JsonObject.class);
                mergeLanguageJsonObject(onlineMessages, main.getConfigValues().getLanguageConfig());
            } catch (JsonParseException | IllegalStateException | IOException ex) {
                ex.printStackTrace();
                System.out.println("SkyblockAddons: There was an error loading the language file online");
            }
        }).start();
    }

    public static String getTranslatedString(String parentPath, String value)
    {
        String text;
        try {
            SkyblockAddons main = SkyblockAddons.getInstance();
            List<String> path = new LinkedList<String>(Arrays.asList((parentPath).split(Pattern.quote("."))));
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
        return text ;
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
     * @param jsonObject The object to be merged (online entries).
     * @param targetObject The object to me merged in to (local entries).
     */
    private void mergeLanguageJsonObject(JsonObject jsonObject, JsonObject targetObject) {
        for (Map.Entry<String, JsonElement> entry : targetObject.entrySet()) {
            String memberName = entry.getKey();
            JsonElement value = entry.getValue();
            if (jsonObject.has(memberName)) {
                if (value instanceof JsonObject) {
                    mergeLanguageJsonObject(jsonObject.getAsJsonObject(memberName), (JsonObject)value);
                } else {
                    targetObject.add(memberName, value);
                }
            }
        }
    }

    private Set<ResourceLocation> rescaling = new HashSet<>();
    private Map<ResourceLocation, Object> rescaled = new HashMap<>();

    /**
     *
     * Enter a resource location, width, and height and this will
     * rescale that image in a new thread, and return a new dynamic
     * texture.
     *
     * While the image is processing in the other thread, it will
     * return the original image, but *at most* it should take a few
     * seconds.
     *
     * Once the image is processed the result is cached in the map,
     * and will not be re-done. If you need this resource location
     * to be scaled again, set the redo flag to true.
     *
     * @param resourceLocation The original image to scale.
     * @param width The width to scale to.
     * @param height The Height to scale to.
     * @param redo Whether to redo the scaling if it is already complete.
     * @return Either the scaled resource if it is complete, or the original resource if not.
     */
    public ResourceLocation getScaledResource(ResourceLocation resourceLocation, int width, int height, boolean redo) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        if (!redo && rescaled.containsKey(resourceLocation)) {
            Object object = rescaled.get(resourceLocation);
            if (object instanceof ResourceLocation) {
                return (ResourceLocation) object;
            } else if (object instanceof BufferedImage) {
                String name = "sba_scaled_"+resourceLocation.getResourcePath().replace("/", "_").replace(".", "_");
                ResourceLocation scaledResourceLocation = textureManager.getDynamicTextureLocation(name, new DynamicTexture((BufferedImage) object));
                rescaled.put(resourceLocation, scaledResourceLocation);
                return scaledResourceLocation;
            }
        }

        if (rescaling.contains(resourceLocation)) return resourceLocation; // Not done yet.

        if (redo) {
            if (rescaled.containsKey(resourceLocation)) {
                Object removed = rescaled.remove(resourceLocation);
                if (removed instanceof ResourceLocation) {
                    textureManager.deleteTexture((ResourceLocation)removed);
                }
            }
        }

        rescaling.add(resourceLocation);

        new Thread(() -> {
            try {
                BufferedImage originalImage = ImageIO.read(SkyblockAddonsGui.class.getClassLoader().getResourceAsStream("assets/"+resourceLocation.getResourceDomain()+"/"+resourceLocation.getResourcePath()));
                Image scaledImageAbstract = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                Graphics graphics = scaledImage.getGraphics();
                graphics.drawImage(scaledImageAbstract, 0, 0, null);
                graphics.dispose();

                rescaled.put(resourceLocation, scaledImage);
                rescaling.remove(resourceLocation);
            } catch (Exception ex) {
                ex.printStackTrace();
                rescaled.put(resourceLocation, resourceLocation);
                rescaling.remove(resourceLocation);
            }
        }).start();

        return resourceLocation; // Processing has started in another thread, but not done yet.
    }

    public boolean isAxe(Item item) {
        return Items.wooden_axe.equals(item) ||
                Items.stone_axe.equals(item) ||
                Items.golden_axe.equals(item) ||
                Items.iron_axe.equals(item) ||
                Items.diamond_axe.equals(item);
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
     * @param modId The modid to check.
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

    private Map<String, Vec4b> savedMapDecorations = new HashMap<>();

    public void drawMapEdited(MapItemRenderer.Instance instance, boolean isScoreSummary, float zoom) {
        Minecraft mc = Minecraft.getMinecraft();
        int startX = 0;
        int startY = 0;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f = 0.0F;
        GlStateManager.enableTexture2D();
        mc.getTextureManager().bindTexture(instance.location);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(1, 771, 0, 1);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((float)(startX) + f, (float)(startY + 128) - f, -0.009999999776482582D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos((float)(startX + 128) - f, (float)(startY + 128) - f, -0.009999999776482582D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos((float)(startX + 128) - f, (float)(startY) + f, -0.009999999776482582D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos((float)(startX) + f, (float)(startY) + f, -0.009999999776482582D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(MapItemRenderer.mapIcons);
        int decorationCount = 0;

        // We don't need to show any markers...
        if (isScoreSummary) return;

        // Prevent marker flickering...
        if (!instance.mapData.mapDecorations.isEmpty()) {
            savedMapDecorations.clear();
            savedMapDecorations.putAll(instance.mapData.mapDecorations);
        }

        // Don't add markers that we replaced with smooth client side ones...
        Set<String> dontAddMarkerNames = new HashSet<>();

        // Add these markers later because they are the smooth client side ones
        // and should get priority.
        Set<MapMarker> markersToAdd = new LinkedHashSet<>();
        Map<String, DungeonPlayer> dungeonPlayers = main.getDungeonUtils().getPlayers();
        for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            // We only add smooth markers for us & our teammates
            if (!dungeonPlayers.containsKey(entityPlayer.getName()) && mc.thePlayer != entityPlayer) {
                continue;
            }

            MapMarker mapMarker = new MapMarker(entityPlayer);

            // If this player's marker already exists, lets update the saved one instead
            if (dungeonPlayers.containsKey(entityPlayer.getName())) {
                DungeonPlayer dungeonPlayer = dungeonPlayers.get(entityPlayer.getName());
                if (dungeonPlayer.getMapMarker() == null) {
                    dungeonPlayer.setMapMarker(mapMarker);
                } else {
                    mapMarker = dungeonPlayer.getMapMarker();
                }
            }

            // Check if there is a vanilla marker around the same spot as our custom
            // marker. If so, we probably found the corresponding marker for this player.
            int duplicates = 0;
            Map.Entry<String, Vec4b> duplicate = null;
            for (Map.Entry<String, Vec4b> vec4b : savedMapDecorations.entrySet()) {

                if (vec4b.getValue().func_176110_a() == mapMarker.getIconType() &&
                        Math.abs(vec4b.getValue().func_176112_b() - mapMarker.getX()) <= 5 &&
                        Math.abs(vec4b.getValue().func_176113_c() - mapMarker.getZ()) <= 5) {
                    duplicates++;
                    duplicate = vec4b;
                }
            }

            // However, if we find more than one duplicate marker, we can't be
            // certain that this we found the player's corresponding marker.
            if (duplicates == 1) {
                mapMarker.setMapMarkerName(duplicate.getKey());
            }

            // For the ones that we replaced, lets make sure we skip the vanilla ones later.
            if (mapMarker.getMapMarkerName() != null) {
                dontAddMarkerNames.add(mapMarker.getMapMarkerName());
            }
            markersToAdd.add(mapMarker);
        }

        // The final set of markers that will be used....
        Set<MapMarker> allMarkers = new LinkedHashSet<>();

        for (Map.Entry<String, Vec4b> vec4b : savedMapDecorations.entrySet()) {
            // If we replaced this marker with a smooth one OR this is the player's marker, lets skip.
            if (dontAddMarkerNames.contains(vec4b.getKey()) || vec4b.getValue().func_176110_a() == 1) continue;

            // Check if this marker key is linked to a player
            DungeonPlayer foundDungeonPlayer = null;
            boolean linkedToPlayer = false;
            for (DungeonPlayer dungeonPlayer : dungeonPlayers.values()) {
                if (dungeonPlayer.getMapMarker() != null && dungeonPlayer.getMapMarker().getMapMarkerName() != null &&
                        vec4b.getKey().equals(dungeonPlayer.getMapMarker().getMapMarkerName())) {
                    linkedToPlayer = true;
                    foundDungeonPlayer = dungeonPlayer;
                    break;
                }
            }

            // Vec4b
            // a -> Icon Type
            // b -> X
            // c -> Z
            // d -> Icon Direction instance.mapData.mapDecorations.values()

            // If this isn't linked to a player, lets just add the marker normally...
            if (!linkedToPlayer) {
                allMarkers.add(new MapMarker(vec4b.getValue().func_176110_a(), vec4b.getValue().func_176112_b(),
                        vec4b.getValue().func_176113_c(), vec4b.getValue().func_176111_d()));
            } else {
                // This marker is linked to a player, lets update that marker's data to the server's
                MapMarker mapMarker = foundDungeonPlayer.getMapMarker();
                mapMarker.setX(vec4b.getValue().func_176112_b());
                mapMarker.setZ(vec4b.getValue().func_176113_c());
                mapMarker.setRotation(vec4b.getValue().func_176111_d());
                allMarkers.add(mapMarker);
            }
        }
        // Add the smooth markers from before
        allMarkers.addAll(markersToAdd);

        // Sort the markers to ensure we are on top & we use the same ordering as the server.
        LinkedHashSet<MapMarker> sortedMarkers = allMarkers.stream()
                .sorted((first, second) -> {
                    boolean firstIsNull = first.getMapMarkerName() == null;
                    boolean secondIsNull = second.getMapMarkerName() == null;

                    if (first.getIconType() != second.getIconType()) {
                        return Byte.compare(second.getIconType(), first.getIconType());
                    }

                    if (firstIsNull && secondIsNull) {
                        return 0;
                    } else if (firstIsNull) {
                        return 1;
                    } else if (secondIsNull) {
                        return -1;
                    }

                    return second.getMapMarkerName().compareTo(first.getMapMarkerName());
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (MapMarker mapMarker : sortedMarkers) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)startX + mapMarker.getX() / 2.0F + 64.0F, (float)startY + mapMarker.getZ() / 2.0F + 64.0F, -0.02F);
            GlStateManager.rotate((mapMarker.getRotation() * 360) / 16.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(4.0F/zoom, 4.0F/zoom, 3.0F);
            byte iconType = mapMarker.getIconType();
            float f1 = (float)(iconType % 4) / 4.0F;
            float f2 = (float)(iconType / 4) / 4.0F;
            float f3 = (float)(iconType % 4 + 1) / 4.0F;
            float f4 = (float)(iconType / 4 + 1) / 4.0F;

            NetworkPlayerInfo markerNetworkPlayerInfo = null;
            if (main.getConfigValues().isEnabled(Feature.SHOW_PLAYER_HEADS_ON_MAP) && mapMarker.getPlayerName() != null) {
                for (NetworkPlayerInfo networkPlayerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                    if (mapMarker.getPlayerName().equals(networkPlayerInfo.getGameProfile().getName())) {
                        markerNetworkPlayerInfo = networkPlayerInfo;
                        break;
                    }
                }
            }

            if (markerNetworkPlayerInfo != null) {
                GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
                drawRect(-1.2, -1.2, 1.2, 1.2, 0xFF000000);

                GlStateManager.color(1, 1, 1, 1);

                if (main.getConfigValues().isEnabled(Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES) &&
                        dungeonPlayers.containsKey(mapMarker.getPlayerName())) {
                    DungeonPlayer dungeonPlayer = dungeonPlayers.get(mapMarker.getPlayerName());
                    if (dungeonPlayer.isLow()) {
                        GlStateManager.color(1, 1, 0.5F, 1);
                    } else if (dungeonPlayer.isCritical()) {
                        GlStateManager.color(1, 0.5F, 0.5F, 1);
                    }
                }

                mc.getTextureManager().bindTexture(markerNetworkPlayerInfo.getLocationSkin());
                drawScaledCustomSizeModalRect(-1, -1, 8.0F, 8, 8, 8, 2, 2, 64.0F, 64.0F, false);
                if (mapMarker.isWearingHat()) {
                    drawScaledCustomSizeModalRect(-1, -1, 40.0F, 8, 8, 8, 2, 2, 64.0F, 64.0F, false);
                }
            } else {
                GlStateManager.translate(-0.125F, 0.125F, 0.0F);
                mc.getTextureManager().bindTexture(MapItemRenderer.mapIcons);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                float eachDecorationZOffset = -0.001F;
                worldrenderer.pos(-1.0D, 1.0D, (float)decorationCount * eachDecorationZOffset).tex(f1, f2).endVertex();
                worldrenderer.pos(1.0D, 1.0D, (float)decorationCount * eachDecorationZOffset).tex(f3, f2).endVertex();
                worldrenderer.pos(1.0D, -1.0D, (float)decorationCount * eachDecorationZOffset).tex(f3, f4).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, (float)decorationCount * eachDecorationZOffset).tex(f1, f4).endVertex();
                tessellator.draw();
            }
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
            ++decorationCount;
        }
    }

    public void drawCenteredString(String text, float x, float y, int color) {
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x - Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2F, y, color, true);
    }

    public Location getLocation() {
        if (inDungeon) {
            return Location.DUNGEON;
        }

        return location;
    }

    public void drawScaledCustomSizeModalRect(float x, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight, boolean linearTexture) {
        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + uWidth) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();

        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    public void drawCylinder(double x, double y, double z, float radius, float height, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity renderViewEntity = mc.getRenderViewEntity();

        double viewX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double)partialTicks;
        double viewY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double)partialTicks;
        double viewZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double)partialTicks;

        x -= viewX;
        y -= viewY;
        z -= viewZ;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        float currentAngle = 0;
        float angleStep = 0.1F;
        while (currentAngle < 2 * Math.PI) {
            float xOffset = radius * (float) Math.cos(currentAngle);
            float zOffset = radius * (float) Math.sin(currentAngle);
            worldrenderer.pos(x+xOffset, y+height, z+zOffset).endVertex();
            worldrenderer.pos(x+xOffset, y+0, z+zOffset).endVertex();
            currentAngle += angleStep;
        }
        worldrenderer.pos(x+radius, y+height, z).endVertex();
        worldrenderer.pos(x+radius, y+0.0, z).endVertex();
        tessellator.draw();
    }

    public static WorldClient getDummyWorld() {
        return DUMMY_WORLD;
    }

    public static Gson getGson() {
        return GSON;
    }

    public float[] getCurrentGLTransformations() {
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf);
        buf.rewind();
        org.lwjgl.util.vector.Matrix4f mat = new org.lwjgl.util.vector.Matrix4f();
        mat.load(buf);

        float x = mat.m30;
        float y = mat.m31;
        float z = mat.m32;

        float scale = (float) Math.sqrt(mat.m00 * mat.m00 + mat.m01 * mat.m01 + mat.m02 * mat.m02);

        return new float[] {x, y, z, scale};
    }

    public ItemStack createItemStack(Item item, boolean enchanted) {
        return createItemStack(item, 0, null, null, enchanted);
    }

    public ItemStack createItemStack(Item item, String name, String skyblockID, boolean enchanted) {
        return createItemStack(item, 0, name, skyblockID, enchanted);
    }

    public ItemStack createItemStack(Item item, int meta, String name, String skyblockID, boolean enchanted) {
        ItemStack stack = new ItemStack(item, 1, meta);

        if (name != null) {
            stack.setStackDisplayName(name);
        }

        if (enchanted) {
            stack.addEnchantment(Enchantment.protection, 0);
        }

        if (skyblockID != null) {
            setItemStackSkyblockID(stack, skyblockID);
        }

        return stack;
    }

    public ItemStack createSkullItemStack(String name, String skyblockID, String skullID, String textureURL) {
        ItemStack stack = new ItemStack(Items.skull, 1, 3);

        NBTTagCompound texture = new NBTTagCompound();
        texture.setString("Value", TextUtils.encodeSkinTextureURL(textureURL));

        NBTTagList textures = new NBTTagList();
        textures.appendTag(texture);

        NBTTagCompound properties = new NBTTagCompound();
        properties.setTag("textures", textures);

        NBTTagCompound skullOwner = new NBTTagCompound();
        skullOwner.setTag("Properties", properties);

        skullOwner.setString("Id", skullID);

        stack.setTagInfo("SkullOwner", skullOwner);

        if (name != null) {
            stack.setStackDisplayName(name);
        }

        if (skyblockID != null) {
            setItemStackSkyblockID(stack, skyblockID);
        }

        return stack;
    }

    public void setItemStackSkyblockID(ItemStack itemStack, String skyblockID) {
        NBTTagCompound extraAttributes = new NBTTagCompound();
        extraAttributes.setString("id", skyblockID);
        itemStack.setTagInfo("ExtraAttributes", extraAttributes);
    }
}
