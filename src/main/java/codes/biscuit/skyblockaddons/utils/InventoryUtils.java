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
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public static final String MADDOX_BATPHONE_ID = "AATROX_BATPHONE";
    public static final String JUNGLE_AXE_ID = "JUNGLE_AXE";
    public static final String TREECAPITATOR_ID = "TREECAPITATOR_AXE";
    public static final String CHICKEN_HEAD_ID = "CHICKEN_HEAD";
    public static final HashSet<String> BAT_PERSON_SET_IDS = new HashSet<>(Arrays.asList("BAT_PERSON_BOOTS", "BAT_PERSON_LEGGINGS", "BAT_PERSON_CHESTPLATE", "BAT_PERSON_HELMET"));
    public static final String GRAPPLING_HOOK_ID = "GRAPPLING_HOOK";

    private static final Pattern REVENANT_UPGRADE_PATTERN = Pattern.compile("Next Upgrade: \\+([0-9]+❈) \\(([0-9,]+)/([0-9,]+)\\)");

    private List<ItemStack> previousInventory;
    private final Multimap<String, ItemDiff> itemPickupLog = ArrayListMultimap.create();

    @Setter
    private boolean inventoryWarningShown;

    /**
     * Whether the player is wearing a Skeleton Helmet.
     */
    @Getter
    private boolean wearingSkeletonHelmet;

    @Getter
    private boolean usingToxicArrowPoison;

    @Getter
    private final SlayerArmorProgress[] slayerArmorProgresses = new SlayerArmorProgress[4];

    @Getter
    private InventoryType inventoryType;
    @Getter
    String inventoryKey;
    @Getter
    private int inventoryPageNum;
    @Getter
    private String inventorySubtype;
    private final SkyblockAddons main = SkyblockAddons.getInstance();


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
                if (i == SKYBLOCK_MENU_SLOT) { // Skip the SkyBlock Menu slot altogether (which includes the Quiver Arrow now)
                    continue;
                }

                ItemStack previousItem = null;
                ItemStack newItem = null;

                try {
                    previousItem = previousInventory.get(i);
                    newItem = newInventory.get(i);

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
                } catch (RuntimeException exception) {
                    CrashReport crashReport = CrashReport.makeCrashReport(exception, "Comparing current inventory to previous inventory");
                    CrashReportCategory inventoryDetails = crashReport.makeCategory("Inventory Details");
                    inventoryDetails.addCrashSection("Previous", "Size: " + previousInventory.size());
                    inventoryDetails.addCrashSection("New", "Size: " + newInventory.size());
                    CrashReportCategory itemDetails = crashReport.makeCategory("Item Details");
                    itemDetails.addCrashSection("Previous Item", "Item: " + (previousItem != null ? previousItem.toString() : "null") + "\n"
                        + "Display Name: " + (previousItem != null ? previousItem.getDisplayName() : "null") + "\n"
                        + "Index: " + i + "\n"
                        + "Map Value: " + (previousItem != null ? (previousInventoryMap.get(previousItem.getDisplayName()) != null ? previousInventoryMap.get(previousItem.getDisplayName()).toString() : "null") : "null"));
                    itemDetails.addCrashSection("New Item", "Item: " + (newItem != null ? newItem.toString() : "null") + "\n"
                            + "Display Name: " + (newItem != null ? newItem.getDisplayName() : "null") + "\n"
                            + "Index: " + i + "\n"
                            + "Map Value: " + (newItem != null ? (previousInventoryMap.get(newItem.getDisplayName()) != null ? previousInventoryMap.get(newItem.getDisplayName()).toString() : "null") : "null"));
                    throw new ReportedException(crashReport);
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

            if (main.getConfigValues().isEnabled(Feature.DRAGON_STATS_TRACKER)) {
                DragonTracker.getInstance().checkInventoryDifferenceForDrops(inventoryDifference);
            }

            if (SlayerTracker.getInstance().isTrackerEnabled()) {
                SlayerTracker.getInstance().checkInventoryDifferenceForDrops(inventoryDifference);
            }

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
            if (item != null && SKELETON_HELMET_ID.equals(ItemUtils.getSkyblockItemID(item))) {
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
                if (item != null && TOXIC_ARROW_POISON_ID.equals(ItemUtils.getSkyblockItemID(item))) {
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
                String itemID = itemStack != null ? ItemUtils.getSkyblockItemID(itemStack) : null;

                if (itemID != null && (itemID.startsWith("REVENANT") || itemID.startsWith("TARANTULA") ||
                        itemID.startsWith("FINAL_DESTINATION") || itemID.startsWith("REAPER"))) {
                    String percent = null;
                    String defence = null;
                    List<String> lore = ItemUtils.getItemLore(itemStack);
                    for (String loreLine : lore) {
                        Matcher matcher = REVENANT_UPGRADE_PATTERN.matcher(TextUtils.stripColor(loreLine));
                        if (matcher.matches()) { // Example: line§5§o§7Next Upgrade: §a+240❈ §8(§a14,418§7/§c15,000§8)
                            try {
                                float percentage = Float.parseFloat(matcher.group(2).replace(",", "")) /
                                        Integer.parseInt(matcher.group(3).replace(",", "")) * 100;
                                BigDecimal bigDecimal = new BigDecimal(percentage).setScale(0, RoundingMode.HALF_UP);
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
     * Returns true if the player is wearing a full armor set with IDs contained in the given set
     *
     * @param player the player
     * @param armorSetIds the given set of armor IDs
     * @return {@code true} iff all player armor contained in given set, {@code false} otherwise.
     */
    public static boolean isWearingFullSet(EntityPlayer player, Set<String> armorSetIds) {
        boolean flag = true;
        ItemStack[] armorInventory = player.inventory.armorInventory;
        for (int i = 0; i < 4; i++) {
            String itemID = ItemUtils.getSkyblockItemID(armorInventory[i]);
            if (itemID == null || !armorSetIds.contains(itemID)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * @return Log of recent Inventory changes
     */
    public Collection<ItemDiff> getItemPickupLog() {
        return itemPickupLog.values();
    }

    /**
     * Detects, stores, and returns the Skyblock inventory type of the given {@code GuiChest}. The inventory type is the
     * kind of menu the player has open, like a crafting table, or an enchanting table for example. If no known inventory
     * type is detected, {@code null} will be stored.
     *
     * @return an {@link InventoryType} enum constant representing the current Skyblock inventory type
     */
    public InventoryType updateInventoryType(GuiChest guiChest) {
        // Get the open chest and test if it's the same one that we've seen before
        IInventory inventory = guiChest.lowerChestInventory;
        if (inventory.getDisplayName() == null) {
            return inventoryType = null;
        }
        String chestName = TextUtils.stripColor(inventory.getDisplayName().getUnformattedText());

        // Initialize inventory to null and get the open chest name
        inventoryType = null;

        // Find an inventory match if possible
        for (InventoryType inventoryTypeItr : InventoryType.values()) {
            Matcher m = inventoryTypeItr.getInventoryPattern().matcher(chestName);
            if (m.matches()) {
                if (m.groupCount() > 0) {
                    try {
                        inventoryPageNum = Integer.parseInt(m.group("page"));
                    } catch (Exception e) {
                        inventoryPageNum = 0;
                    }
                    try {
                        inventorySubtype = m.group("type");
                    } catch (Exception e) {
                        inventorySubtype = null;
                    }
                } else {
                    inventoryPageNum = 0;
                    inventorySubtype = null;
                }
                inventoryType = inventoryTypeItr;
                break;
            }
        }
        inventoryKey = getInventoryKey(inventoryType, inventoryPageNum);
        return inventoryType;
    }

    private String getInventoryKey(InventoryType inventoryType, int inventoryPageNum) {
        if (inventoryType == null) {
            return null;
        }
        return inventoryType.getInventoryName() + inventoryPageNum;
    }
}
