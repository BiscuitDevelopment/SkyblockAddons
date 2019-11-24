package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;
import net.minecraft.launchwrapper.Launch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SoundManagerHook {

    private static Method getNormalizedVolume = null;

    public static float getNormalizedVolume(SoundManager soundManager, ISound sound, SoundPoolEntry entry, SoundCategory category) {
        try {
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (main != null && main.getUtils() != null && main.getUtils().isPlayingSound()) {
                return 1;
            } else {
                if (getNormalizedVolume == null) {
                    getNormalizedVolume = soundManager.getClass().getDeclaredMethod((boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment") ? "getNormalizedVolume" : "func_148594_a",
                            ISound.class, SoundPoolEntry.class, SoundCategory.class);
                    getNormalizedVolume.setAccessible(true);
                }
                return (float)getNormalizedVolume.invoke(soundManager, sound, entry, category);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return 0.5F; // A good middle-ground in case things fail.
    }
}
