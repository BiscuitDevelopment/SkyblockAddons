package codes.biscuit.skyblockaddons.features;

import lombok.Getter;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/*
An abstraction of an "entity" that may be composed of several entity parts
E.g. a JerryPresent (3 armorstands), or a zealot (enderman and nametag armorstand), or sea emperor, etc.
 */
public class EntityAggregate {

    @Getter private ArrayList<Entity> entityParts;

    public EntityAggregate(Entity... parts) {
        entityParts = new ArrayList<>();
        for (Entity e : parts) {
            entityParts.add(e);
        }
    }

    /*
    The aggregate entity is dead when all of its components are dead
     */
    public boolean isDead() {
        for (Entity part : entityParts) {
            if (!part.isDead) {
                return false;
            }
        }
        return true;
    }

}
