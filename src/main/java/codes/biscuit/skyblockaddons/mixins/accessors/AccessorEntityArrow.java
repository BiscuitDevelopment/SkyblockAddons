package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.entity.projectile.EntityArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityArrow.class)
public interface AccessorEntityArrow {

    @Accessor
    boolean getInGround();
}
