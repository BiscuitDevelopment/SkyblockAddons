package codes.biscuit.skyblockaddons.utils.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple {@link java.lang.Thread.UncaughtExceptionHandler} that prints the thread name,
 * exception message, and list of incomplete fetch requests when a data fetching thread in
 * {@link DataUtils} throws an uncaught exception.
 */
public class UncaughtFetchExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Logger logger;

    public UncaughtFetchExceptionHandler() {
        this.logger = LogManager.getLogger();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Exception in thread \"{}\"", t.getName());
        logger.error(e.getMessage());
    }
}
