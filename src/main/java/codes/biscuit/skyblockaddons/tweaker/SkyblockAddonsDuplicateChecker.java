package codes.biscuit.skyblockaddons.tweaker;

import net.minecraftforge.fml.relauncher.FMLRelaunchLog;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * This class contains a series of checks that need to be run before the SkyblockAddons transformers are used.
 */
public class SkyblockAddonsDuplicateChecker implements IFMLCallHook {

    /**
     * Checks that need to run after the transformers are initialized but before the transformers are used
     */
    @Override
    public Void call() {
        log(Level.INFO, "Searching for duplicate SkyblockAddons installations!");

        try {
            Field nameField = Class.forName("net.minecraftforge.fml.relauncher.CoreModManager$FMLPluginWrapper").getField("name");
            boolean coreFound = false;

            nameField.setAccessible(true);

            for (Object coreMod : SkyblockAddonsLoadingPlugin.getCoremodList()) {
                String name = (String) nameField.get(coreMod);

                if (name.equals(SkyblockAddonsLoadingPlugin.class.getSimpleName())) {
                    if (!coreFound) {
                        coreFound = true;
                    } else {
                        throw new RuntimeException("Launch failed because a duplicate installation of SkyblockAddons was found. Please remove it and restart Minecraft!");
                    }
                }
            }

            nameField.setAccessible(false);
        } catch (ReflectiveOperationException ex) {
            log(Level.ERROR, "An error occurred while checking for duplicate SkyblockAddons installations!");
            ex.printStackTrace();
            // It's okay, this is just for duplicate detection anyways...
        }

        log(Level.INFO, "Search for duplicate SkyblockAddons installations complete!");

        return null;
    }

    public void log(Level level, String message) {
        String name = "SkyblockAddons/" + this.getClass().getSimpleName();
        FMLRelaunchLog.log(name, level, (SkyblockAddonsTransformer.isDeobfuscated() ? "" : "[" + name + "] ") + message);
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }
}
