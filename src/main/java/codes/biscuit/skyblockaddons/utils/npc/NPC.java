package codes.biscuit.skyblockaddons.utils.npc;

import codes.biscuit.skyblockaddons.utils.EnumUtils;

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
enum NPC {
    AUCTION_MASTER(17.5,71,-78.5, asList(Tag.PLAYER, Tag.OTHER), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.AUCTION_HOUSE)),
    BANKER(20.5,71,-40.5, asList(Tag.PLAYER, Tag.OTHER), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.BANK)),
    BAKER(34.5, 71, -44.5, asList(Tag.PLAYER, Tag.EVENT), singletonList(EnumUtils.Location.VILLAGE)),
    LOBBY_SELECTOR(-9,70,-79, asList(Tag.PLAYER, Tag.OTHER), singletonList(EnumUtils.Location.VILLAGE)),
    LUMBER_MERCHANT(-18.5,70,-90, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    ADVENTURER(-18.5,70,-77, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    FISH_MERCHANT(-25.5,70,-77, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    ARMORSMITH(-25.5,70,-90, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    BLACKSMITH(-19.5,71,-124.5, asList(Tag.PLAYER, Tag.UTILITY), singletonList(EnumUtils.Location.VILLAGE)),
    BLACKSMITH_2(-39.5,77,-299.5, asList(Tag.PLAYER, Tag.UTILITY), singletonList(EnumUtils.Location.GOLD_MINE)),
    FARM_MERCHANT(-7,70,-48.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    MINE_MERCHANT(-19,70,-48.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    WEAPONSMITH(-19,70,-41.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    BUILDER(-7,70,-41.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    LIBRARIAN(17.5,71,-16.5, asList(Tag.PLAYER, Tag.MERCHANT), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.LIBRARY)),
    MARCO(9.5,71,-14, asList(Tag.PLAYER, Tag.QUEST_NPC), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.FLOWER_HOUSE)),
    ALCHEMIST(-33.5,73,-14.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    PAT(-129.5,73,-98.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.GRAVEYARD)),
    EVENT_MASTER(-61.5,71,-54.5, asList(Tag.PLAYER, Tag.OTHER), asList(EnumUtils.Location.COLOSSEUM, EnumUtils.Location.VILLAGE)),
    GOLD_FORGER(-27.5,74,-294.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.GOLD_MINE)),
    IRON_FORGER(-1.5,75,-307.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.GOLD_MINE)),
    RUSTY(-20,78,-326, asList(Tag.PLAYER, Tag.UTILITY), singletonList(EnumUtils.Location.GOLD_MINE)),
    MADDOX_THE_SLAYER(-87,66,-70, asList(Tag.PLAYER, Tag.QUEST_NPC), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.TAVERN)),
    SIRIUS(91.5,75,176.5, asList(Tag.PLAYER, Tag.OTHER), singletonList(EnumUtils.Location.WILDERNESS)),

    // Furniture
    HARP(-394.5, 110.5, 33.5, singletonList(Tag.FURNITURE), singletonList(EnumUtils.Location.SAVANNA_WOODLAND));

    double x;
    double y;
    double z;
    List<Tag> tags;
    List<EnumUtils.Location> locations;

    /**
     * Creates a new NPC entry.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param tags NPC Type(s)
     * @param locations NPC Location(s)
     */
    NPC(double x, double y, double z, List<Tag> tags, List<EnumUtils.Location> locations) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.tags = tags;
        this.locations = locations;
    }

    double getX() {
        return x;
    }

    double getY() {
        return y;
    }

    double getZ() {
        return z;
    }

    List<Tag> getTags() {
        return tags;
    }

    List<EnumUtils.Location> getLocations() {
        return locations;
    }

    boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    boolean hasLocation(EnumUtils.Location location) {
        return locations.contains(location);
    }
}
