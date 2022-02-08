package codes.biscuit.skyblockaddons.utils.data;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * This is a {@link ResponseHandler} that returns an object deserialized to the given type from a JSON response if the
 * response was successful (code 200). If the response has any other error code, {@link HttpResponseException} is thrown.
 *
 * @param <T> the type to deserialize the JSON to
 */
public class JSONResponseHandler<T> implements ResponseHandler<T> {
    private static final Gson gson = SkyblockAddons.getGson();
    private final Type type;

    /**
     * Creates a new {@code JSONResponseHandler} with the {@link Type} to deserialize the response to.
     *
     * @param type the {@code Type} to deserialize the response to
     */
    public JSONResponseHandler(Type type) {
        this.type = type;
    }

    @Override
    public T handleResponse(HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();

        if (status == 200) {
            if (entity != null) {
                return gson.fromJson(EntityUtils.toString(entity, StandardCharsets.UTF_8), type);
            } else {
                return null;
            }
        } else {
            EntityUtils.consume(entity);
            throw new HttpResponseException(status, "Unexpected response status: " + status);
        }
    }
}
