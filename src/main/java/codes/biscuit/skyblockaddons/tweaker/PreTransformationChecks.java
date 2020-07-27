package codes.biscuit.skyblockaddons.tweaker;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Map;

public class PreTransformationChecks {
    @Getter
    private static boolean labymodClient;
    @Getter
    private static boolean deobfuscated;
    @Getter
    private static boolean usingNotchMappings;

    @SuppressWarnings("unchecked")
    static void runChecks() {
        deobfuscated = false;
        labymodClient = false;
        boolean foundLaunchClass = false;

        try {
            Class<?> launch = Class.forName("net.minecraft.launchwrapper.Launch");
            Field blackboardField = launch.getField("blackboard");
            Map<String,Object> blackboard = (Map<String, Object>) blackboardField.get(null);
            deobfuscated = (boolean) blackboard.get("fml.deobfuscatedEnvironment");
            foundLaunchClass = true;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ex) {
            // If the class doesn't exist, its probably just obfuscated labymod client, so leave it false.
        }

        try {
            Class.forName("net.labymod.api.LabyModAddon"); // Try to find a labymod class.
            PreTransformationChecks.labymodClient = !foundLaunchClass; // If the launch class is also found, they are probably using labymod for forge and not the client.
        } catch (ClassNotFoundException ex) {
            // They just aren't using LabyMod.
        }

        usingNotchMappings = !deobfuscated;
    }
}
