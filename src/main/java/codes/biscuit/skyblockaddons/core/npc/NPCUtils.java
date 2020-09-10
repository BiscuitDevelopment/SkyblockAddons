package codes.biscuit.skyblockaddons.core.npc;

import codes.biscuit.skyblockaddons.utils.InventoryUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is a set of utility methods relating to Skyblock NPCs
 *
 * @author Biscuit
 * @author ILikePlayingGames
 * @version 2.0
 */
public class NPCUtils {

    private static final int HIDE_RADIUS_SQUARED = 3 * 3;

    @Getter private static Set<Vec3> npcLocations = new HashSet<>();

    /**
     * Checks if the NPC is a merchant with both buying and selling capabilities
     *
     * @param inventory The inventory to check
     * @return {@code true} if the NPC is a merchant with buying and selling capabilities, {@code false} otherwise
     */
    public static boolean isSellMerchant(IInventory inventory) {
        //TODO Fix for Hypixel localization
        int sellSlot = inventory.getSizeInventory() - 4 - 1;
        ItemStack itemStack = inventory.getStackInSlot(sellSlot);

        if (itemStack != null) {
            if (itemStack.getItem() == Item.getItemFromBlock(Blocks.hopper) && itemStack.hasDisplayName() &&
                    TextUtils.stripColor(itemStack.getDisplayName()).equals("Sell Item")) {
                return true;
            }

            List<String> tooltip = itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, false);
            for (String line : tooltip) {
                if (TextUtils.stripColor(line).equals("Click to buyback!")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a given entity is near any NPC.
     *
     * @param entityToCheck The entity to check
     * @return {@code true} if the entity is near an NPC, {@code false} otherwise
     */
    public static boolean isNearNPC(Entity entityToCheck) {
        for (Vec3 npcLocation : npcLocations) {
            if (entityToCheck.getDistanceSq(npcLocation.xCoord, npcLocation.yCoord, npcLocation.zCoord) <= HIDE_RADIUS_SQUARED) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the given entity is an NPC
     *
     * @param entity the entity to check
     * @return {@code true} if the entity is an NPC, {@code false} otherwise
     */
    public static boolean isNPC(Entity entity) {
        if (entity instanceof EntityOtherPlayerMP) {
            /*
             Player NPCs all have a UUID of type 2. Also check for an absence of the Skyblock menu in the inventory to
             make sure they're an NPC.
             */
            return entity.getUniqueID().version() == 2 && ((EntityOtherPlayerMP) entity).inventory.getStackInSlot(8)
                    == null;
        }

        return false;
    }
}
