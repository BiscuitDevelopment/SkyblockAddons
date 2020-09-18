package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.events.SkyblockJoinedEvent;
import codes.biscuit.skyblockaddons.events.SkyblockLeftEvent;
import codes.biscuit.skyblockaddons.features.backpacks.Backpack;
import codes.biscuit.skyblockaddons.features.itemdrops.ItemDropChecker;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter @Setter
public class Utils {

    /** Added to the beginning of messages. */
    private static final String MESSAGE_PREFIX =
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

    private static final Pattern SERVER_REGEX = Pattern.compile("([0-9]{2}/[0-9]{2}/[0-9]{2}) (mini[0-9]{1,3}[A-Za-z])");
    private static final Pattern PURSE_REGEX = Pattern.compile("(?:Purse|Piggy): (?<coins>[0-9.]*)(?: .*)?");
    private static final Pattern SLAYER_PROGRESS_REGEX = Pattern.compile("(?<progress>[0-9.k]*)/(?<total>[0-9.k]*) (?:Kills|Combat XP)$");

    /** In English, Chinese Simplified, Traditional Chinese. */
    private static final Set<String> SKYBLOCK_IN_ALL_LANGUAGES = Sets.newHashSet("SKYBLOCK","\u7A7A\u5C9B\u751F\u5B58", "\u7A7A\u5CF6\u751F\u5B58");

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

    // Featured link
    private boolean lookedOnline;
    private URI featuredLink;

    private long lastDamaged = -1;

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

    public void checkGameLocationDate() {
        boolean foundScoreboard = false;

        boolean foundLocation = false;
        boolean foundJerryWave = false;
        boolean foundAlphaIP = false;
        boolean foundInDungeon = false;
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
                    String strippedLine = TextUtils.keepScoreboardCharacters(TextUtils.stripColor(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, line.getPlayerName()))).trim();

                    if (strippedLine.endsWith("am") || strippedLine.endsWith("pm")) {
                        timeString = strippedLine;
                    }

                    if (strippedLine.endsWith("st") || strippedLine.endsWith("nd") || strippedLine.endsWith("rd") || strippedLine.endsWith("th")) {
                        dateString = strippedLine;
                    }

                    Matcher matcher = PURSE_REGEX.matcher(strippedLine);
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

                    if (strippedLine.contains("mini")) {
                        matcher = SERVER_REGEX.matcher(strippedLine);
                        if (matcher.matches()) {
                            serverID = matcher.group(2);
                        }
                    }

                    if (strippedLine.endsWith("Combat XP") || strippedLine.endsWith("Kills")) {
                        parseSlayerProgress(strippedLine);
                    }

                    if (!foundLocation) {
                        for (Location loopLocation : Location.values()) {
                            if (strippedLine.endsWith(loopLocation.getScoreboardName())) {
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
                        if (strippedLine.startsWith("Wave")) {
                            foundJerryWave = true;

                            int newJerryWave;
                            try {
                                newJerryWave = Integer.parseInt(TextUtils.keepIntegerCharactersOnly(strippedLine));
                            } catch (NumberFormatException ignored) {
                                newJerryWave = 0;
                            }
                            if (jerryWave != newJerryWave) {
                                jerryWave = newJerryWave;
                            }
                        }
                    }

                    if (!foundAlphaIP && strippedLine.contains("alpha.hypixel.net")) {
                        foundAlphaIP = true;
                        alpha = true;
                        profileName = "Alpha";
                    }

                    if (!foundInDungeon && strippedLine.contains("Dungeon Cleared: ")) {
                        foundInDungeon = true;
                        inDungeon = true;
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
                JsonObject responseJson = new Gson().fromJson(response.toString(), JsonObject.class);
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

    public boolean isMaterialForRecipe(ItemStack itemStack) {
        List<String> lore = ItemUtils.getItemLore(itemStack);
        for (String loreLine : lore) {
            if ("Right-click to view recipes!".equals(TextUtils.stripColor(loreLine))) {
                return true;
            }
        }
        return false;
    }

    public String getReforgeFromItem(ItemStack item) {
        if (item.hasTagCompound()) {
            NBTTagCompound extraAttributes = item.getTagCompound();
            if (extraAttributes.hasKey("ExtraAttributes")) {
                extraAttributes = extraAttributes.getCompoundTag("ExtraAttributes");
                if (extraAttributes.hasKey("modifier")) {
                    String reforge = WordUtils.capitalizeFully(extraAttributes.getString("modifier"));

                    reforge = reforge.replace("_sword", ""); //fixes reforges like "Odd_sword"
                    reforge = reforge.replace("_bow", "");

                    return reforge;
                }
            }
        }
        return null;
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

    /**
     * Checks if the given item is a pickaxe.
     *
     * @param item the item to check
     * @return {@code true} if this item is a pickaxe, {@code false} otherwise
     */
    public boolean isPickaxe(Item item) {
        return Items.wooden_pickaxe.equals(item) || Items.stone_pickaxe.equals(item) || Items.golden_pickaxe.equals(item) || Items.iron_pickaxe.equals(item) || Items.diamond_pickaxe.equals(item);
    }

    public void drawTextWithStyle(String text, float x, float y, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);

        if (main.getConfigValues().getTextStyle() == EnumUtils.TextStyle.STYLE_TWO) {
            int colorAlpha = Math.max(getAlpha(color), 4);
            int colorBlack = new Color(0, 0, 0, colorAlpha/255F).getRGB();
            String strippedText = TextUtils.stripColor(text);
            Minecraft.getMinecraft().fontRendererObj.drawString(strippedText,1, 0, colorBlack, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(strippedText, -1, 0, colorBlack, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(strippedText, 0, 1, colorBlack, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(strippedText, 0, -1, colorBlack, false);
            Minecraft.getMinecraft().fontRendererObj.drawString(text, 0, 0, color, false);
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString(text, 0, 0, color, true);
        }

        GlStateManager.popMatrix();
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

    /**
     * Draws a solid color rectangle with the specified coordinates and color (ARGB format). Args: x1, y1, x2, y2, color
     */
    public void drawRect(double left, double top, double right, double bottom, int color) {
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

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void loadLanguageFile(boolean pullOnline) {
        loadLanguageFile(main.getConfigValues().getLanguage());
        if (pullOnline) main.getUtils().tryPullingLanguageOnline(main.getConfigValues().getLanguage()); // Try getting an updated version online after loading the local one.
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
                JsonObject onlineMessages = new Gson().fromJson(response.toString(), JsonObject.class);
                mergeLanguageJsonObject(onlineMessages, main.getConfigValues().getLanguageConfig());
            } catch (JsonParseException | IllegalStateException | IOException ex) {
                ex.printStackTrace();
                System.out.println("SkyblockAddons: There was an error loading the language file online");
            }
        }).start();
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

    public BufferedReader getBufferedReader(String localPath) {
        try {
            return new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(localPath)));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void pullOnlineData() {
        logger.info("Attempting to grab data from online.");
        new Thread(() -> {
            try {
                boolean isCurrentBeta = SkyblockAddons.VERSION.contains("b");
                URL url = new URL("https://raw.githubusercontent.com/BiscuitDevelopment/SkyblockAddons/"+(isCurrentBeta ? "development" : "master")+"/src/main/resources/data.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", Utils.USER_AGENT);

                logger.info("Online data - Got response code " + connection.getResponseCode());

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }
                connection.disconnect();

                main.setOnlineData(new Gson().fromJson(response.toString(), OnlineData.class));
                logger.info("Successfully grabbed online data.");

                main.getUpdater().processUpdateCheckResult();
            } catch (Exception ex) {
                logger.warn("There was an error while trying to pull the online data...");
                logger.catching(ex);
            }
        }).start();
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

    public void drawCenteredString(String text, float x, float y, int color) {
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x - Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2F, y, color, true);
    }

    public Location getLocation() {
        if (inDungeon) {
            return Location.DUNGEON;
        }

        return location;
    }
}
