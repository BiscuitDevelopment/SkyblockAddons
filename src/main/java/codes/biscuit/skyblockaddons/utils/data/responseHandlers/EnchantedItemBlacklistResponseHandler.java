package codes.biscuit.skyblockaddons.utils.data.responseHandlers;

import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemBlacklist;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This class is the response handler for the EnchantedItemBlacklist {@code HTTP GET} request in
 * {@link codes.biscuit.skyblockaddons.utils.data.DataReader}. It deserializes the JSON response content into an
 * {@link EnchantedItemBlacklist} object.
 */
public class EnchantedItemBlacklistResponseHandler implements ResponseHandler<EnchantedItemBlacklist> {
    private final Gson GSON = new Gson();

    @Override
    public EnchantedItemBlacklist handleResponse(HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();

        if (status == 200) {
            HttpEntity entity = response.getEntity();
            JsonReader jsonReader = new JsonReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));

            return GSON.fromJson(jsonReader, EnchantedItemBlacklist.class);
        }
        else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }
}
