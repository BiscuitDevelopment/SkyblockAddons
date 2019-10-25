package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.FileListHelper;
import net.minecraftforge.fml.relauncher.ModListHelper;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class Utils {

    private final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    private final Pattern ITEM_COOLDOWN_PATTERN = Pattern.compile("§5§o§8Cooldown: §a([0-9]+)s");
    private final Pattern ALTERNATE_COOLDOWN_PATTERN = Pattern.compile("§5§o§8([0-9]+) Second Cooldown");
    private final Pattern ITEM_ABILITY_PATTERN = Pattern.compile("§5§o§6Item Ability: ([A-Za-z ]+) §e§l[A-Z ]+");

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

    private Map<Attribute, MutableInt> attributes = new EnumMap<>(Attribute.class);
    private List<String> enchantmentMatch = new LinkedList<>();
    private List<String> enchantmentExclusion = new LinkedList<>();
    private Set<CooldownEntry> cooldownEntries = new HashSet<>();
    private Backpack backpackToRender = null;
    private static boolean onSkyblock = false;
    private EnumUtils.Location location = null;
    private String profileName = null;
    private boolean playingSound = false;
    private boolean copyNBT = false;
    private String serverID = "";
    private SkyblockDate currentDate = new SkyblockDate(SkyblockDate.SkyblockMonth.EARLY_WINTER, 1, 1, 1);
    private int lastHoveredSlot = -1;
    private EnumUtils.InventoryType inventoryType = null;

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
    // english, chinese simplified
    private static Set<String> skyblockInAllLanguages = Sets.newHashSet("SKYBLOCK","\u7A7A\u5C9B\u751F\u5B58");

    public void checkGameLocationDate() {
        boolean foundLocation = false;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.theWorld != null) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective sidebarObjective = mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1);
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

    private static final Pattern NUMBERS_SLASHES = Pattern.compile("[^0-9 /]");
    private static final Pattern LETTERS_NUMBERS = Pattern.compile("[^a-z A-Z:0-9/']");

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
                main.getRenderListener().getDownloadInfo().setNewestVersion(newestVersion);
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
                            main.getRenderListener().getDownloadInfo().setDownloadLink(link);
                            if (i == 2 || i == 3) { // 0.0.x or 0.0.0-bx
                                main.getRenderListener().getDownloadInfo().setPatch();
                                main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.PATCH_AVAILABLE);
                                sendUpdateMessage(true,true);
                            } else {
                                main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.MAJOR_AVAILABLE);
                                sendUpdateMessage(true,false);
                            }
                        }
                        break;
                    } else if (thisVersionNumbers.get(i) > newestVersionNumbers.get(i)) {
                        main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.DEVELOPMENT);
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    void sendUpdateMessage(boolean showDownload, boolean showAutoDownload) {
        String newestVersion = main.getRenderListener().getDownloadInfo().getNewestVersion();
        sendMessage(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + "--------" + EnumChatFormatting.GRAY + "[" +
                EnumChatFormatting.AQUA + EnumChatFormatting.BOLD + " SkyblockAddons " + EnumChatFormatting.GRAY + "]" + EnumChatFormatting.GRAY + EnumChatFormatting.STRIKETHROUGH + "--------");
        if (main.getRenderListener().getDownloadInfo().getMessageType() == EnumUtils.UpdateMessageType.DOWNLOAD_FINISHED) {
            ChatComponentText deleteOldFile = new ChatComponentText(EnumChatFormatting.RED+Message.MESSAGE_DELETE_OLD_FILE.getMessage()+"\n");
            sendMessage(deleteOldFile);
        } else {
            ChatComponentText newUpdate = new ChatComponentText(EnumChatFormatting.AQUA+Message.MESSAGE_NEW_UPDATE.getMessage(newestVersion)+"\n");
            sendMessage(newUpdate);
        }

        ChatComponentText buttonsMessage = new ChatComponentText("");
        if (showDownload) {
            buttonsMessage = new ChatComponentText(EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD + "[" + Message.MESSAGE_DOWNLOAD_LINK.getMessage(newestVersion) + "]");
            buttonsMessage.setChatStyle(buttonsMessage.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, main.getRenderListener().getDownloadInfo().getDownloadLink())));
            buttonsMessage.appendSibling(new ChatComponentText(" "));
        }

        if (showAutoDownload) {
            ChatComponentText downloadAutomatically = new ChatComponentText(EnumChatFormatting.GREEN.toString() + EnumChatFormatting.BOLD + "[" + Message.MESSAGE_DOWNLOAD_AUTOMATICALLY.getMessage(newestVersion) + "]");
            downloadAutomatically.setChatStyle(downloadAutomatically.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sba update")));
            buttonsMessage.appendSibling(downloadAutomatically);
            buttonsMessage.appendSibling(new ChatComponentText(" "));
        }

        ChatComponentText openModsFolder = new ChatComponentText(EnumChatFormatting.YELLOW.toString() + EnumChatFormatting.BOLD + "[" + Message.MESSAGE_OPEN_MODS_FOLDER.getMessage(newestVersion) + "]");
        openModsFolder.setChatStyle(openModsFolder.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sba folder")));
        buttonsMessage.appendSibling(openModsFolder);

        sendMessage(buttonsMessage);
        if (main.getRenderListener().getDownloadInfo().getMessageType() != EnumUtils.UpdateMessageType.DOWNLOAD_FINISHED) {
            ChatComponentText discord = new ChatComponentText(EnumChatFormatting.AQUA + Message.MESSAGE_VIEW_PATCH_NOTES.getMessage() + " " +
                    EnumChatFormatting.BLUE.toString() + EnumChatFormatting.BOLD + "[" + Message.MESSAGE_JOIN_DISCORD.getMessage() + "]");
            discord.setChatStyle(discord.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/PqTAEek")));
            sendMessage(discord);
        }
        sendMessage(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + "----------------------------------");
    }

    public void checkDisabledFeatures() {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/biscuut/SkyblockAddons/master/disabledFeatures.txt");
                URLConnection connection = url.openConnection();
                connection.setReadTimeout(5000);
                connection.addRequestProperty("User-Agent", "SkyblockAddons");
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

    public boolean isNotNPC(Entity entity) {
        if (entity instanceof EntityOtherPlayerMP) {
            EntityPlayer p = (EntityPlayer)entity;
            Team team = p.getTeam();
            if (team instanceof ScorePlayerTeam) {
                ScorePlayerTeam playerTeam = (ScorePlayerTeam)team;
                String color = playerTeam.getColorPrefix();
                return color == null || !color.equals("");
            }
        }
        return true;
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
                connection.disconnect();
                JsonObject responseJson = new Gson().fromJson(response.toString(), JsonObject.class);
                long estimate = responseJson.get("estimate").getAsLong();
                long currentTime = responseJson.get("queryTime").getAsLong();
                int magmaSpawnTime = (int)((estimate-currentTime)/1000);
                FMLLog.info("[SkyblockAddons] Query time was " + currentTime +", server time estimate is " +
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

    public boolean isMaterialForRecipe(ItemStack item) {
        final List<String> tooltip = item.getTooltip(null, false);
        for (String s : tooltip) {
            if (s.equals("§5§o§eRight-click to view recipes!")) {
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

    public void downloadPatch(String version) {
        File sbaFolder = getSBAFolder(true);
        if (sbaFolder != null) {
            new Thread(() -> {
                try {
                    String fileName = "SkyblockAddons-"+version+"-for-MC-1.8.9.jar";
                    URL url = new URL("https://github.com/biscuut/SkyblockAddons/releases/download/v"+version+"/"+fileName);
                    File outputFile = new File(sbaFolder.toString()+File.separator+fileName);
                    URLConnection connection = url.openConnection();
                    long totalFileSize = connection.getContentLengthLong();
                    main.getRenderListener().getDownloadInfo().setTotalBytes(totalFileSize);
                    main.getRenderListener().getDownloadInfo().setOutputFileName(fileName);
                    main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.DOWNLOADING);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        main.getRenderListener().getDownloadInfo().setDownloadedBytes(main.getRenderListener().getDownloadInfo().getDownloadedBytes()+bytesRead);
                    }
                    main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.DOWNLOAD_FINISHED);
                } catch (IOException e) {
                    main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.FAILED);
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @SuppressWarnings("unchecked")
    public File getSBAFolder(boolean changeMessage) {
        try {
            Method setupCoreModDir = CoreModManager.class.getDeclaredMethod("setupCoreModDir", File.class);
            setupCoreModDir.setAccessible(true);
            File coreModFolder = (File) setupCoreModDir.invoke(null, Minecraft.getMinecraft().mcDataDir);
            setupCoreModDir.setAccessible(false);
            if (coreModFolder.isDirectory()) {
                FilenameFilter fileFilter = (dir, name) -> name.endsWith(".jar");
                File[] coreModList = coreModFolder.listFiles(fileFilter);
                if (coreModList != null) {
                    Field mccversion = FMLInjectionData.class.getDeclaredField("mccversion");
                    mccversion.setAccessible(true);
                    File versionedModDir = new File(coreModFolder, (String)mccversion.get(null));
                    mccversion.setAccessible(false);
                    if (versionedModDir.isDirectory()) {
                        File[] versionedCoreMods = versionedModDir.listFiles(fileFilter);
                        if (versionedCoreMods != null) {
                            coreModList = ObjectArrays.concat(coreModList, versionedCoreMods, File.class);
                        }
                    }
                    coreModList = ObjectArrays.concat(coreModList, ModListHelper.additionalMods.values().toArray(new File[0]), File.class);
                    FileListHelper.sortFileList(coreModList);
                    for (File coreMod : coreModList) {
                        JarFile jar = new JarFile(coreMod);
                        ZipEntry modInfo = jar.getEntry("mcmod.info");
                        if (modInfo != null) {
                            MetadataCollection metadata = MetadataCollection.from(jar.getInputStream(modInfo), coreMod.getName());
                            Field metadatas = metadata.getClass().getDeclaredField("metadatas");
                            metadatas.setAccessible(true);
                            for (String modId : ((Map<String, ModMetadata>)metadatas.get(metadata)).keySet()) {
                                if (modId.equals("skyblockaddons")) {
                                    return coreMod.getParentFile();
                                }
                            }
                            metadatas.setAccessible(false);
                        }
                    }
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException | IOException e) {
            e.printStackTrace();
            if (changeMessage) main.getRenderListener().getDownloadInfo().setMessageType(EnumUtils.UpdateMessageType.FAILED);
        }
        return null;
    }

    public int getNBTInteger(ItemStack item, String... path) {
        if (item != null && item.hasTagCompound()) {
            NBTTagCompound tag = item.getTagCompound();
            for (String tagName : path) {
                if (path[path.length-1] == tagName) continue;
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

    public void logEntry(ItemStack itemStack) {
        if (itemStack != null && itemStack.hasDisplayName()) {
            Item item = itemStack.getItem();
            String name = itemStack.getDisplayName();
            int cooldownSeconds = getLoreCooldown(itemStack);
            if (cooldownSeconds != -1) {
                String abilityName = getAbilityName(itemStack);
                CooldownEntry cooldownEntry = getCooldownEntry(item, name);
                if (!item.isDamageable() && abilityName == null) return; // if its not a tool and has no ability, its not gonna have a cooldown
                if (cooldownEntry != null) {
                    cooldownEntry.setLastUse();
                } else {
                    cooldownEntries.add(new CooldownEntry(item,name,cooldownSeconds));
                }
            }
        }
    }

    public double getItemCooldown(ItemStack item) {
        if (item != null) {
            Iterator<CooldownEntry> iterator = cooldownEntries.iterator();
            while (iterator.hasNext()) {
                CooldownEntry entry = iterator.next();
                double cooldown = entry.getCooldown();
                if (entry.getItem().equals(item.getItem()) && entry.getItemName().equals(item.getDisplayName())) {
                    return cooldown;
                }
                if (cooldown == 1) {
                    iterator.remove();
                }
            }
        }
        return -1;
    }

    public CooldownEntry getItemCooldown(String itemName) {
        Iterator<CooldownEntry> iterator = cooldownEntries.iterator();
        while (iterator.hasNext()) {
            CooldownEntry entry = iterator.next();
            if (entry.getItemName().equals(itemName)) {
                return entry;
            }
            double cooldown = entry.getCooldown();
            if (cooldown == 1) {
                iterator.remove();
            }
        }
        return null;
    }

    private int getLoreCooldown(ItemStack item) {
        for (String loreLine : item.getTooltip(Minecraft.getMinecraft().thePlayer, false)) {
            Matcher matcher = ITEM_COOLDOWN_PATTERN.matcher(loreLine);
            if (matcher.matches()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ignored) { }
            } else {
                matcher = ALTERNATE_COOLDOWN_PATTERN.matcher(loreLine);
                if (matcher.matches()) {
                    try {
                        return Integer.parseInt(matcher.group(1));
                    } catch (NumberFormatException ignored) { }
                }
            }
        }
        return -1;
    }

    private String getAbilityName(ItemStack item) {
        for (String loreLine : item.getTooltip(Minecraft.getMinecraft().thePlayer, false)) {
            Matcher matcher = ITEM_ABILITY_PATTERN.matcher(loreLine);
            if (matcher.matches()) {
                try {
                    return matcher.group(1);
                } catch (NumberFormatException ignored) { }
            }
        }
        return null;
    }

    private CooldownEntry getCooldownEntry(Item item, String itemname) {
        for (CooldownEntry entry : cooldownEntries) {
            if (entry.getItem().equals(item) && entry.getItemName().equals(itemname)) {
                return entry;
            }
        }
        return null;
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

    public void setLastHoveredSlot(int lastHoveredSlot) {
        this.lastHoveredSlot = lastHoveredSlot;
    }

    public int getLastHoveredSlot() {
        return lastHoveredSlot;
    }

    public void reorderEnchantmentList(List<String> enchantments) {
        SortedMap<Integer, String> orderedEnchants = new TreeMap<>();
        for (int i = 0; i < enchantments.size(); i++) {
            int nameEnd = enchantments.get(i).lastIndexOf(' ');
            if (nameEnd < 0) nameEnd = enchantments.get(i).length();

            int key = ORDERED_ENCHANTMENTS.indexOf(enchantments.get(i).substring(0, nameEnd).toLowerCase());
            if (key < 0) key = 100 + i;
            orderedEnchants.put(key, enchantments.get(i));
        }

        enchantments.clear();
        enchantments.addAll(orderedEnchants.values());
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public EnumUtils.InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(EnumUtils.InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }
}
