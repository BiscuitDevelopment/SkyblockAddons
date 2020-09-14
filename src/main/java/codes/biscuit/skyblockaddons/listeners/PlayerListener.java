package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.hooks.GuiChestHook;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import codes.biscuit.skyblockaddons.events.DungeonPlayerReviveEvent;
import codes.biscuit.skyblockaddons.events.SkyblockPlayerDeathEvent;
import codes.biscuit.skyblockaddons.features.BaitManager;
import codes.biscuit.skyblockaddons.features.EndstoneProtectorManager;
import codes.biscuit.skyblockaddons.features.backpacks.Backpack;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackManager;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTracker;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.features.enchantedItemBlacklist.EnchantedItemPlacementBlocker;
import codes.biscuit.skyblockaddons.features.powerorbs.PowerOrbManager;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.features.tabtimers.TabEffectManager;
import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.objects.IntPair;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO Fix for Hypixel localization
public class PlayerListener {

    private static final Pattern NO_ARROWS_LEFT_PATTERN = Pattern.compile("(?:§r)?§cYou don't have any more Arrows left in your Quiver!§r");
    private static final Pattern ONLY_HAVE_ARROWS_LEFT_PATTERN = Pattern.compile("(?:§r)?§cYou only have (?<arrows>[0-9]+) Arrows left in your Quiver!§r");
    private static final Pattern ENCHANTMENT_TOOLTIP_PATTERN = Pattern.compile("§.§.(§9[\\w ]+(, )?)+");
    private static final Pattern ABILITY_CHAT_PATTERN = Pattern.compile("§r§aUsed §r§6[A-Za-z ]+§r§a! §r§b\\([0-9]+ Mana\\)§r");
    private static final Pattern PROFILE_CHAT_PATTERN = Pattern.compile("§aYou are playing on profile: §e([A-Za-z]+).*");
    private static final Pattern SWITCH_PROFILE_CHAT_PATTERN = Pattern.compile("§aYour profile was changed to: §e([A-Za-z]+).*");
    private static final Pattern MINION_CANT_REACH_PATTERN = Pattern.compile("§cI can't reach any (?<mobName>[A-Za-z]*)(?:s)");
    private static final Pattern DRAGON_KILLED_PATTERN = Pattern.compile(" *[A-Z]* DRAGON DOWN!");
    private static final Pattern DRAGON_SPAWNED_PATTERN = Pattern.compile("☬ The (?<dragonType>[A-Za-z ]+) Dragon has spawned!");
    private static final Pattern SLAYER_COMPLETED_PATTERN = Pattern.compile(" {3}» Talk to Maddox to claim your (?<slayerType>[A-Za-z]+) Slayer XP!");
    private static final Pattern DEATH_MESSAGE_PATTERN = Pattern.compile("§r§c ☠ §r(?:§[\\da-fk-or])(?<playerName>\\w+)(?<afterNameFormatting>§r§7)* (?<causeOfDeath>.+)§r§7\\.§r");
    private static final Pattern REVIVE_MESSAGE_PATTERN = Pattern.compile("§r§a ❣ §r(?:§[\\da-fk-or])(?<revivedPlayer>\\w+)§r§a was revived(?: by §r(?:§[\\da-fk-or])(?<reviver>\\w+)§r§a)*!§r");
    private static final Pattern ACCESSORY_BAG_REFORGE_PATTERN = Pattern.compile("You applied the (?<reforge>\\w+) reforge to (?:\\d+) accessories in your Accessory Bag!");

    // Between these two coordinates is the whole "arena" area where all the magmas and stuff are.
    private static final AxisAlignedBB MAGMA_BOSS_SPAWN_AREA = new AxisAlignedBB(-244, 0, -566, -379, 255, -635);

    private static final Set<String> SOUP_RANDOM_MESSAGES = new HashSet<>(Arrays.asList("I feel like I can fly!", "What was in that soup?",
            "Hmm… tasty!", "Hmm... tasty!", "You can now fly for 2 minutes.", "Your flight has been extended for 2 extra minutes.",
            "You can now fly for 200 minutes.", "Your flight has been extended for 200 extra minutes."));

    private static final Set<String> LEGENDARY_SEA_CREATURE_MESSAGES = new HashSet<>(Arrays.asList("The Water Hydra has come to test your strength.",
            "The Sea Emperor arises from the depths...", "What is this creature!?"));

    private static final Set<String> BONZO_STAFF_SOUNDS = new HashSet<>(Arrays.asList("fireworks.blast", "fireworks.blast_far",
            "fireworks.twinkle", "fireworks.twinkle_far", "mob.ghast.moan"));

    private long lastWorldJoin = -1;
    private long lastBoss = -1;
    private int magmaTick = 1;
    private int timerTick = 1;
    private long lastMinionSound = -1;
    private long lastBossSpawnPost = -1;
    private long lastBossDeathPost = -1;
    private long lastMagmaWavePost = -1;
    private long lastBlazeWavePost = -1;
    private Class<?> lastOpenedInventory;
    private long lastClosedInv = -1;
    private long lastFishingAlert = 0;
    private long lastBobberEnteredWater = Long.MAX_VALUE;
    private long lastSkyblockServerJoinAttempt = 0;

    @Getter private long rainmakerTimeEnd = -1;

    private boolean oldBobberIsInWater;
    private double oldBobberPosY = 0;

    @Getter private Set<UUID> countedEndermen = new HashSet<>();
    @Getter private TreeMap<Long, Set<Vec3>> recentlyKilledZealots = new TreeMap<>();

    @Getter private Set<IntPair> recentlyLoadedChunks = new HashSet<>();

    @Getter @Setter private EnumUtils.MagmaTimerAccuracy magmaAccuracy = EnumUtils.MagmaTimerAccuracy.NO_DATA;
    @Getter @Setter private int magmaTime = 0;
    @Getter @Setter private int recentMagmaCubes = 0;
    @Getter @Setter private int recentBlazes = 0;

    @Getter private TreeMap<Long, Vec3> explosiveBowExplosions = new TreeMap<>();

    private final SkyblockAddons main = SkyblockAddons.getInstance();
    private final Logger logger = main.getLogger();
    private final ActionBarParser actionBarParser = new ActionBarParser();

    /**
     * Reset all the timers and stuff when joining a new world.
     */
    @SubscribeEvent()
    public void onWorldJoin(EntityJoinWorldEvent e) {
        Entity entity = e.entity;

        if (entity == Minecraft.getMinecraft().thePlayer) {
            lastWorldJoin = System.currentTimeMillis();
            lastBoss = -1;
            magmaTick = 1;
            timerTick = 1;
            main.getInventoryUtils().resetPreviousInventory();
            recentlyLoadedChunks.clear();
            countedEndermen.clear();
            EndstoneProtectorManager.reset();

            IslandWarpGui.Marker doubleWarpMarker = IslandWarpGui.getDoubleWarpMarker();
            if (doubleWarpMarker != null) {
                IslandWarpGui.setDoubleWarpMarker(null);
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/warp " + doubleWarpMarker.getWarpName());
            }

            NPCUtils.getNpcLocations().clear();
        }
    }

    /**
     * Keep track of recently loaded chunks for the magma boss timer.
     */
    @SubscribeEvent()
    public void onChunkLoad(ChunkEvent.Load e) {
        if (main.getUtils().isOnSkyblock()) {
            int x = e.getChunk().xPosition;
            int z = e.getChunk().zPosition;
            IntPair coords = new IntPair(x, z);
            recentlyLoadedChunks.add(coords);
            main.getScheduler().schedule(Scheduler.CommandType.DELETE_RECENT_CHUNK, 20, x, z);
        }
    }

    /**
     * Interprets the action bar to extract mana, health, and defence. Enables/disables mana/health prediction,
     * and looks for mana usage messages in chat while predicting.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChatReceive(ClientChatReceivedEvent e) {
        String unformattedText = e.message.getUnformattedText();

        // Type 2 means it's an action bar message.
        if (e.type == 2) {
            // Log the message to the game log if action bar message logging is enabled.
            if (DevUtils.isLoggingActionBarMessages()) {
                logger.info("[ACTION BAR] " + unformattedText);
            }

            // Parse using ActionBarParser and display the rest message instead
            String restMessage = actionBarParser.parseActionBar(unformattedText);
            if (main.isUsingOofModv1() && restMessage.trim().length() == 0) {
                e.setCanceled(true);
            }

            if (main.getUtils().isInDungeon() && main.getConfigValues().isEnabled(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY)) {
                main.getDungeonUtils().parseCollectedEssence(restMessage);
            }

            e.message = new ChatComponentText(restMessage);
        } else {
            String formattedText = e.message.getFormattedText();
            String strippedText = TextUtils.stripColor(formattedText);

            Matcher matcher;

            if (main.getRenderListener().isPredictMana() && unformattedText.startsWith("Used ") && unformattedText.endsWith("Mana)")) {
                int manaLost = Integer.parseInt(unformattedText.split(Pattern.quote("! ("))[1].split(Pattern.quote(" Mana)"))[0]);
                changeMana(-manaLost);
            }

            // Fire a SkyblockPlayerDeathEvent if the message says that a player died.
            if ((matcher = DEATH_MESSAGE_PATTERN.matcher(formattedText)).matches()) {
                EntityPlayer deadPlayer = null;

                /*
                 If the group "afterNameFormatting" matches, it means someone other than the client player died.
                 If it doesn't match, the client player died.
                 */
                if (matcher.group("afterNameFormatting") != null) {
                    for (EntityPlayer player:
                         Minecraft.getMinecraft().theWorld.playerEntities) {
                        if (player.getName().contains(matcher.group("playerName"))) {
                            deadPlayer = player;
                            break;
                        }
                    }
                } else {
                    deadPlayer = Minecraft.getMinecraft().thePlayer;
                }

                MinecraftForge.EVENT_BUS.post(new SkyblockPlayerDeathEvent(deadPlayer, matcher.group("causeOfDeath")));
            }

            if (main.getConfigValues().isEnabled(Feature.SUMMONING_EYE_ALERT) && formattedText.equals("§r§6§lRARE DROP! §r§5Summoning Eye§r")) {
                main.getUtils().playLoudSound("random.orb", 0.5); // credits to tomotomo, thanks lol
                main.getRenderListener().setTitleFeature(Feature.SUMMONING_EYE_ALERT);
                main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds());

            } else if (formattedText.equals("§r§aA special §r§5Zealot §r§ahas spawned nearby!§r")) {
                if (main.getConfigValues().isEnabled(Feature.SPECIAL_ZEALOT_ALERT)) {
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setTitleFeature(Feature.SUMMONING_EYE_ALERT);
                    main.getRenderListener().setTitleFeature(Feature.SPECIAL_ZEALOT_ALERT);
                    main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                }
                if (main.getConfigValues().isEnabled(Feature.ZEALOT_COUNTER)) {
                    // Edit the message to include counter.
                    e.message = new ChatComponentText(formattedText + ColorCode.GRAY + " (" + main.getPersistentValues().getKills() + ")");
                }
                main.getPersistentValues().addEyeResetKills();

            } else if (main.getConfigValues().isEnabled(Feature.LEGENDARY_SEA_CREATURE_WARNING) && LEGENDARY_SEA_CREATURE_MESSAGES.contains(unformattedText)) {
                main.getUtils().playLoudSound("random.orb", 0.5);
                main.getRenderListener().setTitleFeature(Feature.LEGENDARY_SEA_CREATURE_WARNING);
                main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds());

            } else if (main.getConfigValues().isEnabled(Feature.DISABLE_MAGICAL_SOUP_MESSAGES) && SOUP_RANDOM_MESSAGES.contains(unformattedText)) {
                e.setCanceled(true);

            } else if (main.getConfigValues().isEnabled(Feature.DISABLE_TELEPORT_PAD_MESSAGES) && (formattedText.startsWith("§r§aWarped from ") || formattedText.equals("§r§cThis Teleport Pad does not have a destination set!§r"))) {
                e.setCanceled(true);

            } else if ((matcher = SLAYER_COMPLETED_PATTERN.matcher(strippedText)).matches()) { // §r   §r§5§l» §r§7Talk to Maddox to claim your Wolf Slayer XP!§r
                SlayerTracker.getInstance().completedSlayer(matcher.group("slayerType"));

            } else if (strippedText.startsWith("☬ You placed a Summoning Eye!")) { // §r§5☬ §r§dYou placed a Summoning Eye! §r§7(§r§e5§r§7/§r§a8§r§7)§r
                DragonTracker.getInstance().addEye();

            } else if (strippedText.equals("You recovered a Summoning Eye!")) {
                DragonTracker.getInstance().removeEye();

            } else if ((matcher = DRAGON_SPAWNED_PATTERN.matcher(strippedText)).matches()) {
                DragonTracker.getInstance().dragonSpawned(matcher.group("dragonType"));

            } else if (DRAGON_KILLED_PATTERN.matcher(strippedText).matches()) {
                DragonTracker.getInstance().dragonKilled();

            } else if (formattedText.startsWith("§7Sending to server ")) {
                lastSkyblockServerJoinAttempt = System.currentTimeMillis();
                DragonTracker.getInstance().reset();

            } else if (unformattedText.equals("You laid an egg!")) { // Put the Chicken Head on cooldown for 20 seconds when the player lays an egg.
                CooldownManager.put(InventoryUtils.CHICKEN_HEAD_DISPLAYNAME, 20000);

            } else if (formattedText.startsWith("§r§eYou added a minute of rain!")) {
                if (this.rainmakerTimeEnd == -1 || this.rainmakerTimeEnd < System.currentTimeMillis()) {
                    this.rainmakerTimeEnd = System.currentTimeMillis() + (1000 * 60); // Set the timer to a minute from now.
                } else {
                    this.rainmakerTimeEnd += (1000 * 60); // Extend the timer one minute.
                }
            } else if (main.getConfigValues().isEnabled(Feature.SHOW_ENCHANTMENTS_REFORGES) &&
                    (matcher = ACCESSORY_BAG_REFORGE_PATTERN.matcher(unformattedText)).matches()) {
                GuiChestHook.setLastAccessoryBagReforge(matcher.group("reforge"));
            }

            if (main.getConfigValues().isEnabled(Feature.NO_ARROWS_LEFT_ALERT)) {
                if (NO_ARROWS_LEFT_PATTERN.matcher(formattedText).matches()) {
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setSubtitleFeature(Feature.NO_ARROWS_LEFT_ALERT);
                    main.getRenderListener().setArrowsLeft(-1);
                    main.getScheduler().schedule(Scheduler.CommandType.RESET_SUBTITLE_FEATURE, main.getConfigValues().getWarningSeconds());

                } else if ((matcher = ONLY_HAVE_ARROWS_LEFT_PATTERN.matcher(formattedText)).matches()) {
                    int arrowsLeft = Integer.parseInt(matcher.group("arrows"));
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setSubtitleFeature(Feature.NO_ARROWS_LEFT_ALERT);
                    main.getRenderListener().setArrowsLeft(arrowsLeft);
                    main.getScheduler().schedule(Scheduler.CommandType.RESET_SUBTITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                }
            }

            if (main.getUtils().isInDungeon()) {
                Matcher reviveMessageMatcher = REVIVE_MESSAGE_PATTERN.matcher(formattedText);

                if (reviveMessageMatcher.matches()) {
                    List<EntityPlayer> players = Minecraft.getMinecraft().theWorld.playerEntities;

                    String revivedPlayerName = reviveMessageMatcher.group("revivedPlayer");
                    String reviverName = reviveMessageMatcher.group("reviver");
                    EntityPlayer revivedPlayer = null;
                    EntityPlayer revivingPlayer = null;

                    for (EntityPlayer player : players) {
                        if (player.getName().equals(revivedPlayerName)) {
                            revivedPlayer = player;
                        }

                        if (reviverName != null && player.getName().equals(reviverName)) {
                            revivingPlayer = player;
                        }

                        if (revivedPlayer != null && revivingPlayer != null) {
                            break;
                        }
                    }

                    if (revivingPlayer != null) {
                        MinecraftForge.EVENT_BUS.post(new DungeonPlayerReviveEvent(revivedPlayer, revivingPlayer));
                    } else {
                        MinecraftForge.EVENT_BUS.post(new DungeonPlayerReviveEvent(revivedPlayer));
                    }
                }

                if (main.getConfigValues().isEnabled(Feature.SHOW_DUNGEON_MILESTONE)) {
                    DungeonMilestone dungeonMilestone = main.getDungeonUtils().parseMilestone(formattedText);
                    if (dungeonMilestone != null) {
                        main.getDungeonUtils().setDungeonMilestone(dungeonMilestone);
                    }
                }

                if (main.getConfigValues().isEnabled(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY)) {
                    main.getDungeonUtils().parseBonusEssence(formattedText);
                }
            }


            matcher = ABILITY_CHAT_PATTERN.matcher(formattedText);
            if (matcher.matches()) {
                CooldownManager.put(Minecraft.getMinecraft().thePlayer.getHeldItem());
            } else {
                matcher = PROFILE_CHAT_PATTERN.matcher(formattedText);
                if (matcher.matches()) {
                    main.getUtils().setProfileName(matcher.group(1));
                } else {
                    matcher = SWITCH_PROFILE_CHAT_PATTERN.matcher(formattedText);
                    if (matcher.matches()) {
                        main.getUtils().setProfileName(matcher.group(1));
                    }
                }
            }
        }
    }

    /**
     * This method is triggered by the player right-clicking on something.
     * Yes, it says it works for left-clicking blocks too but it actually doesn't, so please don't use it to detect that.
     * <br>
     * Also, when the player right-clicks on a block, {@code PlayerInteractEvent} gets fired twice. The first time,
     * the correct action type {@code Action.RIGHT_CLICK_BLOCK}, is used. The second time, the action type is
     * {@code Action.RIGHT_CLICK_AIR} for some reason. Both of these events will cause a {@code C08PacketPlayerBlockPlacement}
     * packet to be sent to the server, so block both of them if you want to prevent a block from being placed.
     * <br>
     * Look at {@code Minecraft#rightClickMouse()} to see when the event is fired.
     *
     * @see Minecraft
     */
    @SubscribeEvent()
    public void onInteract(PlayerInteractEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItem = e.entityPlayer.getHeldItem();

        if (main.getUtils().isOnSkyblock() && heldItem != null) {
            // Change the GUI background color when a backpack is opened to match the backpack's color.
            if (heldItem.getItem() == Items.skull) {
                Backpack backpack = BackpackManager.getFromItem(heldItem);
                if (backpack != null) {
                    BackpackManager.setOpenedBackpackColor(backpack.getBackpackColor());
                }
            } else if (heldItem.getItem().equals(Items.fishing_rod)
                    && (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)) {
                // Update fishing status
                if (main.getConfigValues().isEnabled(Feature.FISHING_SOUND_INDICATOR)) {
                    oldBobberIsInWater = false;
                    lastBobberEnteredWater = Long.MAX_VALUE;
                    oldBobberPosY = 0;
                }
                if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS) && mc.thePlayer.fishEntity != null) {
                    CooldownManager.put(mc.thePlayer.getHeldItem());
                }
            } else if (heldItem.getItem().equals(Items.blaze_rod)) {
                String itemId = ItemUtils.getSkyBlockItemID(heldItem);

                if (main.getConfigValues().isEnabled(Feature.DISABLE_EMBER_ROD) && main.getUtils().getLocation() == Location.ISLAND
                        && itemId != null && itemId.equals("EMBER_ROD")) {
                    e.setCanceled(true);
                }
            }

            if (main.getConfigValues().isEnabled(Feature.AVOID_PLACING_ENCHANTED_ITEMS)) {
                if (EnchantedItemPlacementBlocker.shouldBlockPlacement(heldItem, e)) {
                    // Block the player from placing this item if it's on the enchanted item blacklist.
                    e.setCanceled(true);
                }
            }
        }
    }

    /**
     * The main timer for a bunch of stuff.
     */
    @SubscribeEvent()
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            timerTick++;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null) { // Predict health every tick if needed.

                if (actionBarParser.getHealthUpdate() != null && System.currentTimeMillis() - actionBarParser.getLastHealthUpdate() > 3000) {
                    actionBarParser.setHealthUpdate(null);
                }
                if (main.getRenderListener().isPredictHealth()) {
                    EntityPlayerSP p = mc.thePlayer;
                    if (p != null) { //Reverse calculate the player's health by using the player's vanilla hearts. Also calculate the health change for the gui item.
                        int newHealth = Math.round(getAttribute(Attribute.MAX_HEALTH) * (p.getHealth() / p.getMaxHealth()));
                        main.getScheduler().schedule(Scheduler.CommandType.SET_LAST_SECOND_HEALTH, 1, newHealth);
                        if (actionBarParser.getLastSecondHealth() != -1 && actionBarParser.getLastSecondHealth() != newHealth) {
                            actionBarParser.setHealthUpdate(newHealth - actionBarParser.getLastSecondHealth());
                            actionBarParser.setLastHealthUpdate(System.currentTimeMillis());
                        }
                        setAttribute(Attribute.HEALTH, newHealth);
                    }
                }
                if (shouldTriggerFishingIndicator()) { // The logic fits better in its own function
                    main.getUtils().playLoudSound("random.successful_hit", 0.8);
                }
                if (timerTick == 20) { // Add natural mana every second (increase is based on your max mana).
                    main.getUtils().parseTabList();

                    if (main.getRenderListener().isPredictMana()) {
                        changeMana(getAttribute(Attribute.MAX_MANA) / 50);
                        if (getAttribute(Attribute.MANA) > getAttribute(Attribute.MAX_MANA))
                            setAttribute(Attribute.MANA, getAttribute(Attribute.MAX_MANA));
                    }

                    if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.TAB_EFFECT_TIMERS)) {
                        TabEffectManager.getInstance().updatePotionEffects();
                    }

                } else if (timerTick % 5 == 0) { // Check inventory, location, updates, and skeleton helmet every 1/4 second.
                    EntityPlayerSP player = mc.thePlayer;

                    if (player != null) {
                        EndstoneProtectorManager.checkGolemStatus();
                        main.getUtils().parseSidebar();
                        main.getInventoryUtils().checkIfInventoryIsFull(mc, player);

                        if (main.getUtils().isOnSkyblock()) {
                            main.getInventoryUtils().checkIfWearingSkeletonHelmet(player);
                            main.getInventoryUtils().checkIfUsingToxicArrowPoison(player);
                            main.getInventoryUtils().checkIfWearingSlayerArmor(player);
                        }

                        if (mc.currentScreen == null && main.getPlayerListener().didntRecentlyJoinWorld()) {
                            main.getInventoryUtils().getInventoryDifference(player.inventory.mainInventory);
                        }

                        if (main.getConfigValues().isEnabled(Feature.BAIT_LIST) && BaitManager.getInstance().isHoldingRod()) {
                            BaitManager.getInstance().refreshBaits();
                        }
                    }

                    main.getInventoryUtils().cleanUpPickupLog();

                } else if (timerTick > 20) { // To keep the timer going from 1 to 21 only.
                    timerTick = 1;
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityEvent(LivingEvent.LivingUpdateEvent e) {
        if (!main.getUtils().isOnSkyblock()) {
            return;
        }

        Entity entity = e.entity;

        if (entity instanceof EntityOtherPlayerMP && main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_NEAR_NPCS) && entity.ticksExisted < 5) {
            float health = ((EntityOtherPlayerMP) entity).getHealth();

            if (NPCUtils.getNpcLocations().containsKey(entity.getUniqueID())) {
                if (health != 20.0F) {
                    NPCUtils.getNpcLocations().remove(entity.getUniqueID());
                }
            } else if (NPCUtils.isNPC(entity)) {
                NPCUtils.getNpcLocations().put(entity.getUniqueID(), entity.getPositionVector());
            }
        }

        if (entity instanceof EntityArmorStand && entity.hasCustomName()) {
            PowerOrbManager.getInstance().detectPowerOrb(entity);

            if (main.getUtils().getLocation() == Location.ISLAND) {
                int cooldown = main.getConfigValues().getWarningSeconds() * 1000 + 5000;
                if (main.getConfigValues().isEnabled(Feature.MINION_FULL_WARNING) &&
                        entity.getCustomNameTag().equals("§cMy storage is full! :(")) {
                    long now = System.currentTimeMillis();
                    if (now - lastMinionSound > cooldown) {
                        lastMinionSound = now;
                        main.getUtils().playLoudSound("random.pop", 1);
                        main.getRenderListener().setSubtitleFeature(Feature.MINION_FULL_WARNING);
                        main.getScheduler().schedule(Scheduler.CommandType.RESET_SUBTITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                    }
                } else if (main.getConfigValues().isEnabled(Feature.MINION_STOP_WARNING)) {
                    Matcher matcher = MINION_CANT_REACH_PATTERN.matcher(entity.getCustomNameTag());
                    if (matcher.matches()) {
                        long now = System.currentTimeMillis();
                        if (now - lastMinionSound > cooldown) {
                            lastMinionSound = now;
                            main.getUtils().playLoudSound("random.orb", 1);

                            String mobName = matcher.group("mobName");
                            main.getRenderListener().setCannotReachMobName(mobName);
                            main.getRenderListener().setSubtitleFeature(Feature.MINION_STOP_WARNING);
                            main.getScheduler().schedule(Scheduler.CommandType.RESET_SUBTITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        if (e.target instanceof EntityEnderman) {
            if (isZealot(e.target)) {
                countedEndermen.add(e.target.getUniqueID());
            }
        }
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        if (e.entity instanceof EntityEnderman) {
            if (countedEndermen.remove(e.entity.getUniqueID())) {
                main.getPersistentValues().addKill();
                EndstoneProtectorManager.onKill();
            } else if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT)) {
                if (isZealot(e.entity)) {
                    long now = System.currentTimeMillis();
                    if (recentlyKilledZealots.containsKey(now)) {
                        recentlyKilledZealots.get(now).add(e.entity.getPositionVector());
                    } else {
                        recentlyKilledZealots.put(now, Sets.newHashSet(e.entity.getPositionVector()));
                    }

                    explosiveBowExplosions.keySet().removeIf((explosionTime) -> now - explosionTime > 150);
                    Map.Entry<Long, Vec3> latestExplosion = explosiveBowExplosions.lastEntry();
                    if (latestExplosion == null) return;

                    Vec3 explosionLocation = latestExplosion.getValue();

//                    int possibleZealotsKilled = 1;
//                    System.out.println("This means "+possibleZealotsKilled+" may have been killed...");
//                    int originalPossibleZealotsKilled = possibleZealotsKilled;

                    Vec3 deathLocation = e.entity.getPositionVector();

                    double distance = explosionLocation.distanceTo(deathLocation);
//                    System.out.println("Distance was "+distance+"!");
                    if (explosionLocation.distanceTo(deathLocation) < 4.6) {
//                        possibleZealotsKilled--;

                        main.getPersistentValues().addKill();
                        EndstoneProtectorManager.onKill();
                    }

//                    System.out.println((originalPossibleZealotsKilled-possibleZealotsKilled)+" zealots were actually killed...");
                }
            }
        }

        NPCUtils.getNpcLocations().remove(e.entity.getUniqueID());
    }

    public boolean isZealot(Entity enderman) {
        List<EntityArmorStand> stands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityArmorStand.class,
                new AxisAlignedBB(enderman.posX - 1, enderman.posY, enderman.posZ - 1, enderman.posX + 1, enderman.posY + 5, enderman.posZ + 1));
        if (stands.isEmpty()) return false;

        EntityArmorStand armorStand = stands.get(0);
        return armorStand.hasCustomName() && armorStand.getCustomNameTag().contains("Zealot");
    }

    /**
     * The main timer for the magma boss checker.
     */
    @SubscribeEvent()
    public void onClientTickMagma(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            if (main.getConfigValues().isEnabled(Feature.MAGMA_WARNING) && main.getUtils().isOnSkyblock()) {
                if (mc != null && mc.theWorld != null) {
                    if (magmaTick % 5 == 0) {
                        boolean foundBoss = false;
                        long currentTime = System.currentTimeMillis();
                        for (Entity entity : mc.theWorld.loadedEntityList) { // Loop through all the entities.
                            if (entity instanceof EntityMagmaCube) {
                                EntitySlime magma = (EntitySlime) entity;
                                if (magma.getSlimeSize() > 10) { // Find a big magma boss
                                    foundBoss = true;
                                    if ((lastBoss == -1 || System.currentTimeMillis() - lastBoss > 1800000)) {
                                        lastBoss = System.currentTimeMillis();
                                        main.getRenderListener().setTitleFeature(Feature.MAGMA_WARNING); // Enable warning and disable again in four seconds.
                                        magmaTick = 16; // so the sound plays instantly
                                        main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds());
//                                logServer(mc);
                                    }
                                    magmaAccuracy = EnumUtils.MagmaTimerAccuracy.SPAWNED;
                                    if (currentTime - lastBossSpawnPost > 300000) {
                                        lastBossSpawnPost = currentTime;
                                        main.getUtils().sendInventiveTalentPingRequest(EnumUtils.MagmaEvent.BOSS_SPAWN);
                                    }
                                }
                            }
                        }
                        if (!foundBoss && main.getRenderListener().getTitleFeature() == Feature.MAGMA_WARNING) {
                            main.getRenderListener().setTitleFeature(null);
                        }
                        if (!foundBoss && magmaAccuracy == EnumUtils.MagmaTimerAccuracy.SPAWNED) {
                            magmaAccuracy = EnumUtils.MagmaTimerAccuracy.ABOUT;
                            magmaTime = 7200;
                            if (currentTime - lastBossDeathPost > 300000) {
                                lastBossDeathPost = currentTime;
                                main.getUtils().sendInventiveTalentPingRequest(EnumUtils.MagmaEvent.BOSS_DEATH);
                            }
                        }
                    }
                    if (main.getRenderListener().getTitleFeature() == Feature.MAGMA_WARNING && magmaTick % 4 == 0) { // Play sound every 4 ticks or 1/5 second.
                        main.getUtils().playLoudSound("random.orb", 0.5);
                    }
                }
            }
            magmaTick++;
            if (magmaTick > 20) {
                if ((magmaAccuracy == EnumUtils.MagmaTimerAccuracy.EXACTLY || magmaAccuracy == EnumUtils.MagmaTimerAccuracy.ABOUT)
                        && magmaTime == 0) {
                    magmaAccuracy = EnumUtils.MagmaTimerAccuracy.SPAWNED_PREDICTION;
                    main.getScheduler().schedule(Scheduler.CommandType.RESET_MAGMA_PREDICTION, 20);
                }
                magmaTime--;
                magmaTick = 1;
            }
        }
    }

    @SubscribeEvent()
    public void onEntitySpawn(EntityEvent.EnteringChunk e) {
        Entity entity = e.entity;

        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT) && entity instanceof EntityArrow) {
            EntityArrow arrow = (EntityArrow)entity;

            EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
            ItemStack heldItem = p.getHeldItem();
            if (heldItem != null && "EXPLOSIVE_BOW".equals(ItemUtils.getSkyBlockItemID(heldItem))) {

                AxisAlignedBB playerRadius = new AxisAlignedBB(p.posX - 3, p.posY - 3, p.posZ - 3, p.posX + 3, p.posY + 3, p.posZ + 3);
                if (playerRadius.isVecInside(arrow.getPositionVector())) {

//                    System.out.println("Spawned explosive arrow!");
                    main.getNewScheduler().scheduleRepeatingTask(new SkyblockRunnable() {
                        @Override
                        public void run() {
                            if (arrow.isDead || arrow.isCollided || arrow.inGround) {
                                cancel();

//                                System.out.println("Arrow is done, added an explosion!");
                                Vec3 explosionLocation = new Vec3(arrow.posX, arrow.posY, arrow.posZ);
                                explosiveBowExplosions.put(System.currentTimeMillis(), explosionLocation);

                                recentlyKilledZealots.keySet().removeIf((killedTime) -> System.currentTimeMillis() - killedTime > 150);
                                Set<Vec3> filteredRecentlyKilledZealots = new HashSet<>();
                                for (Map.Entry<Long, Set<Vec3>> recentlyKilledZealotEntry : recentlyKilledZealots.entrySet()) {
                                    filteredRecentlyKilledZealots.addAll(recentlyKilledZealotEntry.getValue());
                                }
                                if (filteredRecentlyKilledZealots.isEmpty()) return;

//                                int possibleZealotsKilled = filteredRecentlyKilledZealots.size();
//                                System.out.println("This means "+possibleZealotsKilled+" may have been killed...");
//                                int originalPossibleZealotsKilled = possibleZealotsKilled;

                                for (Vec3 zealotDeathLocation : filteredRecentlyKilledZealots) {
                                    double distance = explosionLocation.distanceTo(zealotDeathLocation);
//                                    System.out.println("Distance was "+distance+"!");
                                    if (distance < 4.6) {
//                                        possibleZealotsKilled--;

                                        main.getPersistentValues().addKill();
                                        EndstoneProtectorManager.onKill();
                                    }
                                }

//                                System.out.println((originalPossibleZealotsKilled-possibleZealotsKilled)+" zealots were actually killed...");
                            }
                        }
                    }, 0, 1);
                }
            }
        }

        if (main.getUtils().getLocation() == Location.BLAZING_FORTRESS) {
            if (MAGMA_BOSS_SPAWN_AREA.isVecInside(new Vec3(entity.posX, entity.posY, entity.posZ))) { // timers will trigger if 15 magmas/8 blazes spawn in the box within a 4 second time period
                long currentTime = System.currentTimeMillis();
                if (e.entity instanceof EntityMagmaCube) {
                    if (!recentlyLoadedChunks.contains(new IntPair(e.newChunkX, e.newChunkZ)) && entity.ticksExisted == 0) {
                        recentMagmaCubes++;
                        main.getScheduler().schedule(Scheduler.CommandType.SUBTRACT_MAGMA_COUNT, 4);
                        if (recentMagmaCubes >= 17) {
                            magmaTime = 600;
                            magmaAccuracy = EnumUtils.MagmaTimerAccuracy.EXACTLY;
                            if (currentTime - lastMagmaWavePost > 300000) {
                                lastMagmaWavePost = currentTime;
                                main.getUtils().sendInventiveTalentPingRequest(EnumUtils.MagmaEvent.MAGMA_WAVE);
                            }
                        }
                    }
                } else if (e.entity instanceof EntityBlaze) {
                    if (!recentlyLoadedChunks.contains(new IntPair(e.newChunkX, e.newChunkZ)) && entity.ticksExisted == 0) {
                        recentBlazes++;
                        main.getScheduler().schedule(Scheduler.CommandType.SUBTRACT_BLAZE_COUNT, 4);
                        if (recentBlazes >= 10) {
                            magmaTime = 1200;
                            magmaAccuracy = EnumUtils.MagmaTimerAccuracy.EXACTLY;
                            if (currentTime - lastBlazeWavePost > 300000) {
                                lastBlazeWavePost = currentTime;
                                main.getUtils().sendInventiveTalentPingRequest(EnumUtils.MagmaEvent.BLAZE_WAVE);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent()
    public void onEnderTeleport(EnderTeleportEvent e) {
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.DISABLE_ENDERMAN_TELEPORTATION_EFFECT)) {
            e.setCanceled(true);
        }
    }

    /**
     * Modifies item tooltips and activates the copy item nbt feature
     */
    @SubscribeEvent()
    public void onItemTooltip(ItemTooltipEvent e) {
        ItemStack hoveredItem = e.itemStack;

        if (e.toolTip != null && main.getUtils().isOnSkyblock()) {
            if (main.getConfigValues().isEnabled(Feature.HIDE_GREY_ENCHANTS)) {
                for (int i = 1; i <= 3; i++) { // only a max of 2 gray enchants are possible
                    if (i >= e.toolTip.size()) continue; // out of bounds

                    GuiScreen gui = Minecraft.getMinecraft().currentScreen;
                    if (gui instanceof GuiChest) {
                        Container chest = ((GuiChest) gui).inventorySlots;
                        if (chest instanceof ContainerChest) {
                            IInventory inventory = ((ContainerChest) chest).getLowerChestInventory();
                            if (inventory.hasCustomName() && "Enchant Item".equals(inventory.getDisplayName().getUnformattedText())) {
                                continue; // dont replace enchants when you are enchanting items in an enchantment table
                            }
                        }
                    }
                    String line = e.toolTip.get(i);
                    if (!line.startsWith("§5§o§9") && (line.contains("Respiration") || line.contains("Aqua Affinity")
                            || line.contains("Depth Strider") || line.contains("Efficiency"))) {
                        e.toolTip.remove(line);
                        i--;
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS)) {
                for (int i = 0; i < e.toolTip.size(); i++) {
                    e.toolTip.set(i, RomanNumeralParser.replaceNumeralsWithIntegers(e.toolTip.get(i)));
                }
            }

            if (main.getConfigValues().isEnabled(Feature.ORGANIZE_ENCHANTMENTS)) {

                List<String> enchantments = new ArrayList<>();
                int enchantStartIndex = -1;
                int enchantEndIndex = -1;

                for (int i = 0; i < e.toolTip.size(); i++) {
                    if (ENCHANTMENT_TOOLTIP_PATTERN.matcher(e.toolTip.get(i)).matches()) {
                        String line = TextUtils.stripColor(e.toolTip.get(i));
                        int comma = line.indexOf(',');
                        if (comma < 0 || line.length() <= comma + 2) {
                            enchantments.add(line);
                        } else {
                            enchantments.add(line.substring(0, comma));
                            enchantments.add(line.substring(comma + 2));
                        }
                        if (enchantStartIndex < 0) enchantStartIndex = i;
                    } else if (enchantStartIndex >= 0) {
                        enchantEndIndex = i;
                        break;
                    }
                }

                if (enchantments.size() > 4) {
                    e.toolTip.subList(enchantStartIndex, enchantEndIndex).clear(); // Remove old enchantments
                    main.getUtils().reorderEnchantmentList(enchantments);
                    int columns = enchantments.size() < 15 ? 2 : 3;
                    for (int i = 0; !enchantments.isEmpty(); i++) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("§5§o");
                        for (int j = 0; j < columns && !enchantments.isEmpty(); j++) {
                            sb.append("§9");
                            sb.append(enchantments.get(0));
                            sb.append(", ");
                            enchantments.remove(0);
                        }
                        sb.setLength(sb.length() - 2);
                        e.toolTip.add(enchantStartIndex + i, sb.toString());
                    }
                }
            }

            int insertAt = e.toolTip.size();
            insertAt--; // 1 line for the rarity
            if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
                insertAt -= 2; // 1 line for the item name, and 1 line for the nbt
                if (e.itemStack.isItemDamaged()) {
                    insertAt--; // 1 line for damage
                }
            }

            if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_ANVIL_USES)) {
                // Anvil Uses ~ original done by Dahn#6036
                int anvilUses = main.getUtils().getNBTInteger(hoveredItem, "ExtraAttributes", "anvil_uses");
                if (anvilUses != -1) {
                    int hotPotatoCount = main.getUtils().getNBTInteger(hoveredItem, "ExtraAttributes", "hot_potato_count");
                    if (hotPotatoCount != -1) {
                        anvilUses -= hotPotatoCount;
                    }
                    if (anvilUses > 0) {
                        e.toolTip.add(insertAt++, Message.MESSAGE_ANVIL_USES.getMessage(String.valueOf(anvilUses)));
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.SHOW_BROKEN_FRAGMENTS)) {
                if (hoveredItem.getDisplayName().contains("Dragon Fragment")) {
                    if (hoveredItem.hasTagCompound()) {
                        NBTTagCompound extraAttributes = hoveredItem.getSubCompound("ExtraAttributes", false);

                        if (extraAttributes != null) {
                            if (extraAttributes.hasKey("bossId") && extraAttributes.hasKey("spawnedFor")) {
                                e.toolTip.add(insertAt++, "§c§lBROKEN FRAGMENT§r");
                            }
                        }
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE) && hoveredItem.hasTagCompound()) {
                NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(hoveredItem);
                if (extraAttributes != null) {
                    int baseStatBoost = ItemUtils.getBaseStatBoostPercentage(extraAttributes);
                    if (baseStatBoost != -1) {

                        ColorCode colorCode = main.getConfigValues().getRestrictedColor(Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE);
                        if (main.getConfigValues().isEnabled(Feature.BASE_STAT_BOOST_COLOR_BY_RARITY)) {

                            int rarityIndex = baseStatBoost/10;
                            if (rarityIndex < 0) rarityIndex = 0;
                            if (rarityIndex >= ItemRarity.values().length) rarityIndex = ItemRarity.values().length - 1;

                            colorCode = ItemRarity.values()[rarityIndex].getColorCode();
                        }
                        e.toolTip.add(insertAt++, "§7Base Stat Boost: " + colorCode + "+" + baseStatBoost + "%");
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_DUNGEON_FLOOR) && hoveredItem.hasTagCompound()) {
                NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(hoveredItem);
                if (extraAttributes != null) {
                    int floor = ItemUtils.getDungeonFloor(extraAttributes);
                    if (floor != -1) {
                        ColorCode colorCode = main.getConfigValues().getRestrictedColor(Feature.SHOW_ITEM_DUNGEON_FLOOR);
                        e.toolTip.add(insertAt++, "§7Obtained on Floor: " + colorCode + (floor == 0 ? "Entrance" : floor));
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.SHOW_RARITY_UPGRADED) && hoveredItem.hasTagCompound()) {
                NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(hoveredItem);

                if (extraAttributes != null && ItemUtils.getRarityUpgrades(extraAttributes) > 0) {
                    e.toolTip.add(insertAt++, main.getConfigValues().getRestrictedColor(Feature.SHOW_RARITY_UPGRADED) + "§lRARITY UPGRADED");
                }
            }

            // Append Skyblock Item ID to end of tooltip if in developer mode
            if (main.isDevMode() && e.showAdvancedItemTooltips) {
                String itemId = ItemUtils.getSkyBlockItemID(e.itemStack);

                if (itemId != null) {
                    if (!Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
                        insertAt = e.toolTip.size();
                    } else {
                        // Before the NBT line
                        insertAt = e.toolTip.size() - 1;
                    }

                    e.toolTip.add(insertAt, EnumChatFormatting.DARK_GRAY + "skyblock:" + itemId);
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if (e.gui == null && GuiChest.class.equals(lastOpenedInventory)) {
            lastClosedInv = System.currentTimeMillis();
            lastOpenedInventory = null;
        }
        if (e.gui != null) {
            lastOpenedInventory = e.gui.getClass();

            if (e.gui instanceof GuiChest) {
                Minecraft mc = Minecraft.getMinecraft();
                IInventory chestInventory = ((GuiChest) e.gui).lowerChestInventory;
                if (chestInventory.hasCustomName()) {
                    if (chestInventory.getDisplayName().getUnformattedText().contains("Backpack")) {
                        if (ThreadLocalRandom.current().nextInt(0, 2) == 0) {
                            mc.thePlayer.playSound("mob.horse.armor", 0.5F, 1);
                        } else {
                            mc.thePlayer.playSound("mob.horse.leather", 0.5F, 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * This method handles key presses while the player is in-game.
     * For handling of key presses while a GUI (e.g. chat, pause menu, F3) is open,
     * see {@link GuiScreenListener#onKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre)}
     *
     * @param e the {@code KeyInputEvent} that occurred
     */
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent e) {
        if (main.getOpenSettingsKey().isPressed()) {
            main.getUtils().setFadingIn(true);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);

        } else if (main.getOpenEditLocationsKey().isPressed()) {
            main.getUtils().setFadingIn(false);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null);

        } else if (main.getDeveloperCopyNBTKey().isPressed()) {
            // Copy Mob Data
            if (main.isDevMode()) {
                DevUtils.copyEntityData();
            }
        }

        if (main.getConfigValues().isEnabled(Feature.DUNGEONS_MAP_DISPLAY) && main.getUtils().isInDungeon()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_MINUS) && Keyboard.getEventKeyState()) {
                float zoomScaleFactor = main.getUtils().denormalizeScale(main.getConfigValues().getMapZoom().getValue(), 0.5F, 5, 0.1F);
                main.getConfigValues().getMapZoom().setValue(main.getUtils().normalizeValueNoStep(zoomScaleFactor - 0.5F, 0.5F, 5));
            } else if (Keyboard.isKeyDown(Keyboard.KEY_EQUALS) && Keyboard.getEventKeyState()) {
                float zoomScaleFactor = main.getUtils().denormalizeScale(main.getConfigValues().getMapZoom().getValue(), 0.5F, 5, 0.1F);
                main.getConfigValues().getMapZoom().setValue(main.getUtils().normalizeValueNoStep(zoomScaleFactor + 0.5F, 0.5F, 5));
            }
        }
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        if (!main.getUtils().isOnSkyblock()) {
            return;
        }

        if (main.getConfigValues().isEnabled(Feature.STOP_BONZO_STAFF_SOUNDS) && BONZO_STAFF_SOUNDS.contains(event.name)) {
            event.result = null;
        }
    }

    /**
     * This method is called when a player dies in Skyblock.
     *
     * @param e the event that caused this method to be called
     */
    @SubscribeEvent
    public void onPlayerDeath(SkyblockPlayerDeathEvent e) {
        if (main.getUtils().isOnSkyblock()) {
            /*  Resets all user input on dead as to not walk backwards or strafe into the portal */
            if (main.getConfigValues().isEnabled(Feature.PREVENT_MOVEMENT_ON_DEATH) &&
                    e.entityPlayer == Minecraft.getMinecraft().thePlayer) {
                KeyBinding.unPressAllKeys();
            }

            /*
            Don't show log for losing all items when the player dies in dungeons.
             The items come back after the player is revived and the large log causes a distraction.
             */
            if (main.getConfigValues().isEnabled(Feature.ITEM_PICKUP_LOG) &&
                    e.entityPlayer == Minecraft.getMinecraft().thePlayer && main.getUtils().isInDungeon()) {
                main.getInventoryUtils().resetPreviousInventory();
            }

            if (main.getConfigValues().isEnabled(Feature.DUNGEON_DEATH_COUNTER) && main.getUtils().isInDungeon()) {
                main.getDungeonUtils().getDeathCounter().increment();
            }
        }
    }

    /**
     * This method is called when a player in Dungeons gets revived.
     *
     * @param e the event that caused this method to be called
     */
    @SubscribeEvent
    public void onDungeonPlayerRevive(DungeonPlayerReviveEvent e) {
        // Reset the previous inventory so the screen doesn't get spammed with a large pickup log
        if (main.getConfigValues().isEnabled(Feature.ITEM_PICKUP_LOG)) {
            main.getInventoryUtils().resetPreviousInventory();
        }
    }

    public boolean aboutToJoinSkyblockServer() {
        return System.currentTimeMillis() - lastSkyblockServerJoinAttempt < 6000;
    }

    public boolean didntRecentlyJoinWorld() {
        return System.currentTimeMillis() - lastWorldJoin > 3000;
    }

    public int getMaxTickers() {
        return actionBarParser.getMaxTickers();
    }

    public int getTickers() {
        return actionBarParser.getTickers();
    }

    public void setLastSecondHealth(int lastSecondHealth) {
        actionBarParser.setLastSecondHealth(lastSecondHealth);
    }

    public boolean shouldResetMouse() {
        return System.currentTimeMillis() - lastClosedInv > 100;
    }

    Integer getHealthUpdate() {
        return actionBarParser.getHealthUpdate();
    }

    private void changeMana(int change) {
        setAttribute(Attribute.MANA, getAttribute(Attribute.MANA) + change);
    }

    private int getAttribute(Attribute attribute) {
        return main.getUtils().getAttributes().get(attribute).getValue();
    }

    private void setAttribute(Attribute attribute, int value) {
        main.getUtils().getAttributes().get(attribute).setValue(value);
    }

    private boolean shouldTriggerFishingIndicator() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.thePlayer.fishEntity != null && mc.thePlayer.getHeldItem() != null
                && mc.thePlayer.getHeldItem().getItem().equals(Items.fishing_rod)
                && main.getConfigValues().isEnabled(Feature.FISHING_SOUND_INDICATOR)) {
            // Highly consistent detection by checking when the hook has been in the water for a while and
            // suddenly moves downward. The client may rarely bug out with the idle bobbing and trigger a false positive.
            EntityFishHook bobber = mc.thePlayer.fishEntity;
            long currentTime = System.currentTimeMillis();
            if (bobber.isInWater() && !oldBobberIsInWater) lastBobberEnteredWater = currentTime;
            oldBobberIsInWater = bobber.isInWater();
            if (bobber.isInWater() && Math.abs(bobber.motionX) < 0.01 && Math.abs(bobber.motionZ) < 0.01
                    && currentTime - lastFishingAlert > 1000 && currentTime - lastBobberEnteredWater > 1500) {
                double movement = bobber.posY - oldBobberPosY; // The Entity#motionY field is inaccurate for this purpose
                oldBobberPosY = bobber.posY;
                if (movement < -0.04d) {
                    lastFishingAlert = currentTime;
                    return true;
                }
            }
        }
        return false;
    }
}
