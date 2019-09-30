package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

/**
 * Utility methods related to player inventories
 */
public class InventoryUtils {

    private static final String SUMMONING_EYE_DISPLAY_NAME = "\u00A75Summoning Eye";

    /**
     * Display name of the Quiver Arrow item
     */
    private static final String QUIVER_ARROW_DISPLAY_NAME = "\u00A78Quiver Arrow";

    /**
     * Display name of the Skeleton Helmet
     */
    private static final String SKELETON_HELMET_DISPLAY_NAME = "Skeleton's Helmet";

    private List<ItemStack> previousInventory;
    private Multimap<String, ItemDiff> itemPickupLog = ArrayListMultimap.create();
    private boolean inventoryIsFull;
    private boolean wearingSkeletonHelmet;

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
                ItemStack previousItem = previousInventory.get(i);
                ItemStack newItem = newInventory.get(i);

                if(previousItem != null) {
                    int amount = previousInventoryMap.getOrDefault(previousItem.getDisplayName(), 0) + previousItem.stackSize;
                    previousInventoryMap.put(previousItem.getDisplayName(), amount);
                }

                if(newItem != null) {
                    if(newItem.getDisplayName().equals(QUIVER_ARROW_DISPLAY_NAME)) {

                        newInventory.set(i, previousItem);
                        if(previousItem != null) {
                            newItem = previousItem;
                        } else {
                            continue;
                        }
                    }
                    if (newItem.getDisplayName().contains(" "+ EnumChatFormatting.DARK_GRAY+"x")) {
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
                    main.getUtils().playSound("random.orb", 0.5);
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
        if (item != null && item.hasDisplayName() && item.getDisplayName().contains(SKELETON_HELMET_DISPLAY_NAME)) {
            wearingSkeletonHelmet = true;
            return;
        }
        wearingSkeletonHelmet = false;
    }

    /**
     * @return Whether the player is wearing a Skeleton Helmet
     */
    public boolean isWearingSkeletonHelmet() {
        return wearingSkeletonHelmet;
    }

    /**
     * @return Log of recent Inventory changes
     */
    public Collection<ItemDiff> getItemPickupLog() {
        return itemPickupLog.values();
    }

    private Item lastItem = null;
    private long lastDrop = System.currentTimeMillis();
    private int dropCount = 1;

    public boolean shouldCancelDrop(Slot slot) {
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            return shouldCancelDrop(stack);
        }
        return false;
    }

    public boolean shouldCancelDrop(ItemStack stack) {
        if (main.getUtils().cantDropItem(stack, EnumUtils.Rarity.getRarity(stack), false)) {
            Item item = stack.getItem();
            if (lastItem != null && lastItem == item && System.currentTimeMillis() - lastDrop < 3000 && dropCount >= 2) {
                lastDrop = System.currentTimeMillis();
            } else {
                if (lastItem == item) {
                    if (System.currentTimeMillis() - lastDrop > 3000) {
                        dropCount = 1;
                    } else {
                        dropCount++;
                    }
                } else {
                    dropCount = 1;
                }
                SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getColor(
                        Feature.STOP_DROPPING_SELLING_RARE_ITEMS).getChatFormatting() +
                        Message.MESSAGE_CLICK_MORE_TIMES.getMessage(String.valueOf(3-dropCount)));
                lastItem = item;
                lastDrop = System.currentTimeMillis();
                main.getUtils().playSound("note.bass", 0.5);
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
}
