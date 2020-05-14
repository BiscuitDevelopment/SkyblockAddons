package codes.biscuit.skyblockaddons.tweaker;

import lombok.Getter;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the setup class for SkyblockAddons. It is used to get and store data from the FML core mod manager that is
 * used when applying the SkyblockAddons transformers.
 */
public class SkyblockAddonsSetup implements IFMLCallHook {
    @Getter private static SkyblockAddonsSetup instance;

    // Data injected from FML
    private static HashMap<String, Object> callData;
    private static Logger logger;

    // Variables for storing the injected data for easy access
    private static LaunchClassLoader classLoader;
    @Getter private static boolean deobfuscatedEnvironment;
    @Getter private static boolean runtimeDeobfuscationEnabled;
    @Getter private static boolean usingLabyModClient;

    /**
     * Creates a new instance of {@code SkyblockAddonsSetup}.
     * This is called in the FML Core Mod Manager before SBA is loaded as a core mod.
     *
     * @see net.minecraftforge.fml.relauncher.CoreModManager
     */
    public SkyblockAddonsSetup() {
        instance = this;
        callData = new HashMap<>();
        logger = LogManager.getLogger("SkyblockAddons Setup");
    }

    /**
     * Injected with data from the FML environment:
     * "runtimeDeobfuscationEnabled" : true if runtime deobfuscation is enabled
     * "mcLocation" : The Minecraft folder
     * "classLoader" : The FML Class Loader
     * "coremodLocation" : The folder core mods are located in
     * "deobfuscationFileName" : I'm not sure what this is
     *
     * @param data the injected data from FML
     */
    @Override
    public void injectData(Map<String, Object> data) {
        callData.putAll(data);
    }

    /**
     * Runs setup tasks that are necessary for the loading of SkyblockAddons.
     * This method is called before SBA is loaded as a core mod
     *
     * @return unused
     */
    @Override
    public Void call() {
        logger.debug("SBA Setup Called");
        loadData();
        checkIfUsingLabyModClient();

        return null;
    }

    /**
     * Checks if the LabyMod Client is being used and sets the flag {@code usingLabyModClient}.
     */
    private void checkIfUsingLabyModClient() {
        Class<?> launchClass = null;
        Class<?> labyModAddonClass = null;

        try {
            launchClass = classLoader.findClass("net.minecraft.launchwrapper.Launch");
            labyModAddonClass = classLoader.findClass("net.labymod.api.LabyModAddon");

            logger.debug("LabyMod Client detected!");
        } catch (ClassNotFoundException e) {
            logger.debug("LabyMod Client not found!");
        }

        usingLabyModClient = launchClass != null && labyModAddonClass != null;
    }

    /**
     * Loads data injected in {@link #injectData(Map)} into variables in this class.
     */
    private void loadData() {
        classLoader = (LaunchClassLoader) callData.get("classLoader");
        deobfuscatedEnvironment = !(boolean) callData.get("runtimeDeobfuscationEnabled");
        runtimeDeobfuscationEnabled = (boolean) callData.get("runtimeDeobfuscationEnabled");
    }
}
