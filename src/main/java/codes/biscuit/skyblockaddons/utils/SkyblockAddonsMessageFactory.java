package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;

/**
 * This is a simple {@code MessageFactory} implementation that adds the logger name in square brackets
 * to the beginning of each log event message. This is required since the Minecraft Log4J config doesn't
 * include logger names when writing logs to file.
 */
public class SkyblockAddonsMessageFactory implements MessageFactory {

    private String loggerName;

    /**
     * Creates a new instance of {@code SkyblockAddonsMessageFactory} that uses the given logger name. The logger name
     * in square brackets will be added as a prefix before every message that this instance generates.
     *
     * @param loggerName the name of the logger
     */
    public SkyblockAddonsMessageFactory(String loggerName) {
        this.loggerName = loggerName;
    }

    @Override
    public Message newMessage(Object message) {
        return new SimpleMessage(getMessageWithLoggerName(message.toString()));
    }

    @Override
    public Message newMessage(String message) {
        return new SimpleMessage(getMessageWithLoggerName(message));
    }

    @Override
    public Message newMessage(String message, Object... params) {
        return new ParameterizedMessage(getMessageWithLoggerName(message), params);
    }

    private String getMessageWithLoggerName(String message) {
        return String.format("[%s/%s] %s", SkyblockAddons.MOD_NAME, loggerName, message);
    }
}
