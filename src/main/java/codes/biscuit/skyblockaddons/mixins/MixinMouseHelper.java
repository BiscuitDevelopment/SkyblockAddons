package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MouseHelper.class)
public class MixinMouseHelper {

    @Redirect(
            method = "ungrabMouseCursor",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/input/Mouse;setCursorPosition(II)V",
                    ordinal = 0
            )
    )
    public void ungrabMouseCursor() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isEnabled(Feature.DONT_RESET_CURSOR_INVENTORY) && !main.getPlayerListener().shouldResetMouse())
            Mouse.setCursorPosition(Mouse.getX(), Mouse.getY());
        else
            Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
    }

}
