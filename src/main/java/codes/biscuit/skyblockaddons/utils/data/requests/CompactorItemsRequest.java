package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.skyblockdata.CompactorItem;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class CompactorItemsRequest extends RemoteFileRequest<HashMap<String, CompactorItem>> {
    public CompactorItemsRequest() {
        super("skyblock/compactorItems.json", new JSONResponseHandler<>(
                new TypeToken<HashMap<String, CompactorItem>>() {}.getType()));
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        ItemUtils.setCompactorItems(Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
    }
}
