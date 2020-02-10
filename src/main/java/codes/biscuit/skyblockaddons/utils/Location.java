package codes.biscuit.skyblockaddons.utils;

import lombok.Getter;

/**
 * Contains all of the Skyblock locations (I hope).
 */
@Getter
public enum Location {

    UNKNOWN("Unknown", "unknown"), // Used if for some reason the location wasn't found

    ISLAND("Your Island", "private-island"),
    // Hub
    VILLAGE("Village", "village"),
    AUCTION_HOUSE("Auction House", "auction-house"),
    BANK("Bank", "bank"),
    LIBRARY("Library", "library"),
    COAL_MINE("Coal Mine", "coal-mine"),
    GRAVEYARD("Graveyard", "graveyard"),
    COLOSSEUM("Colosseum", "colosseum"),
    WILDERNESS("Wilderness", "wilderness"),
    MOUNTAIN("Mountain", "mountain"),
    WIZARD_TOWER("Wizard Tower", "wizard-tower"),
    RUINS("Ruins", "ruins"),
    FOREST("Forest", "forest"),
    FARM("Farm", "farm"),
    FISHERMANS_HUT("Fisherman's Hut", "fishermans-hut"),
    HIGH_LEVEL("High Level", "high-level"),
    FLOWER_HOUSE("Flower House", "flower-house"),
    CANVAS_ROOM("Canvas Room", "canvas-room"),
    TAVERN("Tavern", "tavern"),
    FASHION_SHOP("Fashion Shop", "fashion-shop"),
    // Floating Islands
    BIRCH_PARK("Birch Park", "birch-park"),
    SPRUCE_WOODS("Spruce Woods", "spruce-woods"),
    JUNGLE_ISLAND("Jungle Island", "jungle-island"),
    SAVANNA_WOODLAND("Savanna Woodland", "savanna-woodland"),
    DARK_THICKET("Dark Thicket", "dark-thicket"),

    GOLD_MINE("Gold Mine", "gold-mine"),

    DEEP_CAVERNS("Deep Caverns", "deep-caverns"),
    GUNPOWDER_MINES("Gunpowder Mines", "gunpowder-mines"),
    LAPIS_QUARRY("Lapis Quarry", "lapis-quarry"),
    PIGMAN_DEN("Pigmen's Den", "pigman-den"),
    SLIMEHILL("Slimehill", "slimehill"),
    DIAMOND_RESERVE("Diamond Reserve", "diamond-reserve"),
    OBSIDIAN_SANCTUARY("Obsidian Sanctuary", "obsidian-sanctuary"),
    THE_BARN("The Barn", "barn"),

    MUSHROOM_DESERT("Mushroom Desert", "mushroom-desert"),

    SPIDERS_DEN("Spider's Den", "spiders-den"),

    BLAZING_FORTRESS("Blazing Fortress", "blazing-fortress"),

    THE_END("The End", "the-end"),
    DRAGONS_NEST("Dragon's Nest", "dragons-nest"),

    // Jerry's workshop
    JERRY_POND("Jerry Pond", "jerry-pond"),
    JERRYS_WORKSHOP("Jerry's Workshop", "jerry-workshop");

    private final String scoreboardName;
    private final String discordIconKey;

    Location(String scoreboardName, String discordIconKey) {
        this.scoreboardName = scoreboardName;
        this.discordIconKey = discordIconKey;
    }
}