package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemLists;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemPlacementBlocker;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class EnchantedItemListsRequest extends RemoteFileRequest<EnchantedItemLists> {
    public EnchantedItemListsRequest() {
        super("skyblockaddons/enchantedItemLists.json", new JSONResponseHandler<>(EnchantedItemLists.class));
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        EnchantedItemPlacementBlocker.setItemLists(Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
    }
}
