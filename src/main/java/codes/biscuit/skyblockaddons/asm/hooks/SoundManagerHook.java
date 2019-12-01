package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;

public class SoundManagerHook {

    public static float getNormalizedVolume(SoundManager soundManager, ISound sound, SoundPoolEntry entry, SoundCategory category) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main != null && main.getUtils() != null && main.getUtils().isPlayingSound()) {
            return 1;
        } else {
            return soundManager.getNormalizedVolume(sound, entry, category);
        }
    }
}
