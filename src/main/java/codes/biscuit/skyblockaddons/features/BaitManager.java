package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Keeps track of bait in the Player's Inventory.
 *
 * @author Charzard4261
 */
public class BaitManager {

    /**
     * The BaitListManager instance.
     */
    @Getter private static final BaitManager instance = new BaitManager();

    public static final Map<BaitType, Integer> DUMMY_BAITS = new HashMap<>();

    static {
        DUMMY_BAITS.put(BaitType.CARROT, 1);
        DUMMY_BAITS.put(BaitType.MINNOW, 2);
        DUMMY_BAITS.put(BaitType.WHALE, 3);
    }

    /**
     * A map of all baits in the inventory and their count
     */
    @Getter private Map<BaitType, Integer> baitsInInventory = new HashMap<>();

    /**
     * Check if our Player is holding a Fishing Rod, and filters out the Grapple Hook (If any more items are made that
     * are Items.fishing_rods but aren't used for fishing, add them here)
     *
     * @return True if it can be used for fishing
     */
    public boolean isHoldingRod() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        if (player != null) {
            ItemStack item = player.getHeldItem();
            if (item == null || item.getItem() != Items.fishing_rod) return false;

            return !"GRAPPLING_HOOK".equals(ItemUtils.getSkyBlockItemID(item));
        }
        return false;
    }

    /**
     * Re-count all baits in the inventory
     */
    public void refreshBaits() {
        baitsInInventory.clear();

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        for (ItemStack itemStack : player.inventory.mainInventory) {

            if (itemStack == null || !itemStack.hasDisplayName()) continue;

            BaitType bait = BaitType.getByDisplayName(itemStack.getDisplayName());
            if (bait == null) continue;

            baitsInInventory.put(bait, baitsInInventory.getOrDefault(bait, 0) + itemStack.stackSize);
        }

    }

    @Getter
    public enum BaitType { // TODO Convert to using item IDs...
        MINNOW("§fMinnow Bait"),
        FISH("§fFish Bait"),
        LIGHT("§fLight Bait"),
        DARK("§fDark Bait"),
        SPIKED("§fSpiked Bait"),
        SPOOKY("§fSpooky Bait"),
        CARROT("§fCarrot Bait"),
        BLESSED("§aBlessed Bait"),
        WHALE("§9Whale Bait"),
        ICE("§aIce Bait");

        /*
         * Display Name of the bait.
         */
        private String displayName;
        /*
         * Resource Location of an image for the bait.
         */
        private ResourceLocation resourceLocation;

        BaitType(String displayName) {
            this.displayName = displayName;
            this.resourceLocation = new ResourceLocation("skyblockaddons", "baits/"+this.name().toLowerCase(Locale.US)+".png");
        }

        /**
         * Check to see if the given name matches a bait's name.
         *
         * @param name Display Name of the Item to check
         * @return The matching BaitType or null
         */
        public static BaitType getByDisplayName(String name) {
            for (BaitType bait : values()) {
                if (name.startsWith(bait.displayName)) {
                    return bait;
                }
            }
            return null;
        }
    }
}