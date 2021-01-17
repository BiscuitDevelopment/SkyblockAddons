package codes.biscuit.skyblockaddons.features.cooldowns;

import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager class for items on cooldown. <br/>
 * Register a new item with {@link #put(ItemStack) put} and check cooldowns with
 * {@link #isOnCooldown(ItemStack) isOnCooldown}, {@link #getRemainingCooldown(ItemStack) getRemainingCooldown} and
 * {@link #getRemainingCooldownPercent(ItemStack) getRemainingCooldownPercent}
 */
public class CooldownManager {

    private static final Pattern ITEM_COOLDOWN_PATTERN = Pattern.compile("Cooldown: ([0-9]+)s");
    private static final Pattern ALTERNATE_COOLDOWN_PATTERN = Pattern.compile("([0-9]+) Second Cooldown");

    private static final Map<String, CooldownEntry> cooldowns = new HashMap<>();

    private static CooldownEntry get(ItemStack item) {
        return (item == null || !item.hasDisplayName()) ? CooldownEntry.NULL_ENTRY : get(item.getDisplayName());
    }

    private static CooldownEntry get(String itemName) {
        return cooldowns.getOrDefault(itemName, CooldownEntry.NULL_ENTRY);
    }

    /**
     * Put an item on cooldown by reading the cooldown value from its lore.
     *
     * @param item ItemStack to put on cooldown
     */
    public static void put(ItemStack item) {
        if(item == null || !item.hasDisplayName()) {
            return;
        }

        int cooldown = getLoreCooldown(item);
        if(cooldown > 0) {
            // cooldown is returned in seconds and required in milliseconds
            put(item.getDisplayName(), cooldown * 1000);
        }
    }


    /**
     * Put an item on cooldown with provided cooldown, for items that do not show their cooldown
     * in their lore.
     *
     * @param item Item to put on cooldown
     * @param cooldown Cooldown in milliseconds
     */
    public static void put(ItemStack item, long cooldown) {
        if(item == null || !item.hasDisplayName() || cooldown < 0) {
            return;
        }

        put(item.getDisplayName(), cooldown);
    }

    /**
     * Put an item on cooldown by item name and provided cooldown.
     *
     * @param itemName Displayname of the item to put on cooldown
     * @param cooldown Cooldown in milliseconds
     */
    public static void put(String itemName, long cooldown) {
        if(cooldown < 0) {
            throw new IllegalArgumentException("Cooldown must be positive and not 0");
        }

        if (!cooldowns.containsKey(itemName) || !cooldowns.get(itemName).isOnCooldown()) { // Don't allow overriding a current cooldown.
            CooldownEntry cooldownEntry = new CooldownEntry(cooldown);
            cooldowns.put(itemName, cooldownEntry);
        }
    }

    /**
     * Check if an item is on cooldown.
     *
     * @param item Item to check
     * @return Whether that item is on cooldown. {@code true} if it is, {@code false} if it's not, it's not registered,
     * is null or doesn't have a displayname
     */
    public static boolean isOnCooldown(ItemStack item) {
        return get(item).isOnCooldown();
    }

    /**
     * Check if an item is on cooldown by item name.
     *
     * @param itemName Displayname of the item to check
     * @return Whether that item is on cooldown. {@code true} if it is, {@code false} if it's not or it's not registered
     */
    public static boolean isOnCooldown(String itemName) {
        return get(itemName).isOnCooldown();
    }

    /**
     * Get the remaining cooldown of an item in milliseconds
     *
     * @param item Item to get the cooldown of
     * @return Remaining time until its cooldown runs out or {@code 0} if it's not on cooldown
     */
    public static long getRemainingCooldown(ItemStack item) {
        return get(item).getRemainingCooldown();
    }

    /**
     * Get the remaining cooldown of an item in milliseconds by its name
     *
     * @param itemName Displayname of the item to get the cooldown of
     * @return Remaining time until its cooldown runs out or {@code 0} if it's not on cooldown
     */
    public static long getRemainingCooldown(String itemName) {
        return get(itemName).getRemainingCooldown();
    }

    /**
     * Get the remaining cooldown of an item in percent between {@code 0 to 1}
     *
     * @param item Item to get the cooldown of
     * @return Remaining cooldown in percent or {@code 0} if the item is not on cooldown
     */
    public static double getRemainingCooldownPercent(ItemStack item) {
        return get(item).getRemainingCooldownPercent();
    }

    /**
     * Get the remaining cooldown of an item in percent between {@code 0 to 1} by its name
     *
     * @param itemName Displayname of the item to get the cooldown of
     * @return Remaining cooldown in percent or {@code 0} if the item is not on cooldown
     */
    public static double getRemainingCooldownPercent(String itemName) {
        return get(itemName).getRemainingCooldownPercent();
    }

    /**
     * Read the cooldown value of an item from it's lore.
     * This requires that the lore shows the cooldown either like {@code X Second Cooldown} or
     * {@code Cooldown: Xs}. Cooldown is returned in seconds.
     *
     * @param itemStack Item to read cooldown from
     * @return Read cooldown in seconds or {@code -1} if no cooldown was found
     * @see #ITEM_COOLDOWN_PATTERN
     * @see #ALTERNATE_COOLDOWN_PATTERN
     */
    private static int getLoreCooldown(ItemStack itemStack) {
        for (String loreLine : ItemUtils.getItemLore(itemStack)) {
            String strippedLoreLine = TextUtils.stripColor(loreLine);

            Matcher matcher = ITEM_COOLDOWN_PATTERN.matcher(strippedLoreLine);
            if (matcher.matches()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ignored) {}

            } else {
                matcher = ALTERNATE_COOLDOWN_PATTERN.matcher(strippedLoreLine);
                if (matcher.matches()) {
                    try {
                        return Integer.parseInt(matcher.group(1));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return -1;
    }

}
