package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface AccessorKeyBinding {

    @Accessor
    void setKeyDescription(String description);
}
