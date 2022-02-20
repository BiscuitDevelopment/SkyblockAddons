package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CooldownsRequest extends RemoteFileRequest<HashMap<String, Integer>> {
    public CooldownsRequest() {
        super("skyblock/cooldowns.json", new JSONResponseHandler<>(new TypeToken<HashMap<String, Integer>>() {
        }.getType()));
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        CooldownManager.setItemCooldowns(Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
    }
}
