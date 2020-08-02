package codes.biscuit.skyblockaddons.tweaker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLRelaunchLog;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * This class contains a series of checks that need to be run before the SkyblockAddons transformers are used.
 */
public class PreTransformationChecks implements IFMLCallHook {
    private static final String loggerName = SkyblockAddons.MOD_NAME + " PTC";
    @Getter
    private static boolean deobfuscated;
    @Getter
    private static boolean usingNotchMappings;

    /**
     * Checks that need to run before the transformers are initialized
     */
    static void runPreInitChecks() {
        FMLRelaunchLog.log(loggerName, Level.DEBUG, "Running pre-init checks...");

        // Environment Obfuscation checks
        deobfuscated = false;

        deobfuscated = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

        usingNotchMappings = !deobfuscated;
        FMLRelaunchLog.log(loggerName, Level.DEBUG, "Pre-init checks complete.");
        FMLRelaunchLog.log(loggerName, Level.DEBUG, "Results:");
        FMLRelaunchLog.log(loggerName, Level.DEBUG, "De-obfuscated Environment: %b", deobfuscated);
        FMLRelaunchLog.log(loggerName, Level.DEBUG, "Using Notch Mappings: %b", usingNotchMappings);
    }

    /**
     * Checks that need to run after the transformers are initialized but before the transformers are used
     */
    static void runPreTransformationChecks() {
        // TODO Localize the errors
        FMLRelaunchLog.log(loggerName, Level.DEBUG, "Running pre-transformation checks...");

        // Duplicate SkyblockAddons check
        List<Object> coreMods = SkyblockAddonsLoadingPlugin.coreMods;

        try {
            Field nameField = coreMods.get(0).getClass().getField("name");
            boolean sbaCoreFound = false;

            nameField.setAccessible(true);

            for (Object coreMod : coreMods) {
                String name = (String) nameField.get(coreMod);

                if (name.equals(SkyblockAddonsLoadingPlugin.class.getSimpleName()) || name.equals("SkyblockAddons Core")) {
                    if (!sbaCoreFound) {
                        sbaCoreFound = true;
                    }
                    else {
                        throw new RuntimeException("A duplicate installation of SkyblockAddons was found." +
                                " Please remove it and restart Minecraft.");
                    }
                }
            }

            nameField.setAccessible(false);
        } catch (NoSuchFieldException e) {
            FMLRelaunchLog.log(loggerName, Level.ERROR, e,"The name field wasn't found. Duplicate check failed.");
        } catch (IllegalAccessException e) {
            FMLRelaunchLog.log(loggerName, Level.ERROR, e,"The name field can't be accessed. Duplicate check failed.");
        }

        FMLRelaunchLog.log(loggerName, Level.DEBUG, "Pre-transformation checks complete.");
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // unused
    }

    @Override
    public Void call() {
        PreTransformationChecks.runPreTransformationChecks();
        return null;
    }
}
