package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.utils.item.ItemUtils;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

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
    private final SkyblockAddons MAIN;
    private final Logger LOGGER;

    /** The list that is checked to determine if an item is allowed to be dropped */
    private ItemDropList itemDropList;

    // Variables used for checking drop confirmations
    private ItemStack itemOfLastDropAttempt;
    private long timeOfLastDropAttempt;
    private int attemptsRequiredToConfirm;

    /**
     * Creates a new instance of the item checker for the Stop Dropping/Selling Rare Items feature.
     *
     * @param main the SkyblockAddons instance
     */
    public ItemDropChecker(SkyblockAddons main) {
        Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        this.MAIN = main;
        this.LOGGER = LogManager.getLogger("SBA Item Drop Checker");

        // Try to get the lists from the file.
        try {
            String ITEM_DROP_LIST_FILE_PATH = "Stop dropping or selling rare items/itemDropList.json";
            JsonReader jsonFileReader = new JsonReader(Files.newBufferedReader(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource(ITEM_DROP_LIST_FILE_PATH)).toURI())));
            itemDropList = GSON.fromJson(jsonFileReader, ItemDropList.class);
        } catch (FileNotFoundException | URISyntaxException e) {
            LOGGER.error("The item drop list doesn't exist or the path is incorrect. The developer did something wrong.");
            LOGGER.catching(e);
        } catch (IOException e) {
            LOGGER.error("Error reading from item drop list!");
            LOGGER.catching(e);
        }
    }

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
     * Checks if this item can be dropped or sold.
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

    /**
     * Checks if this item can be dropped or sold.
     *
     * @param item the item to check
     * @param itemIsInHotbar whether this item is in the player's hotbar
     * @return {@code true} if this item can be dropped or sold, {@code false} otherwise
     */
    public boolean canDropItem(ItemStack item, boolean itemIsInHotbar) {
        LOGGER.entry(item, itemIsInHotbar);

        if (item == null) {
            LOGGER.throwing(new NullPointerException("Item cannot be null!"));
        }

        if (MAIN.getUtils().isOnSkyblock()) {
            if (ItemUtils.getSkyBlockItemID(item) == null) {
                // Allow dropping of Skyblock items without IDs
                return LOGGER.exit(true);
            }
            else if (ItemUtils.getRarity(item) == null) {
            /*
             If this Skyblock item has an ID but no rarity, allow dropping it.
             This really shouldn't happen but just in case it does, this condition is here.
             */
                return LOGGER.exit(true);
            }

            String itemID = ItemUtils.getSkyBlockItemID(item);
            Rarity rarity = ItemUtils.getRarity(item);
            List<String> blacklist = itemDropList.getBlacklist();
            List<String> whitelist = itemDropList.getWhitelist();

            if (itemIsInHotbar) {
                if (rarity.compareTo(itemDropList.getMinimumHotbarRarity()) < 0 && !blacklist.contains(itemID)) {
                    return LOGGER.exit(true);
                }
                else {
                    // Dropping rare non-whitelisted items from the hotbar is not allowed.
                    if (whitelist.contains(itemID)) {
                        return LOGGER.exit(true);
                    }
                    else {
                        playAlert();
                        return LOGGER.exit(false);
                    }
                }
            }
            else {
                if (rarity.compareTo(itemDropList.getMinimumInventoryRarity()) < 0 && !blacklist.contains(itemID)) {
                    return LOGGER.exit(true);
                }
                else {
                    /*
                     If the item is above the minimum rarity and not whitelisted, require the player to attempt
                     to drop it three times to confirm they want to drop it.
                    */
                    if (whitelist.contains(itemID)) {
                        return LOGGER.exit(true);
                    }
                    else {
                        return LOGGER.exit(dropConfirmed(item, 3));
                    }
                }
            }
        }
        else if (MAIN.getConfigValues().isEnabled(Feature.DROP_CONFIRMATION) &&
                MAIN.getConfigValues().isEnabled(Feature.DOUBLE_DROP_IN_OTHER_GAMES)) {
            return dropConfirmed(item, 2);
        }
        else {
            return LOGGER.exit(true);
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
        LOGGER.entry(item, numberOfActions);

        if (item == null) {
            LOGGER.throwing(new NullPointerException("Item cannot be null!"));
        }
        else if (numberOfActions < 2) {
            LOGGER.throwing(new IllegalArgumentException("At least two attempts are required."));
        }

        // If there's no drop confirmation active, set up a new one.
        if (itemOfLastDropAttempt == null) {
            itemOfLastDropAttempt = item;
            timeOfLastDropAttempt = Minecraft.getSystemTime();
            attemptsRequiredToConfirm = numberOfActions - 1;
            onDropConfirmationFail();
            return LOGGER.exit(false);
        }
        else {
            long DROP_CONFIRMATION_TIMEOUT = 3000L;

            // Reset the current drop confirmation on time out or if the item being dropped changes.
            if (Minecraft.getSystemTime() - timeOfLastDropAttempt > DROP_CONFIRMATION_TIMEOUT ||
                    !ItemStack.areItemStacksEqual(item, itemOfLastDropAttempt)) {
                resetDropConfirmation();
                return dropConfirmed(item, numberOfActions);
            }
            else {
                if (attemptsRequiredToConfirm >= 1) {
                    onDropConfirmationFail();
                    return LOGGER.exit(false);
                }
                else {
                    resetDropConfirmation();
                    return LOGGER.exit(true);
                }
            }
        }
    }

    /**
     * Called whenever a drop confirmation fails due to the player not attempting to drop the item enough times.
     * A message is sent and a sound is played notifying the player how many more times they need to drop the item.
     */
    public void onDropConfirmationFail() {
        LOGGER.entry();

        Utils utils = MAIN.getUtils();
        ChatFormatting colourCode = MAIN.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION);

        if (attemptsRequiredToConfirm >= 2) {
            String multipleAttemptsRequiredMessage = Message.MESSAGE_CLICK_MORE_TIMES.getMessage(Integer.toString(attemptsRequiredToConfirm));

            utils.sendMessage(colourCode + multipleAttemptsRequiredMessage);
        }
        else {
            String oneMoreAttemptRequiredMessage = Message.MESSAGE_CLICK_ONE_MORE_TIME.getMessage();

            utils.sendMessage(colourCode + oneMoreAttemptRequiredMessage);
        }
        playAlert();
        attemptsRequiredToConfirm--;
        LOGGER.exit();
    }

    /**
     * Plays an alert sound when a drop attempt is denied.
     */
    public void playAlert() {
        LOGGER.entry();
        MAIN.getUtils().playLoudSound("note.bass", 0.5);
        LOGGER.exit();
    }

    /**
     * Reset the drop confirmation settings.
     */
    public void resetDropConfirmation() {
        LOGGER.entry();
        itemOfLastDropAttempt = null;
        timeOfLastDropAttempt = 0L;
        attemptsRequiredToConfirm = 0;
        LOGGER.exit();
    }
}
