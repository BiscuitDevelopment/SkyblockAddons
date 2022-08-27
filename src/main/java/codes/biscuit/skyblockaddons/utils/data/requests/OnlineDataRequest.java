package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.OnlineData;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class OnlineDataRequest extends RemoteFileRequest<OnlineData> {
    public OnlineDataRequest() {
        super("skyblockaddons/data.json", new JSONResponseHandler<>(OnlineData.class));
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        SkyblockAddons main = SkyblockAddons.getInstance();
        main.setOnlineData(Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
        main.getUpdater().checkForUpdate();
    }
}
