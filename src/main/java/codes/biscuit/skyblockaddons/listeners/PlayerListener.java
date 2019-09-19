package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.events.PlayerKillEntityEvent;
import codes.biscuit.skyblockaddons.utils.Attribute;
import codes.biscuit.skyblockaddons.utils.CoordsPair;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.RomanNumeralParser;
import codes.biscuit.skyblockaddons.utils.Scheduler;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class PlayerListener {

    private boolean sentUpdate = false;
    private long lastWorldJoin = -1;
    private long lastBoss = -1;
    private int magmaTick = 1;
    private int timerTick = 1;
    private long lastScreenOpen = -1;
    private long lastMinionSound = -1;
    private Integer healthUpdate = null;
    private long lastHealthUpdate;

    private final Set<EntityLivingBase> attackedEntity = new HashSet<>();
    private Set<CoordsPair> recentlyLoadedChunks = new HashSet<>();
    private EnumUtils.MagmaTimerAccuracy magmaAccuracy = EnumUtils.MagmaTimerAccuracy.NO_DATA;
    private int magmaTime = 0;
    private int recentMagmaCubes = 0;
    private int recentBlazes = 0;

//    private Feature.Accuracy magmaTimerAccuracy = null;
//    private long magmaTime = 7200;

    private SkyblockAddons main;

    public PlayerListener(SkyblockAddons main) {
        this.main = main;
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        DamageSource damageSource = event.getSource();
        event.getAmount();

        if (damageSource != null) {
            Entity trueSource = damageSource.getTrueSource();

            if (trueSource != null && Minecraft.getMinecraft().player.getName().equals(trueSource.getName())) {
                main.getUtils().sendMessage("Dmg: " + event.getAmount() + ": " + entity.getHealth() + ": " + entity.getMaxHealth()) ;
                this.attackedEntity.add(entity);
            }
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();

        if (this.attackedEntity.remove(entity))
            MinecraftForge.EVENT_BUS.post(new PlayerKillEntityEvent(entity));
    }

    /**
     * Reset all the timers and stuff when joining a new world.
     */
    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent event) {
        if (Minecraft.getMinecraft().player.equals(event.getEntity())) {
            lastWorldJoin = System.currentTimeMillis();
            lastBoss = -1;
            magmaTick = 1;
            timerTick = 1;
            main.getInventoryUtils().resetPreviousInventory();
            recentlyLoadedChunks.clear();
        }
    }

    /**
     * Keep track of recently loaded chunks for the magma boss timer.
     */
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (main.getUtils().isOnSkyblock()) {
            int x = event.getChunk().x;
            int z = event.getChunk().z;
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
    public void onChatReceive(ClientChatReceivedEvent event) {
        String message = event.getMessage().getUnformattedText();
        if (event.getType() == ChatType.GAME_INFO) {
            if (message.endsWith("\u270E Mana\u00A7r")) {
                try {
                    String returnMessage;
                    if (message.startsWith("\u00A7d\u00A7lTHE END RACE")) { // Might be doing the end race!
                        // Example Action Bar: '§d§lTHE END RACE §e00:52.370            §b147/147✎ Mana§r'
                        String[] messageSplit = message.split(" {12}");
                        String[] manaSplit = main.getUtils().getNumbersOnly(messageSplit[1]).split(Pattern.quote("/"));
                        setAttribute(Attribute.MANA, Integer.parseInt(manaSplit[0]));
                        setAttribute(Attribute.MAX_MANA, Integer.parseInt(manaSplit[1].trim()));

                        main.getRenderListener().setPredictMana(false);
                        main.getRenderListener().setPredictHealth(true);
                        returnMessage = messageSplit[0];
                    } else {
                        // Example Action Bar: '§c586/586❤     §a247§a❈ Defense     §b173/173✎ Mana§r'
                        String[] splitMessage = message.split(" {5}");
                        String healthPart = splitMessage[0];
                        String defencePart = null;
                        String collectionPart = null;
                        String manaPart;
                        if (splitMessage.length > 2) {
                            if (!splitMessage[1].contains("(")) { // Example Collection Bar: '§c986/986❤     §3+1 Mining (10,714.7/15,000)     §b297/323✎ Mana§r'
                                defencePart = splitMessage[1];
                            } else {
                                collectionPart = splitMessage[1];
                            }
                            manaPart = splitMessage[2];
                        } else {
                            manaPart = splitMessage[1];
                        }
                        String[] healthSplit = main.getUtils().getNumbersOnly(main.getUtils().stripColor(healthPart)).split(Pattern.quote("/"));
                        int newHealth = Integer.parseInt(healthSplit[0]);
                        int health = getAttribute(Attribute.HEALTH);
                        if(newHealth != health) {
                            healthUpdate = newHealth - health;
                            lastHealthUpdate = System.currentTimeMillis();
                        }
                        setAttribute(Attribute.HEALTH, newHealth);
                        setAttribute(Attribute.MAX_HEALTH, Integer.parseInt(healthSplit[1]));
                        if (defencePart != null) {
                            setAttribute(Attribute.DEFENCE, Integer.parseInt(main.getUtils().getNumbersOnly(defencePart).trim()));
                        }
                        String[] manaSplit = main.getUtils().getNumbersOnly(manaPart).split(Pattern.quote("/"));
                        setAttribute(Attribute.MANA, Integer.parseInt(manaSplit[0]));
                        setAttribute(Attribute.MAX_MANA, Integer.parseInt(manaSplit[1].trim()));
                        main.getRenderListener().setPredictMana(false);
                        main.getRenderListener().setPredictHealth(false);
                        StringBuilder newMessage = new StringBuilder();
                        boolean showHealth = main.getConfigValues().isDisabled(Feature.HEALTH_BAR) && main.getConfigValues().isDisabled(Feature.HEALTH_TEXT);
                        boolean showDefence = defencePart != null && main.getConfigValues().isDisabled(Feature.DEFENCE_PERCENTAGE) && main.getConfigValues().isDisabled(Feature.DEFENCE_TEXT);
                        boolean showMana = main.getConfigValues().isDisabled(Feature.MANA_BAR) && main.getConfigValues().isDisabled(Feature.MANA_TEXT);
                        if (showHealth) {
                            newMessage.append(healthPart);
                        }
                        if (collectionPart != null) {
                            if (showHealth) newMessage.append("     ");
                            newMessage.append(collectionPart);
                        }
                        if (showDefence) {
                            if (showHealth) newMessage.append("     ");
                            newMessage.append(defencePart);
                        }
                        if (showMana) {
                            if (showHealth || showDefence) newMessage.append("     ");
                            newMessage.append(manaPart);
                        }
                        returnMessage = newMessage.toString();
                    }
                    if (main.isUsingOofModv1() && returnMessage.trim().isEmpty()) {
                        event.setCanceled(true);
                    }
                    event.setMessage(new TextComponentString(returnMessage));
                    return;
                } catch (Exception ex) {
                    main.getRenderListener().setPredictMana(true);
                    main.getRenderListener().setPredictHealth(true);
                }
            }
            main.getRenderListener().setPredictMana(true);
            main.getRenderListener().setPredictHealth(true);
        } else {
            if (main.getRenderListener().isPredictMana() && message.startsWith("Used ") && message.endsWith("Mana)")) {
                int manaLost = Integer.parseInt(message.split(Pattern.quote("! ("))[1].split(Pattern.quote(" Mana)"))[0]);
                changeMana(-manaLost);
            }

            /*  Resets all user input on dead as to not walk backwards or stafe into the portal
                Might get trigger upon encountering a non named "You" though this chance is so
                minimal it can be discarded as a bug. */
            if (main.getConfigValues().isEnabled(Feature.PREVENT_MOVEMENT_ON_DEATH) && event.getMessage().getFormattedText().startsWith("\u00A7r\u00A7c \u2620 \u00A7r\u00A77You ")) {
                KeyBinding.unPressAllKeys();
            }
            // credits to tomotomo, thanks lol
            if (main.getConfigValues().isEnabled(Feature.SUMMONING_EYE_ALERT) && "\u00A7r\u00A76\u00A7lRARE DROP! \u00A7r\u00A75Summoning Eye\u00A7r".equals(event.getMessage().getFormattedText())){
                main.getUtils().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5);
                main.getRenderListener().setTitleFeature(Feature.SUMMONING_EYE_ALERT);
                main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds());
            }
        }
    }

    private void changeMana(int change) {
        setAttribute(Attribute.MANA, getAttribute(Attribute.MANA)+change);
    }

    private int getAttribute(Attribute attribute) {
        return main.getUtils().getAttributes().get(attribute).getValue();
    }

    private void setAttribute(Attribute attribute, int value) {
        main.getUtils().getAttributes().get(attribute).setValue(value);
    }

    /**
     * The main timer for a bunch of stuff.
     */
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            timerTick++;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null) { // Predict health every tick if needed.

                if(healthUpdate != null && System.currentTimeMillis()-lastHealthUpdate > 3000) {
                    healthUpdate = null;
                }
                if (main.getRenderListener().isPredictHealth()) {
                    EntityPlayerSP p = mc.player;
                    if (p != null) { //Reverse calculate the player's health by using the player's vanilla hearts. Also calculate the health change for the gui item.
                        int newHealth = Math.round(getAttribute(Attribute.MAX_HEALTH) * (p.getHealth() / p.getMaxHealth()));
                        if(newHealth != getAttribute(Attribute.HEALTH)) {
                            healthUpdate = newHealth - getAttribute(Attribute.HEALTH);
                            lastHealthUpdate = System.currentTimeMillis();
                        }
                        setAttribute(Attribute.HEALTH, newHealth);
                    }
                }
                if (timerTick == 20) { // Add natural mana every second (increase is based on your max mana).
                    if (main.getRenderListener().isPredictMana()) {
                        changeMana(getAttribute(Attribute.MAX_MANA) / 50);
                        if (getAttribute(Attribute.MANA) > getAttribute(Attribute.MAX_MANA))
                            setAttribute(Attribute.MANA, getAttribute(Attribute.MAX_MANA));
                    }
                } else if (timerTick % 5 == 0) { // Check inventory, location, updates, and skeleton helmet every 1/4 second.
                    EntityPlayerSP p = mc.player;
                    if (p != null) {
                        main.getUtils().checkGameLocationDate();
                        main.getInventoryUtils().checkIfInventoryIsFull(mc, p);
                        main.getInventoryUtils().checkIfWearingSkeletonHelmet(p);
                        if (!sentUpdate) {
                            main.getUtils().checkUpdates();
                            sentUpdate = true;
                        }

                        if (mc.currentScreen != null) {
                            lastScreenOpen = System.currentTimeMillis();
                        } else if (main.getConfigValues().isEnabled(Feature.ITEM_PICKUP_LOG)
                                && main.getPlayerListener().didntRecentlyJoinWorld()) {
                            main.getInventoryUtils().getInventoryDifference(p.inventory.mainInventory.toArray(new ItemStack[0]));
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
    public void onEntityEvent(LivingEvent.LivingUpdateEvent event) {
        Entity entity = event.getEntity();
        if (main.getUtils().isOnSkyblock() && entity instanceof EntityArmorStand && entity.hasCustomName()) {
            if (main.getUtils().getLocation() == EnumUtils.Location.ISLAND) {
                int cooldown = main.getConfigValues().getWarningSeconds() * 1000 + 5000;
                if (main.getConfigValues().isEnabled(Feature.MINION_FULL_WARNING) &&
                        "\u00A7cMy storage is full! :(".equals(entity.getCustomNameTag())) {
                    long now = System.currentTimeMillis();
                    if (now - lastMinionSound > cooldown) { //this just spams message...
                        lastMinionSound = now;
                        main.getUtils().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1);
                        main.getRenderListener().setSubtitleFeature(Feature.MINION_FULL_WARNING);
                        main.getScheduler().schedule(Scheduler.CommandType.RESET_SUBTITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                    }
                } else if (main.getConfigValues().isEnabled(Feature.MINION_STOP_WARNING) &&
                        entity.getCustomNameTag().startsWith("\u00A7cI can\'t reach any ")) {
                    long now = System.currentTimeMillis();
                    if (now - lastMinionSound > cooldown) {
                        lastMinionSound = now;
                        main.getUtils().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1);
                        String mobName = entity.getCustomNameTag().split(Pattern.quote("\u00A7cI can\'t reach any "))[1].toLowerCase();
                        if (mobName.lastIndexOf("s") == mobName.length() - 1) {
                            mobName = mobName.substring(0, mobName.length() - 1);
                        }
                        main.getRenderListener().setCannotReachMobName(mobName);
                        main.getRenderListener().setSubtitleFeature(Feature.MINION_STOP_WARNING);
                        main.getScheduler().schedule(Scheduler.CommandType.RESET_SUBTITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                    }
                } // Apparently it no longer has a health bar
            }// else if (magmaAccuracy == EnumUtils.MagmaTimerAccuracy.SPAWNED &&
//                    main.getConfigValues().isEnabled(Feature.MAGMA_BOSS_TIMER)) {
//                String name = main.getUtils().stripColor(entity.getCustomNameTag());
//                if (name.contains("Magma Cube Boss")) {
//                    magmaBossHealth = Integer.valueOf(name.split(Pattern.quote("Magma Cube Boss "))[1].split(Pattern.quote("/"))[0]);
//                }
//            }
        }
    }

    // Doesn't work at the moment, using line 378 instead.
//    @SubscribeEvent
//    public void onDeath(LivingDeathEvent e) {
//        if (e.entity instanceof EntityMagmaCube) {
//            EntitySlime magma = (EntitySlime)e.entity;
//            if (magma.getSlimeSize() > 10 && (magmaAccuracy == EnumUtils.MagmaTimerAccuracy.SPAWNED ||
//                    magmaAccuracy == EnumUtils.MagmaTimerAccuracy.SPAWNED_PREDICTION)) {
//                magmaAccuracy = EnumUtils.MagmaTimerAccuracy.ABOUT;
//                magmaTime = 7200;
//            }
//        }
//    }

    private long lastBossSpawnPost = -1;
    private long lastBossDeathPost = -1;

    /**
     * The main timer for the magma boss checker.
     */
    @SubscribeEvent
    public void onClientTickMagma(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            if (main.getConfigValues().isEnabled(Feature.MAGMA_WARNING) && main.getUtils().isOnSkyblock()) {
                if (mc != null && mc.world != null) {
                    if (magmaTick % 5 == 0) {
                        boolean foundBoss = false;
                        long currentTime = System.currentTimeMillis();
                        for (Entity entity : mc.world.loadedEntityList) { // Loop through all the entities.
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
                                    if (currentTime- lastBossSpawnPost > 300000) {
                                        lastBossSpawnPost = currentTime;
                                        main.getUtils().sendPostRequest(EnumUtils.MagmaEvent.BOSS_SPAWN);
                                    }
                                }
                            }
                        }
                        if (!foundBoss && magmaAccuracy == EnumUtils.MagmaTimerAccuracy.SPAWNED) {
                            magmaAccuracy = EnumUtils.MagmaTimerAccuracy.ABOUT;
                            setMagmaTime(7200, true);
                            if (currentTime- lastBossDeathPost > 300000) {
                                lastBossDeathPost = currentTime;
                                main.getUtils().sendPostRequest(EnumUtils.MagmaEvent.BOSS_DEATH);
                            }
                        }
                    }
                    if (main.getRenderListener().getTitleFeature() == Feature.MAGMA_WARNING && magmaTick % 4 == 0) { // Play sound every 4 ticks or 1/5 second.
                        main.getUtils().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5);
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

    private long lastMagmaWavePost = -1;
    private long lastBlazeWavePost = -1;

    @SubscribeEvent
    public void onTickMagmaBossChecker(EntityEvent.EnteringChunk event) { // EntityJoinWorldEvent

        // Between these two coordinates is the whole "arena" area where all the magmas and stuff are.
        AxisAlignedBB spawnArea = new AxisAlignedBB(-244, 0, -566, -379, 255, -635);

        if (main.getUtils().getLocation() == EnumUtils.Location.BLAZING_FORTRESS) {
            Entity entity =  event.getEntity();
            if (spawnArea.contains(new Vec3d(entity.posX, entity.posY, entity.posZ))) { // timers will trigger if 15 magmas/8 blazes spawn in the box within a 4 second time period
                long currentTime = System.currentTimeMillis();
                if (event.getEntity() instanceof EntityMagmaCube) {
                    if (!recentlyLoadedChunks.contains(new CoordsPair(event.getNewChunkX(), event.getNewChunkZ())) && entity.ticksExisted == 0) {
                        recentMagmaCubes++;
                        main.getScheduler().schedule(Scheduler.CommandType.SUBTRACT_MAGMA_COUNT, 4);
                        if (recentMagmaCubes >= 17) {
                            setMagmaTime(600, true);
                            magmaAccuracy = EnumUtils.MagmaTimerAccuracy.EXACTLY;
                            if (currentTime- lastMagmaWavePost > 300000) {
                                lastMagmaWavePost = currentTime;
                                main.getUtils().sendPostRequest(EnumUtils.MagmaEvent.MAGMA_WAVE);
                            }
                        }
                    }
                } else if (event.getEntity() instanceof EntityBlaze) {
                    if (!recentlyLoadedChunks.contains(new CoordsPair(event.getNewChunkX(), event.getNewChunkZ())) && entity.ticksExisted == 0) {
                        recentBlazes++;
                        main.getScheduler().schedule(Scheduler.CommandType.SUBTRACT_BLAZE_COUNT, 4);
                        if (recentBlazes >= 10) {
                            setMagmaTime(1200, true);
                            magmaAccuracy = EnumUtils.MagmaTimerAccuracy.EXACTLY;
                            if (currentTime- lastBlazeWavePost > 300000) {
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
     * This is simply to help players copy item nbt (for creating texture packs/other stuff).
     */
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack hoveredItem = event.getItemStack();

        if (main.getConfigValues().isEnabled(Feature.SHOW_ITEM_ANVIL_USES)) {
            // Anvil Uses ~ original done by Dahn#6036
            if (hoveredItem.hasTagCompound()) {
                NBTTagCompound nbt = hoveredItem.getTagCompound();
                if (nbt.hasKey("ExtraAttributes")) {
                    if (nbt.getCompoundTag("ExtraAttributes").hasKey("anvil_uses")) {
                        int insertAt = event.getToolTip().size();
                        if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
                            insertAt -= 3; // 1 line for the item name, 1 line for the nbt, and 1 line for the rarity
                            if (event.getItemStack().isItemDamaged()) {
                                insertAt--; // 1 line for damage
                            }
                        }
                        int anvilUses = nbt.getCompoundTag("ExtraAttributes").getInteger("anvil_uses");
                        if (nbt.getCompoundTag("ExtraAttributes").hasKey("hot_potato_count")) {
                            int hotPotatoCount = nbt.getCompoundTag("ExtraAttributes").getInteger("hot_potato_count");
                            anvilUses -= hotPotatoCount;
                        }
                        if (anvilUses > 0) {
                            event.getToolTip().add(insertAt, "Anvil Uses: " + TextFormatting.RED.toString() + anvilUses);
                        }
                    }
                }
            }
        }

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
                    main.getUtils().sendMessage(TextFormatting.GREEN + "Copied this item's NBT to clipboard!");
                } catch (IllegalStateException ex1) {
                    main.getUtils().sendMessage(TextFormatting.RED + "Error copying item NBT to clipboard!");
                    ex.printStackTrace();
                }
            }
        }

        if (main.getUtils().isOnSkyblock() &&
                main.getConfigValues().isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS) &&
                event.getToolTip() != null) {
            for (int i = 0; i < event.getToolTip().size(); i++)
                event.getToolTip().set(i, RomanNumeralParser.replaceNumeralsWithIntegers(event.getToolTip().get(i)));
        }
    }


    private Class lastOpenedInventory = null;
    private long lastClosedInv = -1;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() == null && GuiChest.class.equals(lastOpenedInventory)) {
            lastClosedInv = System.currentTimeMillis();
            lastOpenedInventory = null;
        }
        if (event.getGui() != null) {
            lastOpenedInventory = event.getGui().getClass();
        }
    }

//    @SubscribeEvent(receiveCanceled = true)
//    public void onKeyInput(InputEvent.KeyInputEvent e) {
//        if (main.getLockSlot().isPressed()) {
//            main.getConfigValues().getLockedSlots().add(main.getUtils().getLastHoveredSlot());
//        }
//    }

    public boolean shouldResetMouse() {
        return System.currentTimeMillis() - lastClosedInv > 100;
    }

    public boolean didntRecentlyCloseScreen() {
        return System.currentTimeMillis() - lastScreenOpen > 500;
    }

    public boolean didntRecentlyJoinWorld() {
        return System.currentTimeMillis() - lastWorldJoin > 3000;
    }

    public enum GUIType {
        MAIN,
        EDIT_LOCATIONS
    }

    Integer getHealthUpdate() {
        return healthUpdate;
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
//        main.getConfigValues().setNextMagmaTimestamp(System.currentTimeMillis()+(magmaTime*1000));
//        if (save) {
//            main.getConfigValues().saveConfig();
//        }
    }

    public Set<CoordsPair> getRecentlyLoadedChunks() {
        return recentlyLoadedChunks;
    }
}