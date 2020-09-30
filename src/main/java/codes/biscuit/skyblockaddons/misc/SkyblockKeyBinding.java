package codes.biscuit.skyblockaddons.misc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Message;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.ArrayUtils;

import static codes.biscuit.skyblockaddons.SkyblockAddons.MOD_NAME;

@Getter
public class SkyblockKeyBinding {

    private final KeyBinding keyBinding;
    private final String name;
    private final int defaultKey;
    private final Message message;
    private boolean registered = false;

    public SkyblockKeyBinding(String name, int defaultKey, Message message) {
        this.name = name;
        this.defaultKey = defaultKey;
        this.message = message;
        keyBinding = new KeyBinding("key.skyblockaddons."+ this.getName(), this.getDefaultKey(), MOD_NAME);
    }

    // TODO localize errors?

    /**
     * Returns the current key code for this key binding.
     *
     * @return the current key code for this key binding
     */
    public int getKeyCode() {
        return keyBinding.getKeyCode();
    }

    /**
     * Returns true if the key is pressed (used for continuous querying). Should be used in tickers.
     * JavaDoc is from linked method.
     *
     * @see KeyBinding#isKeyDown()
     */
    public boolean isKeyDown() {
        if (registered) {
            return keyBinding.isKeyDown();
        }
        else {
            return false;
        }
    }

    /**
     * Returns true on the initial key press. For continuous querying use {@link this#isKeyDown()}. Should be used in key
     * events.
     * JavaDoc is from linked method.
     *
     * @see KeyBinding#isPressed()
     */
    public boolean isPressed() {
        if (registered) {
            return keyBinding.isPressed();
        }
        else {
            return false;
        }
    }

    /**
     * Adds this keybinding to {@link Minecraft#gameSettings}.
     */
    public void register() {
        ClientRegistry.registerKeyBinding(keyBinding);
        registered = true;
    }

    /**
     * Removes this keybinding from {@link Minecraft#gameSettings}.
     */
    public void deRegister() {
        if (registered) {
            int index = ArrayUtils.indexOf(Minecraft.getMinecraft().gameSettings.keyBindings, keyBinding);

            if (index == ArrayUtils.INDEX_NOT_FOUND) {
                SkyblockAddons.getLogger().error("Keybinding was registered but no longer exists in the registry. Something else must have removed it." +
                        " This shouldn't happen; please inform an SBA developer.");
                return;
            }

            Minecraft.getMinecraft().gameSettings.keyBindings = ArrayUtils.remove(Minecraft.getMinecraft().gameSettings.keyBindings, index);
            registered = false;

        } else {
            SkyblockAddons.getLogger().error("Tried to de-register a key binding with the name \"" + name + "\" which wasn't registered.");
        }
    }
}
