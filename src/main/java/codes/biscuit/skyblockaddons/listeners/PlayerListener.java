package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

public class PlayerListener {

    private boolean sentUpdate = false;
    private long lastWorldJoin = -1;
    private long lastBoss = -1;
    private int magmaTick = 1;
    private int timerTick = 1;
    private long lastMinionSound = -1;
    private Integer healthUpdate = null;
    private long lastHealthUpdate;

//    private Feature.Accuracy magmaTimerAccuracy = null;
//    private long magmaTime = 7200;

    private SkyblockAddons main;

    public PlayerListener(SkyblockAddons main) {
        this.main = main;
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
        }
    }

    /**
     * Interprets the action bar to extract mana, health, and defence. Enables/disables mana/health prediction,
     * and looks for mana usage messages in chat while predicting.
     */
    @SubscribeEvent()
    public void onChatReceive(ClientChatReceivedEvent e) {
        String message = e.message.getUnformattedText();
        if (e.type == 2) {
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
                        String manaPart;
                        if (splitMessage.length > 2) {
                            defencePart = splitMessage[1];
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
                            setAttribute(Attribute.DEFENCE, Integer.valueOf(main.getUtils().getNumbersOnly(defencePart).trim()));
                        }
                        String[] manaSplit = main.getUtils().getNumbersOnly(manaPart).split(Pattern.quote("/"));
                        setAttribute(Attribute.MANA, Integer.parseInt(manaSplit[0]));
                        setAttribute(Attribute.MAX_MANA, Integer.parseInt(manaSplit[1].trim()));
                        main.getRenderListener().setPredictMana(false);
                        main.getRenderListener().setPredictHealth(false);
                        StringBuilder newMessage = new StringBuilder();
                        boolean showHealth = main.getConfigValues().getDisabledFeatures().contains(Feature.HEALTH_BAR) && main.getConfigValues().getDisabledFeatures().contains(Feature.HEALTH_TEXT);
                        boolean showDefence = defencePart != null && main.getConfigValues().getDisabledFeatures().contains(Feature.DEFENCE_PERCENTAGE) && main.getConfigValues().getDisabledFeatures().contains(Feature.DEFENCE_TEXT);
                        boolean showMana = main.getConfigValues().getDisabledFeatures().contains(Feature.MANA_BAR) && main.getConfigValues().getDisabledFeatures().contains(Feature.MANA_TEXT);
                        if (showHealth) {
                            newMessage.append(healthPart);
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
                    if (returnMessage.length() == 0) {
                        returnMessage = "\u00a0"; // This is to solve an issue with oof mod, which doesn't check if a string is empty before dealing with it (and it spams chat).
                    }
                    e.message = new ChatComponentText(returnMessage);
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
     * This blocks interaction with Ember Rods on your island, to avoid blowing up chests.
     */
    @SubscribeEvent()
    public void onInteract(PlayerInteractEvent e) {
        if (!main.getConfigValues().getDisabledFeatures().contains(Feature.DISABLE_EMBER_ROD)) {
            Minecraft mc = Minecraft.getMinecraft();
            ItemStack heldItem = e.entityPlayer.getHeldItem();
            if (e.entityPlayer == mc.thePlayer && heldItem != null) {
                if (heldItem.getItem().equals(Items.blaze_rod) && heldItem.isItemEnchanted() && main.getUtils().getLocation() == EnumUtils.Location.ISLAND) {
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

                if(healthUpdate != null && System.currentTimeMillis()-lastHealthUpdate > 3000) {
                    healthUpdate = null;
                }
                if (main.getRenderListener().isPredictHealth()) {
                    EntityPlayerSP p = mc.thePlayer;
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
                    EntityPlayerSP p = mc.thePlayer;
                    if (p != null) {
                        main.getUtils().checkGameAndLocation();
                        main.getInventoryUtils().checkIfInventoryIsFull(mc, p);
                        main.getInventoryUtils().checkIfWearingSkeletonHelmet(p);
                        if (!sentUpdate) {
                            main.getUtils().checkUpdates();
                            sentUpdate = true;
                        }

                        if(mc.currentScreen == null) {
                            main.getInventoryUtils().getInventoryDifference(p.inventory.mainInventory);
                        }
                    }

                    main.getInventoryUtils().updateItemPickupLog();

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
        if (main.getUtils().isOnSkyblock() && main.getUtils().getLocation() == EnumUtils.Location.ISLAND) {
            Entity entity = e.entity;
            if (entity instanceof EntityArmorStand && entity.hasCustomName()) {
                int cooldown = main.getConfigValues().getWarningSeconds()*1000+5000;
                if (!main.getConfigValues().getDisabledFeatures().contains(Feature.MINION_FULL_WARNING) &&
                        entity.getCustomNameTag().equals("\u00A7cMy storage is full! :(")) {
                    long now = System.currentTimeMillis();
                    if (now - lastMinionSound > cooldown) { //this just spams message...
                        lastMinionSound = now;
                        main.getUtils().playSound("random.pop", 1);
                        main.getRenderListener().setSubtitleFeature(Feature.MINION_FULL_WARNING);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                main.getRenderListener().setSubtitleFeature(null);
                            }
                        }, main.getConfigValues().getWarningSeconds() * 1000);
                    }
                } else if (!main.getConfigValues().getDisabledFeatures().contains(Feature.MINION_STOP_WARNING) &&
                        entity.getCustomNameTag().startsWith("\u00A7cI can\'t reach any ")) {
                    long now = System.currentTimeMillis();
                    if (now - lastMinionSound > cooldown) {
                        lastMinionSound = now;
                        main.getUtils().playSound("random.orb", 1);
                        String mobName = entity.getCustomNameTag().split(Pattern.quote("\u00A7cI can\'t reach any "))[1].toLowerCase();
                        if (mobName.lastIndexOf("s") == mobName.length() - 1) {
                            mobName = mobName.substring(0, mobName.length() - 1);
                        }
                        main.getRenderListener().setCannotReachMobName(mobName);
                        main.getRenderListener().setSubtitleFeature(Feature.MINION_STOP_WARNING);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                main.getRenderListener().setSubtitleFeature(null);
                            }
                        }, main.getConfigValues().getWarningSeconds() * 1000);
                    }
                }
            }
        }
    }

    /**
     * The main timer for the magma boss checker.
     */
    @SubscribeEvent()
    public void onTickMagmaBossChecker(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START && !main.getConfigValues().getDisabledFeatures().contains(Feature.MAGMA_WARNING) && main.getUtils().isOnSkyblock()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.theWorld != null) {
                if ((lastBoss == -1 || System.currentTimeMillis() - lastBoss > 1800000) && magmaTick % 5 == 0) {
                    for (Entity entity : mc.theWorld.loadedEntityList) { // Loop through all the entities.
                        if (entity instanceof EntityMagmaCube) {
                            EntitySlime magma = (EntitySlime) entity;
                            int size = magma.getSlimeSize();
                            if (size > 10) { // Find a big magma boss
                                lastBoss = System.currentTimeMillis();
                                main.getRenderListener().setTitleFeature(Feature.MAGMA_WARNING); // Enable warning and disable again in four seconds.
                                magmaTick = 16; // so the sound plays instantly
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        main.getRenderListener().setTitleFeature(null);
                                    }
                                }, main.getConfigValues().getWarningSeconds()*1000); // 4 second warning.
//                                logServer(mc);
                            }
                        }
                    }
                }
                if (main.getRenderListener().getTitleFeature() == Feature.MAGMA_WARNING && magmaTick % 4 == 0) { // Play sound every 4 ticks or 1/5 second.
                    main.getUtils().playSound("random.orb", 0.5);
                }
            }
            magmaTick++;
            if (magmaTick > 20) {
                magmaTick = 1;
            }
        }
    }

    /**
     * This is simply to help players copy item nbt (for creating texture packs/other stuff).
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
                    main.getUtils().sendMessage(EnumChatFormatting.GREEN + "Copied this item's NBT to clipboard!");
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public long getLastWorldJoin() {
        return lastWorldJoin;
    }

    public enum GUIType {
        MAIN,
        EDIT_LOCATIONS
    }

    Integer getHealthUpdate() {
        return healthUpdate;
    }
}
