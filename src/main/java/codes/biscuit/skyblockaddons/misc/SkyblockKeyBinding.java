package codes.biscuit.skyblockaddons.misc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Message;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

import static codes.biscuit.skyblockaddons.SkyblockAddons.MOD_NAME;

@Getter
public class SkyblockKeyBinding {

    private final KeyBinding keyBinding;
    private final String name;
    private final int defaultKeyCode;
    private final Message message;
    private boolean registered = false;
    private boolean isFirstRegistration = true;
    /*
    This is the key code stored before the key binding is de-registered
    It's set to a number larger than Keyboard.KEYBOARD_SIZE by default to indicate no previous key code is stored.
     */
    private int previousKeyCode = 999;

    public SkyblockKeyBinding(String name, int defaultKey, Message message) {
        this.name = name;
        this.defaultKeyCode = defaultKey;
        this.message = message;
        keyBinding = new KeyBinding("key.skyblockaddons."+ this.getName(), this.getDefaultKeyCode(), MOD_NAME);
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
     * Adds this keybinding to {@link Minecraft#gameSettings}. If the key binding is not being registered for the first
     * time, its previous keycode setting from before its last de-registration is restored.
     */
    public void register() {
        if (registered) {
            SkyblockAddons.getLogger().error("Tried to register a key binding with the name \"" + name + "\" which is already registered.");
            return;
        }

        ClientRegistry.registerKeyBinding(keyBinding);

        if (isFirstRegistration) {
            isFirstRegistration = false;
        } else if (previousKeyCode < Keyboard.KEYBOARD_SIZE) {
            keyBinding.setKeyCode(defaultKeyCode);
            KeyBinding.resetKeyBindingArrayAndHash();
        }
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
                registered = false;
                return;
            }

            Minecraft.getMinecraft().gameSettings.keyBindings = ArrayUtils.remove(Minecraft.getMinecraft().gameSettings.keyBindings, index);

            /*
            The key binding still exists in the internal list even though it's removed from the settings menu.
            We have to set its key to KEY_NONE so it does not conflict with other key bindings.
             */
            previousKeyCode = keyBinding.getKeyCode();
            keyBinding.setKeyCode(Keyboard.KEY_NONE);
            KeyBinding.resetKeyBindingArrayAndHash();
            registered = false;
        } else {
            SkyblockAddons.getLogger().error("Tried to de-register a key binding with the name \"" + name + "\" which wasn't registered.");
        }
    }
}
