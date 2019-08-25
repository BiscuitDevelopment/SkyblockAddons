package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.lang3.mutable.MutableInt;

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
    private BackpackInfo backpackToRender = null;
    private static boolean onSkyblock = false;
    private EnumUtils.Location location = null;
    private boolean playingSound = false;
    private boolean copyNBT = false;
    private String serverID = "";
    private SkyblockDate currentDate = new SkyblockDate(SkyblockDate.SkyblockMonth.EARLY_WINTER, 1, 1, 1);

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
        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte) 1, new ChatComponentText(text));
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message); // Just for logs
        }
    }

    private void sendMessage(ChatComponentText text) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte) 1, text);
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message); // Just for logs
        }
    }

    private static final Pattern SERVER_REGEX = Pattern.compile("([0-9]{2}/[0-9]{2}/[0-9]{2}) (mini[0-9]{1,3}[A-Za-z])");

    public void checkGameLocationDate() {
        boolean foundLocation = false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.theWorld != null) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective sidebarObjective = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
            if (sidebarObjective != null) {
                onSkyblock = stripColor(sidebarObjective.getDisplayName()).startsWith("SKYBLOCK");
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

    public float normalizeValue(float value, float valueMin, float valueMax, float valueStep) {
        return MathHelper.clamp_float((this.snapToStepClamp(value, valueMin, valueMax, valueStep) - valueMin) / (valueMax - valueMin), 0.0F, 1.0F);
    }

    public float denormalizeValue(float value, float valueMin, float valueMax, float valueStep) {
        return this.snapToStepClamp(valueMin + (valueMax - valueMin) * MathHelper.clamp_float(value, 0.0F, 1.0F), valueMin, valueMax, valueStep);
    }

    private float snapToStepClamp(float value, float valueMin, float valueMax, float valueStep) {
        value = this.snapToStep(value, valueStep);
        return MathHelper.clamp_float(value, valueMin, valueMax);
    }

    private float snapToStep(float value, float valueStep) {
        if (valueStep > 0.0F) {
            value = valueStep * (float) Math.round(value / valueStep);
        }

        return value;
    }

//    private final Pattern LETTERS = Pattern.compile("[^a-z A-Z]");
    private static final Pattern NUMBERS_SLASHES = Pattern.compile("[^0-9 /]");
    private static final Pattern LETTERS_NUMBERS = Pattern.compile("[^a-z A-Z:0-9/]");

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
                            sendMessage(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + "--------------" + EnumChatFormatting.GRAY + "[" + EnumChatFormatting.BLUE + EnumChatFormatting.BOLD + " SkyblockAddons " + EnumChatFormatting.GRAY + "]" + EnumChatFormatting.GRAY + EnumChatFormatting.STRIKETHROUGH + "--------------");
                            ChatComponentText newVersion = new ChatComponentText(EnumChatFormatting.YELLOW+Message.MESSAGE_NEW_VERSION.getMessage(newestVersion)+"\n");
                            newVersion.setChatStyle(newVersion.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link)));
                            sendMessage(newVersion);
                            ChatComponentText discord = new ChatComponentText(EnumChatFormatting.YELLOW+Message.MESSAGE_DISCORD.getMessage());
                            discord.setChatStyle(discord.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/PqTAEek")));
                            sendMessage(discord);
                            sendMessage(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + "---------------------------------------");
                        }
                        break;
                    } else if (thisVersionNumbers.get(i) > newestVersionNumbers.get(i)) {
                        sendMessage(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + "--------------" + EnumChatFormatting.GRAY + "[" + EnumChatFormatting.BLUE + EnumChatFormatting.BOLD + " SkyblockAddons " + EnumChatFormatting.GRAY + "]" + EnumChatFormatting.GRAY + EnumChatFormatting.STRIKETHROUGH + "--------------");
                        sendMessage(EnumChatFormatting.YELLOW + Message.MESSAGE_DEVELOPMENT_VERSION.getMessage(SkyblockAddons.VERSION, newestVersion));
                        sendMessage(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + "---------------------------------------");
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
                    break;
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
        Minecraft.getMinecraft().thePlayer.playSound(sound, 1, (float) pitch);
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
                long currentTime = System.currentTimeMillis();
                int magmaSpawnTime = (int)((estimate-currentTime)/1000);
                FMLLog.info("[SkyblockAddons] System time is " + currentTime +", server time estimate is " +
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
                if (mc != null && mc.thePlayer != null) {
                    String postString;
                    if (event == EnumUtils.MagmaEvent.PING) {
                        postString = "minecraftUser=" + mc.thePlayer.getName() + "&lastFocused=" + System.currentTimeMillis() / 1000 + "&serverId=" + serverID;
                    } else {
                        postString = "type=" + event.getInventiveTalentEvent() + "&isModRequest=true&minecraftUser=" + mc.thePlayer.getName() + "&serverId=" + serverID;
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

    public boolean isDevEnviroment() {
        return (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }

    public int getDefaultBlue(int alpha) {
        return new Color(189, 236, 252, alpha).getRGB();
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

    public BackpackInfo getBackpackToRender() {
        return backpackToRender;
    }

    public void setBackpackToRender(BackpackInfo backpackToRender) {
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
}
