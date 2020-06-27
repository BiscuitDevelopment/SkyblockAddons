package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.Message;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.settings.KeyBinding;

@Getter
public class SkyblockKeyBinding {

    @Setter private KeyBinding keyBinding;
    private String name;
    private int defaultKey;
    private Message message;

    public SkyblockKeyBinding(String name, int defaultKey, Message message) {
        this.name = name;
        this.defaultKey = defaultKey;
        this.message = message;
    }
}
