package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerListener {

    private final Pattern ENCHANTMENT_TOOLTIP_PATTERN = Pattern.compile("§.§.(§9[\\w ]+(, )?)+");
    private final Pattern ABILITY_CHAT_PATTERN = Pattern.compile("§r§aUsed §r§6[A-Za-z ]+§r§a! §r§b\\([0-9]+ Mana\\)§r");
    private final Pattern PROFILE_CHAT_PATTERN = Pattern.compile("§aYou are playing on profile: §e([A-Za-z]+).*");
    private final Pattern SWITCH_PROFILE_CHAT_PATTERN = Pattern.compile("§aYour profile was changed to: §e([A-Za-z]+).*");
    private final Pattern COLLECTIONS_CHAT_PATTERN = Pattern.compile("§.\\+(?:§[0-9a-f])?([0-9.]+) §?[0-9a-f]?([A-Za-z]+) (\\([0-9.,]+/[0-9.,]+\\))");
    private final Set<String> randomMessages = new HashSet<>(Arrays.asList("I feel like I can fly!", "What was in that soup?", "Hmm… tasty!", "Hmm... tasty!", "You can now fly for 2 minutes.", "Your Magical Mushroom Soup flight has been extended for 2 extra minutes."));


    private boolean sentUpdate = false;

    private long lastWorldJoin = -1;
    private long lastBoss = -1;
    private int magmaTick = 1;
    private int timerTick = 1;
    private long lastMinionSound = -1;
    private long lastBossSpawnPost = -1;
    private long lastBossDeathPost = -1;
    private long lastMagmaWavePost = -1;
    private long lastBlazeWavePost = -1;
    private Class<?> lastOpenedInventory = null;
    private long lastClosedInv = -1;
    private long lastFishingAlert = 0;
    private long lastBobberEnteredWater = Long.MAX_VALUE;

    private boolean oldBobberIsInWater = false;
    private double oldBobberPosY = 0;

    private Set<Entity> countedEndermen = new HashSet<>();
    private Set<CoordsPair> recentlyLoadedChunks = new HashSet<>();
    private EnumUtils.MagmaTimerAccuracy magmaAccuracy = EnumUtils.MagmaTimerAccuracy.NO_DATA;
    private int magmaTime = 0;
    private int recentMagmaCubes = 0;
    private int recentBlazes = 0;

    private final SkyblockAddons main;
    private final ActionBarParser actionBarParser;

    public PlayerListener(SkyblockAddons main) {
        this.main = main;
        actionBarParser = new ActionBarParser(main);
    }

    /**
     * Reset all the timers and stuff when joining a new world.
     */
    @SubscribeEvent()
    public void onWorldJoin(EntityJoinWorldEvent e) {
        if (e.entity == Minecraft.getMinecraft().thePlayer) {
            lastWorldJoin = System.currentTimeMillis();
            lastBoss = -1;
            magmaTick = 1;
            timerTick = 1;
            main.getInventoryUtils().resetPreviousInventory();
            recentlyLoadedChunks.clear();
            countedEndermen.clear();
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
            CoordsPair coords = new CoordsPair(x, z);
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
        String message = e.message.getUnformattedText();
        if (e.type == 2) {
            // action bar message, parse using ActionBarParser and display the rest message instead
            String restMessage = actionBarParser.parseActionBar(message);
            if (main.isUsingOofModv1() && restMessage.trim().length() == 0) {
                e.setCanceled(true);
            }
            e.message = new ChatComponentText(restMessage);
        } else {
            final String formattedText = e.message.getFormattedText();
            if (main.getRenderListener().isPredictMana() && message.startsWith("Used ") && message.endsWith("Mana)")) {
                int manaLost = Integer.parseInt(message.split(Pattern.quote("! ("))[1].split(Pattern.quote(" Mana)"))[0]);
                changeMana(-manaLost);
            }

            /*  Resets all user input on dead as to not walk backwards or stafe into the portal
                Might get trigger upon encountering a non named "You" though this chance is so
                minimal it can be discarded as a bug. */
            if (main.getConfigValues().isEnabled(Feature.PREVENT_MOVEMENT_ON_DEATH) && formattedText.startsWith("§r§c \u2620 §r§7You ")) {
                KeyBinding.unPressAllKeys();
            }
            // credits to tomotomo, thanks lol
            if (main.getConfigValues().isEnabled(Feature.SUMMONING_EYE_ALERT) && formattedText.equals("§r§6§lRARE DROP! §r§5Summoning Eye§r")) {
                main.getUtils().playLoudSound("random.orb", 0.5);
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
                    e.message = new ChatComponentText(e.message.getFormattedText() + ChatFormatting.GRAY + " (" + main.getPersistentValues().getKills() + ")");
                    main.getPersistentValues().setKills(-1); // This is triggered before the death of the killed zealot, so this is set to -1 to account for that.
                }
            }

            if (main.getConfigValues().isEnabled(Feature.DISABLE_MAGICAL_SOUP_MESSAGES) && randomMessages.contains(message)) {
                e.setCanceled(true);
            }

            if (main.getConfigValues().isEnabled(Feature.NO_ARROWS_LEFT_ALERT)) {
                if (formattedText.equals("§r§cYou don't have any more Arrows left in your Quiver!§r")) {
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setSubtitleFeature(Feature.NO_ARROWS_LEFT_ALERT);
                    main.getScheduler().schedule(Scheduler.CommandType.RESET_SUBTITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                } else if (formattedText.startsWith("§r§cYou only have") && formattedText.endsWith("Arrows left in your Quiver!§r")) {
                    int arrowsLeft = Integer.parseInt(main.getUtils().getNumbersOnly(formattedText).trim());
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setSubtitleFeature(Feature.NO_ARROWS_LEFT_ALERT);
                    main.getRenderListener().setArrowsLeft(arrowsLeft);// THIS IS IMPORTANT
                    main.getScheduler().schedule(Scheduler.CommandType.RESET_SUBTITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                }
            }

            Matcher matcher = ABILITY_CHAT_PATTERN.matcher(e.message.getFormattedText());
            if (matcher.matches()) {
                CooldownManager.put(Minecraft.getMinecraft().thePlayer.getHeldItem());
            } else {
                matcher = PROFILE_CHAT_PATTERN.matcher(e.message.getFormattedText());
                if (matcher.matches()) {
                    main.getUtils().setProfileName(matcher.group(1));
                } else {
                    matcher = SWITCH_PROFILE_CHAT_PATTERN.matcher(e.message.getFormattedText());
                    if (matcher.matches()) {
                        main.getUtils().setProfileName(matcher.group(1));
                    }
                }
            }
        }
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

    /**
     * This blocks interaction with Ember Rods on your island, to avoid blowing up chests, and placing enchanted items
     * such as enchanted lava buckets.
     */
    @SubscribeEvent()
    public void onInteract(PlayerInteractEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItem = e.entityPlayer.getHeldItem();
        if (main.getUtils().isOnSkyblock() && e.entityPlayer == mc.thePlayer && heldItem != null) {
            // Prevent using ember rod on personal island
            if (heldItem.getItem().equals(Items.fishing_rod) // Update fishing status
                    && (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR)) {
                if (main.getConfigValues().isEnabled(Feature.FISHING_SOUND_INDICATOR)) {
                    oldBobberIsInWater = false;
                    lastBobberEnteredWater = Long.MAX_VALUE;
                    oldBobberPosY = 0;
                }
                if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS) && mc.thePlayer.fishEntity != null) {
                    CooldownManager.put(mc.thePlayer.getHeldItem());
                }
            } else if (main.getConfigValues().isEnabled(Feature.AVOID_PLACING_ENCHANTED_ITEMS) && EnchantedItemBlacklist.shouldBlockUsage(heldItem, e.action)) {
                e.setCanceled(true);
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
                    if (main.getRenderListener().isPredictMana()) {
                        changeMana(getAttribute(Attribute.MAX_MANA) / 50);
                        if (getAttribute(Attribute.MANA) > getAttribute(Attribute.MAX_MANA))
                            setAttribute(Attribute.MANA, getAttribute(Attribute.MAX_MANA));
                    }
                } else if (timerTick % 5 == 0) { // Check inventory, location, updates, and skeleton helmet every 1/4 second.
                    EntityPlayerSP p = mc.thePlayer;
                    if (p != null) {
                        main.getUtils().checkGameLocationDate();
                        main.getInventoryUtils().checkIfInventoryIsFull(mc, p);
                        main.getInventoryUtils().checkIfWearingSkeletonHelmet(p);
                        main.getInventoryUtils().checkIfWearingRevenantArmor(p);
                        if (!sentUpdate) {
                            main.getUtils().checkUpdates();
                            sentUpdate = true;
                        }

                        if (mc.currentScreen == null && main.getConfigValues().isEnabled(Feature.ITEM_PICKUP_LOG)
                                && main.getPlayerListener().didntRecentlyJoinWorld()) {
                            main.getInventoryUtils().getInventoryDifference(p.inventory.mainInventory);
                        }

                        if(main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.TAB_EFFECT_TIMERS)){
                            TabEffectManager.getInstance().updatePotionEffects();
                        }
                    }

                    main.getInventoryUtils().cleanUpPickupLog();

                } else if (timerTick > 20) { // To keep the timer going from 1 to 21 only.
                    timerTick = 1;
                }
            }
        }
    }

    /**
     * Checks for minion holograms.
     * Original contribution by Michael#3549.
     */
    @SubscribeEvent
    public void onEntityEvent(LivingEvent.LivingUpdateEvent e) {
        Entity entity = e.entity;

        if (main.getUtils().isOnSkyblock() && entity instanceof EntityArmorStand && entity.hasCustomName()) {
            String customNameTag = entity.getCustomNameTag();

            PowerOrb powerOrb = PowerOrb.getByDisplayname(customNameTag);
            if (powerOrb != null
                    && Minecraft.getMinecraft().thePlayer != null
                    && powerOrb.isInRadius(entity.getPosition().distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()))) {
                String[] customNameTagSplit = customNameTag.split(" ");
                String secondsString = customNameTagSplit[customNameTagSplit.length - 1]
                        .replaceAll("§e", "")
                        .replaceAll("s", "");
                try {
                    // Apparently they don't have a second count for moment after spawning, that's what this try-catch is for
                    int seconds = Integer.parseInt(secondsString);
                    PowerOrbManager.getInstance().put(powerOrb, seconds);
                } catch (NumberFormatException ignored) {
                }
            }

            if (main.getUtils().getLocation() == EnumUtils.Location.ISLAND) {
                int cooldown = main.getConfigValues().getWarningSeconds() * 1000 + 5000;
                if (main.getConfigValues().isEnabled(Feature.MINION_FULL_WARNING) &&
                        entity.getCustomNameTag().equals("§cMy storage is full! :(")) {
                    long now = System.currentTimeMillis();
                    if (now - lastMinionSound > cooldown) { //this just spams message...
                        lastMinionSound = now;
                        main.getUtils().playLoudSound("random.pop", 1);
                        main.getRenderListener().setSubtitleFeature(Feature.MINION_FULL_WARNING);
                        main.getScheduler().schedule(Scheduler.CommandType.RESET_SUBTITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                    }
                } else if (main.getConfigValues().isEnabled(Feature.MINION_STOP_WARNING) &&
                        entity.getCustomNameTag().startsWith("§cI can't reach any ")) {
                    long now = System.currentTimeMillis();
                    if (now - lastMinionSound > cooldown) {
                        lastMinionSound = now;
                        main.getUtils().playLoudSound("random.orb", 1);
                        String mobName = entity.getCustomNameTag().split(Pattern.quote("§cI can't reach any "))[1].toLowerCase();
                        if (mobName.lastIndexOf("s") == mobName.length() - 1) {
                            mobName = mobName.substring(0, mobName.length() - 1);
                        }
                        main.getRenderListener().setCannotReachMobName(mobName);
                        main.getRenderListener().setSubtitleFeature(Feature.MINION_STOP_WARNING);
                        main.getScheduler().schedule(Scheduler.CommandType.RESET_SUBTITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
    	if (main.getConfigValues().isEnabled(Feature.ZEALOT_COUNTER) && e.target instanceof EntityEnderman) {
            List<EntityArmorStand> stands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityArmorStand.class,
                    new AxisAlignedBB(e.target.posX - 1, e.target.posY, e.target.posZ - 1, e.target.posX + 1, e.target.posY + 5, e.target.posZ + 1));
            if (stands.isEmpty()) return;

            EntityArmorStand armorStand = stands.get(0);
            if (armorStand.hasCustomName() && armorStand.getCustomNameTag().contains("Zealot")) {
                countedEndermen.add(e.target);
            }
    	}
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
    	if (main.getConfigValues().isEnabled(Feature.ZEALOT_COUNTER) && e.entity instanceof EntityEnderman) {
            if (countedEndermen.remove(e.entity)) {
                main.getPersistentValues().addKill();
            }
    	}
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
                                        main.getUtils().sendPostRequest(EnumUtils.MagmaEvent.BOSS_SPAWN);
                                    }
                                }
                            }
                        }
                        if (!foundBoss && main.getRenderListener().getTitleFeature() == Feature.MAGMA_WARNING) {
                            main.getRenderListener().setTitleFeature(null);
                        }
                        if (!foundBoss && magmaAccuracy == EnumUtils.MagmaTimerAccuracy.SPAWNED) {
                            magmaAccuracy = EnumUtils.MagmaTimerAccuracy.ABOUT;
                            setMagmaTime(7200, true);
                            if (currentTime - lastBossDeathPost > 300000) {
                                lastBossDeathPost = currentTime;
                                main.getUtils().sendPostRequest(EnumUtils.MagmaEvent.BOSS_DEATH);
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
    public void onTickMagmaBossChecker(EntityEvent.EnteringChunk e) { // EntityJoinWorldEvent

        // Between these two coordinates is the whole "arena" area where all the magmas and stuff are.
        AxisAlignedBB spawnArea = new AxisAlignedBB(-244, 0, -566, -379, 255, -635);

        if (main.getUtils().getLocation() == EnumUtils.Location.BLAZING_FORTRESS) {
            Entity entity = e.entity;
            if (spawnArea.isVecInside(new Vec3(entity.posX, entity.posY, entity.posZ))) { // timers will trigger if 15 magmas/8 blazes spawn in the box within a 4 second time period
                long currentTime = System.currentTimeMillis();
                if (e.entity instanceof EntityMagmaCube) {
                    if (!recentlyLoadedChunks.contains(new CoordsPair(e.newChunkX, e.newChunkZ)) && entity.ticksExisted == 0) {
                        recentMagmaCubes++;
                        main.getScheduler().schedule(Scheduler.CommandType.SUBTRACT_MAGMA_COUNT, 4);
                        if (recentMagmaCubes >= 17) {
                            setMagmaTime(600, true);
                            magmaAccuracy = EnumUtils.MagmaTimerAccuracy.EXACTLY;
                            if (currentTime - lastMagmaWavePost > 300000) {
                                lastMagmaWavePost = currentTime;
                                main.getUtils().sendPostRequest(EnumUtils.MagmaEvent.MAGMA_WAVE);
                            }
                        }
                    }
                } else if (e.entity instanceof EntityBlaze) {
                    if (!recentlyLoadedChunks.contains(new CoordsPair(e.newChunkX, e.newChunkZ)) && entity.ticksExisted == 0) {
                        recentBlazes++;
                        main.getScheduler().schedule(Scheduler.CommandType.SUBTRACT_BLAZE_COUNT, 4);
                        if (recentBlazes >= 10) {
                            setMagmaTime(1200, true);
                            magmaAccuracy = EnumUtils.MagmaTimerAccuracy.EXACTLY;
                            if (currentTime - lastBlazeWavePost > 300000) {
                                lastBlazeWavePost = currentTime;
                                main.getUtils().sendPostRequest(EnumUtils.MagmaEvent.BLAZE_WAVE);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Modifies item tooltips and activates the copy item nbt feature
     */
    @SubscribeEvent()
    public void onItemTooltip(ItemTooltipEvent e) {
        ItemStack hoveredItem = e.itemStack;
        if (hoveredItem.hasTagCompound() && GuiScreen.isCtrlKeyDown() && main.getUtils().isCopyNBT()) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String nbt = hoveredItem.getTagCompound().toString();
            try {
                if (!clipboard.getData(DataFlavor.stringFlavor).equals(nbt)) {
                    clipboard.setContents(new StringSelection(nbt), null);
                    main.getUtils().sendMessage(ChatFormatting.GREEN + "Copied this item's NBT to clipboard!");
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                try {
                    clipboard.setContents(new StringSelection(nbt), null);
                    main.getUtils().sendMessage(ChatFormatting.GREEN + "Copied this item's NBT to clipboard!");
                } catch (IllegalStateException ex1) {
                    main.getUtils().sendMessage(ChatFormatting.RED + "Error copying item NBT to clipboard!");
                    ex.printStackTrace();
                }
            }
        }

        if (e.toolTip != null && main.getUtils().isOnSkyblock() && !main.getConfigValues().isRemoteDisabled(Feature.HIDE_GREY_ENCHANTS)) {
            for (int i = 1; i <= 3; i++) { // only a max of 2 gray enchants are possible
                if (i >= e.toolTip.size()) continue; // out of bounds

                GuiScreen gui = Minecraft.getMinecraft().currentScreen;
                if (gui instanceof GuiChest) {
                    Container chest = ((GuiChest)gui).inventorySlots;
                    if (chest instanceof ContainerChest) {
                        IInventory inventory = ((ContainerChest)chest).getLowerChestInventory();
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
            if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_ANVIL_USES)) {
                // Anvil Uses ~ original done by Dahn#6036
                int anvilUses = main.getUtils().getNBTInteger(hoveredItem, "ExtraAttributes", "anvil_uses");
                if (anvilUses != -1) {
                    int insertAt = e.toolTip.size();
                    insertAt--; // 1 line for the rarity
                    if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
                        insertAt -= 2; // 1 line for the item name, and 1 line for the nbt
                        if (e.itemStack.isItemDamaged()) {
                            insertAt--; // 1 line for damage
                        }
                    }
                    int hotPotatoCount = main.getUtils().getNBTInteger(hoveredItem, "ExtraAttributes", "hot_potato_count");
                    if (hotPotatoCount != -1) {
                        anvilUses -= hotPotatoCount;
                    }
                    if (anvilUses > 0) {
                        e.toolTip.add(insertAt, Message.MESSAGE_ANVIL_USES.getMessage(String.valueOf(anvilUses)));
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
                        String line = main.getUtils().stripColor(e.toolTip.get(i));
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
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onKeyInput(InputEvent.KeyInputEvent e) {
        if (main.getOpenSettingsKey().isPressed()) {
            main.getUtils().setFadingIn(true);
            main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
        }
        else if (main.getOpenEditLocationsKey().isPressed()) {
            main.getUtils().setFadingIn(false);
            main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.EDIT_LOCATIONS, 0, null);
        }
    }

    private boolean shouldTriggerFishingIndicator() {
        Minecraft mc =  Minecraft.getMinecraft();
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
                if (movement < -0.04d){
                    lastFishingAlert = currentTime;
                    return true;
                }
            }
        }
        return false;
    }

    public enum GUIType {
        MAIN,
        EDIT_LOCATIONS
    }

    public boolean shouldResetMouse() {
        return System.currentTimeMillis() - lastClosedInv > 100;
    }

    public boolean didntRecentlyJoinWorld() {
        return System.currentTimeMillis() - lastWorldJoin > 3000;
    }

    Integer getHealthUpdate() {
        return actionBarParser.getHealthUpdate();
    }

    public EnumUtils.MagmaTimerAccuracy getMagmaAccuracy() {
        return magmaAccuracy;
    }

    int getMagmaTime() {
        return magmaTime;
    }

    public int getRecentBlazes() {
        return recentBlazes;
    }

    public int getRecentMagmaCubes() {
        return recentMagmaCubes;
    }

    public void setRecentBlazes(int recentBlazes) {
        this.recentBlazes = recentBlazes;
    }

    public void setRecentMagmaCubes(int recentMagmaCubes) {
        this.recentMagmaCubes = recentMagmaCubes;
    }

    public void setMagmaAccuracy(EnumUtils.MagmaTimerAccuracy magmaAccuracy) {
        this.magmaAccuracy = magmaAccuracy;
    }

    @SuppressWarnings("unused")
    public void setMagmaTime(int magmaTime, boolean save) {
        this.magmaTime = magmaTime;
    }

    public Set<CoordsPair> getRecentlyLoadedChunks() {
        return recentlyLoadedChunks;
    }

    public void setLastSecondHealth(int lastSecondHealth) {
        actionBarParser.setLastSecondHealth(lastSecondHealth);
    }

    public int getTickers() {
        return actionBarParser.getTickers();
    }

    public int getMaxTickers() {
        return actionBarParser.getMaxTickers();
    }
}
