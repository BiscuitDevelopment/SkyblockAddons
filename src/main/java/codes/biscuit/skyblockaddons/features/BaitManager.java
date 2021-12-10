package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.core.ItemType;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
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
    @Getter private final Map<BaitType, Integer> baitsInInventory = new HashMap<>();

    /**
     * Check if our Player is holding a Fishing Rod, and filters out the Grapple Hook and Soul Whip and other items
     * that are {@code Items.fishing_rod}s but aren't used for fishing. This is done by checking for the item type of
     * "FISHING ROD" which is displayed beside the item rarity.
     *
     * @return {@code true} if the player is holding a fishing rod that can be used for fishing, {@code false} otherwise
     */
    public boolean isHoldingRod() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        if (player != null) {
            ItemStack item = player.getHeldItem();
            if (item == null || item.getItem() != Items.fishing_rod) return false;

            return ItemUtils.getItemType(item) == ItemType.FISHING_ROD;
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
            String skyblockID = ItemUtils.getSkyblockItemID(itemStack);
            if (skyblockID == null) {
                continue;
            }

            BaitType bait = BaitType.getByItemID(skyblockID);
            if (bait == null) {
                continue;
            }

            baitsInInventory.put(bait, baitsInInventory.getOrDefault(bait, 0) + itemStack.stackSize);
        }

    }

    @Getter
    public enum BaitType {
        MINNOW("§fMinnow Bait", "MINNOW_BAIT", "7b2c475f-ac70-34ce-af36-be5009df1585", "5f91a14867ef9867872dcdc8c54e3d4cfb5e525dccfb7f99a7314a45faeb301e"),
        FISH("§fFish Bait", "FISH_BAIT", "1b379ea9-7eb2-3e40-bf48-559eaf4da1f6", "f9bb18e1cfe6edf03535e5fdc6482e09a7fdf95125c8611269b9de9d54715b9d"),
        LIGHT("§fLight Bait", "LIGHT_BAIT", "0f759208-92ff-3686-b38b-d4f821a9247e", "336a9add25645bfcc377c25ef0c2e9901d19493c3e981ebc6ba7a1a1b6466ce4"),
        DARK("§fDark Bait", "DARK_BAIT", "df2c3cc0-6792-3af3-b127-e0d934b485b7", "c33c516f3f380916d41a9355a319f85943aacc8a9c1b4a138017484b61114f68"),
        SPIKED("§fSpiked Bait", "SPIKED_BAIT", "a82c3c93-de2a-338c-a766-7c2985f6648c", "a12bc5ef26f367bdbd7116e80fde4af1823c0e9360afab02a9d3a4cb6d5fcd82"),
        SPOOKY("§fSpooky Bait", "SPOOKY_BAIT", "74b7ab7c-3268-3a70-8937-026e1416dd7f", "51074ba79616c7d8cf8e33849039f67410a2f7c9ce793d447e21f5aa24d50108"),
        CARROT("§fCarrot Bait", "CARROT_BAIT", "0842969c-2692-3bd7-8483-2b7b2c2b7f63", "4d3a6bd98ac1833c664c4909ff8d2dc62ce887bdcf3cc5b3848651ae5af6b"),
        BLESSED("§aBlessed Bait", "BLESSED_BAIT", "54b94885-521b-3700-a138-31330f0aba0d", "b060a46497cee7611346c9dd8eb8f5de4483de259147211353848356de15d19"),
        WHALE("§9Whale Bait", "WHALE_BAIT", "d88b7890-b977-3de5-acc6-5e14b17cc1f3", "33aa71676e81fb53a040dfdca3e5b47d53e6efd665e69fb439778e8c4efb1cc"),
        ICE("§aIce Bait", "ICE_BAIT", "b2b19dcd-dc67-31df-a790-e6cf07ae12ac", "11136616d8c4a87a54ce78a97b551610c2b2c8f6d410bc38b858f974b113b208"),
        SHARK("§aShark Bait", "SHARK_BAIT", "9a6c7271-a12d-3941-914c-7c456a086c5a", "edff904124efe486b3a54261dbb8072b0a4e11615ad8d7394d814e0e8c8ef9eb")
        ;

        private final String itemID;
        private final ItemStack itemStack;

        BaitType(String name, String itemID, String skullID, String textureURL) {
            this.itemID = itemID;
            this.itemStack = ItemUtils.createSkullItemStack(name, itemID, skullID, textureURL);
        }

        /**
         * Check to see if the given name matches a bait's skyblock item ID
         *
         * @return The matching BaitType or null
         */
        public static BaitType getByItemID(String itemID) {
            for (BaitType bait : values()) {
                if (itemID.startsWith(bait.itemID)) {
                    return bait;
                }
            }
            return null;
        }
    }
}