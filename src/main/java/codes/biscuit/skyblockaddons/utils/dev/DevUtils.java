package codes.biscuit.skyblockaddons.utils.dev;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This is a class of utilities for Skyblock Addons developers.
 *
 * @author ILikePlayingGames
 * @version 1.0
 */
public class DevUtils {
    private static final int ENTITY_COPY_RADIUS = 3;

    /**
     * Copies the provided NBT tag to the clipboard as a formatted string.
     *
     * @param nbtTag the NBT tag to copy
     * @param message the message to show in chat when the NBT tag is copied
     */
    public static void copyNBTTagToClipboard(NBTBase nbtTag, String message) {
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
     * Copies a list of strings to the clipboard, each on separate lines.
     *
     * @param strings the string to copy
     */
    public static void copyStringsToClipboard(List<String> strings) {
        writeToClipboard(buildMultiLineStringFromList(strings), "Value was copied to clipboard!");
    }

    /**
     * <p>Copies a list of strings to the clipboard, each on separate lines.</p>
     * <p>Also shows the provided message in chat when successful</p>
     *
     * @param strings the strings to copy
     * @param successMessage the custom message to show after successful copy
     */
    public static void copyStringsToClipboard(List<String> strings, String successMessage) {
        writeToClipboard(buildMultiLineStringFromList(strings), successMessage);
    }

    /**
     * Returns the radius for copying entity data
     *
     * @return the radius for copying entity data
     */
    public static int getEntityCopyRadius() {
        return ENTITY_COPY_RADIUS;
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
            if (nbtTagCompound.hasNoTags()) {
                stringBuilder.append('}');
            }
            else {
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

                stringBuilder.append(System.lineSeparator()).append('}');
            }
        }
        // This includes the tags: byte, short, int, long, float, double, and string
        else {
            stringBuilder.append(nbt.toString());
        }

        return stringBuilder.toString();
    }

    // Internal methods

    // Converts a list of strings to a single multi-line string
    private static String buildMultiLineStringFromList(List<String> strings) {
        ListIterator<String> listIterator = strings.listIterator();
        StringBuilder stringBuilder = new StringBuilder();

        while (listIterator.hasNext()) {
            String currentString = listIterator.next();

            stringBuilder.append(currentString);

            // Go to the next line.
            if (listIterator.hasNext()) {
                stringBuilder.append(System.lineSeparator());
            }
        }

        return stringBuilder.toString();
    }

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
