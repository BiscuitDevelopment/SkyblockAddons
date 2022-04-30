package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EffectRenderer.class)
public interface AccessorEffectRenderer {

    @Accessor
    ResourceLocation getParticleTextures();

    @Accessor
    TextureManager getRenderer();
}
