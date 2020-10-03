package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.core.EntityAggregate;
import net.minecraft.entity.Entity;

import java.util.*;

/*
Designed to hold pointers from Entities to EntityAggregates for quick reference
Given an EntityAggregate, we can quickly add each of it's parts to the mapping or remove each of them
It's abstracted out so any EntityAggregate can work here
 */
public class EntityAggregateMap<T extends EntityAggregate> {

    private HashMap<Entity, T> theMap;

    public EntityAggregateMap() {
        theMap = new HashMap<>();
    }

    /*
    Get the aggregate entity from an aggregate part
    A return value of null indicates the aggregate is not in the mapping or is null
     */
    public T getAggregate(Entity aggregatePart) {

        return theMap.get(aggregatePart);
    }

    public boolean containsAggregate(Entity aggregatePart) {

        return theMap.containsKey(aggregatePart);
    }

    // Set of the aggregate components in the mapping
    public Set<Entity> getPartSet() {
        return theMap.keySet();
    }

    // Set of the aggregates in the mapping
    public Set<T> getAggregateSet() {
        return new HashSet<>(theMap.values());
    }

    // Number of the aggregate components in the mapping
    public int numParts() {
        return theMap.size();
    }

    // Return if the map is empty
    public boolean isEmpty() {
        return theMap.isEmpty();
    }

    // Number of the aggregates in the mapping
    public int numAggregates() {
        return (new HashSet<>(theMap.values())).size();
    }


    // Remove an aggregate by removing each of it's individual parts
    public void addAggregate(T e) {
        if (e == null) return;
        for (Entity part: e.getEntityParts()) {
            theMap.put(part, e);
        }
    }


    // Remove an aggregate by removing each of it's individual parts
    public void removeAggregate(T e) {
        if (e == null) return;
        if (theMap.containsValue(e)) {
            for (Entity part: e.getEntityParts()) {
                theMap.remove(part);
            }
        }
    }



}
