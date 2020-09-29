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
public class SkyblockAddonsSetup implements IFMLCallHook {
    private static final String LOGGER_NAME = SkyblockAddons.MOD_NAME + " Setup";
    @Getter
    private static boolean deobfuscated;
    @Getter
    private static boolean usingNotchMappings;

    /**
     * Checks that need to run before the transformers are initialized
     */
    static void runPreInitChecks() {
        logDebug("Running pre-init checks...");

        // Environment Obfuscation check
        deobfuscated = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
        usingNotchMappings = !deobfuscated;

        logDebug("Pre-init checks complete.");
        logDebug("Results:");
        logDebug("De-obfuscated Environment: %b", deobfuscated);
        logDebug("Using Notch Mappings: %b", usingNotchMappings);
    }

    /**
     * Checks that need to run after the transformers are initialized but before the transformers are used
     */
    void runPreTransformationChecks() {
        logDebug( "Running pre-transformation checks...");

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
                        throw new RuntimeException("Launch failed because a duplicate installation of" +
                                " SkyblockAddons was found. Please remove it and restart Minecraft.");
                    }
                }
            }

            nameField.setAccessible(false);
        } catch (NoSuchFieldException e) {
            logError(e,"The name field wasn't found. Duplicate check failed.");
        } catch (IllegalAccessException e) {
            logError(e,"The name field can't be accessed. Duplicate check failed.");
        } catch (NullPointerException e) {
            logError(e, "The name field was set to null. This really shouldn't happen. Duplicate check failed.");
        }

        logDebug("Pre-transformation checks complete.");
    }

    /*
    Logging methods for adding the logger name to the beginning of log messages
    These are required since Minecraft excludes logger names when writing to the log file.
     */
    private static String addLoggerName(String format) {
        return String.format("[%s] %s", LOGGER_NAME, format);
    }

    private static void logDebug(String format) {
        FMLRelaunchLog.log(LOGGER_NAME, Level.DEBUG, addLoggerName(format));
    }

    private static void logDebug(String format, Object... data) {
        FMLRelaunchLog.log(LOGGER_NAME, Level.DEBUG, addLoggerName(format), data);
    }

    private static void logError(Throwable ex, String format) {
        FMLRelaunchLog.log(LOGGER_NAME, Level.ERROR, ex, addLoggerName(format));
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // unused
    }

    @Override
    public Void call() {
        runPreTransformationChecks();
        return null;
    }
}
