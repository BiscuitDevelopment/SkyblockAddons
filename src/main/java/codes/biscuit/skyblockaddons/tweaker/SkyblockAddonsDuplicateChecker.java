package codes.biscuit.skyblockaddons.tweaker;

import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.FMLRelaunchLog;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * This class contains a series of checks that need to be run before the SkyblockAddons transformers are used.
 * This check only works with mod version 1.6.0+ since it only works with versions with this check implemented.
 * In earlier versions, their coremod loads first and gets called twice for reasons I don't really understand.
 * This check will never run in that circumstance.
 */
public class SkyblockAddonsDuplicateChecker implements IFMLCallHook {

    /**
     * Checks that need to run after the transformers are initialized but before the transformers are used
     */
    @Override
    public Void call() {
        logDebug("Searching for duplicate SkyblockAddons installations...");

        try {
            Field loadPluginsField = CoreModManager.class.getDeclaredField("loadPlugins");
            loadPluginsField.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<Object> coremodList = (List<Object>) loadPluginsField.get(null);

            Field nameField = coremodList.get(0).getClass().getField("name");
            boolean coreFound = false;

            nameField.setAccessible(true);

            for (Object coreMod : coremodList) {
                String name = (String) nameField.get(coreMod);

                if (name.equals(SkyblockAddonsLoadingPlugin.class.getSimpleName())) {
                    if (!coreFound) {
                        coreFound = true;
                    } else {
                        throw new RuntimeException("Launch failed because a duplicate installation of SkyblockAddons was found." +
                                " Please remove it and restart Minecraft!");
                    }
                }
            }

            nameField.setAccessible(false);

            logDebug("No duplicates installations were found");
        } catch (ReflectiveOperationException ex) {
            log(Level.ERROR, ex, "An error occurred while checking for duplicate SkyblockAddons installations!");
            // It's okay, this is just for duplicate detection anyways...
        }

        return null;
    }

    /**
     * This method writes a message to the game log at the given log level, along with a {@code Throwable}, if provided.
     * The mod name and class name are added to the beginning of the message if the mod is running in a production
     * environment since the Minecraft client does not log this info.
     *
     * @param level the log level to write to
     * @param message the message
     */
    private void log(Level level, Throwable throwable, String message) {
        String loggerName = "SkyblockAddons/" + this.getClass().getSimpleName();

        if (throwable != null) {
            FMLRelaunchLog.log(loggerName, level, throwable, (SkyblockAddonsTransformer.isDeobfuscated() ? "" : "[" + loggerName + "] ") + message);
        } else {
            FMLRelaunchLog.log(loggerName, level, (SkyblockAddonsTransformer.isDeobfuscated() ? "" : "[" + loggerName + "] ") + message);
        }
    }

    /**
     * This method writes a message to the game log at the {@code DEBUG} level.
     * The mod name and class name are added to the beginning of the message if the mod is running in a production
     * environment since the Minecraft client does not log this info.
     *
     * @param message the message
     */
    private void logDebug(String message) {
        log(Level.DEBUG, null, message);
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }
}
