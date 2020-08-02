package codes.biscuit.skyblockaddons.tweaker;

import lombok.Getter;
import net.minecraft.launchwrapper.Launch;

public class PreTransformationChecks {

    @Getter
    private static boolean deobfuscated;
    @Getter
    private static boolean usingNotchMappings;

    static void runChecks() {
        // Environment Obfuscation checks
        deobfuscated = false;

        deobfuscated = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

        usingNotchMappings = !deobfuscated;
    }
}
