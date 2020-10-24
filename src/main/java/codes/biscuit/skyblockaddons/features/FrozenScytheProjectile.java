package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.core.EntityAggregate;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

public class FrozenScytheProjectile extends EntityAggregate {

    @Getter
    private static Map<UUID, FrozenScytheProjectile> frozenScytheProjectiles = new HashMap<>();


    /**
     * Returns an instance of FrozenScytheProjectile if this entity is in fact part of a frozen
     * scythe projectile, or null if not.
     */
    public static FrozenScytheProjectile getFrozenScytheProjectile(Entity targetEntity) {
        if (!(targetEntity instanceof EntityArmorStand) || !targetEntity.isInvisible()) {
            return null;
        }

        // Check if the projectile already exists...
        for (FrozenScytheProjectile projectile : frozenScytheProjectiles.values()) {
            if (projectile.getEntities().contains(targetEntity.getUniqueID())) {
                return projectile;
            }
        }

        // Check a small range around...
        List<EntityArmorStand> stands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityArmorStand.class,
                new AxisAlignedBB(targetEntity.posX - 0.1, targetEntity.posY - 2, targetEntity.posZ - 0.1,
                        targetEntity.posX + 0.1, targetEntity.posY + 2, targetEntity.posZ + 0.1));

        EntityArmorStand ice1 = null, ice2 = null, ice3 = null, ice4 = null;
        for (EntityArmorStand stand : stands) {
            if (!stand.isInvisible()) {
                continue;
            }
            // Check if the foot has ice or packed ice on it
        }
        // Verify that we've found all parts, and that the positions make sense
        /*
        if (present == null || fromLine == null || toLine == null || present.posY > fromLine.posY || fromLine.posY > toLine.posY) {
            return null;
        }
         */
        // Rotation is the same as the player
        return null;
        //return new FrozenScytheProjectile(present.getUniqueID(), fromLine.getUniqueID(), toLine.getUniqueID(), presentColor, fromYou, forYou);
    }
}
