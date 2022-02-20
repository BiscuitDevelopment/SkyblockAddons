package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerData;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ContainersRequest extends RemoteFileRequest<HashMap<String, ContainerData>> {
    public ContainersRequest() {
        super("skyblock/containers.json", new JSONResponseHandler<>(new TypeToken<HashMap<String, ContainerData>>() {
        }.getType()));
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        ItemUtils.setContainers(Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
    }
}
