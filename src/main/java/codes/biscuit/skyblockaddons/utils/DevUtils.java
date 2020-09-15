package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.util.Constants;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is a class of utilities for SkyblockAddons developers.
 *
 * @author ILikePlayingGames
 * @version 2.3
 */
public class DevUtils {
    /** Pattern used for removing the placeholder emoji player names from the Hypixel scoreboard */
    public static final Pattern SIDEBAR_PLAYER_NAME_PATTERN = Pattern.compile("[\uD83D\uDD2B\uD83C\uDF6B\uD83D\uDCA3\uD83D\uDC7D\uD83D\uDD2E\uD83D\uDC0D\uD83D\uDC7E\uD83C\uDF20\uD83C\uDF6D\u26BD\uD83C\uDFC0\uD83D\uDC79\uD83C\uDF81\uD83C\uDF89\uD83C\uDF82]+");
    /** Entity names for {@link this#copyEntityData(String, int)}*/
    public static final List<String> ENTITY_NAMES = EntityList.getEntityNameList();

    public static final int ENTITY_COPY_RADIUS = 3;
    public static final int SIDEBAR_COPY_WIDTH = 30;

    @Getter @Setter
    private static boolean loggingActionBarMessages = false;

    static {
        ENTITY_NAMES.add("PlayerSP");
        ENTITY_NAMES.add("PlayerMP");
        ENTITY_NAMES.add("OtherPlayerMP");
    }

    /**
     * Copies the objective and scores that are being displayed on a scoreboard's sidebar.
     * When copying the sidebar, the control codes (e.g. §a) are removed.
     *
     * @param scoreboard the {@link Scoreboard} to copy the sidebar from
     */
    public static void copyScoreboardSideBar(Scoreboard scoreboard) {
        copyScoreboardSidebar(scoreboard, true);
    }

    /**
     * Copies the objective and scores that are being displayed on a scoreboard's sidebar.
     *
     * @param scoreboard the {@link Scoreboard} to copy the sidebar from
     * @param stripControlCodes if {@code true}, the control codes will be removed, otherwise they will be copied
     */
    public static void copyScoreboardSidebar(Scoreboard scoreboard, boolean stripControlCodes) {
        if (scoreboard == null) {
            throw new NullPointerException("Scoreboard cannot be null!");
        }

        ScoreObjective sideBarObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (sideBarObjective == null) {
            throw new NullPointerException("Nothing is being displayed in the sidebar!");
        }

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.CANADA);

        String objectiveName = sideBarObjective.getDisplayName();
        List<Score> scores = (List<Score>) scoreboard.getSortedScores(sideBarObjective);

        if (scores == null || scores.isEmpty()) {
            SkyblockAddons.getInstance().getUtils().sendErrorMessage("No scores were found!");
        }
        else {
            int width = SIDEBAR_COPY_WIDTH;

            if (stripControlCodes) {
                objectiveName = StringUtils.stripControlCodes(objectiveName);
            }

            // Remove scores that aren't rendered.
            scores = scores.stream().filter(input -> input.getPlayerName() != null && !input.getPlayerName().startsWith("#"))
                    .skip(Math.max(scores.size() - 15, 0)).collect(Collectors.toList());

            /*
            Minecraft renders the scoreboard from bottom to top so to keep the same order when writing it from top
            to bottom, we need to reverse the scores' order.
            */
            Collections.reverse(scores);

            for (Score score:
                    scores) {
                ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
                String playerName = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());

                // Strip colours and emoji player names.
                playerName = SIDEBAR_PLAYER_NAME_PATTERN.matcher(playerName).replaceAll("");

                if (stripControlCodes) {
                    playerName = StringUtils.stripControlCodes(playerName);
                }

                int points = score.getScorePoints();

                width = Math.max(width, (playerName + " " + points).length());
                formatter.format("%-" + width + "." +
                        width + "s %d%n", playerName, points);
            }

            // Insert the objective name at the top of the sidebar string.
            sb.insert(0, "\n").insert(0, org.apache.commons.lang3.StringUtils.center(objectiveName, width));

            copyStringToClipboard(sb.toString(), "Sidebar copied to clipboard!");
        }
    }

    /**
     * Copies the NBT data of entities around the player. The classes of {@link Entity} to include and the radius
     * around the player to copy from can be customized.
     *
     * @param includedEntityClasses the classes of entities that should be included when copying NBT data
     * @param copyRadius copy the NBT data of entities inside this radius(in blocks) around the player
     */
    public static void copyEntityData(List<Class<? extends Entity>> includedEntityClasses, int copyRadius) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        List<Entity> loadedEntitiesCopy = new LinkedList<>(Minecraft.getMinecraft().theWorld.loadedEntityList);
        ListIterator<Entity> loadedEntitiesCopyIterator;
        StringBuilder stringBuilder = new StringBuilder();

        if (includedEntityClasses == null) {
            throw new IllegalArgumentException("The list of entity types cannot be null!");
        }
        else if (includedEntityClasses.isEmpty()) {
            throw new IllegalArgumentException("The list of entity types cannot be empty!");
        }
        else if (copyRadius < 0) {
            throw new IllegalArgumentException("The entity copy radius cannot be negative!");
        }

        loadedEntitiesCopyIterator = loadedEntitiesCopy.listIterator();

        // Copy the NBT data from the loaded entities.
        while (loadedEntitiesCopyIterator.hasNext()) {
            Entity entity = loadedEntitiesCopyIterator.next();
            NBTTagCompound entityData = new NBTTagCompound();
            boolean isPartOfIncludedClasses = false;

            // Checks to ignore entities if they're irrelevant
            if (entity.getDistanceToEntity(player) > copyRadius) {
                continue;
            }

            for (Class<?> entityClass : includedEntityClasses) {
                if (entityClass.isAssignableFrom(entity.getClass())) {
                    isPartOfIncludedClasses = true;
                }
            }

            if (!isPartOfIncludedClasses) {
                continue;
            }

            entity.writeToNBT(entityData);

            // Add spacing before each new entry.
            if (stringBuilder.length() > 0) {
                stringBuilder.append(System.lineSeparator()).append(System.lineSeparator());
            }

            stringBuilder.append("Class: ").append(entity.getClass().getSimpleName()).append(System.lineSeparator());
            if (entity.hasCustomName() || EntityPlayer.class.isAssignableFrom(entity.getClass())) {
                stringBuilder.append("Name: ").append(entity.getName()).append(System.lineSeparator());
            }

            stringBuilder.append("NBT Data:").append(System.lineSeparator());
            stringBuilder.append(prettyPrintNBT(entityData));
        }

        if (stringBuilder.length() > 0) {
            copyStringToClipboard(stringBuilder.toString(), ColorCode.GREEN + "Entity data was copied to clipboard!");
        }
        else {
            SkyblockAddons.getInstance().getUtils().sendErrorMessage("No entities matching the given parameters were found.");
        }
    }

    /**
     * Copies the NBT data of entities around the player. The classes of {@link Entity} to include and the radius
     * around the player to copy from can be customized.
     *
     * @param includedEntityNames a String that is a comma-separated list of the names of the entities that should be included when copying the NBT data
     * @param copyRadius copy the NBT data of entities inside this radius around the player.
     * @see EntityList
     */
    public static void copyEntityData(String includedEntityNames, int copyRadius) {
        Matcher listMatcher = Pattern.compile("(^[A-Z_]+)(?:,[A-Z_]+)*$", Pattern.CASE_INSENSITIVE).matcher(includedEntityNames);

        if (copyRadius <= 0) {
            throw new IllegalArgumentException("The entity copy radius cannot be negative!");
        }

        if (listMatcher.matches()) {
            List<Class<? extends Entity>> entityClasses = new ArrayList<>();
            String[] entityNamesArray = includedEntityNames.split(",");

            for (String entityName : entityNamesArray) {
                if (EntityList.isStringValidEntityName(entityName)) {
                    int entityId = EntityList.getIDFromString(entityName);

                    // The default ID returned when a match isn't found is the pig's id for some reason.
                    if (entityId != 90 || entityName.equals("Pig")) {
                        entityClasses.add(EntityList.getClassFromID(entityId));
                    }
                    // EntityList doesn't have mappings for the player classes.
                    else if (entityName.equals("Player")) {
                        entityClasses.add(EntityPlayerSP.class);
                        entityClasses.add(EntityOtherPlayerMP.class);
                    }
                }
                else if (entityName.equals("PlayerSP")) {
                    entityClasses.add(EntityPlayerSP.class);
                }
                else if (entityName.equals("PlayerMP") | entityName.equals("OtherPlayerMP")) {
                    entityClasses.add(EntityOtherPlayerMP.class);
                }
                else {
                    throw new IllegalArgumentException("The entity name \"" + entityName + "\" is invalid.");
                }
            }

            copyEntityData(entityClasses, copyRadius);
        }
        else {
            throw new IllegalArgumentException("Incorrect format! Use \"Name\" or \"Name1,Name2,Name3\".");
        }
    }

    /**
     * <p>Copies the NBT data of nearby entities using the default settings.</p>
     * <br>
     * <p>Default settings:</p>
     * <p>Included Entity Types: players, armor stands, and mobs</p>
     * <p>Radius: {@link DevUtils#ENTITY_COPY_RADIUS}</p>
     * <p>Include own NBT data: {@code true}</p>
     *
     * @see EntityList
     */
    public static void copyEntityData() {
        copyEntityData(Collections.singletonList(EntityLivingBase.class), ENTITY_COPY_RADIUS);
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
     * Copies the header and footer of the tab player list to the clipboard
     *
     * @see net.minecraft.client.gui.GuiPlayerTabOverlay
     */
    public static void copyTabListHeaderAndFooter() {
        Minecraft mc = Minecraft.getMinecraft();
        IChatComponent tabHeader = mc.ingameGUI.getTabList().header;
        IChatComponent tabFooter = mc.ingameGUI.getTabList().footer;
        StringBuilder output = new StringBuilder("Header:").append("\n");

        output.append(tabHeader.getFormattedText());

        if (!tabHeader.getSiblings().isEmpty()) {
            for (IChatComponent sibling : tabHeader.getSiblings()) {
                output.append(sibling.getFormattedText());
            }
        }

        output.append("\n\n");
        output.append("Footer:").append("\n");

        output.append(tabFooter.getFormattedText());

        if (!tabFooter.getSiblings().isEmpty()) {
            for (IChatComponent sibling : tabFooter.getSiblings()) {
                output.append(sibling.getFormattedText());
            }
        }

        copyStringToClipboard(output.toString(), "Tab list header and footer copied!");
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

        } else if (tagID == Constants.NBT.TAG_BYTE_ARRAY || tagID == Constants.NBT.TAG_INT_ARRAY) {
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
            } else {
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

        } else if (tagID == Constants.NBT.TAG_LIST) {
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

        } else if (tagID == Constants.NBT.TAG_COMPOUND) {
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

                    if (key.contains("backpack_data") && currentCompoundTagElement instanceof NBTTagByteArray) {
                        try {
                            NBTTagCompound backpackData = CompressedStreamTools.readCompressed(new ByteArrayInputStream(((NBTTagByteArray)currentCompoundTagElement).getByteArray()));

                            stringBuilder.append(",").append(System.lineSeparator());
                            stringBuilder.append(key).append("(decoded): ").append(
                                    prettyPrintNBT(backpackData));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

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

    /*
     Internal methods
     */
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
