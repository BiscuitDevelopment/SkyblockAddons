package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreature;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreatureManager;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.reflect.TypeToken;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class SeaCreaturesRequest extends RemoteFileRequest<Map<String, SeaCreature>> {
    public SeaCreaturesRequest() {
        super("skyblock/seaCreatures.json", new JSONResponseHandler<>(
                new TypeToken<Map<String, SeaCreature>>() {}.getType()));
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        SeaCreatureManager.getInstance().setSeaCreatures(Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
    }
}
