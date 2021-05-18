package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Manages the Fetchur Feature, Pointing out which item Fetchur wants next
 * @author Pedro9558
 */
public class FetchurManager {

    @Getter private static final FetchurManager instance = new FetchurManager();
    // Hypixel timezone
    // Currently using new york timezone, gotta check november 7th to see if this still works
    @Getter private final TimeZone fetchurZone = TimeZone.getTimeZone("America/New_York");

    @Getter private final String fetchurTaskCompletedPhrase = "thanks thats probably what i needed";

    @Getter private final String fetchurAlreadyDidTaskPhrase = "come back another time, maybe tmrw";

    // Used for storage, essential for Fetchur Warner
    @Getter @Setter private FetchurItem currentItemSaved;

    // Used to track if the player was warned that fetchur changed!
    @Getter @Setter
    private boolean alreadyWarned;

    // Variables used for testing purposes
    private int tick = 0;
    private int test = 0;
    //

    // A list containing the items fetchur wants
    // If you want to put it in a repository, YOU MUST PUT IT IN THE EXACT SAME ORDER AS I PLACED ON THIS LIST
    // Changing the order will affect the algorythm
    private final FetchurItem[] items = new FetchurItem[] {new FetchurItem(new ItemStack(Blocks.wool, 50, 14),"Red Wool"),
    new FetchurItem(new ItemStack(Blocks.stained_glass, 20, 4),"Yellow Stained Glass"),
    new FetchurItem(new ItemStack(Items.compass, 1), "Compass"),
    new FetchurItem(new ItemStack(Items.prismarine_crystals, 20), "Mithril"),
    new FetchurItem(new ItemStack(Items.fireworks, 1), "Firework Rocket"),
    new FetchurItem(ItemUtils.createSkullItemStack("§fCheap Coffee", null, "2fd02c32-6d35-3a1a-958b-e8c5a657c7d4" ,"194221a0de936bac5ce895f2acad19c64795c18ce5555b971594205bd3ec"), "Cheap Coffee"),
    new FetchurItem(new ItemStack(Items.oak_door, 1), "Wooden Door"),
    new FetchurItem(new ItemStack(Items.rabbit_foot, 3), "Rabbit's Feet"),
    new FetchurItem(new ItemStack(Blocks.tnt, 1),"Superboom TNT"),
    new FetchurItem(new ItemStack(Blocks.pumpkin, 1),"Pumpkin"),
    new FetchurItem(new ItemStack(Items.flint_and_steel, 1), "Flint and Steel"),
    new FetchurItem(new ItemStack(Blocks.quartz_ore, 50),"Nether Quartz Ore"),
    new FetchurItem(new ItemStack(Items.ender_pearl, 16), "Ender Pearl")};

    // Prefix to detect fetchur quotes for the item
    private String[] prefixes = new String[] {"theyre","its"};

    /**
     *
     * @return The item Fetchur wants today!
     */
    public FetchurItem getFetchurToday() {
        final Date d = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM, d, yyyy hh:mm:ss a z");
        sdf.setTimeZone(fetchurZone);
        FetchurItem result = null;
        int day = Integer.parseInt(sdf.format(d).split(",")[2].replace(" ", ""));
            int itemIndex = 0;
            for (int i = 0; i <= day; i++) {
                if (i == day) {
                    result = items[itemIndex - 1];
                   /* Use this to check the rendering on each item
                    if (tick > 500) {
                        tick = 0;
                        if (test >= items.length) {
                            test = 0;
                        }
                        test++;
                    } else {
                        tick++;
                    }
                    if (test >= items.length) {
                        test = 0;
                    }
                    return items[test];*/
                }
                if (itemIndex == items.length) {
                    itemIndex = 0;
                }
                itemIndex++;
            }
        return result;
    }
    public boolean hasMadeFetchur() {
        if (SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getLastTimeFetchur() == null)
            return false;
        final Date current = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM, d, yyyy hh:mm:ss a z");
        sdf.setTimeZone(fetchurZone);
        final Date lastTimeFetchur = SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getLastTimeFetchur();
        int fetchurDay = Integer.parseInt(sdf.format(lastTimeFetchur).split(",")[2].replace(" ", ""));
        int currentDay = Integer.parseInt(sdf.format(current).split(",")[2].replace(" ", ""));

        return fetchurDay == currentDay;
    }

    /**
     * A class representing the item fetchur wants
     * containing the item instance and the text format of the item
     */
    public static class FetchurItem {
        @Getter private final ItemStack itemStack;
        @Getter private final String itemText;
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
