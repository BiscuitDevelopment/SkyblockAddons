package codes.biscuit.skyblockaddons.utils.data;

import codes.biscuit.skyblockaddons.exceptions.LoadingException;
import lombok.Getter;
import lombok.NonNull;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpRequestFutureTask;

import java.net.URI;
import java.util.concurrent.ExecutionException;

public class RemoteFileRequest<T> {
    protected static final String NO_DATA_RECEIVED_ERROR = "No data received for get request to \"%s\"";
    private final String REQUEST_URL;
    private final ResponseHandler<T> RESPONSE_HANDLER;
    private final FutureCallback<T> FETCH_CALLBACK;
    private final boolean ESSENTIAL;

    @Getter
    private HttpRequestFutureTask<T> futureTask;

    public RemoteFileRequest(@NonNull String requestPath, @NonNull ResponseHandler<T> responseHandler) {
        REQUEST_URL = DataConstants.CDN_BASE_URL + requestPath;
        RESPONSE_HANDLER = responseHandler;
        FETCH_CALLBACK = new DataFetchCallback<>(URI.create(REQUEST_URL));
        ESSENTIAL = false;
        futureTask = null;
    }

    public RemoteFileRequest(@NonNull String requestPath, @NonNull ResponseHandler<T> responseHandler,
                             @NonNull FutureCallback<T> fetchCallback) {
        REQUEST_URL = DataConstants.CDN_BASE_URL + requestPath;
        RESPONSE_HANDLER = responseHandler;
        FETCH_CALLBACK = fetchCallback;
        ESSENTIAL = false;
        futureTask = null;
    }

    public RemoteFileRequest(@NonNull String requestPath, @NonNull ResponseHandler<T> responseHandler,
                             boolean essential) {
        REQUEST_URL = DataConstants.CDN_BASE_URL + requestPath;
        RESPONSE_HANDLER = responseHandler;
        FETCH_CALLBACK = new DataFetchCallback<>(URI.create(REQUEST_URL));
        ESSENTIAL = essential;
        futureTask = null;
    }

    public RemoteFileRequest(@NonNull String requestPath, @NonNull ResponseHandler<T> responseHandler,
                             boolean essential, boolean usingCustomUrl) {
        REQUEST_URL = usingCustomUrl ? requestPath : DataConstants.CDN_BASE_URL + requestPath;
        RESPONSE_HANDLER = responseHandler;
        FETCH_CALLBACK = new DataFetchCallback<>(URI.create(REQUEST_URL));
        ESSENTIAL = essential;
        futureTask = null;
    }

    public RemoteFileRequest(@NonNull String requestPath, @NonNull ResponseHandler<T> responseHandler,
                             @NonNull FutureCallback<T> fetchCallback, boolean essential) {
        REQUEST_URL = DataConstants.CDN_BASE_URL + requestPath;
        RESPONSE_HANDLER = responseHandler;
        FETCH_CALLBACK = fetchCallback;
        ESSENTIAL = essential;
        futureTask = null;
    }

    public void execute(@NonNull FutureRequestExecutionService executionService) {
        futureTask = executionService.execute(new HttpGet(REQUEST_URL), null, RESPONSE_HANDLER, FETCH_CALLBACK);
    }

    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        throw new LoadingException(String.format("Loading method not implemented for file %s",
                REQUEST_URL.substring(REQUEST_URL.lastIndexOf('/' + 1))), new RuntimeException());
    }

    public String getUrl() {
        return REQUEST_URL;
    }

    public boolean isEssential() {
        return ESSENTIAL;
    }

    protected T getResult() throws InterruptedException, ExecutionException, RuntimeException {
        return futureTask.get();
    }

    protected boolean isDone() {
        return futureTask.isDone();
    }
}
