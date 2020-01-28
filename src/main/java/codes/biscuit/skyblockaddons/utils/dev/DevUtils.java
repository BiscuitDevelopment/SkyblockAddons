package codes.biscuit.skyblockaddons.utils.dev;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.nbt.NBTTagCompound;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * This is a class of utilities for Skyblock Addons developers.
 *
 * @author ILikePlayingGames
 * @version 1.0
 */
public class DevUtils {

    public static void copyNBTToClipboard(NBTTagCompound nbtTagCompound) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection outputString;

        outputString = new StringSelection(nbtTagCompound.toString());

        try {
            clipboard.setContents(outputString, outputString);
            SkyblockAddons.getInstance().getUtils().sendMessage("NBT data copied to clipboard.");
        } catch (IllegalStateException exception) {
            SkyblockAddons.getInstance().getUtils().sendErrorMessage("Clipboard not available.");
        }

    }
}
