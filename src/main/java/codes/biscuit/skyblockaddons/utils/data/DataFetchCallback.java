package codes.biscuit.skyblockaddons.utils.data;

import org.apache.http.concurrent.FutureCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

/**
 * This is a simple {@link FutureCallback} to log the result of a request for debugging.
 *
 * @param <T> the type of the result, unused
 */
public class DataFetchCallback<T> implements FutureCallback<T> {
    private final Logger LOGGER;
    private final String URL_STRING;

    public DataFetchCallback(URI url) {
        LOGGER = LogManager.getLogger();
        this.URL_STRING = url.toString();
    }

    @Override
    public void completed(T result) {
        LOGGER.debug("Successfully fetched {}", URL_STRING);
    }

    @Override
    public void failed(Exception ex) {
        LOGGER.error("Failed to fetch {}", URL_STRING);
        LOGGER.error(ex.getMessage());
    }

    @Override
    public void cancelled() {
        LOGGER.debug("Cancelled fetching {}", URL_STRING);
    }
}
