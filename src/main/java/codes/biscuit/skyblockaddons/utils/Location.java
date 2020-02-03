package codes.biscuit.skyblockaddons.utils;

import lombok.Getter;

/**
 * Contains all of the Skyblock locations (I hope).
 */
@Getter
public enum Location {
    ISLAND("Your Island"),

    // Hub
    VILLAGE("Village"),
    AUCTION_HOUSE("Auction House"),
    BANK("Bank"),
    LIBRARY("Library"),
    COAL_MINE("Coal Mine"),
    GRAVEYARD("Graveyard"),
    COLOSSEUM("Colosseum"),
    WILDERNESS("Wilderness"),
    MOUNTAIN("Mountain"),
    WIZARD_TOWER("Wizard Tower"),
    RUINS("Ruins"),
    FOREST("Forest"),
    FARM("Farm"),
    FISHERMANS_HUT("Fisherman's Hut"),
    HIGH_LEVEL("High Level"),
    FLOWER_HOUSE("Flower House"),
    CANVAS_ROOM("Canvas Room"),
    TAVERN("Tavern"),

    // The Park
    BIRCH_PARK("Birch Park"),
    SPRUCE_WOODS("Spruce Woods"),
    SAVANNA_WOODLAND("Savanna Woodland"),
    DARK_THICKET("Dark Thicket"),
    JUNGLE_ISLAND("Jungle Island"),

    GOLD_MINE("Gold Mine"),

    // Deep Caverns
    DEEP_CAVERNS("Deep Caverns"),
    GUNPOWDER_MINES("Gunpowder Mines"),
    LAPIS_QUARRY("Lapis Quarry"),
    PIGMAN_DEN("Pigmen's Den"),
    SLIMEHILL("Slimehill"),
    DIAMOND_RESERVE("Diamond Reserve"),
    OBSIDIAN_SANCTUARY("Obsidian Sanctuary"),

    THE_BARN("The Barn"),

    MUSHROOM_DESERT("Mushroom Desert"),

    SPIDERS_DEN("Spider's Den"),

    BLAZING_FORTRESS("Blazing Fortress"),

    // The End
    THE_END("The End"),
    DRAGONS_NEST("Dragon's Nest"),

    //TODO: Jerry's workshop

    NONE("None");

    /** The name of this location as shown on the in-game scoreboard. */
    private String scoreboardName;

    Location(String scoreboardName) {
        this.scoreboardName = scoreboardName;
    }
}
