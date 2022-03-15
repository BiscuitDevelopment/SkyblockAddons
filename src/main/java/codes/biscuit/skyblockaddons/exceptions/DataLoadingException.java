package codes.biscuit.skyblockaddons.exceptions;

import static net.minecraft.util.EnumChatFormatting.DARK_RED;

/**
 * This exception is thrown when the mod fails to load a necessary data file during startup.
 */
public class DataLoadingException extends LoadingException {
    private static final String ERROR_MESSAGE_FORMAT = "Failed to load file at\n" + DARK_RED + "%s";

    public DataLoadingException(String filePathString, Throwable cause) {
        super(String.format(ERROR_MESSAGE_FORMAT, filePathString), cause, true);
    }
}