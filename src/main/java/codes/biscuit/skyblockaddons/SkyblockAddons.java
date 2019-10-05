package codes.biscuit.skyblockaddons;

import codes.biscuit.skyblockaddons.commands.SkyblockAddonsCommand;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.listeners.RenderListener;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import codes.biscuit.skyblockaddons.utils.ForgeInitHelper;
import codes.biscuit.skyblockaddons.utils.InventoryUtils;
import codes.biscuit.skyblockaddons.utils.Message;
import codes.biscuit.skyblockaddons.utils.Scheduler;
import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Mod(modid = SkyblockAddons.MOD_ID, version = SkyblockAddons.VERSION, name = SkyblockAddons.MOD_NAME, clientSideOnly = true, acceptedMinecraftVersions = "[1.12,1.12.2]")
public class SkyblockAddons {

    static final String MOD_ID = "skyblockaddons";
    static final String MOD_NAME = "SkyblockAddons";
    public static final String VERSION = "1.3.4";

    private static SkyblockAddons instance; // for Mixins cause they don't have a constructor
    private ConfigValues configValues;
    private Map<Class<?>, Object> listeners = new HashMap<>();
    private Utils utils = new Utils(this);
    private InventoryUtils inventoryUtils = new InventoryUtils(this);
    private Scheduler scheduler = new Scheduler(this);
    private boolean usingLabymod = false;
    private boolean usingOofModv1 = false;
    private int openSettingsKeyIndex = -1;
    private int openEditLocationsKeyIndex = -1;
    private int lockSlotKeyIndex = -1;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        instance = this;
        configValues = new ConfigValues(this, e.getSuggestedConfigurationFile());
        ForgeInitHelper.addCommand(SkyblockAddonsCommand.class);
        ForgeInitHelper.addListener(PlayerListener.class);
        ForgeInitHelper.addListener(RenderListener.class);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(scheduler);
        ClientRegistry.registerKeyBinding(new KeyBinding("Open Settings", Keyboard.KEY_NONE, MOD_NAME));
        ClientRegistry.registerKeyBinding(new KeyBinding("Edit GUI Locations", Keyboard.KEY_NONE, MOD_NAME));
        ClientRegistry.registerKeyBinding(new KeyBinding("Lock Slot", Keyboard.KEY_L, MOD_NAME));
        int keyBindings = Minecraft.getMinecraft().gameSettings.keyBindings.length;
        openSettingsKeyIndex = keyBindings - 3;
        openEditLocationsKeyIndex = keyBindings - 2;
        lockSlotKeyIndex = keyBindings - 1;

        for (Class<?> clazz : ForgeInitHelper.LISTENER_CLASSES) {
            try {
                Object listener = clazz.getConstructor(SkyblockAddons.class).newInstance(this);
                this.listeners.put(clazz, listener);
                MinecraftForge.EVENT_BUS.register(listener);
            } catch (Exception ignored) { }
        }

        for (Class<? extends CommandBase> clazz : ForgeInitHelper.COMMAND_CLASSES) {
            try {
                CommandBase base = clazz.getConstructor(SkyblockAddons.class).newInstance(this);
                ClientCommandHandler.instance.registerCommand(base);
            } catch (Exception ignored) { }
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        configValues.loadConfig();
        loadKeyBindingDescriptions();
        usingLabymod = Loader.isModLoaded("labymod");

        if (Loader.isModLoaded("refractionoof")) {
            for (ModContainer modContainer : Loader.instance().getModList()) {
                if ("refractionoof".equals(modContainer.getModId()) && "1.0".equals(modContainer.getVersion()))
                    usingOofModv1 = true;
            }
        }

        utils.checkDisabledFeatures();
        scheduleMagmaCheck();
    }

    public void loadKeyBindingDescriptions() {
        KeyBinding[] keys = Minecraft.getMinecraft().gameSettings.keyBindings;
        keys[openSettingsKeyIndex] = new KeyBinding(Message.SETTING_SETTINGS.getMessage(), keys[openSettingsKeyIndex].getKeyCode(), keys[openSettingsKeyIndex].getKeyCategory());
        keys[openEditLocationsKeyIndex] = new KeyBinding(Message.SETTING_EDIT_LOCATIONS.getMessage(), keys[openEditLocationsKeyIndex].getKeyCode(), keys[openEditLocationsKeyIndex].getKeyCategory());
        keys[lockSlotKeyIndex] =  new KeyBinding(Message.SETTING_LOCK_SLOT.getMessage(), keys[lockSlotKeyIndex].getKeyCode(), keys[lockSlotKeyIndex].getKeyCategory());
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
        return (PlayerListener)this.listeners.get(PlayerListener.class);
    }

    public RenderListener getRenderListener() {
        return (RenderListener)this.listeners.get(RenderListener.class);
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

    public KeyBinding getOpenSettingsKey() {
        return Minecraft.getMinecraft().gameSettings.keyBindings[openSettingsKeyIndex];
    }

    public KeyBinding getOpenEditLocationsKey() {
        return Minecraft.getMinecraft().gameSettings.keyBindings[openEditLocationsKeyIndex];
    }

    public KeyBinding getLockSlot() {
        return Minecraft.getMinecraft().gameSettings.keyBindings[lockSlotKeyIndex];
    }

}