package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Translations;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StringUtils;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Keyboard;

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

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    /** Pattern used for removing the placeholder emoji player names from the Hypixel scoreboard */
    public static final Pattern SIDEBAR_PLAYER_NAME_PATTERN = Pattern.compile("[\uD83D\uDD2B\uD83C\uDF6B\uD83D\uDCA3\uD83D\uDC7D\uD83D\uDD2E\uD83D\uDC0D\uD83D\uDC7E\uD83C\uDF20\uD83C\uDF6D\u26BD\uD83C\uDFC0\uD83D\uDC79\uD83C\uDF81\uD83C\uDF89\uD83C\uDF82]+");
    /** All possible Minecraft entity names, for tab completion */
    public static final List<String> ALL_ENTITY_NAMES = EntityList.getEntityNameList();

    // If you change this, please change it in the string "commandUsage.sba.help.copyEntity" as well.
    public static final int DEFAULT_ENTITY_COPY_RADIUS = 3;
    private static final List<Class<? extends Entity>> DEFAULT_ENTITY_NAMES = Collections.singletonList(EntityLivingBase.class);
    private static final boolean DEFAULT_SIDEBAR_FORMATTED = false;

    @Getter @Setter
    private static boolean loggingActionBarMessages = false;
    private static CopyMode copyMode = CopyMode.ENTITY;
    private static List<Class<? extends Entity>> entityNames = DEFAULT_ENTITY_NAMES;
    private static int entityCopyRadius = DEFAULT_ENTITY_COPY_RADIUS;
    private static boolean sidebarFormatted = DEFAULT_SIDEBAR_FORMATTED;

    static {
        ALL_ENTITY_NAMES.add("PlayerSP");
        ALL_ENTITY_NAMES.add("PlayerMP");
        ALL_ENTITY_NAMES.add("OtherPlayerMP");
    }

    public static void setSidebarFormatted(boolean formatted) {
        sidebarFormatted = formatted;
    }

    public static void resetSidebarFormattedToDefault() {
        sidebarFormatted = DEFAULT_SIDEBAR_FORMATTED;
    }

    /**
     * Copies the objective and scores that are being displayed on a scoreboard's sidebar.
     * When copying the sidebar, the control codes (e.g. §a) are removed.
     */
    public static void copyScoreboardSideBar() {
        copyScoreboardSidebar(sidebarFormatted);
    }

    /**
     * Copies the objective and scores that are being displayed on a scoreboard's sidebar.
     *
     * @param stripControlCodes if {@code true}, the control codes will be removed, otherwise they will be copied
     */
    private static void copyScoreboardSidebar(boolean stripControlCodes) {
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            main.getUtils().sendErrorMessage("Nothing is being displayed in the sidebar!");
            return;
        }

        ScoreObjective sideBarObjective = scoreboard.getObjectiveInDisplaySlot(1);
        if (sideBarObjective == null) {
            main.getUtils().sendErrorMessage("Nothing is being displayed in the sidebar!");
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        String objectiveName = sideBarObjective.getDisplayName();
        List<Score> scores = (List<Score>) scoreboard.getSortedScores(sideBarObjective);

        if (scores == null || scores.isEmpty()) {
            main.getUtils().sendErrorMessage("No scores were found!");
            return;
        }

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

        stringBuilder.append(objectiveName).append("\n");

        for (Score score: scores) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String playerName = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());

            // Strip colours and emoji player names.
            playerName = SIDEBAR_PLAYER_NAME_PATTERN.matcher(playerName).replaceAll("");

            if (stripControlCodes) {
                playerName = StringUtils.stripControlCodes(playerName);
            }

            int points = score.getScorePoints();

            stringBuilder.append(playerName).append("[").append(points).append("]").append("\n");
        }

        copyStringToClipboard(stringBuilder.toString(), ColorCode.GREEN + "Sidebar copied to clipboard!");
    }

    /**
     * Copies the NBT data of entities around the player. The classes of {@link Entity} to include and the radius
     * around the player to copy from can be customized.
     *
     * @param includedEntityClasses the classes of entities that should be included when copying NBT data
     * @param copyRadius copy the NBT data of entities inside this radius(in blocks) around the player
     */
    private static void copyEntityData(List<Class<? extends Entity>> includedEntityClasses, int copyRadius) {
        EntityPlayerSP player = mc.thePlayer;
        List<Entity> loadedEntitiesCopy = new LinkedList<>(mc.theWorld.loadedEntityList);
        ListIterator<Entity> loadedEntitiesCopyIterator;
        StringBuilder stringBuilder = new StringBuilder();

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
            main.getUtils().sendErrorMessage("No entities matching the given parameters were found.");
        }
    }

    public static void setEntityNamesFromString(String includedEntityNames) {
        List<Class<? extends Entity>> entityClasses = getEntityClassListFromString(includedEntityNames);
        if (entityClasses == null || entityClasses.isEmpty()) {
            main.getUtils().sendErrorMessage("The entity class list is not valid or is empty! Falling back to default.");
            resetEntityNamesToDefault();
        } else {
            entityNames = entityClasses;
        }
    }

    public static void setEntityCopyRadius(int copyRadius) {
        if (copyRadius <= 0) {
            main.getUtils().sendErrorMessage("Radius cannot be negative! Falling back to " + DEFAULT_ENTITY_COPY_RADIUS + ".");
            resetEntityCopyRadiusToDefault();
        } else {
            entityCopyRadius = copyRadius;
        }
    }

    public static void resetEntityNamesToDefault() {
        entityNames = DEFAULT_ENTITY_NAMES;
    }

    public static void resetEntityCopyRadiusToDefault() {
        entityCopyRadius = DEFAULT_ENTITY_COPY_RADIUS;
    }

    /**
     * <p>Copies the NBT data of nearby entities using the default settings.</p>
     * <br>
     * <p>Default settings:</p>
     * <p>Included Entity Types: players, armor stands, and mobs</p>
     * <p>Radius: {@link DevUtils#DEFAULT_ENTITY_COPY_RADIUS}</p>
     * <p>Include own NBT data: {@code true}</p>
     *
     * @see EntityList
     */
    public static void copyEntityData() {
        copyEntityData(entityNames, entityCopyRadius);
    }

    /**
     * Compiles a list of entity classes from a string.
     *
     * @param text The string to parse
     * @return The list of entities
     */
    private static List<Class<? extends Entity>> getEntityClassListFromString(String text) {
        Matcher listMatcher = Pattern.compile("(^[A-Z_]+)(?:,[A-Z_]+)*$", Pattern.CASE_INSENSITIVE).matcher(text);

        if (!listMatcher.matches()) {
            return null;
        }

        List<Class<? extends Entity>> entityClasses = new ArrayList<>();
        String[] entityNamesArray = text.split(",");

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
            } else if (entityName.equals("PlayerSP")) {
                entityClasses.add(EntityPlayerSP.class);
            } else if (entityName.equals("PlayerMP") | entityName.equals("OtherPlayerMP")) {
                entityClasses.add(EntityOtherPlayerMP.class);
            } else {
                main.getUtils().sendErrorMessage("The entity name \"" + entityName + "\" is invalid. Skipping!");
            }
        }

        return entityClasses;
    }

    public static void copyData() {
        if (copyMode == CopyMode.ENTITY) {
            copyEntityData();

        } else if (copyMode == CopyMode.BLOCK) {
            copyBlockData();

        } else if (copyMode == CopyMode.SIDEBAR) {
            copyScoreboardSideBar();

        } else if (copyMode == CopyMode.TAB_LIST) {
            copyTabListHeaderAndFooter();
        }
    }

    /**
     * Copies the provided NBT tag to the clipboard as a formatted string.
     *
     * @param nbtTag the NBT tag to copy
     * @param message the message to show in chat when the NBT tag is copied
     */
    public static void copyNBTTagToClipboard(NBTBase nbtTag, String message) {
        if (nbtTag == null) {
            main.getUtils().sendErrorMessage("This item has no NBT data!");
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
        IChatComponent tabHeader = mc.ingameGUI.getTabList().header;
        IChatComponent tabFooter = mc.ingameGUI.getTabList().footer;

        if (tabHeader == null && tabFooter == null) {
            main.getUtils().sendErrorMessage("There is no header or footer!");
            return;
        }

        StringBuilder output = new StringBuilder();

        if (tabHeader != null) {
            output.append("Header:").append("\n");
            output.append(tabHeader.getFormattedText());
            output.append("\n\n");
        }

        if (tabHeader != null) {
            output.append("Footer:").append("\n");
            output.append(tabFooter.getFormattedText());
        }

        copyStringToClipboard(output.toString(),  ColorCode.GREEN + "Successfully copied the tab list header and footer!");
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
     * @return the server brand if the client is connected to a server, {@code null} otherwise
     */
    public static String getServerBrand() {
        final Pattern SERVER_BRAND_PATTERN = Pattern.compile("(.+) <- (?:.+)");

        if (!mc.isSingleplayer()) {
            Matcher matcher = SERVER_BRAND_PATTERN.matcher(mc.thePlayer.getClientBrand());

            if (matcher.find()) {
                // Group 1 is the server brand.
                return matcher.group(1);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Copy the block data with its tile entity data if the block has one.
     */
    public static void copyBlockData() {
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK ||
                mc.objectMouseOver.getBlockPos() == null) {
            main.getUtils().sendErrorMessage("You are not looking at a block!");
            return;
        }

        BlockPos blockPos = mc.objectMouseOver.getBlockPos();

        IBlockState blockState = mc.theWorld.getBlockState(blockPos);
        if (mc.theWorld.getWorldType() != WorldType.DEBUG_WORLD) {
            blockState = blockState.getBlock().getActualState(blockState, mc.theWorld, blockPos);
        }

        TileEntity tileEntity = mc.theWorld.getTileEntity(blockPos);
        NBTTagCompound nbt = new NBTTagCompound();
        if (tileEntity != null) {
            NBTTagCompound nbtTileEntity = new NBTTagCompound();
            tileEntity.writeToNBT(nbtTileEntity);
            nbt.setTag("tileEntity", nbtTileEntity);
        } else {
            nbt.setInteger("x", blockPos.getX());
            nbt.setInteger("y", blockPos.getY());
            nbt.setInteger("z", blockPos.getZ());
        }

        nbt.setString("type", Block.blockRegistry.getNameForObject(blockState.getBlock()).toString());
        blockState.getProperties().forEach((key, value) -> nbt.setString(key.getName(), value.toString()));

        writeToClipboard(prettyPrintNBT(nbt), ColorCode.GREEN + "Successfully copied the block data!");
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
            main.getUtils().sendMessage(successMessage);
        } catch (IllegalStateException exception) {
            main.getUtils().sendErrorMessage("Clipboard not available!");
        }
    }

    public static void setCopyMode(CopyMode copyMode) {
        DevUtils.copyMode = copyMode;
        main.getUtils().sendMessage(ColorCode.YELLOW + Translations.getMessage("messages.copyModeSet", copyMode, Keyboard.getKeyName(main.getDeveloperCopyNBTKey().getKeyCode())));
    }

    public enum CopyMode {
        ENTITY,
        BLOCK,

        TAB_LIST,
        SIDEBAR
    }
}
