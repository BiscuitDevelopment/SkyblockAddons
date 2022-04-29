package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TileEntitySpecialRenderer.class)
public interface AccessorTileEntitySpecialRenderer {

    @Invoker("bindTexture")
    void bindTexture(ResourceLocation texture);
}
