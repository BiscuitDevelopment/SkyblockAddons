package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.SkillXpManager;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class SkillXpRequest extends RemoteFileRequest<SkillXpManager.JsonInput> {
    public SkillXpRequest() {
        super("skyblock/skillXp.json", new JSONResponseHandler<>(SkillXpManager.JsonInput.class));
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        SkyblockAddons.getInstance().getSkillXpManager().initialize(Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
    }
}
