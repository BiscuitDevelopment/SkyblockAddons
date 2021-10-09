package codes.biscuit.skyblockaddons.utils.pojo;

import com.google.gson.JsonObject;
import lombok.Data;

@Data
public class SkyblockAddonsAPIResponse {

    private boolean success;
    private JsonObject response;
}
