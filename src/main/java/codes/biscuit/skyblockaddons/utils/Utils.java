package codes.biscuit.skyblockaddons.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static boolean onIsland = false;
    private static boolean onSkyblock = false;

    // Static cause I can't be bothered to pass the instance ok stop bullying me
    public static void sendMessage(String text) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent((byte)1, new ChatComponentText(text));
        MinecraftForge.EVENT_BUS.post(new ClientChatReceivedEvent((byte)1, new ChatComponentText(text))); // Let other mods pick up the new message
        if (!event.isCanceled()) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(event.message); // Just for logs
        }
    }

    public static void checkIfOnSkyblockAndIsland() { // Most of this is replicated from the scoreboard rendering code so not many comments here xD
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.theWorld != null) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective scoreobjective = null;
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(mc.thePlayer.getName());
            if (scoreplayerteam != null) {
                int randomNumber = scoreplayerteam.getChatFormat().getColorIndex();
                if (randomNumber >= 0) {
                    scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + randomNumber);
                }
            }
            ScoreObjective scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);
            if (scoreobjective1 != null) {
                String scoreboardTitle = scoreobjective1.getDisplayName();
                onSkyblock = stripColor(scoreboardTitle).startsWith("SKYBLOCK");
                Collection<Score> collection = scoreboard.getSortedScores(scoreobjective1);
                List<Score> list = Lists.newArrayList(collection.stream().filter(score -> score.getPlayerName() != null && !score.getPlayerName().startsWith("#")).collect(Collectors.toList()));
                if (list.size() > 15) {
                    collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
                } else {
                    collection = list;
                }
                for (Score score1 : collection) {
                    ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
                    String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
                    if (s1.equals(" \u00A77\u23E3 \u00A7aYour Isla\uD83C\uDFC0\u00A7and")) {
                        onIsland = true;
                        return;
                    }
                }
            }
        }
        onIsland = false;
    }

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile( "(?i)" + '\u00A7' + "[0-9A-FK-OR]" );
    private static String stripColor(final String input) {
        if (input == null) return null;
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static boolean isOnIsland() {
        return onIsland;
    }

    public static boolean isOnSkyblock() {
        return onSkyblock;
    }
}
