package codes.biscuit.skyblockaddons;

import codes.biscuit.skyblockaddons.commands.SkyblockAddonsCommand;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = SkyblockAddons.MOD_ID, version = SkyblockAddons.VERSION, name = SkyblockAddons.MOD_NAME, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class SkyblockAddons {

    static final String MOD_ID = "skyblockaddons";
    static final String MOD_NAME = "SkyblockAddons";
    public static final String VERSION = "1.0-b5";

    public static SkyblockAddons INSTANCE; // for Mixins cause they don't have a constructor
    private ConfigValues configValues;
    private PlayerListener playerListener = new PlayerListener(this);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        INSTANCE = this;
        this.configValues = new ConfigValues(e.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(playerListener);
        ClientCommandHandler.instance.registerCommand(new SkyblockAddonsCommand(this));
        configValues.loadConfig();
    }

    public ConfigValues getConfigValues() {
        return configValues;
    }


    public PlayerListener getPlayerListener() {
        return playerListener;
    }
}