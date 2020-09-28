package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.features.ItemDiff;
import codes.biscuit.skyblockaddons.features.SlayerArmorProgress;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTracker;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO Fix for Hypixel localization

/**
 * Utility methods related to player inventories
 */
public class InventoryUtils {

    /** Slot index the SkyBlock menu is at. */
    private static final int SKYBLOCK_MENU_SLOT = 8;

    /** Display name of the Skeleton Helmet. */
    private static final String SKELETON_HELMET_ID = "SKELETON_HELMET";
    private static final String TOXIC_ARROW_POISON_ID = "TOXIC_ARROW_POISON";

    public static final String MADDOX_BATPHONE_DISPLAYNAME = "§aMaddox Batphone";
    public static final String JUNGLE_AXE_DISPLAYNAME = "§aJungle Axe";
    public static final String TREECAPITATOR_DISPLAYNAME = "§5Treecapitator";
    public static final String CHICKEN_HEAD_DISPLAYNAME = "§fChicken Head";

    private static final Pattern REVENANT_UPGRADE_PATTERN = Pattern.compile("Next Upgrade: \\+([0-9]+❈) \\(([0-9,]+)/([0-9,]+)\\)");

    private List<ItemStack> previousInventory;
    private Multimap<String, ItemDiff> itemPickupLog = ArrayListMultimap.create();

    @Setter
    private boolean inventoryWarningShown;

    /** Whether the player is wearing a Skeleton Helmet. */
    @Getter private boolean wearingSkeletonHelmet;

    @Getter private boolean usingToxicArrowPoison;

    @Getter private SlayerArmorProgress[] slayerArmorProgresses = new SlayerArmorProgress[4];

    @Getter private InventoryType inventoryType;

    private SkyblockAddons main = SkyblockAddons.getInstance();

    /**
     * Copies an inventory into a List of copied ItemStacks
     *
     * @param inventory Inventory to copy
     * @return List of copied ItemStacks
     */
    private List<ItemStack> copyInventory(ItemStack[] inventory) {
        List<ItemStack> copy = new ArrayList<>(inventory.length);
        for (ItemStack item : inventory) {
            if (item != null) {
                copy.add(ItemStack.copyItemStack(item));
            } else {
                copy.add(null);
            }
        }
        return copy;
    }

    /**
     * Compares previously recorded Inventory state with current Inventory state to determine changes and
     * stores them in {@link #itemPickupLog}
     *
     * @param currentInventory Current Inventory state
     */
    public void getInventoryDifference(ItemStack[] currentInventory) {
        List<ItemStack> newInventory = copyInventory(currentInventory);
        Map<String, Pair<Integer, NBTTagCompound>> previousInventoryMap = new HashMap<>();
        Map<String, Pair<Integer, NBTTagCompound>> newInventoryMap = new HashMap<>();

        if (previousInventory != null) {

            for(int i = 0; i < newInventory.size(); i++) {
                if (i == SKYBLOCK_MENU_SLOT) { // Skip the SkyBlock Menu slot all together (which includes the Quiver Arrow now)
                    continue;
                }

                ItemStack previousItem = previousInventory.get(i);
                ItemStack newItem = newInventory.get(i);

                if(previousItem != null) {
                    int amount;
                    if (previousInventoryMap.containsKey(previousItem.getDisplayName())) {
                        amount = previousInventoryMap.get(previousItem.getDisplayName()).getKey() + previousItem.stackSize;
                    } else {
                        amount = previousItem.stackSize;
                    }
                    NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(previousItem);
                    if (extraAttributes != null) {
                        extraAttributes = (NBTTagCompound) extraAttributes.copy();
                    }
                    previousInventoryMap.put(previousItem.getDisplayName(), new Pair<>(amount, extraAttributes));
                }

                if(newItem != null) {
                    if (newItem.getDisplayName().contains(" "+ ColorCode.DARK_GRAY+"x")) {
                        String newName = newItem.getDisplayName().substring(0, newItem.getDisplayName().lastIndexOf(" "));
                        newItem.setStackDisplayName(newName); // This is a workaround for merchants, it adds x64 or whatever to the end of the name.
                    }
                    int amount;
                    if (newInventoryMap.containsKey(newItem.getDisplayName())) {
                        amount = newInventoryMap.get(newItem.getDisplayName()).getKey() + newItem.stackSize;
                    }  else {
                        amount = newItem.stackSize;
                    }
                    NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(newItem);
                    if (extraAttributes != null) {
                        extraAttributes = (NBTTagCompound) extraAttributes.copy();
                    }
                    newInventoryMap.put(newItem.getDisplayName(), new Pair<>(amount, extraAttributes));
                }
            }

            List<ItemDiff> inventoryDifference = new LinkedList<>();
            Set<String> keySet = new HashSet<>(previousInventoryMap.keySet());
            keySet.addAll(newInventoryMap.keySet());

            keySet.forEach(key -> {
                int previousAmount = 0;
                if (previousInventoryMap.containsKey(key)) {
                    previousAmount = previousInventoryMap.get(key).getKey();
                }

                int newAmount = 0;
                if (newInventoryMap.containsKey(key)) {
                    newAmount = newInventoryMap.get(key).getKey();
                }

                int diff = newAmount - previousAmount;
                if (diff != 0) { // Get the NBT tag from whichever map the name exists in
                    inventoryDifference.add(new ItemDiff(key, diff, newInventoryMap.getOrDefault(key, previousInventoryMap.get(key)).getValue()));
                }
            });

            DragonTracker.getInstance().checkInventoryDifferenceForDrops(inventoryDifference);
            SlayerTracker.getInstance().checkInventoryDifferenceForDrops(inventoryDifference);

            // Add changes to already logged changes of the same item, so it will increase/decrease the amount
            // instead of displaying the same item twice
            if (main.getConfigValues().isEnabled(Feature.ITEM_PICKUP_LOG)) {
                for (ItemDiff diff : inventoryDifference) {
                    Collection<ItemDiff> itemDiffs = itemPickupLog.get(diff.getDisplayName());
                    if (itemDiffs.size() <= 0) {
                        itemPickupLog.put(diff.getDisplayName(), diff);

                    } else {
                        boolean added = false;
                        for (ItemDiff loopDiff : itemDiffs) {
                            if ((diff.getAmount() < 0 && loopDiff.getAmount() < 0) || (diff.getAmount() > 0 && loopDiff.getAmount() > 0)) {
                                loopDiff.add(diff.getAmount());
                                added = true;
                            }
                        }
                        if (!added) {
                            itemPickupLog.put(diff.getDisplayName(), diff);
                        }
                    }
                }
            }

        }

        previousInventory = newInventory;
    }

    /**
     * Resets the previously stored Inventory state
     */
    public void resetPreviousInventory() {
        previousInventory = null;
    }

    /**
     * Removes items in the pickup log that have been there for longer than {@link ItemDiff#LIFESPAN}
     */
    public void cleanUpPickupLog() {
        itemPickupLog.entries().removeIf(entry -> entry.getValue().getLifetime() > ItemDiff.LIFESPAN);
    }

    /**
     * Checks if the players inventory is full and displays an alarm if so.
     *
     * @param mc Minecraft instance
     * @param p Player to check
     */
    public void checkIfInventoryIsFull(Minecraft mc, EntityPlayerSP p) {
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.FULL_INVENTORY_WARNING)) {
            /*
            If the inventory is full, show the full inventory warning.
            Slot 8 is the Skyblock menu/quiver arrow slot. It's ignored so shooting with a full inventory
            doesn't spam the full inventory warning.
             */
            for (int i = 0; i < p.inventory.mainInventory.length; i++) {
                // If we find an empty slot that isn't slot 8, remove any queued warnings and stop checking.
                if (p.inventory.mainInventory[i] == null && i != 8) {
                    if (inventoryWarningShown) {
                        main.getScheduler().removeQueuedFullInventoryWarnings();
                    }
                    inventoryWarningShown = false;
                    return;
                }
            }

            // If we make it here, the inventory is full. Show the warning.
            if (mc.currentScreen == null && main.getPlayerListener().didntRecentlyJoinWorld() && !inventoryWarningShown) {
                showFullInventoryWarning();
                main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds());

                // Schedule a repeat if needed.
                if (main.getConfigValues().isEnabled(Feature.REPEAT_FULL_INVENTORY_WARNING)) {
                    main.getScheduler().schedule(Scheduler.CommandType.SHOW_FULL_INVENTORY_WARNING, 10);
                    main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, 10 + main.getConfigValues().getWarningSeconds());
                }

                inventoryWarningShown = true;
            }
        }
    }

    /**
     * Shows the full inventory warning.
     */
    public void showFullInventoryWarning() {
        main.getUtils().playLoudSound("random.orb", 0.5);
        main.getRenderListener().setTitleFeature(Feature.FULL_INVENTORY_WARNING);
    }

    /**
     * Checks if the player is wearing a Skeleton Helmet and updates {@link #wearingSkeletonHelmet} accordingly
     *
     * @param p Player to check
     */
    public void checkIfWearingSkeletonHelmet(EntityPlayerSP p) {
        if (main.getConfigValues().isEnabled(Feature.SKELETON_BAR)) {
            ItemStack item = p.getEquipmentInSlot(4);
            if (item != null && SKELETON_HELMET_ID.equals(ItemUtils.getSkyBlockItemID(item))) {
                wearingSkeletonHelmet = true;
                return;
            }
            wearingSkeletonHelmet = false;
        }
    }

    /**
     * Determines if the player is using Toxic Arrow Poison by detecting if it is present in their inventory.
     *
     * @param p the player to check
     */
    public void checkIfUsingToxicArrowPoison(EntityPlayerSP p) {
        if (main.getConfigValues().isEnabled(Feature.TURN_BOW_GREEN_WHEN_USING_TOXIC_ARROW_POISON)) {
            for (ItemStack item : p.inventory.mainInventory) {
                if (item != null && TOXIC_ARROW_POISON_ID.equals(ItemUtils.getSkyBlockItemID(item))) {
                    this.usingToxicArrowPoison = true;
                    return;
                }
            }
            this.usingToxicArrowPoison = false;
        }
    }

    /**
     * The difference between a slot number in any given {@link Container} and what that number would be in a {@link ContainerPlayer}.
     */
    public int getSlotDifference(Container container) {
        if (container instanceof ContainerChest) return 9-((ContainerChest)container).getLowerChestInventory().getSizeInventory();
        else if (container instanceof ContainerHopper) return 4;
        else if (container instanceof ContainerFurnace) return 6;
        else if (container instanceof ContainerBeacon) return 8;
        else return 0;
    }

    /**
     * Checks if the player is wearing any Revenant or Tarantula armor.
     * If the armor is detected, the armor's levelling progress is retrieved to be displayed on the HUD.
     *
     * @param p the player to check
     */
    public void checkIfWearingSlayerArmor(EntityPlayerSP p) {
        if (main.getConfigValues().isEnabled(Feature.SLAYER_INDICATOR)) {
            for (int i = 3; i >= 0; i--) {
                ItemStack itemStack = p.inventory.armorInventory[i];
                String itemID = itemStack != null ? ItemUtils.getSkyBlockItemID(itemStack) : null;

                if (itemID != null && (itemID.startsWith("REVENANT") || itemID.startsWith("TARANTULA"))) {
                    String percent = null;
                    String defence = null;
                    List<String> lore = ItemUtils.getItemLore(itemStack);
                    for (String loreLine : lore) {
                        Matcher matcher = REVENANT_UPGRADE_PATTERN.matcher(TextUtils.stripColor(loreLine));
                        if (matcher.matches()) { // Example: line§5§o§7Next Upgrade: §a+240❈ §8(§a14,418§7/§c15,000§8)
                            try {
                                float percentage = Float.parseFloat(matcher.group(2).replace(",", "")) /
                                        Integer.parseInt(matcher.group(3).replace(",", "")) * 100;
                                BigDecimal bigDecimal = new BigDecimal(percentage).setScale(0, BigDecimal.ROUND_HALF_UP);
                                percent = bigDecimal.toString();
                                defence = ColorCode.GREEN + matcher.group(1);
                                break;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                    if (percent != null && defence != null) {
                        SlayerArmorProgress currentProgress = slayerArmorProgresses[i];

                        if (currentProgress == null || itemStack != currentProgress.getItemStack()) {
                            // The item has changed or didn't exist. Create new object.
                            slayerArmorProgresses[i] = new SlayerArmorProgress(itemStack, percent, defence);
                        } else {
                            // The item has remained the same. Just update the stats.
                            currentProgress.setPercent(percent);
                            currentProgress.setDefence(defence);
                        }
                    }
                } else {
                    slayerArmorProgresses[i] = null;
                }
            }
        }
    }

    /**
     * @return Log of recent Inventory changes
     */
    public Collection<ItemDiff> getItemPickupLog() {
        return itemPickupLog.values();
    }

    /**
     * Detects and stores, and returns the current Skyblock inventory type. The inventory type is the kind of menu the
     * player has open, like a crafting table or an enchanting table for example.
     *
     * @return the detected inventory type, or {@code null} if an unrecognized inventory is detected or there's no inventory open
     */
    public InventoryType updateInventoryType() {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;

        if (!(currentScreen instanceof GuiChest)) {
            return inventoryType = null;
        }

        IInventory inventory = ((GuiChest) currentScreen).lowerChestInventory;


        for (InventoryType inventoryType : InventoryType.values()) {
            if (inventoryType.getInventoryName().equals(inventory.getDisplayName().getUnformattedText())) {
                if (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.BASIC_ACCESSORY_BAG_REFORGING) {
                    return this.inventoryType = getReforgeInventoryType(inventoryType, inventory);

                } else {
                    return this.inventoryType = inventoryType;
                }
            }
        }

        return this.inventoryType = null;
    }

    // Gets the reforge inventory type from a given reforge inventory
    private InventoryType getReforgeInventoryType(InventoryType baseType, IInventory inventory) {
        // This is the barrier item that's present in the advanced reforging menu. This slot is empty in the basic reforging menu.
        ItemStack barrier = inventory.getStackInSlot(13);
        // This is the stained glass pane to the right of the barrier.
        ItemStack glassPane = inventory.getStackInSlot(14);

        /*
        If the barrier is there, it's the advanced reforging menu. If it's not there (since the player placed an item in
        the menu), check if the glass pane next to the slot is named "Reforge Stone." That indicates it's the advanced
        reforging menu. Otherwise, it's the basic menu. Finally, check if it's the single item or accessory bag menu.
         */
        if ((barrier != null && barrier.getItem() == Item.getItemFromBlock(Blocks.barrier)) ||
                (glassPane != null && glassPane.hasDisplayName() && TextUtils.stripColor(glassPane.getDisplayName()).equals("Reforge Stone"))) {
            return baseType == InventoryType.BASIC_REFORGING ? InventoryType.ADVANCED_REFORGING : InventoryType.ADVANCED_ACCESSORY_BAG_REFORGING;

        } else {
            return baseType == InventoryType.BASIC_REFORGING ? InventoryType.BASIC_REFORGING : InventoryType.BASIC_ACCESSORY_BAG_REFORGING;
        }
    }
}
