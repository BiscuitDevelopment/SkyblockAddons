package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SoundManager.class)
public interface AccessorSoundManager {

    @Invoker("getNormalizedVolume")
    float getNormalizedVolume(ISound sound, SoundPoolEntry entry, SoundCategory category);

}
