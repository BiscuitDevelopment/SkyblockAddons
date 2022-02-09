package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import org.apache.logging.log4j.message.*;

/**
 * This is a simple {@code MessageFactory} implementation that adds the mod name and logger display name in square
 * brackets to the beginning of each log event message. This is required since the Minecraft Log4J config doesn't
 * include logger names when writing logs to file.
 */
public class SkyblockAddonsMessageFactory extends AbstractMessageFactory {
    private static final String MESSAGE_FORMAT = "[" + SkyblockAddons.MOD_NAME + "%s] %s";
    private final String LOGGER_DISPLAY_NAME;

    /**
     * Creates a new instance of {@code SkyblockAddonsMessageFactory} that uses the given logger display name.
     * The display name should be the simple class name of the class that is creating the logger.
     * The logger display name will be shown as a {@code [SkyblockAddons/loggerDisplayName]} prefix added to the
     * beginning of all logger messages.
     *
     * @param loggerDisplayName the name of the logger
     */
    public SkyblockAddonsMessageFactory(String loggerDisplayName) {
        LOGGER_DISPLAY_NAME = loggerDisplayName;
    }

    @Override
    public Message newMessage(final Object message) {
        return getFormattedMessage(new ObjectMessage(message));
    }

    @Override
    public Message newMessage(String message) {
        return getFormattedMessage(new SimpleMessage(message));
    }

    @Override
    public Message newMessage(String message, Object... params) {
        return getFormattedMessage(new ParameterizedMessage(message, params));
    }

    private FormattedMessage getFormattedMessage(Message message) {
        if (!LOGGER_DISPLAY_NAME.equals(SkyblockAddons.MOD_NAME)) {
            return new FormattedMessage(MESSAGE_FORMAT, '/' + LOGGER_DISPLAY_NAME, message.getFormattedMessage());
        } else {
            return new FormattedMessage(MESSAGE_FORMAT, null, message.getFormattedMessage());
        }
    }
}
