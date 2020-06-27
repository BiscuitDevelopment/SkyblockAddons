package codes.biscuit.skyblockaddons.utils.npc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Location;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.Vec3;

import java.util.EnumSet;
import java.util.Set;
/**
 * This is a set of utility methods relating to Skyblock NPCs
 *
 * @author Biscuit
 * @author ILikePlayingGames
 * @version 1.0
 */
public class NPCUtils {

    private static final int HIDE_RADIUS = 4;

    /** All the NPCs listed in {@code NPC} */
    private static final Set<NPC> NPC_LIST = EnumSet.allOf(NPC.class);

    /**
     * Checks if the given NPC is a merchant
     *
     * @param NPCName the NPC's in-game name
     * @return {@code true} if the NPC is a merchant, {@code false} if it's not a merchant
     */
    public static boolean isMerchant(String NPCName) {
        for (NPC npc:
             NPC_LIST) {
            if (NPCName.equals(npc.getName()) && npc.hasTag(Tag.MERCHANT)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the NPC is a merchant with both buying and selling capabilities
     *
     * @param NPCName the NPC's in-game name
     * @return {@code true} if the NPC is a merchant with buying and selling capabilities, {@code false} otherwise
     */
    public static boolean isFullMerchant(String NPCName) {
        for (NPC npc:
                NPC_LIST) {
            if (NPCName.equals(npc.getName()) && npc.hasTag(Tag.MERCHANT) && !npc.hasTag(Tag.BUY_ONLY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given entity is near any NPC.
     *
     * @param entity the entity to check
     * @return {@code true} if the entity is near an NPC, {@code false} otherwise
     */
    public static boolean isNearAnyNPC(Entity entity) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Location currentLocation = main.getUtils().getLocation();

        for (NPC npc:
             NPC_LIST) {
            if (npc.hasLocation(currentLocation)) {
                Vec3 NPCVector = new Vec3(npc.getX(), npc.getY(), npc.getZ());
                Vec3 entityVector = entity.getPositionVector();

                if (NPCVector.distanceTo(entityVector) <= HIDE_RADIUS) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a given entity is near any NPC with the given {@link Tag}.
     *
     * @param entity the entity to check
     * @return {@code true} if the entity is near an NPC, {@code false} otherwise
     */
    public static boolean isNearAnyNPCWithTag(Entity entity, Tag tag) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Location currentLocation = main.getUtils().getLocation();

        for (NPC npc:
                NPC_LIST) {
            if (npc.hasLocation(currentLocation)) {
                Vec3 NPCVector = new Vec3(npc.getX(), npc.getY(), npc.getZ());
                Vec3 entityVector = entity.getPositionVector();

                if (NPCVector.distanceTo(entityVector) <= HIDE_RADIUS) {
                    if (npc.hasTag(tag))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a given entity is near the NPC with the given name.
     *
     * @param entity the entity to check
     * @param NPCName the NPC's name in {@link NPC}
     * @return {@code true} if the entity is near the matching NPC,{@code false} if the entity is not near the matching NPC or no matching NPC is found
     */
    public static boolean isNearNPC(Entity entity, String NPCName) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Location currentLocation = main.getUtils().getLocation();

        for (NPC npc:
             NPC_LIST) {
            if (npc.name().equals(NPCName) && npc.hasLocation(currentLocation)) {
                Vec3 NPCVector = new Vec3(npc.getX(), npc.getY(), npc.getZ());
                Vec3 entityVector = entity.getPositionVector();

                if (NPCVector.distanceTo(entityVector) <= HIDE_RADIUS) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a given entity is near any NPC with the given {@link Tag}s.
     *
     * @param entity the entity to check
     * @return {@code true} if the entity is near an NPC with the given tags, {@code false} otherwise
     */
    public static boolean isNearAnyNPCWithTags(Entity entity, Set<Tag> tags) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Location currentLocation = main.getUtils().getLocation();

        for (NPC npc:
                NPC_LIST) {
            if (npc.hasLocation(currentLocation)) {
                Vec3 NPCVector = new Vec3(npc.getX(), npc.getY(), npc.getZ());
                Vec3 entityVector = entity.getPositionVector();

                if (NPCVector.distanceTo(entityVector) <= HIDE_RADIUS) {
                    if (npc.getTags().containsAll(tags))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a given entity is near any player NPC.
     *
     * @param entity the entity to check
     * @return {@code true} if the entity is near any player NPC, {@code false} otherwise
     */
    public static boolean isNearAnyPlayerNPC(Entity entity) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Location currentLocation = main.getUtils().getLocation();

        for (NPC npc:
                NPC_LIST) {
            if (npc.hasLocation(currentLocation)) {
                Vec3 NPCVector = new Vec3(npc.getX(), npc.getY(), npc.getZ());
                Vec3 entityVector = entity.getPositionVector();

                if (NPCVector.distanceTo(entityVector) <= HIDE_RADIUS) {
                    if (npc.hasTag(Tag.PLAYER))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a given entity is near any furniture NPC.
     *
     * @param entity the entity to check
     * @return {@code true} if the entity is near any furniture NPC, {@code false} otherwise
     */
    public static boolean isNearAnyFurnitureNPC(Entity entity) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Location currentLocation = main.getUtils().getLocation();

        for (NPC npc:
                NPC_LIST) {
            if (npc.hasLocation(currentLocation)) {
                Vec3 NPCVector = new Vec3(npc.getX(), npc.getY(), npc.getZ());
                Vec3 entityVector = entity.getPositionVector();

                if (NPCVector.distanceTo(entityVector) <= HIDE_RADIUS) {
                    if (npc.hasTag(Tag.FURNITURE))
                        return true;
                }
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
            EntityOtherPlayerMP player = (EntityOtherPlayerMP) entity;
            ScorePlayerTeam playerTeam = (ScorePlayerTeam) player.getTeam();

            // If it doesn't have a team, it's likely not a player.
            if (player.getTeam() == null) {
                return false;
            }

            // If it doesn't have a color prefix, it's not a player.
            return playerTeam.getColorPrefix().equals("");
        } else if (entity instanceof EntityArmorStand) {
            return entity.isInvisible();
        } else {
            return false;
        }
    }
}
