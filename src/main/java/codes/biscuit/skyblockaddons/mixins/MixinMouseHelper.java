package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHelper.class)
public abstract class MixinMouseHelper {

    @Redirect(method = "ungrabMouseCursor", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;setCursorPosition(II)V", ordinal = 0))
    private void ungrabMouseCursor(int new_x, int new_y) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isDisabled(Feature.DONT_RESET_CURSOR_INVENTORY) || main.getPlayerListener().shouldResetMouse()) {
            Mouse.setCursorPosition(new_x, new_y);
        }
    }

}
