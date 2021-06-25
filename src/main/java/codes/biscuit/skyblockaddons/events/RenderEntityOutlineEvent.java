package codes.biscuit.skyblockaddons.events;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Event that is fired by {@link codes.biscuit.skyblockaddons.features.EntityOutlines.EntityOutlineRenderer} to determine which entities will be outlined.
 * The event is fired twice each tick, first for the {@link Type#XRAY} phase, and second for the {@link Type#NO_XRAY} phase.
 * Event handlers can add entities/colors to be outlined for either phase using the {@link #queueEntitiesToOutline(Function)} event function
 * The resulting list of entities/associated colors is outlined after both events have been handled
 */
public class RenderEntityOutlineEvent extends Event {

    /**
     * The phase of the event (see {@link Type}
     */
    @Getter
    private final Type type;
    /**
     * The entities to outline. This is progressively cumulated from {@link #entitiesToChooseFrom}
     */
    @Getter
    private HashMap<Entity, Integer> entitiesToOutline = null;
    /**
     * The entities we can outline. Note that this set and {@link #entitiesToOutline} are disjoint at all times.
     */
    @Getter
    private HashSet<Entity> entitiesToChooseFrom;

    /**
     * Constructs the event, given the type and optional entities to outline.
     * <p>
     * This will modify {@param potentialEntities} internally, so make a copy before passing it if necessary.
     *
     * @param theType           of the event (see {@link Type}
     */
    public RenderEntityOutlineEvent(Type theType, HashSet<Entity> potentialEntities) {
        type = theType;
        entitiesToChooseFrom = potentialEntities;
        if (potentialEntities != null) {
            entitiesToOutline = new HashMap<>(potentialEntities.size());
        }
    }

    /**
     * Conditionally queue entities around which to render entities
     * Selects from the pool of {@link #entitiesToChooseFrom} to speed up the predicate testing on subsequent calls.
     * Is more efficient (theoretically) than calling {@link #queueEntityToOutline(Entity, int)} for each entity because lists are handled internally.
     * <p>
     * This function loops through all entities and so is not very efficient.
     * It's advisable to encapsulate calls to this function with global checks (those not dependent on an individual entity) for efficiency purposes.
     *
     * @param outlineColor a function to test
     */
    public void queueEntitiesToOutline(Function<Entity, Integer> outlineColor) {
        if (outlineColor == null) {
            return;
        }
        if (entitiesToChooseFrom == null) {
            computeAndCacheEntitiesToChooseFrom();
        }
        Iterator<Entity> itr = entitiesToChooseFrom.iterator();
        while (itr.hasNext()) {
            Entity e = itr.next();
            Integer i = outlineColor.apply(e);
            if (i != null) {
                entitiesToOutline.put(e, i);
                itr.remove();
            }
        }
    }

    /**
     * Adds a single entity to the list of the entities to outline
     *
     * @param entity       the entity to add
     * @param outlineColor the color with which to outline
     */
    public void queueEntityToOutline(Entity entity, int outlineColor) {
        if (entity == null) {
            return;
        }
        if (entitiesToChooseFrom == null) {
            computeAndCacheEntitiesToChooseFrom();
        }
        if (!entitiesToChooseFrom.contains(entity)) {
            return;
        }
        entitiesToOutline.put(entity, outlineColor);
        entitiesToChooseFrom.remove(entity);
    }

    /**
     * Used for on-the-fly generation of entities. Driven by event handlers in a decentralized fashion
     */
    private void computeAndCacheEntitiesToChooseFrom() {
        List<Entity> entities = Minecraft.getMinecraft().theWorld.getLoadedEntityList();
        // Only render outlines around non-null entities within the camera frustum
        entitiesToChooseFrom = new HashSet<>(entities.size());
        // Only consider entities that aren't invisible armorstands to increase FPS significantly
        entities.forEach(e -> {
            if (e != null && !(e instanceof EntityArmorStand && e.isInvisible()) && !(e instanceof EntityItemFrame)) {
                entitiesToChooseFrom.add(e);
            }
        });
        entitiesToOutline = new HashMap<>(entitiesToChooseFrom.size());
    }

    /**
     * The phase of the event.
     * {@link #XRAY} means that this directly precedes entities whose outlines are rendered through walls (Vanilla 1.9+)
     * {@link #NO_XRAY} means that this directly precedes entities whose outlines are rendered only when visible to the client
     */
    public enum Type {
        XRAY,
        NO_XRAY
    }


    public static class EntityAndOutlineColor {
        @Getter
        private final Entity entity;
        @Getter
        private final int color;

        public EntityAndOutlineColor(Entity theEntity, int theColor) {
            entity = theEntity;
            color = theColor;
        }

        @Override
        public int hashCode() {
            return entity.hashCode();
        }
    }
}
