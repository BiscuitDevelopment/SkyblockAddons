package codes.biscuit.skyblockaddons.features.cooldowns;

import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Setter;
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

    @Setter private static Map<String, Integer> itemCooldowns = new HashMap<>();
    private static final Pattern ITEM_COOLDOWN_PATTERN = Pattern.compile("Cooldown: ([0-9]+)s");
    private static final Pattern ALTERNATE_COOLDOWN_PATTERN = Pattern.compile("([0-9]+) Second Cooldown");

    private static final Map<String, CooldownEntry> cooldowns = new HashMap<>();

    private static CooldownEntry get(ItemStack item) {
        return get(ItemUtils.getSkyblockItemID(item));
    }

    private static CooldownEntry get(String itemId) {
        return cooldowns.getOrDefault(itemId, CooldownEntry.NULL_ENTRY);
    }

    public static int getItemCooldown(ItemStack item) {
        return itemCooldowns.getOrDefault(ItemUtils.getSkyblockItemID(item), 0);
    }

    public static int getItemCooldown(String itemId) {
        return itemCooldowns.getOrDefault(itemId, 0);
    }

    /**
     * Put an item on cooldown by reading the cooldown value from the json.
     *
     * @param item ItemStack to put on cooldown
     */
    public static void put(ItemStack item) {
        String itemId = ItemUtils.getSkyblockItemID(item);
        if (itemId == null) {
            return;
        }
        int cooldown = itemCooldowns.getOrDefault(itemId, 0);
        if (cooldown > 0) {
            put(itemId, cooldown);
        }
    }

    /**
     * Put an item on cooldown by reading the cooldown value from the json.
     *
     * @param itemId ItemStack to put on cooldown
     */
    public static void put(String itemId) {
        if (itemId == null) {
            return;
        }
        int cooldown = itemCooldowns.getOrDefault(itemId, 0);
        if (cooldown > 0) {
            put(itemId, cooldown);
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
        String itemId = ItemUtils.getSkyblockItemID(item);
        if (itemId != null && cooldown > 0) {
            put(itemId, cooldown);
        }
    }

    /**
     * Put an item on cooldown by item ID and provided cooldown.
     *
     * @param itemId Skyblock ID of the item to put on cooldown
     * @param cooldown Cooldown in milliseconds
     */
    public static void put(String itemId, long cooldown) {
        if(cooldown < 0) {
            throw new IllegalArgumentException("Cooldown must be positive and not 0");
        }

        if (!cooldowns.containsKey(itemId) || !cooldowns.get(itemId).isOnCooldown()) { // Don't allow overriding a current cooldown.
            CooldownEntry cooldownEntry = new CooldownEntry(cooldown);
            cooldowns.put(itemId, cooldownEntry);
        }
    }

    /**
     * Check if an item is on cooldown.
     *
     * @param item Item to check
     * @return Whether that item is on cooldown. {@code true} if it is, {@code false} if it's not, it's not registered,
     * is null or doesn't have a skyblock ID
     */
    public static boolean isOnCooldown(ItemStack item) {
        return get(item).isOnCooldown();
    }

    /**
     * Check if an item is on cooldown by item ID.
     *
     * @param itemId skyblock ID of the item to check
     * @return Whether that item is on cooldown. {@code true} if it is, {@code false} if it's not or it's not registered
     */
    public static boolean isOnCooldown(String itemId) {
        return get(itemId).isOnCooldown();
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
     * Get the remaining cooldown of an item in milliseconds by its item ID
     *
     * @param itemId Skyblock ID of the item to get the cooldown of
     * @return Remaining time until its cooldown runs out or {@code 0} if it's not on cooldown
     */
    public static long getRemainingCooldown(String itemId) {
        return get(itemId).getRemainingCooldown();
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
     * Get the remaining cooldown of an item in percent between {@code 0 to 1} by its ID
     *
     * @param itemId Skyblock ID of the item to get the cooldown of
     * @return Remaining cooldown in percent or {@code 0} if the item is not on cooldown
     */
    public static double getRemainingCooldownPercent(String itemId) {
        return get(itemId).getRemainingCooldownPercent();
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
