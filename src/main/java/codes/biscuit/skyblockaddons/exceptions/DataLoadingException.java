package codes.biscuit.skyblockaddons.exceptions;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;

import java.util.Arrays;
import java.util.List;

import static net.minecraft.util.EnumChatFormatting.*;

/**
 * This exception is thrown when the mod fails to load a necessary data file during startup.
 */
public class DataLoadingException extends CustomModLoadingErrorDisplayException {
    private final String FILE_PATH_STRING;

    public DataLoadingException(String filePathString) {
        FILE_PATH_STRING = filePathString;
    }

    public DataLoadingException(String filePathString, Throwable cause) {
        super(filePathString, cause);
        FILE_PATH_STRING = filePathString;
    }

    @Override
    public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
    }

    @Override
    public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
        int maxWidth = errorScreen.width - 80;
        int offset = 50;
        int xPos = errorScreen.width / 2;

        errorScreen.drawDefaultBackground();
        errorScreen.drawCenteredString(fontRenderer, String.format(
                "%sSkyblockAddons%s has encountered an error while loading.", AQUA, RESET), xPos,
                offset, 0xFFFFFF);
        offset += 10;
        errorScreen.drawCenteredString(fontRenderer, "Failed to load file at", xPos, offset, 0xFFFFFF);
        offset += 10;
        List<String> filePathStringList = fontRenderer.listFormattedStringToWidth(DARK_RED + FILE_PATH_STRING + RESET,
                maxWidth);
        for (String line : filePathStringList) {
            errorScreen.drawCenteredString(fontRenderer, line, xPos, offset, 0xFFFFFF);
            offset += 10;
        }
        offset += 10;
        errorScreen.drawCenteredString(fontRenderer, "Please restart your game.", xPos, offset, 0xFFFFFF);
        offset += 10;
        List<String> errorPersistString = fontRenderer.listFormattedStringToWidth(String.format("If error persists after restarting, please report it at " +
                "%sdiscord.gg/biscuit.%s", BOLD, RESET), maxWidth);
        for (String line : errorPersistString) {
            errorScreen.drawCenteredString(fontRenderer, line, xPos, offset, 0xFFFFFF);
            offset += 10;
        }
    }

    @Override
    public void printStackTrace(WrappedPrintStream s) {
        s.println(String.format("Failed to load file at \"%s\"", FILE_PATH_STRING));
        if (getCause() != null) {
            Arrays.stream(getCause().getStackTrace()).forEach(
                    stackTraceElement -> s.println(stackTraceElement.toString()));
            super.printStackTrace(s);
        }
    }
}