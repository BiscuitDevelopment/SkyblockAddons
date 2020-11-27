package codes.biscuit.skyblockaddons.core;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

/**
 * An abstraction of an "entity" that may be composed of several entity parts.
 * Examples:
 * - A jerry present (3 armor stands)
 * - A zealot (enderman and name tag armor stand)
 */
public class EntityAggregate {

    @Getter private List<UUID> entities;

    public EntityAggregate(UUID... entities) {
        this.entities = Lists.newArrayList(entities);
    }

    /**
     * The aggregate entity is dead when all of its components are dead
     */
    public boolean isDead() {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) {
            return true;
        }

        for (UUID uuid : entities) {
            Entity entity = getEntityFromUUID(uuid);
            if (entity != null && !entity.isDead) {
                return false;
            }
        }
        return true;
    }

    private Entity getEntityFromUUID(UUID uuid) {
        for (Entity entity : Minecraft.getMinecraft().theWorld.getLoadedEntityList()) {
            if (entity.getUniqueID().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }
}
