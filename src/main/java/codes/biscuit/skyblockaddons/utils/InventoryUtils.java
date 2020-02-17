package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.item.ItemUtils;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods related to player inventories
 */
public class InventoryUtils {

    /** Slot index the SkyBlock menu is at. */
    private static final int SKYBLOCK_MENU_SLOT = 8;

    /** Display name of the Skeleton Helmet. */
    private static final String SKELETON_HELMET_ID = "SKELETON_HELMET";

    public static final String MADDOX_BATPHONE_DISPLAYNAME = "\u00A7aMaddox Batphone";
    public static final String JUNGLE_AXE_DISPLAYNAME = "\u00A7aJungle Axe";
    public static final String FAIRY_SOUL_EXCHANGE_DISPLAYNAME = "\u00a7aExchange Fairy Souls";

    private static final Pattern REVENANT_UPGRADE_PATTERN = Pattern.compile("§5§o§7Next Upgrade: §a\\+([0-9]+❈) §8\\(§a([0-9,]+)§7/§c([0-9,]+)§8\\)");

    private List<ItemStack> previousInventory;
    private Multimap<String, ItemDiff> itemPickupLog = ArrayListMultimap.create();
    private boolean inventoryIsFull;

    /** Whether the player is wearing a Skeleton Helmet. */
    @Getter private boolean wearingSkeletonHelmet;

    @Getter private SlayerArmorProgress[] slayerArmorProgresses = new SlayerArmorProgress[4];

    /**
     * These three are used for {@link InventoryUtils#shouldCancelDrop(ItemStack)}.
     */
    private String lastItemName = null;
    private long lastDrop = System.currentTimeMillis();
    private int dropCount = 1;

    private SkyblockAddons main;

    public InventoryUtils(SkyblockAddons main) {
        this.main = main;
    }

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
        Map<String, Integer> previousInventoryMap = new HashMap<>();
        Map<String, Integer> newInventoryMap = new HashMap<>();

        if (previousInventory != null) {

            for(int i = 0; i < newInventory.size(); i++) {
                if (i == SKYBLOCK_MENU_SLOT) { // Skip the SkyBlock Menu slot all together (which includes the Quiver Arrow now)
                    continue;
                }

                ItemStack previousItem = previousInventory.get(i);
                ItemStack newItem = newInventory.get(i);

                if(previousItem != null) {
                    int amount = previousInventoryMap.getOrDefault(previousItem.getDisplayName(), 0) + previousItem.stackSize;
                    previousInventoryMap.put(previousItem.getDisplayName(), amount);
                }

                if(newItem != null) {
                    if (newItem.getDisplayName().contains(" "+ ChatFormatting.DARK_GRAY+"x")) {
                        String newName = newItem.getDisplayName().substring(0, newItem.getDisplayName().lastIndexOf(" "));
                        newItem.setStackDisplayName(newName); // This is a workaround for merchants, it adds x64 or whatever to the end of the name.
                    }
                    int amount = newInventoryMap.getOrDefault(newItem.getDisplayName(), 0) + newItem.stackSize;
                    newInventoryMap.put(newItem.getDisplayName(), amount);
                }
            }

            List<ItemDiff> inventoryDifference = new LinkedList<>();
            Set<String> keySet = new HashSet<>(previousInventoryMap.keySet());
            keySet.addAll(newInventoryMap.keySet());

            keySet.forEach(key -> {
                int previousAmount = previousInventoryMap.getOrDefault(key, 0);
                int newAmount = newInventoryMap.getOrDefault(key, 0);
                int diff = newAmount - previousAmount;
                if (diff != 0) {
                    inventoryDifference.add(new ItemDiff(key, diff));
                }
            });

            // Add changes to already logged changes of the same item, so it will increase/decrease the amount
            // instead of displaying the same item twice
            for (ItemDiff diff : inventoryDifference) {
                Collection<ItemDiff> itemDiffs = itemPickupLog.get(diff.getDisplayName());
                if (itemDiffs.size() <= 0) {
                    itemPickupLog.put(diff.getDisplayName(), diff);
                } else {
                    boolean added = false;
                    for (ItemDiff loopDiff : itemDiffs) {
                        if ((diff.getAmount() < 0 && loopDiff.getAmount() < 0) ||
                                (diff.getAmount() > 0 && loopDiff.getAmount() > 0)) {
                            loopDiff.add(diff.getAmount());
                            added = true;
                        }
                    }
                    if (!added) itemPickupLog.put(diff.getDisplayName(), diff);
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
            for (ItemStack item : p.inventory.mainInventory) {
                if (item == null) {
                    inventoryIsFull = false;
                    return;
                }
            }
            if (!inventoryIsFull) {
                inventoryIsFull = true;
                if (mc.currentScreen == null && main.getPlayerListener().didntRecentlyJoinWorld()) {
                    main.getUtils().playLoudSound("random.orb", 0.5);
                    main.getRenderListener().setTitleFeature(Feature.FULL_INVENTORY_WARNING);
                    main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds());
                }
            }
        }
    }

    /**
     * Checks if the player is wearing a Skeleton Helmet and updates {@link #wearingSkeletonHelmet} accordingly
     *
     * @param p Player to check
     */
    public void checkIfWearingSkeletonHelmet(EntityPlayerSP p) {
        ItemStack item = p.getEquipmentInSlot(4);
        if (item != null && SKELETON_HELMET_ID.equals(ItemUtils.getSkyBlockItemID(item))) {
            wearingSkeletonHelmet = true;
            return;
        }
        wearingSkeletonHelmet = false;
    }

    public boolean shouldCancelDrop(Slot slot) {
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            return shouldCancelDrop(stack);
        }
        return false;
    }

    public boolean shouldCancelDrop(ItemStack stack) {
        if (main.getUtils().cantDropItem(stack, ItemUtils.getRarity(stack), false)) {
            String heldItemName = stack.hasDisplayName() ? stack.getDisplayName() : stack.getUnlocalizedName();

            if (lastItemName != null && lastItemName.equals(heldItemName) && System.currentTimeMillis() - lastDrop < 3000 && dropCount >= 2) {
                lastDrop = System.currentTimeMillis();
            } else {
                if (heldItemName.equals(lastItemName)) {
                    if (System.currentTimeMillis() - lastDrop > 3000) {
                        dropCount = 1;
                    } else {
                        dropCount++;
                    }
                } else {
                    dropCount = 1;
                }

                // Use a different message if just one more click is needed
                if (dropCount == 2) {
                    SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) +
                            Message.MESSAGE_CLICK_ONE_MORE_TIME.getMessage(String.valueOf(3-dropCount)));
                }
                else {
                    SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) +
                            Message.MESSAGE_CLICK_MORE_TIMES.getMessage(String.valueOf(3-dropCount)));
                }

                lastItemName = heldItemName;
                lastDrop = System.currentTimeMillis();
                main.getUtils().playLoudSound("note.bass", 0.5);
                return true;
            }
        }
        return false;
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

    public void checkIfWearingRevenantArmor(EntityPlayerSP p) {
        if (main.getConfigValues().isEnabled(Feature.SLAYER_INDICATOR)) {
            ChatFormatting color = main.getConfigValues().getRestrictedColor(Feature.SLAYER_INDICATOR);
            for (int i = 3; i > -1; i--) {
                ItemStack item = p.inventory.armorInventory[i];
                String itemID = ItemUtils.getSkyBlockItemID(item);
                if (itemID != null && (itemID.startsWith("REVENANT") || itemID.startsWith("TARANTULA"))) {
                    String progress = null;
                    List<String> tooltip = item.getTooltip(null, false);
                    for (String line : tooltip) {
                        Matcher matcher = REVENANT_UPGRADE_PATTERN.matcher(line);
                        if (matcher.matches()) { // Example: line§5§o§7Next Upgrade: §a+240❈ §8(§a14,418§7/§c15,000§8)
                            try {
//                            progress = color.toString() + matcher.group(2)+"/"+matcher.group(3) + " (" + ConfigColor.GREEN+ matcher.group(1) + color + ")";
                                float percentage = Float.parseFloat(matcher.group(2).replace(",", "")) / Integer.parseInt(matcher.group(3).replace(",", "")) * 100;
                                BigDecimal bigDecimal = new BigDecimal(percentage).setScale(0, BigDecimal.ROUND_HALF_UP);
                                progress = color.toString() + bigDecimal.toString() + "% (" + ChatFormatting.GREEN + matcher.group(1) + color + ")";
                                break;
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                    if (progress != null) {
                        if (slayerArmorProgresses[i] == null) {
                            slayerArmorProgresses[i] = new SlayerArmorProgress(item, progress);
                        }
                        slayerArmorProgresses[i].setProgressText(progress);
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
}
