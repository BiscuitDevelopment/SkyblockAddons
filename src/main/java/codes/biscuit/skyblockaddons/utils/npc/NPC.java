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
  
    /*
    Hub
    It's been broken down into sub-sections since it's such a large island.
     */
    // Village (in Hub)
    ADVENTURER(-18.5,70,-77, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    ALCHEMIST(-33.5,73,-14.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    ANDREW(9.5, 70, -64.5, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    APPRENTICE(-53, 58, 80, asList(Tag.VILLAGER, Tag.INFO), singletonList(EnumUtils.Location.VILLAGE)),
    ARMORSMITH(-25.5,70,-90, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    AUCTION_MASTER(17.5,71,-78.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.OTHER), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.AUCTION_HOUSE)),
    BAKER(34.5, 71, -44.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.EVENT), singletonList(EnumUtils.Location.VILLAGE)),
    BANKER(20.5,71,-40.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.OTHER), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.BANK)),
    BARTENDER(-85.5, 70, -69.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.TAVERN)),
    BLACKSMITH(-19.5,71,-124.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.VILLAGE)),
    BUILDER(-7,70,-41.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    CARPENTER(-54.5, 69, -81.5, asList(Tag.PLAYER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    DUKE(-2, 70, -104, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    DUSK(-28.5, 70, -124.5, asList(Tag.PLAYER, Tag.INFO), singletonList(EnumUtils.Location.VILLAGE)),
    FARM_MERCHANT(-7,70,-48.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    FELIX(-14, 70, -98, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    FISH_MERCHANT(-25.5,70,-77, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    GLADIATOR(-61.5,71,-54.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.OTHER), asList(EnumUtils.Location.COLOSSEUM, EnumUtils.Location.VILLAGE)),
    GUY(51.5, 79, -13.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.VILLAGE)),
    HUB_SELECTOR(-9,70,-79, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.OTHER), singletonList(EnumUtils.Location.VILLAGE)),
    JACK(1, 70, -67, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    JAMIE(-17.5, 70, -67.5, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    LEO(-5.5, 70, -89.5, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    LIAM(-35.5, 70, -97.5, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    LIBRARIAN(17.5,71,-16.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.LIBRARY)),
    LUMBER_MERCHANT(-18.5,70,-90, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    LYNN(12, 70, -101, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    MADDOX_THE_SLAYER(-87,66,-70, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.QUEST_NPC), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.TAVERN)),
    MARCO(9.5,71,-14, asList(Tag.PLAYER, Tag.QUEST_NPC), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.FLOWER_HOUSE)),
    MAYOR_SERAPHINE(4.5, 70, -119.5, asList(Tag.PLAYER, Tag.INFO), singletonList(EnumUtils.Location.VILLAGE)),
    MINE_MERCHANT(-19,70,-48.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),
    RUNE_PEDESTAL(-28.5, 71, -128.5, asList(Tag.FURNITURE, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.VILLAGE)),
    RYU(-6, 70, -118, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    SALESMAN(6.5, 70, -93.5, asList(Tag.PLAYER, Tag.OTHER), singletonList(EnumUtils.Location.VILLAGE)),
    SEYMOUR(-31, 66, -110, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT, Tag.BUY_ONLY, Tag.QUEST_NPC), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.FASHION_SHOP)),
    STELLA(28, 70, -116, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    TAYLOR(-28.5, 71, -107, asList(Tag.PLAYER, Tag.OTHER), asList(EnumUtils.Location.VILLAGE, EnumUtils.Location.FASHION_SHOP)),
    TOM(-15, 70, -84, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    VEX(12, 70, -86, asList(Tag.VILLAGER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.VILLAGE)),
    WEAPONSMITH(-19,70,-41.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.VILLAGE)),

    // Farm (in Hub)
    ARTHUR(51.5, 71, -136.5, asList(Tag.PLAYER, Tag.INFO), singletonList(EnumUtils.Location.FARM)),
    FARMER(-44.5, 72, -162.5, asList(Tag.PLAYER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.FARM)),
    SHANIA(48.5, 72, -159.5, asList(Tag.COW, Tag.QUEST_NPC), singletonList(EnumUtils.Location.FARM)),

    // Wilderness (in Hub)
    LUCIUS(125, 73, 165, asList(Tag.PLAYER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.WILDERNESS)),
    SHIFTY(114.5, 73, 175, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.WILDERNESS)),
    SIRIUS(91.5,75,176.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.OTHER), singletonList(EnumUtils.Location.WILDERNESS)),
    TIA_THE_FAIRY(129.5, 66, 137.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.QUEST_NPC), singletonList(EnumUtils.Location.WILDERNESS)),

    // Other (in Hub)
    FISHERMAN(155.5, 68, 47.5, asList(Tag.PLAYER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.FISHERMANS_HUT)),
    LUMBERJACK(-94.5, 74, -39.5, asList(Tag.PLAYER, Tag.INFO, Tag.QUEST_NPC), singletonList(EnumUtils.Location.FOREST)),
    PAT(-129.5,73,-98.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.GRAVEYARD)),
    WIZARD(46.5, 122, 75.5, asList(Tag.PLAYER, Tag.OTHER), singletonList(EnumUtils.Location.WIZARD_TOWER)),

    // The Barn
    FARMHAND(144.5, 73, -240.5, asList(Tag.VILLAGER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.THE_BARN)),

    // The Park
    CHARLIE(-286, 82, -17, asList(Tag.PLAYER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.BIRCH_PARK)),
    FUNK(-462.5, 110, -15.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.SAVANNA_WOODLAND)),
    GUSTAVE(-385.5, 89, 55.5, asList(Tag.PLAYER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.SPRUCE_WOODS)),
    HARP(-394.5, 110.5, 33.5, asList(Tag.FURNITURE, Tag.IMPORTANT, Tag.QUEST_NPC), singletonList(EnumUtils.Location.SAVANNA_WOODLAND)),
    JULIETTE(-476.5, 134, -117, asList(Tag.PLAYER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.JUNGLE_ISLAND)),
    MELODY(-398.5, 110, 34.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT, Tag.BUY_ONLY, Tag.QUEST_NPC), singletonList(EnumUtils.Location.SAVANNA_WOODLAND)),
    NYKO(-379.5, 60, 36.5, asList(Tag.PLAYER, Tag.MERCHANT), singletonList(EnumUtils.Location.NONE)),
    RYAN(-330.5, 103.5, -103.5, asList(Tag.PLAYER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.DARK_THICKET)),
    VANESSA(-312, 83, -70.5, asList(Tag.PLAYER, Tag.UTILITY), singletonList(EnumUtils.Location.BIRCH_PARK)),
    VIKING(-359.5, 91.5, 76.5, asList(Tag.PLAYER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.SPRUCE_WOODS)),

    // Gold Mine
    BLACKSMITH_2(-39.5,77,-299.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.GOLD_MINE)),
    GOLD_FORGER(-27.5,74,-294.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.GOLD_MINE)),
    IRON_FORGER(-1.5,75,-307.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT), singletonList(EnumUtils.Location.GOLD_MINE)),
    RUSTY(-20,78,-326, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.GOLD_MINE)),

    // Deep Caverns
    LIFT_OPERATOR_1(45.5, 150, 15.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.GUNPOWDER_MINES)),
    LIFT_OPERATOR_2(45.5, 121, 15.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.LAPIS_QUARRY)),
    LIFT_OPERATOR_3(45.5, 101, 15.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.PIGMAN_DEN)),
    LIFT_OPERATOR_4(45.5, 66, 15.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.SLIMEHILL)),
    LIFT_OPERATOR_5(45.5, 38, 15.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.DIAMOND_RESERVE)),
    LIFT_OPERATOR_6(45.5, 13, 15.5, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.UTILITY), singletonList(EnumUtils.Location.OBSIDIAN_SANCTUARY)),
    WALTER(19, 156, 36, asList(Tag.PLAYER, Tag.IMPORTANT, Tag.MERCHANT, Tag.BUY_ONLY), singletonList(EnumUtils.Location.GUNPOWDER_MINES)),

    // Spider's Den
    HAYMITCH(-202.5, 84, -240.5, asList(Tag.PLAYER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.SPIDERS_DEN)),

    // Blazing Fortress
    ELLE(-311.5, 96, -405.5, asList(Tag.PLAYER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.BLAZING_FORTRESS)),

    // The End
    GUBER(-494.5, 121, -241.5, asList(Tag.PLAYER, Tag.QUEST_NPC), singletonList(EnumUtils.Location.THE_END)),
    PEARL_DEALER(-504.5, 101, -284.5, asList(Tag.PLAYER, Tag.INFO), singletonList(EnumUtils.Location.THE_END));

    // TODO Jerry's Workshop


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
