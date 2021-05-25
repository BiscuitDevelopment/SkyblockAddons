package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Manages the Fetchur Feature, Pointing out which item Fetchur wants next
 *
 * @author Pedro9558
 */
public class FetchurManager {

    private static final long MILISECONDS_IN_A_DAY = 24 * 60 * 60 * 1000;
    // Hypixel timezone
    // Currently using new york timezone, gotta check november 7th to see if this still works
    @Getter
    private static final TimeZone fetchurZone = TimeZone.getTimeZone("America/New_York");
    private static final Calendar fetchurCalendar = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));

    @Getter
    private final String fetchurTaskCompletedPhrase = "thanks thats probably what i needed";

    @Getter
    private final String fetchurAlreadyDidTaskPhrase = "come back another time, maybe tmrw";
    // A list containing the items fetchur wants
    // If you want to put it in a repository, YOU MUST PUT IT IN THE EXACT SAME ORDER AS I PLACED ON THIS LIST
    // Changing the order will affect the algorithm
    private static final FetchurItem[] items = new FetchurItem[]{new FetchurItem(new ItemStack(Blocks.wool, 50, 14), "Red Wool"),
            new FetchurItem(new ItemStack(Blocks.stained_glass, 20, 4), "Yellow Stained Glass"),
            new FetchurItem(new ItemStack(Items.compass, 1), "Compass"),
            new FetchurItem(new ItemStack(Items.prismarine_crystals, 20), "Mithril"),
            new FetchurItem(new ItemStack(Items.fireworks, 1), "Firework Rocket"),
            new FetchurItem(ItemUtils.createSkullItemStack("§fCheap Coffee", null, "2fd02c32-6d35-3a1a-958b-e8c5a657c7d4", "194221a0de936bac5ce895f2acad19c64795c18ce5555b971594205bd3ec"), "Cheap Coffee"),
            new FetchurItem(new ItemStack(Items.oak_door, 1), "Wooden Door"),
            new FetchurItem(new ItemStack(Items.rabbit_foot, 3), "Rabbit's Feet"),
            new FetchurItem(new ItemStack(Blocks.tnt, 1), "Superboom TNT"),
            new FetchurItem(new ItemStack(Blocks.pumpkin, 1), "Pumpkin"),
            new FetchurItem(new ItemStack(Items.flint_and_steel, 1), "Flint and Steel"),
            new FetchurItem(new ItemStack(Blocks.quartz_ore, 50), "Nether Quartz Ore"),
            new FetchurItem(new ItemStack(Items.ender_pearl, 16), "Ender Pearl")};

    @Getter
    private static final FetchurManager instance = new FetchurManager();

    // Used for storage, essential for Fetchur Warner
    @Getter
    @Setter
    private FetchurItem currentItemSaved = null;

    /**
     * Get the item fetchur needs today
     *
     * @return the item
     */
    public FetchurItem getCurrentFetchurItem() {
        // Get the zero-based day of the month
        int dayIdx = getFetchurDayOfMonth(System.currentTimeMillis()) - 1;
        return items[dayIdx % items.length];
    }

    /**
     * Figure out whether the player submitted today's fetchur item.
     * Can return incorrect answer if the player handed in Fetchur today, but sba wasn't loaded at the time.
     * Clicking Fetchur again (and reading the NPC response) will update the value to be correct.
     *
     * @return {@code true} iff the player hasn't yet submitted the item in today (EST).
     */
    public boolean hasFetchedToday() {
        long lastTimeFetched = SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getLastTimeFetchur();
        long currTime = System.currentTimeMillis();
        // Return true iff the days of the month from last submission and current time match
        return currTime - lastTimeFetched < MILISECONDS_IN_A_DAY && getFetchurDayOfMonth(lastTimeFetched) == getFetchurDayOfMonth(currTime);
    }

    /**
     * Returns the day of the month in the fetchur calendar (EST time zone)
     *
     * @param currTimeMilis Epoch UTC miliseconds (e.g. from {@link System#currentTimeMillis()})
     * @return the 1-indexed day of the month in the fetchur time zone
     */
    private int getFetchurDayOfMonth(long currTimeMilis) {
        fetchurCalendar.setTimeInMillis(currTimeMilis);
        return fetchurCalendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Called periodically to check for any changes in the Fetchur item.
     * Will also notify the player of a change if enabled.
     */
    public void recalculateFetchurItem() {
        FetchurItem item = getCurrentFetchurItem();
        if (!item.equals(currentItemSaved)) {
            currentItemSaved = item;
            SkyblockAddons main = SkyblockAddons.getInstance();
            // Warn player when there's a change
            if (main.getConfigValues().isEnabled(Feature.WARN_WHEN_FETCHUR_CHANGES)) {
                main.getUtils().playLoudSound("random.orb", 0.5);
                main.getRenderListener().setTitleFeature(Feature.WARN_WHEN_FETCHUR_CHANGES);
                main.getScheduler().schedule(Scheduler.CommandType.RESET_TITLE_FEATURE, main.getConfigValues().getWarningSeconds());
            }
        }
    }

    /**
     * Triggered if the player has just given the correct item, or has already given the correct item, to Fetchur.
     */
    public void saveLastTimeFetched() {
        SkyblockAddons main = SkyblockAddons.getInstance();
        main.getPersistentValuesManager().getPersistentValues().setLastTimeFetchur(System.currentTimeMillis());
        main.getPersistentValuesManager().saveValues();
    }

    /**
     * Called after persistent loading to seed the saved item (so the warning doesn't trigger when joining skyblock)
     */
    public void postPersistentConfigLoad() {
        if (hasFetchedToday()) {
            currentItemSaved = getCurrentFetchurItem();
        }
    }

    /**
     * A class representing the item fetchur wants
     * containing the item instance and the text format of the item
     */
    public static class FetchurItem {
        @Getter
        private final ItemStack itemStack;
        @Getter
        private final String itemText;

        public FetchurItem(ItemStack itemStack, String itemText) {
            this.itemStack = itemStack;
            this.itemText = itemText;
        }
        @Override
        public boolean equals(Object anotherObject) {
            if (!(anotherObject instanceof FetchurItem))
                return false;
            FetchurItem another = (FetchurItem) anotherObject;
            return another.getItemText().equals(this.getItemText()) && another.getItemStack().equals(this.getItemStack());
        }
    }

}
