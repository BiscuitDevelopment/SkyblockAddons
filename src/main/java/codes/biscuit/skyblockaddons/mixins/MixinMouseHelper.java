package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {

    /**
     * @author Biscuit, ToxicAven
     * @reason Prevent resetting mouse cursor across GUIs
     */
    @Overwrite
    public void ungrabMouseCursor() {
        ungrabMouseCursor(Display.getWidth() / 2, Display.getHeight() / 2);
        Mouse.setGrabbed(false);
    }

    private static void ungrabMouseCursor(int new_x, int new_y) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isDisabled(Feature.DONT_RESET_CURSOR_INVENTORY) || main.getPlayerListener().shouldResetMouse()) {
            Mouse.setCursorPosition(new_x, new_y);
            Mouse.setGrabbed(false);
        }
    }
}
