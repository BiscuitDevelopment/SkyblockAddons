package codes.biscuit.skyblockaddons.utils.data.requests;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Language;
import codes.biscuit.skyblockaddons.utils.data.JSONResponseHandler;
import codes.biscuit.skyblockaddons.utils.data.RemoteFileRequest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class LocalizedStringsRequest extends RemoteFileRequest<JsonObject> {

    public LocalizedStringsRequest(@NonNull Language language) {
        //TODO: Fix this so it runs after getting language manifest
        super(String.format(
                "https://distributions.crowdin.net/d7578b29286a48bcaf7fec87zgb/content/main/src/main/resources/lang/%s.json",
                        language.getPath()),
                new JSONResponseHandler<>(JsonObject.class),
                language != Language.ENGLISH, true);
    }

    @Override
    public void load() throws InterruptedException, ExecutionException, RuntimeException {
        SkyblockAddons main = SkyblockAddons.getInstance();
        overwriteCommonJsonMembers(main.getConfigValues().getLanguageConfig(),
                Objects.requireNonNull(getResult(), NO_DATA_RECEIVED_ERROR));
    }

    /**
     * This is used to merge in the online language entries into the existing ones.
     * Using this method rather than an overwrite allows new entries in development to still exist.
     *
     * @param baseObject   The object to be merged in to (local entries).
     * @param otherObject The object to be merged (online entries).
     */
    private static void overwriteCommonJsonMembers(JsonObject baseObject, JsonObject otherObject) {
        for (Map.Entry<String, JsonElement> entry : otherObject.entrySet()) {
            String memberName = entry.getKey();
            JsonElement otherElement = entry.getValue();

            if (otherElement.isJsonObject()) {
                // If the base object already has this object, then recurse
                if (baseObject.has(memberName) && baseObject.get(memberName).isJsonObject()) {
                    JsonObject baseElementObject = baseObject.getAsJsonObject(memberName);
                    overwriteCommonJsonMembers(baseElementObject, otherElement.getAsJsonObject());

                    // Otherwise we have to add a new object first, then recurse
                } else {
                    JsonObject baseElementObject = new JsonObject();
                    baseObject.add(memberName, baseElementObject);
                    overwriteCommonJsonMembers(baseElementObject, otherElement.getAsJsonObject());
                }

                // If it's a string, then just add or overwrite the base version
            } else if (otherElement.isJsonPrimitive() && otherElement.getAsJsonPrimitive().isString()) {
                baseObject.add(memberName, otherElement);
            }
        }
    }
}
