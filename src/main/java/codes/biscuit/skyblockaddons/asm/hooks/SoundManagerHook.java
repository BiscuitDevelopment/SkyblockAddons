package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsSetup;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SoundManagerHook {

    private static Method getNormalizedVolume = null;

    public static float getNormalizedVolume(SoundManager soundManager, ISound sound, SoundPoolEntry entry, SoundCategory category) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main != null && main.getUtils() != null && main.getUtils().isPlayingSound()) {
            return 1;
        } else {
            if (SkyblockAddonsSetup.isUsingLabyModClient()) { // There are no access transformers in LabyMod.
                try {
                    if (getNormalizedVolume == null) {
                        getNormalizedVolume = soundManager.getClass().getDeclaredMethod("a", ISound.class, SoundPoolEntry.class, SoundCategory.class);
                        getNormalizedVolume.setAccessible(true);
                    }
                    if (getNormalizedVolume != null) {
                        return (float)getNormalizedVolume.invoke(soundManager, sound, entry, category);
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                return soundManager.getNormalizedVolume(sound, entry, category);
            }
        }

        return 0.5F; // A good middle ground in case the labymod reflection fails.
    }
}
