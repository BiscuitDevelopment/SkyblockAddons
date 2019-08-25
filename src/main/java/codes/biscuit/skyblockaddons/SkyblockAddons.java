package codes.biscuit.skyblockaddons;

import codes.biscuit.skyblockaddons.commands.SkyblockAddonsCommand;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.listeners.RenderListener;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import codes.biscuit.skyblockaddons.utils.InventoryUtils;
import codes.biscuit.skyblockaddons.utils.Scheduler;
import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Timer;
import java.util.TimerTask;

@Mod(modid = SkyblockAddons.MOD_ID, version = SkyblockAddons.VERSION, name = SkyblockAddons.MOD_NAME, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class SkyblockAddons {

    static final String MOD_ID = "skyblockaddons";
    static final String MOD_NAME = "SkyblockAddons";
    public static final String VERSION = "1.2.1";

    private static SkyblockAddons instance; // for Mixins cause they don't have a constructor
    private ConfigValues configValues;
    private PlayerListener playerListener = new PlayerListener(this);
    private RenderListener renderListener = new RenderListener(this);
    private Utils utils = new Utils(this);
    private InventoryUtils inventoryUtils = new InventoryUtils(this);
    private Scheduler scheduler = new Scheduler(this);
    private boolean usingLabymod = false;
    private boolean usingOofModv1 = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        instance = this;
        configValues = new ConfigValues(this, e.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(playerListener);
        MinecraftForge.EVENT_BUS.register(renderListener);
        MinecraftForge.EVENT_BUS.register(scheduler);
        ClientCommandHandler.instance.registerCommand(new SkyblockAddonsCommand(this));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        configValues.loadConfig();
        usingLabymod = Loader.isModLoaded("labymod");
        if (Loader.isModLoaded("refractionoof")) {
            for (ModContainer modContainer : Loader.instance().getModList()) {
                if (modContainer.getModId().equals("refractionoof") && modContainer.getVersion().equals("1.0")) {
                    usingOofModv1 = true;
                }
            }
        }
        utils.checkDisabledFeatures();
        scheduleMagmaCheck();
    }

    private void scheduleMagmaCheck() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (Minecraft.getMinecraft() != null) {
                    utils.fetchEstimateFromServer();
                } else {
                    scheduleMagmaCheck();
                }
            }
        }, 5000);
    }

    public ConfigValues getConfigValues() {
        return configValues;
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public RenderListener getRenderListener() {
        return renderListener;
    }

    public Utils getUtils() {
        return utils;
    }

    public InventoryUtils getInventoryUtils() {
        return inventoryUtils;
    }

    public boolean isUsingLabymod() {
        return usingLabymod;
    }

    public static SkyblockAddons getInstance() {
        return instance;
    }

    public boolean isUsingOofModv1() {
        return usingOofModv1;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}