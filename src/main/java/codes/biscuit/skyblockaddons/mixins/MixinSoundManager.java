package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SoundManager.class)
public abstract class MixinSoundManager {

    @Shadow
    protected abstract float getClampedVolume(ISound sound);

    // Modifying master volume for a single sound is very complex, don't do it.

    // This makes sure our warning plays at full volume (only affected by master volume switch).
    @Redirect(method = "playSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundManager;getClampedVolume(Lnet/minecraft/client/audio/ISound;)F", ordinal = 0))
    private float getNormalizedVolumeBypass(SoundManager soundManager, ISound sound) {
        return SkyblockAddons.getInstance().getUtils().isPlayingSound() ? 1 : getClampedVolume(sound);
    }

}