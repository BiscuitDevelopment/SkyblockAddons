package codes.biscuit.skyblockaddons.features.itemdrops;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * This class handles the item checking for the Stop Dropping/Selling Rare Items feature.
 * When the player tries to drop or sell an item, {@link this#canDropItem(ItemStack, boolean)} is called to check
 * the item against the rarity requirements, the blacklist, and the whitelist.
 * These requirements determine if the item is allowed to be dropped/sold.
 *
 * @author ILikePlayingGames
 * @version 0.1
 * @see ItemDropList
 */
public class ItemDropChecker {

    private static final long DROP_CONFIRMATION_TIMEOUT = 3000L;

    private SkyblockAddons main = SkyblockAddons.getInstance();

    // Variables used for checking drop confirmations
    private ItemStack itemOfLastDropAttempt;
    private long timeOfLastDropAttempt;
    private int attemptsRequiredToConfirm;

    /**
     * Checks if this item can be dropped or sold.
     * This method is for items in the inventory, not those in the hotbar.
     *
     * @param item the item to check
     * @return {@code true} if this item can be dropped or sold, {@code false} otherwise
     */
    public boolean canDropItem(ItemStack item) {
        return canDropItem(item, false);
    }

    /**
     * Checks if the item in this slot can be dropped or sold.
     *
     * @param slot the inventory slot to check
     * @return {@code true} if this item can be dropped or sold, {@code false} otherwise
     */
    public boolean canDropItem(Slot slot) {
        if (slot != null && slot.getHasStack()) {
            return canDropItem(slot.getStack());
        }
        else {
            return true;
        }
    }

    public boolean canDropItem(ItemStack item, boolean itemIsInHotbar) {
        return canDropItem(item, itemIsInHotbar, true);
    }

    /**
     * Checks if this item can be dropped or sold.
     *
     * @param item the item to check
     * @param itemIsInHotbar whether this item is in the player's hotbar
     * @return {@code true} if this item can be dropped or sold, {@code false} otherwise
     */
    public boolean canDropItem(ItemStack item, boolean itemIsInHotbar, boolean playAlert) {
        if (main.getUtils().isOnSkyblock()) {
            if (ItemUtils.getSkyBlockItemID(item) == null) {
                // Allow dropping of Skyblock items without IDs
                return true;
            } else if (ItemUtils.getRarity(item) == null) {
            /*
             If this Skyblock item has an ID but no rarity, allow dropping it.
             This really shouldn't happen but just in case it does, this condition is here.
             */
                return true;
            }

            String itemID = ItemUtils.getSkyBlockItemID(item);
            ItemRarity itemRarity = ItemUtils.getRarity(item);
            List<String> blacklist = main.getOnlineData().getDropSettings().getDontDropTheseItems();
            List<String> whitelist = main.getOnlineData().getDropSettings().getAllowDroppingTheseItems();

            if (itemIsInHotbar) {
                if (itemRarity.compareTo(main.getOnlineData().getDropSettings().getMinimumHotbarRarity()) < 0 && !blacklist.contains(itemID)) {
                    return true;
                } else {
                    // Dropping rare non-whitelisted items from the hotbar is not allowed.
                    if (whitelist.contains(itemID)) {
                        return true;
                    } else {
                        if (playAlert) {
                            playAlert();
                        }
                        return false;
                    }
                }
            } else {
                if (itemRarity.compareTo(main.getOnlineData().getDropSettings().getMinimumInventoryRarity()) < 0 && !blacklist.contains(itemID)) {
                    return true;
                } else {
                    /*
                     If the item is above the minimum rarity and not whitelisted, require the player to attempt
                     to drop it three times to confirm they want to drop it.
                    */
                    if (whitelist.contains(itemID)) {
                        return true;
                    } else {
                        return dropConfirmed(item, 3);
                    }
                }
            }
        } else if (main.getConfigValues().isEnabled(Feature.DROP_CONFIRMATION) && main.getConfigValues().isEnabled(Feature.DOUBLE_DROP_IN_OTHER_GAMES)) {
            return dropConfirmed(item, 2);

        } else {
            return true;
        }
    }

    /**
     * Checks if the player has confirmed that they want to drop the given item stack.
     * The player confirms that they want to drop the item when they try to drop it the number of
     * times specified in {@code numberOfActions}
     *
     * @param item the item stack the player is attempting to drop
     * @param numberOfActions the number of times the player has to drop the item to confirm
     * @return {@code true} if the player has dropped the item enough
     */
    public boolean dropConfirmed(ItemStack item, int numberOfActions) {
        if (item == null) {
            throw new NullPointerException("Item cannot be null!");

        } else if (numberOfActions < 2) {
            throw new IllegalArgumentException("At least two attempts are required.");
        }

        // If there's no drop confirmation active, set up a new one.
        if (itemOfLastDropAttempt == null) {
            itemOfLastDropAttempt = item;
            timeOfLastDropAttempt = Minecraft.getSystemTime();
            attemptsRequiredToConfirm = numberOfActions - 1;
            onDropConfirmationFail();
            return false;
        }
        else {
            // Reset the current drop confirmation on time out or if the item being dropped changes.
            if (Minecraft.getSystemTime() - timeOfLastDropAttempt > DROP_CONFIRMATION_TIMEOUT || !ItemStack.areItemStacksEqual(item, itemOfLastDropAttempt)) {
                resetDropConfirmation();
                return dropConfirmed(item, numberOfActions);

            } else {
                if (attemptsRequiredToConfirm >= 1) {
                    onDropConfirmationFail();
                    return false;

                } else {
                    resetDropConfirmation();
                    return true;
                }
            }
        }
    }

    /**
     * Called whenever a drop confirmation fails due to the player not attempting to drop the item enough times.
     * A message is sent and a sound is played notifying the player how many more times they need to drop the item.
     */
    public void onDropConfirmationFail() {
        ColorCode colorCode = main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION);

        if (attemptsRequiredToConfirm >= 2) {
            String multipleAttemptsRequiredMessage = Message.MESSAGE_CLICK_MORE_TIMES.getMessage(Integer.toString(attemptsRequiredToConfirm));
            main.getUtils().sendMessage(colorCode + multipleAttemptsRequiredMessage);

        } else {
            String oneMoreAttemptRequiredMessage = Message.MESSAGE_CLICK_ONE_MORE_TIME.getMessage();
            main.getUtils().sendMessage(colorCode + oneMoreAttemptRequiredMessage);
        }
        playAlert();
        attemptsRequiredToConfirm--;
    }

    /**
     * Plays an alert sound when a drop attempt is denied.
     */
    public void playAlert() {
        main.getUtils().playLoudSound("note.bass", 0.5);
    }

    /**
     * Reset the drop confirmation settings.
     */
    public void resetDropConfirmation() {
        itemOfLastDropAttempt = null;
        timeOfLastDropAttempt = 0L;
        attemptsRequiredToConfirm = 0;
    }
}
