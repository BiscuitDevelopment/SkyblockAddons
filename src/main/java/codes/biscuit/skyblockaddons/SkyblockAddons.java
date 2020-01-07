package codes.biscuit.skyblockaddons;

import codes.biscuit.skyblockaddons.commands.SkyblockAddonsCommand;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.listeners.RenderListener;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
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

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

@Mod(modid = SkyblockAddons.MOD_ID, version = SkyblockAddons.VERSION, name = SkyblockAddons.MOD_NAME, clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class SkyblockAddons {

    static final String MOD_ID = "skyblockaddons";
    static final String MOD_NAME = "SkyblockAddons";
    public static final String VERSION = "1.4.2";

    private static SkyblockAddons instance; // for Mixins cause they don't have a constructor
    private ConfigValues configValues;
    private PersistentValues persistentValues;
    private PlayerListener playerListener = new PlayerListener(this);
    private RenderListener renderListener = new RenderListener(this);
    private Utils utils = new Utils(this);
    private InventoryUtils inventoryUtils = new InventoryUtils(this);
    private Scheduler scheduler = new Scheduler(this);
    private boolean usingLabymod = false;
    private boolean usingOofModv1 = false;
    private KeyBinding[] keyBindings = new KeyBinding[4];

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        instance = this;
        configValues = new ConfigValues(this, e.getSuggestedConfigurationFile());
        persistentValues = new PersistentValues(e.getSuggestedConfigurationFile());
    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(playerListener);
        MinecraftForge.EVENT_BUS.register(renderListener);
        MinecraftForge.EVENT_BUS.register(scheduler);
        ClientCommandHandler.instance.registerCommand(new SkyblockAddonsCommand(this));

        keyBindings[0] = new KeyBinding("key.skyblockaddons.open_settings", Keyboard.KEY_NONE, MOD_NAME);
        keyBindings[1] = new KeyBinding("key.skyblockaddons.edit_gui", Keyboard.KEY_NONE, MOD_NAME);
        keyBindings[2] = new KeyBinding("key.skyblockaddons.lock_slot", Keyboard.KEY_L, MOD_NAME);
        keyBindings[3] = new KeyBinding("key.skyblockaddons.freeze_backpack", Keyboard.KEY_F, MOD_NAME);

        for (KeyBinding keyBinding : keyBindings) {
            ClientRegistry.registerKeyBinding(keyBinding);
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        configValues.loadConfig();
        persistentValues.loadValues();
        loadKeyBindingDescriptions();

        usingLabymod = Loader.isModLoaded("labymod");
        if (Loader.isModLoaded("refractionoof")) {
            for (ModContainer modContainer : Loader.instance().getModList()) {
                if (modContainer.getModId().equals("refractionoof") && modContainer.getVersion().equals("1.0")) {
                    usingOofModv1 = true;
                }
            }
        }
        utils.checkDisabledFeatures();
        utils.getFeaturedURLOnline();
        scheduleMagmaCheck();

        for (Feature feature : Feature.values()) {
            if (feature.isGuiFeature()) {
                feature.getSettings().add(EnumUtils.FeatureSetting.GUI_SCALE);
            }
            if (feature.isColorFeature()) {
                feature.getSettings().add(EnumUtils.FeatureSetting.COLOR);
            }
        }
    }

    private void changeKeyBindDescription(KeyBinding bind, String desc) {
        try {
            Field field = bind.getClass().getDeclaredField(SkyblockAddonsTransformer.isDeobfuscated() ? "keyDescription" : "field_74515_c");
            field.setAccessible(true);
            field.set(bind, desc);
        } catch(NoSuchFieldException | IllegalAccessException e) {
            System.out.println("Could not change key description: " + bind.toString());
            e.printStackTrace();
        }
    }

    public void loadKeyBindingDescriptions() {
        changeKeyBindDescription(keyBindings[0], Message.SETTING_SETTINGS.getMessage());
        changeKeyBindDescription(keyBindings[1], Message.SETTING_EDIT_LOCATIONS.getMessage());
        changeKeyBindDescription(keyBindings[2], Message.SETTING_LOCK_SLOT.getMessage());
        changeKeyBindDescription(keyBindings[3], Message.SETTING_SHOW_BACKPACK_PREVIEW.getMessage());
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
    
    public PersistentValues getPersistentValues() {
    	return persistentValues;
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

    public KeyBinding getOpenSettingsKey() {
        return keyBindings[0];
    }

    public KeyBinding getOpenEditLocationsKey() {
        return keyBindings[1];
    }

    public KeyBinding getLockSlotKey() {
        return keyBindings[2];
    }

    public KeyBinding getFreezeBackpackKey() {
        return keyBindings[3];
    }
}