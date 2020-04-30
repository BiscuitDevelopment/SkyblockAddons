package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

/**
 * Keeps track of bait in the Player's Inventory.
 * Also provides a list of bait and resource locations for relevant images.
 * Images taken from the <a href="https://hypixel-skyblock.fandom.com/wiki/Fishing_Bait">Skyblock Fandom Wiki</a>
 * {@link PowerOrbManager} code by DidiSkywalker used as a reference.
 *
 * @author Charzard4261
 */
public class BaitListManager {

    //TODO Fix Move GUI Box to extend across all current baits (or make a dummy, PowerOrb has that?)

    /**
     * The BaitListManager instance.
     */
    @Getter
    private static final BaitListManager instance = new BaitListManager();

    public HashMap<BaitType, Integer> baitsInInventory = new HashMap<BaitType, Integer>(BaitType.values().length);

    private ItemStack previousHeldItem = null;

    public void compareHeldItems(ItemStack heldItem) {
        if (previousHeldItem != heldItem && holdingRod())
            refreshBaits();

        previousHeldItem = heldItem;
    }

    public boolean holdingRod() {
        EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (p != null && main.getUtils().isOnSkyblock()) {
            //if (main.getConfigValues().isEnabled(Feature.BAIT_LIST)) {
            ItemStack item = p.getHeldItem();
            if (item == null || item.getItem() != Items.fishing_rod) return false;
            if (!item.hasDisplayName()) return true;
            if (!item.getDisplayName().equals("§aGrappling Hook")) return true;

            //}
        }
        return false;
    }

    public void refreshBaits() {
        baitsInInventory.clear();
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        for (ItemStack is : player.inventory.mainInventory) {
            if (is == null) continue;
            if (!is.hasDisplayName()) continue;
            BaitType bait = BaitType.getByDisplayName(is.getDisplayName());
            if (bait == null) continue;
            if (baitsInInventory.containsKey(bait))
                baitsInInventory.replace(bait, baitsInInventory.get(bait) + is.stackSize);
            else
                baitsInInventory.put(bait, is.stackSize);
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
         * @param name Name of the item to check
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
