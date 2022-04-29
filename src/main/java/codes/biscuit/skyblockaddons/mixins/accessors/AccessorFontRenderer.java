package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FontRenderer.class)
public interface AccessorFontRenderer {
    @Accessor
    float getRed();

    @Accessor
    float getGreen();

    @Accessor
    float getBlue();

    @Accessor
    float getAlpha();

    @Accessor
    float getPosX();

    @Accessor
    float getPosY();
}
