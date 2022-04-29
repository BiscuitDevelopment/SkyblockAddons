package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.particle.EntityFX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityFX.class)
public interface AccessorEntityFX {

    @Accessor
    float getParticleScale();

    @Accessor
    void setParticleScale(float scale);
}
