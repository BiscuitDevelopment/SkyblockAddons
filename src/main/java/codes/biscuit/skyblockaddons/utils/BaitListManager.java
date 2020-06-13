package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of bait in the Player's Inventory.
 * Also provides a list of bait and resource locations for relevant images.
 * Images taken from the <a href="https://hypixel-skyblock.fandom.com/wiki/Fishing_Bait">Skyblock Fandom Wiki</a>
 * {@link PowerOrbManager} code by DidiSkywalker used as a reference.
 *
 * @author Charzard4261
 */
public class BaitListManager {

    /**
     * The BaitListManager instance.
     */
    @Getter private static final BaitListManager instance = new BaitListManager();

    /**
     * A map of all baits in the inventory and their count
     */
    public Map<BaitType, Integer> baitsInInventory = new HashMap<>();

    public static final Map<BaitType, Integer> DUMMY_BAITS = new HashMap<>();

    static {
        DUMMY_BAITS.put(BaitType.CARROT, 1);
        DUMMY_BAITS.put(BaitType.MINNOW, 2);
        DUMMY_BAITS.put(BaitType.WHALE, 3);
    }

    /**
     * Check if our Player is holding a Fishing Rod, and filters out the Grapple Hook (If any more items are made that
     * are Items.fishing_rods but aren't used for fishing, add them here)
     *
     * @return True if it can be used for fishing
     */
    public boolean holdingRod() {
        EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (p != null && main.getUtils().isOnSkyblock()) {
            ItemStack item = p.getHeldItem();
            if (item == null || item.getItem() != Items.fishing_rod) return false;

            if (!item.hasDisplayName()) return true;

            return !item.getDisplayName().equals("§aGrappling Hook");
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
    public enum BaitType {
        MINNOW("§fMinnow Bait", new ResourceLocation("skyblockaddons", "baits/minnow.png")),
        FISH("§fFish Bait", new ResourceLocation("skyblockaddons", "baits/fish.png")),
        LIGHT("§fLight Bait", new ResourceLocation("skyblockaddons", "baits/light.png")),
        DARK("§fDark Bait", new ResourceLocation("skyblockaddons", "baits/dark.png")),
        SPIKED("§fSpiked Bait", new ResourceLocation("skyblockaddons", "baits/spiked.png")),
        SPOOKY("§fSpooky Bait", new ResourceLocation("skyblockaddons", "baits/spooky.png")),
        CARROT("§fCarrot Bait", new ResourceLocation("skyblockaddons", "baits/carrot.png")),
        BLESSED("§aBlessed Bait", new ResourceLocation("skyblockaddons", "baits/blessed.png")),
        WHALE("§9Whale Bait", new ResourceLocation("skyblockaddons", "baits/whale.png")),
        ICE("§aIce Bait", new ResourceLocation("skyblockaddons", "baits/ice.png"));

        /*
         * Display Name of the bait.
         */
        private String displayName;
        /*
         * Resource Location of an image for the bait.
         */
        private ResourceLocation resourceLocation;

        BaitType(String displayName, ResourceLocation resourceLocation) {
            this.displayName = displayName;
            this.resourceLocation = resourceLocation;
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