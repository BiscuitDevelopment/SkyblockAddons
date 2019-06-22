package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SoundManager.class)
public abstract class MixinSoundManager {

    @Shadow protected abstract float getNormalizedVolume(ISound sound, SoundPoolEntry entry, SoundCategory category);

    // This makes sure our warning plays at full volume (only affected by master volume switch).
    @Redirect(method = "playSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundManager;getNormalizedVolume(Lnet/minecraft/client/audio/ISound;Lnet/minecraft/client/audio/SoundPoolEntry;Lnet/minecraft/client/audio/SoundCategory;)F", ordinal = 0))
    private float setVolumeBypass(SoundManager soundManager, ISound sound, SoundPoolEntry entry, SoundCategory category) {
        if (SkyblockAddons.INSTANCE.getPlayerListener().isBossWarning() && sound.getSoundLocation().getResourcePath().equals("random.orb")) {
            return 1;
        } else {
            return getNormalizedVolume(sound, entry, category);
        }
    }
}
