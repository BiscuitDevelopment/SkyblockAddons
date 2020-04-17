package codes.biscuit.skyblockaddons.utils.dev;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is a class of utilities for Skyblock Addons developers.
 *
 * @author ILikePlayingGames
 * @version 2.0
 */
public class DevUtils {
    public static final int DEV_KEY = Keyboard.KEY_RCONTROL;
    public static final int ENTITY_COPY_RADIUS = 3;

    /**
     * Copies the objective and scores that are being displayed on a scoreboard's side bar.
     *
     * @param scoreboard the {@link Scoreboard} to copy the side bar from
     */
    public static void copyScoreboardSideBar(Scoreboard scoreboard) {
        //TODO This needs more work, Hypixel's scoreboards have a complex, strange format.

        if (scoreboard == null) {
            return;
        }

        ScoreObjective sideBarObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (sideBarObjective == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.CANADA);

        String objectiveName = sideBarObjective.getDisplayName();

        formatter.format("%s%n", objectiveName);

        List<Score> scores = (List<Score>) scoreboard.getSortedScores(sideBarObjective);
        if (scores == null || scores.isEmpty()) {
            formatter.format("%s", "No scores were found.");
        }
        else {
            int width = objectiveName.length();

            // TODO limit to 15 rows per scoreboard rendering
            // Fix emojis being written

            // Remove scores that aren't rendered.
            scores = scores.stream().filter(input -> input.getPlayerName() != null && !input.getPlayerName().startsWith("#")).collect(Collectors.toList());


            /*
            Minecraft renders the scoreboard from bottom to top so to keep the same order when writing it from top
            to bottom, we need to reverse the scores' order.
            */
            Collections.reverse(scores);

            for (Score score:
                    scores) {
                ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
                String playerName = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());
                int points = score.getScorePoints();

                width = Math.max(width, (playerName + " " + points).length());
                formatter.format("%-" + width + "." +
                        (width - Integer.toString(points).length() - 1) + "s %d%n", playerName, points);
            }
        }

        copyStringToClipboard(sb.toString(), "Scoreboard copied to clipboard!");
    }

    /**
     * Copies the data of all mobs within the entity copy radius of the player
     *
     * @param player the player
     * @param loadedEntities the list of all the entities that are currently loaded in the world
     */
    public static void copyMobData(EntityPlayerSP player, List<Entity> loadedEntities) {
        List<Entity> loadedEntitiesCopy = new LinkedList<>(loadedEntities);
        ListIterator<Entity> loadedEntitiesCopyIterator;
        StringBuilder stringBuilder = new StringBuilder();

        // We only care about mobs.
        loadedEntitiesCopy.removeIf(entity -> entity.getDistanceToEntity(player) > ENTITY_COPY_RADIUS ||
                !(EntityLivingBase.class.isAssignableFrom(entity.getClass())));

        loadedEntitiesCopyIterator = loadedEntitiesCopy.listIterator();

        // Copy the NBT data from the loaded entities.
        while (loadedEntitiesCopyIterator.hasNext()) {
            Entity entity = loadedEntitiesCopyIterator.next();
            NBTTagCompound entityData = new NBTTagCompound();

            stringBuilder.append("Class: ").append(entity.getClass().getSimpleName()).append(System.lineSeparator());
            if (entity.hasCustomName() || EntityPlayer.class.isAssignableFrom(entity.getClass())) {
                stringBuilder.append("Name: ").append(entity.getName()).append(System.lineSeparator());
            }

            stringBuilder.append("NBT Data:").append(System.lineSeparator());
            entity.writeToNBT(entityData);
            stringBuilder.append(prettyPrintNBT(entityData));

            // Add spacing if necessary.
            if (loadedEntitiesCopyIterator.hasNext()) {
                stringBuilder.append(System.lineSeparator()).append(System.lineSeparator());
            }
        }

        copyStringToClipboard(stringBuilder.toString(), ChatFormatting.GREEN + "Entity data was copied to clipboard!");
    }

    /**
     * Copies the provided NBT tag to the clipboard as a formatted string.
     *
     * @param nbtTag the NBT tag to copy
     * @param message the message to show in chat when the NBT tag is copied
     */
    public static void copyNBTTagToClipboard(NBTBase nbtTag, String message) {
        if (nbtTag == null) {
            SkyblockAddons.getInstance().getUtils().sendMessage("This item has no NBT data.");
            return;
        }
        writeToClipboard(prettyPrintNBT(nbtTag), message);
    }

    /**
     * Copies the provided NBT tags to the clipboard as a formatted string.
     *
     * @param nbtTags the NBT tags to copy
     * @param message the message to show in chat when the NBT tag is copied
     */
    public static void copyNBTTagsToClipboard(List<? extends NBTBase> nbtTags, String message) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < nbtTags.size(); i++) {
            if (nbtTags.get(i) == null) {
                SkyblockAddons.getInstance().getUtils().sendMessage("This item has no NBT data.");
                continue;
            }

            stringBuilder.append("Tag ").append(i).append(": ").append(System.lineSeparator());
            stringBuilder.append(prettyPrintNBT(nbtTags.get(i)));

            // Add a blank line if necessary
            if (i < (nbtTags.size()) - 1) {
                stringBuilder.append(System.lineSeparator());
            }
        }

        writeToClipboard(stringBuilder.toString(), message);
    }

    /**
     * Copies a string to the clipboard.
     *
     * @param string the string to copy
     */
    public static void copyStringToClipboard(String string) {
        writeToClipboard(string, "Value was copied to clipboard!");
    }

    /**
     * <p>Copies a string to the clipboard</p>
     * <p>Also shows the provided message in chat when successful</p>
     *
     * @param string the string to copy
     * @param successMessage the custom message to show after successful copy
     */
    public static void copyStringToClipboard(String string, String successMessage) {
        writeToClipboard(string, successMessage);
    }

    /**
     * Retrieves the server brand from the Minecraft client.
     *
     * @param mc the Minecraft client
     * @return the server brand if the client is connected to a server, {@code null} otherwise
     */
    public static String getServerBrand(Minecraft mc) {
        final Pattern SERVER_BRAND_PATTERN = Pattern.compile("(.+) <- (?:.+)");

        if (!mc.isSingleplayer()) {
            Matcher matcher = SERVER_BRAND_PATTERN.matcher(mc.thePlayer.getClientBrand());

            if (matcher.find()) {
                // Group 1 is the server brand.
                return matcher.group(1);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    // FIXME add support for TAG_LONG_ARRAY when updating to 1.12
    /**
     * <p>Converts an NBT tag into a pretty-printed string.</p>
     * <p>For constant definitions, see {@link Constants.NBT}</p>
     *
     * @param nbt the NBT tag to pretty print
     * @return pretty-printed string of the NBT data
     */
    public static String prettyPrintNBT(NBTBase nbt) {
        final String INDENT = "    ";

        int tagID = nbt.getId();
        StringBuilder stringBuilder = new StringBuilder();

        // Determine which type of tag it is.
        if (tagID == Constants.NBT.TAG_END) {
            stringBuilder.append('}');
        }
        else if (tagID == Constants.NBT.TAG_BYTE_ARRAY || tagID == Constants.NBT.TAG_INT_ARRAY) {
            stringBuilder.append('[');
            if (tagID == Constants.NBT.TAG_BYTE_ARRAY) {
                NBTTagByteArray nbtByteArray = (NBTTagByteArray) nbt;
                byte[] bytes = nbtByteArray.getByteArray();

                for (int i = 0; i < bytes.length; i++) {
                    stringBuilder.append(bytes[i]);

                    // Don't add a comma after the last element.
                    if (i < (bytes.length - 1)) {
                        stringBuilder.append(", ");
                    }
                }
            }
            else {
                NBTTagIntArray nbtIntArray = (NBTTagIntArray) nbt;
                int[] ints = nbtIntArray.getIntArray();

                for (int i = 0; i < ints.length; i++) {
                    stringBuilder.append(ints[i]);

                    // Don't add a comma after the last element.
                    if (i < (ints.length - 1)) {
                        stringBuilder.append(", ");
                    }
                }
            }
            stringBuilder.append(']');
        }
        else if (tagID == Constants.NBT.TAG_LIST) {
            NBTTagList nbtTagList = (NBTTagList) nbt;

            stringBuilder.append('[');
            for (int i = 0; i < nbtTagList.tagCount(); i++) {
                NBTBase currentListElement = nbtTagList.get(i);

                stringBuilder.append(prettyPrintNBT(currentListElement));

                // Don't add a comma after the last element.
                if (i < (nbtTagList.tagCount() - 1)) {
                    stringBuilder.append(", ");
                }
            }
            stringBuilder.append(']');
        }
        else if (tagID == Constants.NBT.TAG_COMPOUND) {
            NBTTagCompound nbtTagCompound = (NBTTagCompound) nbt;

            stringBuilder.append('{');
             if (!nbtTagCompound.hasNoTags()) {
                Iterator<String> iterator = nbtTagCompound.getKeySet().iterator();

                stringBuilder.append(System.lineSeparator());

                while (iterator.hasNext()) {
                    String key = iterator.next();
                    NBTBase currentCompoundTagElement = nbtTagCompound.getTag(key);

                    stringBuilder.append(key).append(": ").append(
                            prettyPrintNBT(currentCompoundTagElement));

                    // Don't add a comma after the last element.
                    if (iterator.hasNext()) {
                        stringBuilder.append(",").append(System.lineSeparator());
                    }
                }

                // Indent all lines
                String indentedString = stringBuilder.toString().replaceAll(System.lineSeparator(), System.lineSeparator() + INDENT);
                stringBuilder = new StringBuilder(indentedString);
            }

            stringBuilder.append(System.lineSeparator()).append('}');
        }
        // This includes the tags: byte, short, int, long, float, double, and string
        else {
            stringBuilder.append(nbt.toString());
        }

        return stringBuilder.toString();
    }

    // Internal methods
    private static void writeToClipboard(String text, String successMessage) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection output = new StringSelection(text);

        try {
            clipboard.setContents(output, output);
            SkyblockAddons.getInstance().getUtils().sendMessage(successMessage);
        } catch (IllegalStateException exception) {
            SkyblockAddons.getInstance().getUtils().sendErrorMessage("Clipboard not available.");
        }
    }
}
