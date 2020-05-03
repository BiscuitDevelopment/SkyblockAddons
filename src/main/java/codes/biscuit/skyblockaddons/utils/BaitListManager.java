package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.item.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    @Getter
    private static final BaitListManager instance = new BaitListManager();

    /**
     * A map of all baits in the inventory and their count
     */
    public HashMap<BaitType, Set<Integer,Integer>> baitsInInventory = new HashMap<BaitType, Set<Integer,Integer>>(BaitType.values().length);

    private String previousHeldItem = null;

    /**
     * Check if the ItemStack passed is different to the stored item, and if so refresh the Bait List
     *
     * @param heldItem
     */
    public void compareHeldItems(ItemStack heldItem) {
        if (holdingRod() && (previousHeldItem == null || !previousHeldItem.equals(ItemUtils.getSkyBlockItemID(heldItem))))
            refreshBaits();
        previousHeldItem = heldItem == null ? null : ItemUtils.getSkyBlockItemID(heldItem);
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
            if (!item.getDisplayName().equals("§aGrappling Hook")) return true;
        }
        return false;
    }

    /**
     * Re-count all baits in the inventory
     */
    public void refreshBaits() {
        baitsInInventory.clear();
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        int untilUse = 0;
        for (ItemStack is : player.inventory.mainInventory) {
            if (is == null) continue;
            if (!is.hasDisplayName()) continue;
            BaitType bait = BaitType.getByDisplayName(is.getDisplayName());
            if (bait == null) continue;
            if (baitsInInventory.containsKey(bait))
                baitsInInventory.get(bait).setO2(baitsInInventory.get(bait).getO2() + is.stackSize);
            else {
                baitsInInventory.put(bait, new Set<Integer, Integer>(untilUse, is.stackSize));
                untilUse++;
            }
        }

    }

    public enum BaitType {
        MINNOW("§fMinnow Bait", new ResourceLocation("skyblockaddons", "baits/minnow.png")),
        FISH("§fFish Bait", new ResourceLocation("skyblockaddons", "baits/fish.png")),
        LIGHT("§fLight Bait", new ResourceLocation("skyblockaddons", "baits/light.png")),
        DARK("§fDark Bait", new ResourceLocation("skyblockaddons", "baits/dark.png")),
        SPIKED("S§fpiked Bait", new ResourceLocation("skyblockaddons", "baits/spiked.png")),
        SPOOKY("§fSpooky Bait", new ResourceLocation("skyblockaddons", "baits/spooky.png")),
        CARROT("§fCarrot Bait", new ResourceLocation("skyblockaddons", "baits/carrot.png")),
        BLESSED("§fBlessed Bait", new ResourceLocation("skyblockaddons", "baits/blessed.png")),
        WHALE("§9Whale Bait", new ResourceLocation("skyblockaddons", "baits/whale.png")),
        ICE("§aIce Bait", new ResourceLocation("skyblockaddons", "baits/ice.png"));

        /*
         * Display Name of the bait.
         */
        public String displayName;
        /*
         * Resource Location of an image for the bait.
         */
        public ResourceLocation resourceLocation;

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

    public class Set<T, U>
    {
        @Getter @Setter private T o1;
        @Getter @Setter private U o2;
        public Set(T o1, U o2)
        {
            this.o1 = o1;
            this.o2 = o2;
        }
    }

}
