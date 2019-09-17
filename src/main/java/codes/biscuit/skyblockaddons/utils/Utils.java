package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '\u00A7' + "[0-9A-FK-OR]");

    private Map<Attribute, MutableInt> attributes = new EnumMap<>(Attribute.class);
    private List<String> enchantmentMatch = new LinkedList<>();
    private List<String> enchantmentExclusion = new LinkedList<>();
    private static final Pattern LETTERS_NUMBERS = Pattern.compile("[^a-z A-Z:0-9/']");
    private static boolean onSkyblock = false;
    private EnumUtils.Location location = null;
    private boolean playingSound = false;
    private boolean copyNBT = false;
    private String serverID = "";
    private SkyblockDate currentDate = new SkyblockDate(SkyblockDate.SkyblockMonth.EARLY_WINTER, 1, 1, 1);
    // english, chinese simplified
    private static Set<String> skyblockInAllLanguages = Sets.newHashSet("SKYBLOCK", "\u7A7A\u5C9B\u751F\u5B58");

    private boolean fadingIn;

    private SkyblockAddons main;

    public Utils(SkyblockAddons main) {
        this.main = main;
        addDefaultStats();
    }

    private void addDefaultStats() {
        for (Attribute attribute : Attribute.values()) {
            attributes.put(attribute, new MutableInt(attribute.getDefaultValue()));
        }
    }

    public void sendMessage(String text) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent(ChatType.SYSTEM, new TextComponentString(text));
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().player.sendMessage(event.getMessage()); // Just for logs
        }
    }

    private void sendMessage(TextComponentString text) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent(ChatType.SYSTEM, text);
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().player.sendMessage(event.getMessage()); // Just for logs
        }
    }

    private static final Pattern SERVER_REGEX = Pattern.compile("([0-9]{2}/[0-9]{2}/[0-9]{2}) (mini[0-9]{1,3}[A-Za-z])");
    private Backpack backpackToRender = null;
    private int lastHoveredSlot = -1;

    public float normalizeValue(float value, float valueMin, float valueMax, float valueStep) {
        return MathHelper.clamp((this.snapToStepClamp(value, valueMin, valueMax, valueStep) - valueMin) / (valueMax - valueMin), 0.0F, 1.0F);
    }

    public float denormalizeValue(float value, float valueMin, float valueMax, float valueStep) {
        return this.snapToStepClamp(valueMin + (valueMax - valueMin) * MathHelper.clamp(value, 0.0F, 1.0F), valueMin, valueMax, valueStep);
    }

    private float snapToStepClamp(float value, float valueMin, float valueMax, float valueStep) {
        value = this.snapToStep(value, valueStep);
        return MathHelper.clamp(value, valueMin, valueMax);
    }

    private float snapToStep(float value, float valueStep) {
        if (valueStep > 0.0F) {
            value = valueStep * (float) Math.round(value / valueStep);
        }

        return value;
    }

    //    private final Pattern LETTERS = Pattern.compile("[^a-z A-Z]");
    private static final Pattern NUMBERS_SLASHES = Pattern.compile("[^0-9 /]");

    public void checkGameLocationDate() {
        boolean foundLocation = false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.world != null) { //TODO make sure this works
            Scoreboard scoreboard = mc.world.getScoreboard();
            ScoreObjective sidebarObjective = mc.world.getScoreboard().getObjectiveInDisplaySlot(1);
            if (sidebarObjective != null) {
                String objectiveName = stripColor(sidebarObjective.getDisplayName());
                onSkyblock = false;
                for (String skyblock : skyblockInAllLanguages) {
                    if (objectiveName.startsWith(skyblock)) {
                        onSkyblock = true;
                    }
                }
                Collection<Score> collection = scoreboard.getSortedScores(sidebarObjective);
                List<Score> list = Lists.newArrayList(collection.stream().filter(p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")).collect(Collectors.toList()));
                if (list.size() > 15) {
                    collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
                } else {
                    collection = list;
                }
                String timeString = null;
                for (Score score1 : collection) {
                    ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(score1.getPlayerName());
                    String locationString = keepLettersAndNumbersOnly(
                            stripColor(ScorePlayerTeam.formatPlayerName(scorePlayerTeam, score1.getPlayerName())));
                    if (locationString.endsWith("am") || locationString.endsWith("pm")) {
                        timeString = locationString.trim();
                        timeString = timeString.substring(0, timeString.length()-2);
                    }
                    for (SkyblockDate.SkyblockMonth month : SkyblockDate.SkyblockMonth.values()) {
                        if (locationString.contains(month.getScoreboardString())) {
                            try {
                                currentDate.setMonth(month);
                                String numberPart = locationString.substring(locationString.lastIndexOf(" ") + 1);
                                int day = Integer.valueOf(getNumbersOnly(numberPart));
                                currentDate.setDay(day);
                                if (timeString != null) {
                                    String[] timeSplit = timeString.split(Pattern.quote(":"));
                                    int hour = Integer.valueOf(timeSplit[0]);
                                    currentDate.setHour(hour);
                                    int minute = Integer.valueOf(timeSplit[1]);
                                    currentDate.setMinute(minute);
                                }
                            } catch (IndexOutOfBoundsException | NumberFormatException ignored) {}
                            break;
                        }
                    }
                    if (locationString.contains("mini")) {
                        Matcher matcher = SERVER_REGEX.matcher(locationString);
                        if (matcher.matches()) {
                            serverID = matcher.group(2);
                            continue; // skip to next line
                        }
                    }
                    for (EnumUtils.Location loopLocation : EnumUtils.Location.values()) {
                        if (locationString.endsWith(loopLocation.getScoreboardName())) {
                            if (loopLocation == EnumUtils.Location.BLAZING_FORTRESS &&
                                    location != EnumUtils.Location.BLAZING_FORTRESS) {
                                sendPostRequest(EnumUtils.MagmaEvent.PING); // going into blazing fortress
                                main.getUtils().fetchEstimateFromServer();
                            }
                            location = loopLocation;
                            foundLocation = true;
                            break;
                        }
                    }
                }
            } else {
                onSkyblock = false;
            }
        } else {
            onSkyblock = false;
        }
        if (!foundLocation) {
            location = null;
        }
    }

    private String keepLettersAndNumbersOnly(String text) {
        return LETTERS_NUMBERS.matcher(text).replaceAll("");
    }

//    private String keepLettersOnly(String text) {
//        return LETTERS.matcher(text).replaceAll("");
//    }

    public String getNumbersOnly(String text) {
        return NUMBERS_SLASHES.matcher(text).replaceAll("");
    }

    private String removeDuplicateSpaces(String text) {
        return text.replaceAll("\\s+", " ");
    }

    public void checkUpdates() {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/biscuut/SkyblockAddons/master/build.gradle");
                URLConnection connection = url.openConnection();
                connection.setReadTimeout(5000);
                connection.addRequestProperty("User-Agent", "SkyblockAddons update checker");
                connection.setDoOutput(true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String currentLine;
                String newestVersion = "";
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.contains("version = \"")) {
                        String[] newestVersionSplit = currentLine.split(Pattern.quote("version = \""));
                        newestVersionSplit = newestVersionSplit[1].split(Pattern.quote("\""));
                        newestVersion = newestVersionSplit[0];
                        break;
                    }
                }
                reader.close();
                List<Integer> newestVersionNumbers = new ArrayList<>();
                List<Integer> thisVersionNumbers = new ArrayList<>();
                try {
                    for (String s : newestVersion.split(Pattern.quote("."))) {
                        if (s.contains("-b")) {
                            String[] splitBuild = s.split(Pattern.quote("-b"));
                            newestVersionNumbers.add(Integer.parseInt(splitBuild[0]));
                            newestVersionNumbers.add(Integer.parseInt(splitBuild[1]));
                        } else {
                            newestVersionNumbers.add(Integer.parseInt(s));
                        }
                    }
                    for (String s : SkyblockAddons.VERSION.split(Pattern.quote("."))) {
                        if (s.contains("-b")) {
                            String[] splitBuild = s.split(Pattern.quote("-b"));
                            thisVersionNumbers.add(Integer.parseInt(splitBuild[0]));
                            thisVersionNumbers.add(Integer.parseInt(splitBuild[1]));
                        } else {
                            thisVersionNumbers.add(Integer.parseInt(s));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                for (int i = 0; i < 4; i++) {
                    if (i >= newestVersionNumbers.size()) {
                        newestVersionNumbers.add(i, 0);
                    }
                    if (i >= thisVersionNumbers.size()) {
                        thisVersionNumbers.add(i, 0);
                    }
                    if (newestVersionNumbers.get(i) > thisVersionNumbers.get(i)) {
                        String link = "https://hypixel.net/threads/forge-1-8-9-skyblockaddons-useful-features-for-skyblock.2109217/";
                        try {
                            url = new URL("https://raw.githubusercontent.com/biscuut/SkyblockAddons/master/updatelink.txt");
                            connection = url.openConnection();
                            connection.setReadTimeout(5000);
                            connection.addRequestProperty("User-Agent", "SkyblockAddons");
                            connection.setDoOutput(true);
                            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            while ((currentLine = reader.readLine()) != null) {
                                link = currentLine;
                            }
                            reader.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            sendMessage(ChatFormatting.GRAY.toString() + ChatFormatting.STRIKETHROUGH + "--------------" + ChatFormatting.GRAY + "[" + ChatFormatting.BLUE + ChatFormatting.BOLD + " SkyblockAddons " + ChatFormatting.GRAY + "]" + ChatFormatting.GRAY + ChatFormatting.STRIKETHROUGH + "--------------");
                            TextComponentString newVersion = new TextComponentString(ChatFormatting.YELLOW + Message.MESSAGE_NEW_VERSION.getMessage(newestVersion) + "\n");
                            newVersion.setStyle(newVersion.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)));
                            sendMessage(newVersion);
                            TextComponentString discord = new TextComponentString(ChatFormatting.YELLOW + Message.MESSAGE_DISCORD.getMessage());
                            discord.setStyle(discord.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/PqTAEek")));
                            sendMessage(discord);
                            sendMessage(ChatFormatting.GRAY.toString() + ChatFormatting.STRIKETHROUGH + "---------------------------------------");
                        }
                        break;
                    } else if (thisVersionNumbers.get(i) > newestVersionNumbers.get(i)) {
                        sendMessage(ChatFormatting.GRAY.toString() + ChatFormatting.STRIKETHROUGH + "--------------" + ChatFormatting.GRAY + "[" + ChatFormatting.BLUE + ChatFormatting.BOLD + " SkyblockAddons " + ChatFormatting.GRAY + "]" + ChatFormatting.GRAY + ChatFormatting.STRIKETHROUGH + "--------------");
                        sendMessage(ChatFormatting.YELLOW + Message.MESSAGE_DEVELOPMENT_VERSION.getMessage(SkyblockAddons.VERSION, newestVersion));
                        sendMessage(ChatFormatting.GRAY.toString() + ChatFormatting.STRIKETHROUGH + "---------------------------------------");
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void checkDisabledFeatures() {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/biscuut/SkyblockAddons/master/disabledFeatures.txt");
                URLConnection connection = url.openConnection();
                connection.setReadTimeout(5000);
                connection.addRequestProperty("User-Agent", "SkyblockAddons");
                connection.setDoOutput(true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String currentLine;
                Set<Feature> disabledFeatures = main.getConfigValues().getRemoteDisabledFeatures();
                while ((currentLine = reader.readLine()) != null) {
                    String[] splitLine = currentLine.split(Pattern.quote("|"));
                    if (!currentLine.startsWith("all|")) {
                        if (!SkyblockAddons.VERSION.equals(splitLine[0])) {
                            continue;
                        }
                    }
                    if (splitLine.length > 1) {
                        for (int i = 1; i < splitLine.length; i++) {
                            String part = splitLine[i];
                            Feature feature = Feature.fromId(Integer.valueOf(part));
                            if (feature != null) {
                                disabledFeatures.add(feature);
                            }
                        }
                    }
                }
                reader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public int getDefaultColor(float alphaFloat) {
        int alpha = (int) alphaFloat;
        return new Color(150, 236, 255, alpha).getRGB();
    }

    public void playSound(String sound, double pitch) {
        playingSound = true;
        Minecraft.getMinecraft().player.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft", sound)), 1, (float) pitch);
        playingSound = false;
    }

    public boolean enchantReforgeMatches(String text) {
        text = text.toLowerCase();
        for (String enchant : enchantmentMatch) {
            enchant = enchant.trim().toLowerCase();
            if (!enchant.equals("") && text.contains(enchant)) {
                boolean foundExclusion = false;
                for (String exclusion : enchantmentExclusion) {
                    exclusion = exclusion.trim().toLowerCase();
                    if (!exclusion.equals("") && text.contains(exclusion)) {
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

    private final static String USER_AGENT = "SkyblockAddons/" + SkyblockAddons.VERSION;

    public void fetchEstimateFromServer() {
        new Thread(() -> {
            FMLLog.info("[SkyblockAddons] Getting magma boss spawn estimate from server...");
            try {
                URL url = new URL("https://hypixel-api.inventivetalent.org/api/skyblock/bosstimer/magma/estimatedSpawn");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);

                FMLLog.info("[SkyblockAddons] Got response code " + connection.getResponseCode());

                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                }
                JsonObject responseJson = new Gson().fromJson(response.toString(), JsonObject.class);
                long estimate = responseJson.get("estimate").getAsLong();
                long currentTime = responseJson.get("queryTime").getAsLong();
                int magmaSpawnTime = (int)((estimate-currentTime)/1000);
                FMLLog.info("[SkyblockAddons] Query time was " + currentTime + ", server time estimate is " +
                        estimate+". Updating magma boss spawn to be in "+magmaSpawnTime+" seconds.");

                main.getPlayerListener().setMagmaTime(magmaSpawnTime, true);
                main.getPlayerListener().setMagmaAccuracy(EnumUtils.MagmaTimerAccuracy.ABOUT);
            } catch (IOException ex) {
                FMLLog.warning("[SkyblockAddons] Failed to get magma boss spawn estimate from server");
                ex.printStackTrace();
            }
        }).start();
    }

    public void sendPostRequest(EnumUtils.MagmaEvent event) {
        new Thread(() -> {
            FMLLog.info("[SkyblockAddons] Posting event " + event.getInventiveTalentEvent() + " to InventiveTalent API");

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
                if (mc != null && mc.player != null) {
                    String postString;
                    if (event == EnumUtils.MagmaEvent.PING) {
                        postString = "minecraftUser=" + mc.player.getName() + "&lastFocused=" + System.currentTimeMillis() / 1000 + "&serverId=" + serverID;
                    } else {
                        postString = "type=" + event.getInventiveTalentEvent() + "&isModRequest=true&minecraftUser=" + mc.player.getName() + "&serverId=" + serverID;
                    }
                    connection.setDoOutput(true);
                    try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                        out.writeBytes(postString);
                        out.flush();
                    }
                    FMLLog.info("[SkyblockAddons] Got response code " + connection.getResponseCode());
                    connection.disconnect();
                }
            } catch (IOException ex) {
                FMLLog.warning("[SkyblockAddons] Failed to post event to server");
                ex.printStackTrace();
            }
        }).start();
    }

    public String getReforgeFromItem(ItemStack item) {
        if (item.hasTagCompound()) {
            NBTTagCompound extraAttributes = item.getTagCompound();
            if (extraAttributes.hasKey("ExtraAttributes")) {
                extraAttributes = extraAttributes.getCompoundTag("ExtraAttributes");
                if (extraAttributes.hasKey("modifier")) {
                    return WordUtils.capitalizeFully(extraAttributes.getString("modifier"));
                }
            }
        }
        return null;
    }

    // This reverses the text while leaving the english parts intact and in order.
    // (Maybe its more complicated than it has to be, but it gets the job done.
    String reverseText(String originalText) {
        StringBuilder newString = new StringBuilder();
        String[] parts = originalText.split(" ");
        for (int i = parts.length; i > 0; i--) {
            String textPart = parts[i-1];
            boolean foundCharacter = false;
            for (char letter : textPart.toCharArray()) {
                if (letter > 191) { // Found special character
                    foundCharacter = true;
                    newString.append(new StringBuilder(textPart).reverse().toString());
                    break;
                }
            }
            newString.append(" ");
            if (!foundCharacter) {
                newString.insert(0, textPart);
            }
            newString.insert(0, " ");
        }
        return main.getUtils().removeDuplicateSpaces(newString.toString().trim());
    }

    public boolean cantDropItem(ItemStack item, EnumUtils.Rarity rarity, boolean hotbar) {
        if (hotbar) {
            return item.getItem().isDamageable() || (rarity != EnumUtils.Rarity.COMMON && rarity != EnumUtils.Rarity.UNCOMMON)
                    || (item.hasDisplayName() && item.getDisplayName().contains("Backpack"));
        } else {
            return item.getItem().isDamageable() || (rarity != EnumUtils.Rarity.COMMON && rarity != EnumUtils.Rarity.UNCOMMON
                    && rarity != EnumUtils.Rarity.RARE) || (item.hasDisplayName() && item.getDisplayName().contains("Backpack"));
        }
    }

    public String replaceRomanNumerals(String text) {
        if (text != null) {
            text = checkAndReplaceNumeral(text, " XV", " 15");
            text = checkAndReplaceNumeral(text, " XIV", " 14");
            text = checkAndReplaceNumeral(text, " XIII", " 13");
            text = checkAndReplaceNumeral(text, " XII", " 12");
            text = checkAndReplaceNumeral(text, " XI", " 11");
            text = checkAndReplaceNumeral(text, " X", " 10");
            text = checkAndReplaceNumeral(text, " IX", " 9");
            text = checkAndReplaceNumeral(text, " VIII", " 8");
            text = checkAndReplaceNumeral(text, " VII", " 7");
            text = checkAndReplaceNumeral(text, " VI", " 6");
            text = checkAndReplaceNumeral(text, " V", " 5");
            text = checkAndReplaceNumeral(text, " IV", " 4");
            text = checkAndReplaceNumeral(text, " III", " 3");
            text = checkAndReplaceNumeral(text, " II", " 2");
            text = checkAndReplaceNumeral(text, " I", " 1");
        }
        return text;
    }

    private String checkAndReplaceNumeral(String text, String numeral, String replacement) {
        if (numeral.equals(" I") || numeral.equals(" V") || numeral.equals(" X")) {
            int index = text.indexOf(numeral);
            if (index != -1 && text.length() > index + 2) {
                char charAfter = text.charAt(index + 2);
                if (charAfter != ' ' && charAfter != 'I' && charAfter != 'V' && charAfter != 'X') return text;
            }
        }
//        if (text.startsWith("\u00A75\u00A7o\u00A79")) {
//            return text.replace(numeral, replacement);
//        }
        return text.replace(numeral, replacement);
    }

    public boolean isDevEnviroment() {
        return (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }

    public int getDefaultBlue(int alpha) {
        return new Color(160, 225, 229, alpha).getRGB();
    }

    public String stripColor(final String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public EnumUtils.Location getLocation() {
        return location;
    }

    public boolean isOnSkyblock() {
        return onSkyblock;
    }

    public boolean isFadingIn() {
        return fadingIn;
    }

    public void setFadingIn(boolean fadingIn) {
        this.fadingIn = fadingIn;
    }

    public Backpack getBackpackToRender() {
        return backpackToRender;
    }

    public void setBackpackToRender(Backpack backpackToRender) {
        this.backpackToRender = backpackToRender;
    }

    public List<String> getEnchantmentExclusion() {
        return enchantmentExclusion;
    }

    public List<String> getEnchantmentMatch() {
        return enchantmentMatch;
    }

    public void setEnchantmentExclusion(List<String> enchantmentExclusion) {
        this.enchantmentExclusion = enchantmentExclusion;
    }

    public void setEnchantmentMatch(List<String> enchantmentMatch) {
        this.enchantmentMatch = enchantmentMatch;
    }

    public boolean isPlayingSound() {
        return playingSound;
    }

    public Map<Attribute, MutableInt> getAttributes() {
        return attributes;
    }

    public boolean isCopyNBT() {
        return copyNBT;
    }

    public void setCopyNBT(boolean copyNBT) {
        this.copyNBT = copyNBT;
    }

    public SkyblockDate getCurrentDate() {
        return currentDate;
    }

    public int getLastHoveredSlot() {
        return lastHoveredSlot;
    }

    public void setLastHoveredSlot(int lastHoveredSlot) {
        this.lastHoveredSlot = lastHoveredSlot;
    }

}
