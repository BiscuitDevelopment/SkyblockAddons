package codes.biscuit.skyblockaddons;

import codes.biscuit.skyblockaddons.commands.SkyblockAddonsCommand;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.config.PersistentValuesManager;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.core.OnlineData;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonManager;
import codes.biscuit.skyblockaddons.core.seacreatures.SeaCreatureManager;
import codes.biscuit.skyblockaddons.features.discordrpc.DiscordRPCManager;
import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.listeners.GuiScreenListener;
import codes.biscuit.skyblockaddons.listeners.NetworkListener;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.listeners.RenderListener;
import codes.biscuit.skyblockaddons.misc.SkyblockKeyBinding;
import codes.biscuit.skyblockaddons.misc.Updater;
import codes.biscuit.skyblockaddons.misc.scheduler.NewScheduler;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import codes.biscuit.skyblockaddons.newgui.GuiManager;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.gson.GsonInitializableTypeAdapter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
@Mod(modid = "skyblockaddons", name = "SkyblockAddons", version = "@VERSION@", clientSideOnly = true, acceptedMinecraftVersions = "@MOD_ACCEPTED@")
public class SkyblockAddons {

    public static final String MOD_ID = "skyblockaddons";
    public static final String MOD_NAME = "SkyblockAddons";
    public static String VERSION = "@VERSION@";

    @Getter private static SkyblockAddons instance;
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(EnumMap.class, (InstanceCreator<EnumMap>) type -> {
                Type[] types = (((ParameterizedType) type).getActualTypeArguments());
                return new EnumMap((Class<?>) types[0]);
            })
            .registerTypeAdapterFactory(new GsonInitializableTypeAdapter())
            .create();

    private static final Executor THREAD_EXECUTOR = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNameFormat(SkyblockAddons.MOD_NAME + " - #%d").build());

    private ConfigValues configValues;
    private PersistentValuesManager persistentValuesManager;
    private PlayerListener playerListener;
    private GuiScreenListener guiScreenListener;
    private RenderListener renderListener;
    private InventoryUtils inventoryUtils;
    private Utils utils;
    private Updater updater;
    @Setter private OnlineData onlineData;
    private DiscordRPCManager discordRPCManager;
    private Scheduler scheduler;
    private NewScheduler newScheduler;
    private DungeonManager dungeonManager;
    private GuiManager guiManager;

    private boolean usingLabymod;
    private boolean usingOofModv1;
    @Setter private boolean devMode;
    private List<SkyblockKeyBinding> keyBindings = new LinkedList<>();

    @Getter private final Set<Integer> registeredFeatureIDs = new HashSet<>();

    public SkyblockAddons() {
        instance = this;

        playerListener = new PlayerListener();
        guiScreenListener = new GuiScreenListener();
        renderListener = new RenderListener();
        inventoryUtils = new InventoryUtils();
        utils = new Utils();
        updater = new Updater();
        scheduler = new Scheduler();
        newScheduler = new NewScheduler();
        dungeonManager = new DungeonManager();
        discordRPCManager = new DiscordRPCManager();
        guiManager = new GuiManager();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        configValues = new ConfigValues(e.getSuggestedConfigurationFile());
        persistentValuesManager = new PersistentValuesManager(e.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new NetworkListener());
        MinecraftForge.EVENT_BUS.register(playerListener);
        MinecraftForge.EVENT_BUS.register(guiScreenListener);
        MinecraftForge.EVENT_BUS.register(renderListener);
        MinecraftForge.EVENT_BUS.register(scheduler);
        MinecraftForge.EVENT_BUS.register(newScheduler);

        ClientCommandHandler.instance.registerCommand(new SkyblockAddonsCommand());

        Collections.addAll(keyBindings, new SkyblockKeyBinding("open_settings", Keyboard.KEY_NONE, Message.SETTING_SETTINGS),
                new SkyblockKeyBinding( "edit_gui", Keyboard.KEY_NONE, Message.SETTING_EDIT_LOCATIONS),
                new SkyblockKeyBinding( "lock_slot", Keyboard.KEY_L, Message.SETTING_LOCK_SLOT),
                new SkyblockKeyBinding( "freeze_backpack", Keyboard.KEY_F, Message.SETTING_FREEZE_BACKPACK_PREVIEW),
                new SkyblockKeyBinding("copy_NBT", Keyboard.KEY_RCONTROL, Message.KEY_DEVELOPER_COPY_NBT));

        // Don't register the developer mode key on startup.
        registerKeyBindings(keyBindings.subList(0, 4));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        DataUtils.readLocalAndFetchOnline();
        configValues.loadValues();
        persistentValuesManager.loadValues();

        setKeyBindingDescriptions();

        usingLabymod = utils.isModLoaded("labymod");
        usingOofModv1 = utils.isModLoaded("refractionoof", "1.0");

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

    private void scheduleMagmaBossCheck() {
        // Loop every 5s until the player is in game, where it will pull once.
        newScheduler.scheduleRepeatingTask(new SkyblockRunnable() {
            @Override
            public void run() {
                if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().thePlayer != null) {
                    utils.fetchMagmaBossEstimate();
                    cancel();
                }
            }
        }, 20*5, 20*5);
    }

    public KeyBinding getOpenSettingsKey() {
        return keyBindings.get(0).getKeyBinding();
    }

    public KeyBinding getOpenEditLocationsKey() {
        return keyBindings.get(1).getKeyBinding();
    }

    public KeyBinding getLockSlotKey() {
        return keyBindings.get(2).getKeyBinding();
    }

    public KeyBinding getFreezeBackpackKey() {
        return keyBindings.get(3).getKeyBinding();
    }

    public SkyblockKeyBinding getDeveloperCopyNBTKey() {
        return keyBindings.get(4);
    }

    public void registerKeyBindings(List<SkyblockKeyBinding> keyBindings) {
        for (SkyblockKeyBinding keybinding: keyBindings) {
            keybinding.register();
        }
    }

    public void setKeyBindingDescriptions() {
        for (SkyblockKeyBinding skyblockKeyBinding : keyBindings) {
            skyblockKeyBinding.getKeyBinding().keyDescription = skyblockKeyBinding.getMessage().getMessage();
        }
    }

    public static Gson getGson() {
        return GSON;
    }

    /**
     * @return a {@code Logger} containing the name of the calling class in the prefix.
     */
    public static Logger getLogger() {
        String fullClassName = new Throwable().getStackTrace()[1].getClassName();
        String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

        String loggerName = MOD_NAME + "/" + simpleClassName;

        if (SkyblockAddonsTransformer.isDeobfuscated()) {
            return LogManager.getLogger(loggerName);
        } else {
            return LogManager.getLogger(loggerName, new SkyblockAddonsMessageFactory(loggerName));
        }
    }

    public static void runAsync(Runnable runnable) {
        StackTraceElement stackTraceElement = new Throwable().getStackTrace()[1];
        String fullClassName = stackTraceElement.getClassName();
        String methodName = stackTraceElement.getMethodName();
        String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

        THREAD_EXECUTOR.execute(() -> {
            getLogger().info("Started asynchronous task from " + simpleClassName  + "#" + methodName + ".");
            runnable.run();
            getLogger().info("Asynchronous task from " + simpleClassName  + "#" + methodName + " has finished.");
        });
    }

    // This replaces the version placeholder if the mod is built using IntelliJ instead of Gradle.
    static {
        if (VERSION.contains("@")) { // Debug environment...
            VERSION = "1.6.0";
        }
    }
}
