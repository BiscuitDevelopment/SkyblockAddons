package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

/**
 * Utility methods related to player inventories
 */
public class InventoryUtils {

    /**
     * Display name of the Quiver Arrow item
     */
    private static final String QUIVER_ARROW_DISPLAY_NAME = "\u00A78Quiver Arrow";

    /**
     * Display name of the Skeleton Helmet
     */
    private static final String SKELETON_HELMET_DISPLAY_NAME = "Skeleton's Helmet";

    private List<ItemStack> previousInventory;
    private Map<String, ItemDiff> itemPickupLog = new HashMap<>();
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

        if(previousInventory != null) {

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
                if (itemPickupLog.containsKey(diff.getDisplayName())) {
                    itemPickupLog.get(diff.getDisplayName()).add(diff.getAmount());
                } else {
                    itemPickupLog.put(diff.getDisplayName(), diff);
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
        List<String> logItemsToRemove = new LinkedList<>();
        itemPickupLog.forEach((displayName, itemDiff) -> {
            if (itemDiff.getLifetime() > ItemDiff.LIFESPAN) {
                logItemsToRemove.add(displayName);
            }
        });
        logItemsToRemove.forEach(name -> itemPickupLog.remove(name));
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
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            main.getRenderListener().setTitleFeature(null);
                        }
                    }, main.getConfigValues().getWarningSeconds() * 1000);
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
}
