package codes.biscuit.skyblockaddons.utils.npc;

import codes.biscuit.skyblockaddons.utils.Location;
import lombok.Getter;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * NPC is a list of all the Skyblock NPCs.
 *
 * @author Biscuit
 * @author ILikePlayingGames
 * @version 1.0
 */
@Getter
enum NPC {
    // These are organized by island

    // Hub
    ADVENTURER(-18.5,70,-77, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.VILLAGE)),
    ALCHEMIST(-33.5,73,-14.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.VILLAGE)),
    ARMORSMITH(-25.5,70,-90, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.VILLAGE)),
    AUCTION_MASTER(17.5,71,-78.5, asList(Tag.PLAYER, Tag.OTHER), asList(Location.VILLAGE, Location.AUCTION_HOUSE)),
    BAKER(34.5, 71, -44.5, asList(Tag.PLAYER, Tag.EVENT), singletonList(Location.VILLAGE)),
    BANKER(20.5,71,-40.5, asList(Tag.PLAYER, Tag.OTHER), asList(Location.VILLAGE, Location.BANK)),
    BLACKSMITH(-19.5,71,-124.5, asList(Tag.PLAYER, Tag.UTILITY), singletonList(Location.VILLAGE)),
    BUILDER(-7,70,-41.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.VILLAGE)),
    EVENT_MASTER(-61.5,71,-54.5, asList(Tag.PLAYER, Tag.OTHER), asList(Location.COLOSSEUM, Location.VILLAGE)),
    FARM_MERCHANT(-7,70,-48.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.VILLAGE)),
    FISH_MERCHANT(-25.5,70,-77, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.VILLAGE)),
    HUB_SELECTOR(-9,70,-79, asList(Tag.PLAYER, Tag.OTHER), singletonList(Location.VILLAGE)),
    LIBRARIAN(17.5,71,-16.5, asList(Tag.PLAYER, Tag.MERCHANT), asList(Location.VILLAGE, Location.LIBRARY)),
    LUMBER_MERCHANT(-18.5,70,-90, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.VILLAGE)),
    MADDOX_THE_SLAYER(-87,66,-70, asList(Tag.PLAYER, Tag.QUEST_NPC), asList(Location.VILLAGE, Location.TAVERN)),
    MARCO(9.5,71,-14, asList(Tag.PLAYER, Tag.QUEST_NPC), asList(Location.VILLAGE, Location.FLOWER_HOUSE)),
    MINE_MERCHANT(-19,70,-48.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.VILLAGE)),
    PAT(-129.5,73,-98.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.GRAVEYARD)),
    SIRIUS(91.5,75,176.5, asList(Tag.PLAYER, Tag.OTHER), singletonList(Location.WILDERNESS)),
    WEAPONSMITH(-19,70,-41.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.VILLAGE)),

    // Gold Mine,
    BLACKSMITH_2(-39.5,77,-299.5, asList(Tag.PLAYER, Tag.UTILITY), singletonList(Location.GOLD_MINE)),
    GOLD_FORGER(-27.5,74,-294.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.GOLD_MINE)),
    IRON_FORGER(-1.5,75,-307.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(Location.GOLD_MINE)),
    RUSTY(-20,78,-326, asList(Tag.PLAYER, Tag.UTILITY), singletonList(Location.GOLD_MINE)),

    // Furniture
    HARP(-394.5, 110.5, 33.5, singletonList(Tag.FURNITURE), singletonList(Location.SAVANNA_WOODLAND));

    private double x;
    private double y;
    private double z;
    private List<Tag> tags;
    private List<Location> locations;

    /**
     * Creates a new NPC entry.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param tags NPC Type(s)
     * @param locations NPC Location(s)
     */
    NPC(double x, double y, double z, List<Tag> tags, List<Location> locations) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.tags = tags;
        this.locations = locations;
    }

    boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    boolean hasLocation(Location location) {
        return locations.contains(location);
    }
}
