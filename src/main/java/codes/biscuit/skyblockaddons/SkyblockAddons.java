package codes.biscuit.skyblockaddons;

import codes.biscuit.skyblockaddons.commands.SkyblockAddonsCommand;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = SkyblockAddons.MOD_ID, version = SkyblockAddons.VERSION, name = SkyblockAddons.MOD_NAME, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class SkyblockAddons {

    static final String MOD_ID = "skyblockaddons";
    static final String MOD_NAME = "SkyblockAddons";
    public static final String VERSION = "1.1.1";

    private static SkyblockAddons instance; // for Mixins cause they don't have a constructor
    private ConfigValues configValues;
    private PlayerListener playerListener = new PlayerListener(this);
    private Utils utils = new Utils(this);
    private boolean usingLabymod = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        instance = this;
        configValues = new ConfigValues(e.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(playerListener);
        ClientCommandHandler.instance.registerCommand(new SkyblockAddonsCommand(this));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        configValues.loadConfig();
        usingLabymod = Loader.isModLoaded("labymod");
    }

    public ConfigValues getConfigValues() {
        return configValues;
    }


    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public Utils getUtils() {
        return utils;
    }

    public boolean isUsingLabymod() {
        return usingLabymod;
    }

    public static SkyblockAddons getInstance() {
        return instance;
    }
}