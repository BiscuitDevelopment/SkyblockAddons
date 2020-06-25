package codes.biscuit.skyblockaddons;

import codes.biscuit.skyblockaddons.commands.SkyblockAddonsCommand;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.listeners.GuiScreenListener;
import codes.biscuit.skyblockaddons.listeners.NetworkListener;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.listeners.RenderListener;
import codes.biscuit.skyblockaddons.scheduler.NewScheduler;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.discord.DiscordRPCManager;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@Getter
@Mod(modid = "skyblockaddons", name = "SkyblockAddons", version = "@VERSION@", clientSideOnly = true, acceptedMinecraftVersions = "@MOD_ACCEPTED@", updateJSON = "@UPDATE_JSON@")
public class SkyblockAddons {

    public static final String MOD_ID = "skyblockaddons";
    public static final String MOD_NAME = "SkyblockAddons";
    public static String VERSION = "@VERSION@";

    /** The main instance of the mod, used mainly my mixins who don't get it passed to them. */
    @Getter private static SkyblockAddons instance;

    private ConfigValues configValues;
    private Logger logger;
    private PersistentValues persistentValues;
    private PlayerListener playerListener;
    private GuiScreenListener guiScreenListener;
    private RenderListener renderListener;
    private InventoryUtils inventoryUtils;
    private Utils utils;
    private Updater updater;
    @Setter private OnlineData onlineData;

    /** Get the scheduler that be can be used to easily execute tasks. */
    private Scheduler scheduler = new Scheduler(this);
    private NewScheduler newScheduler = new NewScheduler();
    private boolean usingLabymod = false;
    private boolean usingOofModv1 = false;

    /** Whether developer mode is enabled. */
    @Setter private boolean devMode = false;
    @Setter(AccessLevel.NONE) private KeyBinding[] keyBindings = new KeyBinding[4];
    private DiscordRPCManager discordRPCManager;

    @Getter private final Set<Integer> registeredFeatureIDs = new HashSet<>();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        instance = this;
        configValues = new ConfigValues(this, e.getSuggestedConfigurationFile());
        logger = LogManager.getLogger();
        persistentValues = new PersistentValues(e.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        // Initialize event listeners
        playerListener = new PlayerListener(this);
        guiScreenListener = new GuiScreenListener(this);
        renderListener = new RenderListener(this);
        discordRPCManager = new DiscordRPCManager(this);

        MinecraftForge.EVENT_BUS.register(new NetworkListener());
        MinecraftForge.EVENT_BUS.register(playerListener);
        MinecraftForge.EVENT_BUS.register(guiScreenListener);
        MinecraftForge.EVENT_BUS.register(renderListener);
        MinecraftForge.EVENT_BUS.register(scheduler);
        MinecraftForge.EVENT_BUS.register(newScheduler);

        // Initialize utilities
        inventoryUtils = new InventoryUtils(this);
        utils = new Utils(this);
        updater = new Updater(this);

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
        onlineData = new Gson().fromJson(new JsonReader(utils.getBufferedReader("data.json")), OnlineData.class);
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

        updater.processUpdateCheckResult();
        utils.pullOnlineData();
        scheduleMagmaBossCheck();

        for (Feature feature : Feature.values()) {
            if (feature.isGuiFeature()) feature.getSettings().add(EnumUtils.FeatureSetting.GUI_SCALE);
            if (feature.isColorFeature()) feature.getSettings().add(EnumUtils.FeatureSetting.COLOR);
        }

        if (configValues.isEnabled(Feature.FANCY_WARP_MENU)) {
            // Load in these textures so they don't lag the user loading them in later...
            for (IslandWarpGui.Island island : IslandWarpGui.Island.values()) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(island.getResourceLocation());
            }
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(SkyblockAddonsGui.LOGO);
        Minecraft.getMinecraft().getTextureManager().bindTexture(SkyblockAddonsGui.LOGO_GLOW);
    }

    @Mod.EventHandler
    public void stop(FMLModDisabledEvent e) {
        discordRPCManager.stop();
    }


    private void changeKeyBindDescription(KeyBinding bind, String desc) {
        try {
            Field field = bind.getClass().getDeclaredField(SkyblockAddonsTransformer.isDeobfuscated() ? "keyDescription" : "field_74515_c");
            field.setAccessible(true);
            field.set(bind, desc);
        } catch(NoSuchFieldException | IllegalAccessException e) {
            logger.error("Could not change key description: " + bind.toString());
            logger.catching(e);
        }
    }

    public void loadKeyBindingDescriptions() {
        changeKeyBindDescription(keyBindings[0], Message.SETTING_SETTINGS.getMessage());
        changeKeyBindDescription(keyBindings[1], Message.SETTING_EDIT_LOCATIONS.getMessage());
        changeKeyBindDescription(keyBindings[2], Message.SETTING_LOCK_SLOT.getMessage());
        changeKeyBindDescription(keyBindings[3], Message.SETTING_FREEZE_BACKPACK_PREVIEW.getMessage());
    }

    private void scheduleMagmaBossCheck() {
        // Loop every 5s until the player is in game, where it will pull once.
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null) {
                    utils.fetchMagmaBossEstimate();
                    cancel();
                }
            }
        }, 5000, 5000);
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

    static {
        //noinspection ConstantConditions
        if (VERSION.contains("@")) { // Debug environment...
            VERSION = "1.5.1";
        }
    }
}
