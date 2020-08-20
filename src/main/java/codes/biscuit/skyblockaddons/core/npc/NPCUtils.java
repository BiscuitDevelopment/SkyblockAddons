package codes.biscuit.skyblockaddons.core.npc;

import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;
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
 * @version 1.0
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
        int sellSlot = inventory.getSizeInventory() - 4 - 1;
        ItemStack itemStack = inventory.getStackInSlot(sellSlot);

        if (itemStack != null) {
            if (itemStack.getItem() == Item.getItemFromBlock(Blocks.hopper) && itemStack.hasDisplayName() &&
                    TextUtils.stripColor(itemStack.getDisplayName()).equals("Sell Item")) {
                return true;
            }

            List<String> lore = itemStack.getTooltip(null, false);
            for (String loreLine : lore) {
                if (TextUtils.stripColor(loreLine).equals("Click to buyback!")) {
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
            if (getDistanceSquared(npcLocation, entityToCheck) <= HIDE_RADIUS_SQUARED) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the distance from an npc to an entity
     *
     * @param npcLocation The NPC's position vector
     * @param entityToCheck The entity to check
     * @return The distance between the entities
     */
    private static double getDistanceSquared(Vec3 npcLocation, Entity entityToCheck) {
        double d0 = npcLocation.xCoord - entityToCheck.posX;
        double d1 = npcLocation.yCoord - entityToCheck.posY;
        double d2 = npcLocation.zCoord - entityToCheck.posZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    /**
     * Checks if the given entity is an NPC
     *
     * @param entity the entity to check
     * @return {@code true} if the entity is an NPC, {@code false} otherwise
     */
    public static boolean isNPC(Entity entity) {
        return entity.getUniqueID().version() == 2;
    }
}
