package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Gui.class)
public interface AccessorGui {

    @Accessor("zLevel")
    void setZLevel(float zLevel);

    @Invoker("drawGradientRect")
    void drawGradientRect(int x, int y, int width, int height, int startColor, int endColor);
}
