package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.features.enchants.EnchantManager;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class EnchantmentsRequest extends RemoteFileRequest<EnchantManager.Enchants> {
    public EnchantmentsRequest() {
        super("skyblock/enchants.json", new JSONResponseHandler<>(new TypeToken<EnchantManager.Enchants>() {}.getType()));
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        EnchantManager.setEnchants(Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
    }
}
