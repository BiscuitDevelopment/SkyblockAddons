package codes.biscuit.skyblockaddons.events;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Event that is fired by {@link codes.biscuit.skyblockaddons.features.EntityOutlines.EntityOutlineRenderer} to determine which entities will be outlined.
 * The event is fired twice each tick, first for the {@link Type#XRAY} phase, and second for the {@link Type#NO_XRAY} phase.
 * Event handlers can add entities/colors to be outlined for either phase using the {@link #queueEntitiesToOutline(Predicate, Function)} event function
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
    private final HashMap<Entity, Integer> entitiesToOutline;
    /**
     * The entities we can outline. Note that this set and {@link #entitiesToOutline} are disjoint at all times.
     */
    private final HashSet<Entity> entitiesToChooseFrom;

    /**
     * Constructs the event, given the type and optional entities to outline.
     * <p>
     * This will modify {@param potentialEntities} internally, so make a copy before passing it if necessary.
     *
     * @param theType           of the event (see {@link Type}
     * @param potentialEntities the optional entities to outline
     */
    public RenderEntityOutlineEvent(Type theType, HashSet<Entity> potentialEntities) {
        type = theType;
        entitiesToChooseFrom = potentialEntities;
        entitiesToOutline = new HashMap<>(potentialEntities.size());
    }

    /**
     * Conditionally queue entities around which to render entities
     * Selects from the pool of {@link #entitiesToChooseFrom} to speed up the predicate testing on subsequent calls.
     * <p>
     * This function loops through all entities and so is not very efficient.
     * It's advisable to encapsulate calls to this function with global checks (those not dependent on an individual entity) for efficiency purposes.
     *
     * @param predicate on which each entity is tested for inclusion into the queue (Evaluates to {@code true} for entities that will be included in the queue).
     */
    public void queueEntitiesToOutline(Predicate<Entity> predicate, Function<Entity, Integer> outlineColor) {
        if (predicate == null) {
            return;
        }
        Iterator<Entity> itr = entitiesToChooseFrom.iterator();
        while (itr.hasNext()) {
            Entity e = itr.next();
            if (predicate.test(e)) {
                entitiesToOutline.put(e, outlineColor.apply(e));
                itr.remove();
            }
        }
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
}
