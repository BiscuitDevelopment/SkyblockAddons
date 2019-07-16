package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static boolean onIsland = false;
    private static boolean onSkyblock = false;
    private static boolean inventoryIsFull = false;

    private SkyblockAddons main;

    public Utils(SkyblockAddons main) {
        this.main = main;
    }

    // Static cause I can't be bothered to pass the instance ok stop bullying me
    public void sendMessage(String text) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte)1, new ChatComponentText(text));
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message); // Just for logs
        }
    }

    private void sendMessage(ChatComponentText text) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte)1, text);
        MinecraftForge.EVENT_BUS.post(event); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message); // Just for logs
        }
    }

    public void checkIfInventoryIsFull() {
        if (!main.getConfigValues().getDisabledFeatures().contains(Feature.FULL_INVENTORY_WARNING)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null) {
                EntityPlayerSP player = mc.thePlayer;
                if (player != null) {
                    for (ItemStack item : player.inventory.mainInventory) {
                        if (item == null) {
                            inventoryIsFull = false;
                            return;
                        }
                    }
                    if (!inventoryIsFull) {
                        inventoryIsFull = true;
                        if (mc.currentScreen == null && System.currentTimeMillis() - main.getPlayerListener().getLastWorldJoin() > 3000) {
                            mc.thePlayer.playSound("random.orb", 1, 0.5F);
                            main.getPlayerListener().setFullInventoryWarning(true);
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    main.getPlayerListener().setFullInventoryWarning(false);
                                }
                            }, main.getConfigValues().getWarningSeconds() * 1000);
                        }
                    }
                }
            }
        }
    }

    public void checkIfOnSkyblockAndIsland() { // Most of this is replicated from the scoreboard rendering code so not many comments here xD
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.theWorld != null) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective objective = null;
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(mc.thePlayer.getName());
            if (scoreplayerteam != null)
            {
                int slot = scoreplayerteam.getChatFormat().getColorIndex();
                if (slot >= 0) objective = scoreboard.getObjectiveInDisplaySlot(3 + slot);
            }
            ScoreObjective scoreobjective1 = objective != null ? objective : scoreboard.getObjectiveInDisplaySlot(1);
            if (scoreobjective1 != null) {
                objective = scoreobjective1;
                onSkyblock = stripColor(objective.getDisplayName()).startsWith("SKYBLOCK");
                scoreboard = objective.getScoreboard();
                Collection<Score> collection = scoreboard.getSortedScores(objective);
                List<Score> list = Lists.newArrayList(collection.stream().filter(p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")).collect(Collectors.toList()));
                if (list.size() > 15) {
                    collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
                } else {
                    collection = list;
                }
                for (Score score1 : collection) {
                    ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
                    if (getStringOnly(stripColor(ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName()))).endsWith("Your Island")) {//s1.equals(" \u00A77\u23E3 \u00A7aYour Isla\uD83C\uDFC0\u00A7and")) {
                        onIsland = true;
                        return;
                    }
                }
            }
        }
        onIsland = false;
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

    private String getStringOnly(String text) {
        return Pattern.compile("[^a-z A-Z]").matcher(text).replaceAll("");
    }

    public void checkUpdates() {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/biscuut/SkyblockAddons/master/build.gradle");
                URLConnection connection = url.openConnection();
                connection.setReadTimeout(5000);
                connection.addRequestProperty("User-Agent", "SkyblockAddons update checker");
                connection.setDoOutput(true);
                final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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
                    if (i >= newestVersionNumbers.size() ) {
                        newestVersionNumbers.add(i, 0);
                    }
                    if (i >= thisVersionNumbers.size()) {
                        thisVersionNumbers.add(i, 0);
                    }
                    if (newestVersionNumbers.get(i) > thisVersionNumbers.get(i)) {
                        sendMessage(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + "--------------" + EnumChatFormatting.GRAY + "[" + EnumChatFormatting.BLUE + EnumChatFormatting.BOLD + " SkyblockAddons " + EnumChatFormatting.GRAY + "]" + EnumChatFormatting.GRAY + EnumChatFormatting.STRIKETHROUGH + "--------------");
                        ChatComponentText newVersion = new ChatComponentText(EnumChatFormatting.YELLOW+main.getConfigValues().getMessage(ConfigValues.Message.MESSAGE_NEW_VERSION, newestVersion));
                        newVersion.setChatStyle(newVersion.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://hypixel.net/threads/forge-1-8-9-skyblockaddons-useful-features-for-skyblock.2109217/")));
                        sendMessage(newVersion);
                        sendMessage(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + "---------------------------------------");
                        break;
                    } else if (thisVersionNumbers.get(i) > newestVersionNumbers.get(i)) {
                        sendMessage(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + "--------------" + EnumChatFormatting.GRAY + "[" + EnumChatFormatting.BLUE + EnumChatFormatting.BOLD + " SkyblockAddons " + EnumChatFormatting.GRAY + "]" + EnumChatFormatting.GRAY + EnumChatFormatting.STRIKETHROUGH + "--------------");
                        sendMessage(EnumChatFormatting.YELLOW + main.getConfigValues().getMessage(ConfigValues.Message.MESSAGE_NEW_VERSION, SkyblockAddons.VERSION, newestVersion));
                        sendMessage(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.STRIKETHROUGH + "---------------------------------------");
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private final Pattern STRIP_COLOR_PATTERN = Pattern.compile( "(?i)" + '\u00A7' + "[0-9A-FK-OR]" );
    private String stripColor(final String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public boolean isOnIsland() {
        return onIsland;
    }

    public boolean isOnSkyblock() {
        return onSkyblock;
    }
}
