package codes.biscuit.skyblockaddons.core;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Set;

/**
 * Contains all of the Skyblock locations (I hope).
 */
@Getter
public enum Location {
    // TODO: Jsonify all of these
    ISLAND("Your Island"), // TODO RPC
    GUEST_ISLAND("'s Island", "island"), // TODO RPC

    // Hub
    AUCTION_HOUSE("Auction House"),
    BANK("Bank"),
    BAZAAR("Bazaar Alley"), // TODO RPC
    CANVAS_ROOM("Canvas Room"),
    COAL_MINE("Coal Mine"),
    COLOSSEUM("Colosseum"),
    COLOSSEUM_ARENA("Colosseum Arena", "colosseum"),
    DUEL_ZONE("Duel Zone", "colosseum"),
    ELECTION_ROOM("Election Room"),
    FARM("Farm"),
    FASHION_SHOP("Fashion Shop"),
    FISHERMANS_HUT("Fisherman's Hut"),
    FLOWER_HOUSE("Flower House"),
    FOREST("Forest"),
    GRAVEYARD("Graveyard"),
    HIGH_LEVEL("High Level"),
    LIBRARY("Library"),
    MOUNTAIN("Mountain"),
    RUINS("Ruins"),
    TAVERN("Tavern"),
    VILLAGE("Village"),
    WILDERNESS("Wilderness"),
    WIZARD_TOWER("Wizard Tower"),
    CATACOMBS_ENTRANCE("Catacombs Entrance"),

    // The Park
    BIRCH_PARK("Birch Park"),
    SPRUCE_WOODS("Spruce Woods"),
    SAVANNA_WOODLAND("Savanna Woodland"),
    DARK_THICKET("Dark Thicket"),
    JUNGLE_ISLAND("Jungle Island"),
    HOWLING_CAVE("Howling Cave"),

    GOLD_MINE("Gold Mine"),

    // Deep Caverns
    DEEP_CAVERNS("Deep Caverns"), // TODO RPC
    GUNPOWDER_MINES("Gunpowder Mines"), // TODO RPC
    LAPIS_QUARRY("Lapis Quarry"), // TODO RPC
    PIGMAN_DEN("Pigmen's Den"), // TODO RPC
    SLIMEHILL("Slimehill"),
    DIAMOND_RESERVE("Diamond Reserve"),
    OBSIDIAN_SANCTUARY("Obsidian Sanctuary"),

    THE_BARN("The Barn"),

    // Mushroom Island
    MUSHROOM_DESERT("Mushroom Desert"),
    DESERT_SETTLEMENT("Desert Settlement"),
    TREASURE_HUNTER_CAMP("Treasure Hunter Camp"),
    OASIS("Oasis"),
    MUSHROOM_GORGE("Mushroom Gorge"),
    GLOWING_MUSHROOM_CAVE("Glowing Mushroom Cave"),
    OVERGROWN_MUSHROOM_CAVE("Overgrown Mushroom Cave"),
    JAKES_HOUSE("Jake's House"),
    SHEPHERDS_KEEP("Shepherds Keep"),
    TRAPPERS_DEN("Trappers Den"),

    SPIDERS_DEN("Spider's Den"),

    BLAZING_FORTRESS("Blazing Fortress"),

    // The End
    THE_END("The End"),
    DRAGONS_NEST("Dragon's Nest"),

    // Jerry's workshop
    JERRY_POND("Jerry Pond"), // TODO RPC
    JERRYS_WORKSHOP("Jerry's Workshop"), // TODO RPC

    // Dungeons
    THE_CATACOMBS("The Catacombs"), // TODO RPC
    DUNGEON_HUB("Dungeon Hub"), // TODO RPC

    // Dwarven mines
    DWARVEN_MINES("Dwarven Mines"),
    DWARVEN_VILLAGE("Dwarven Village"),
    GATES_TO_THE_MINES("Gates to the Mines"),
    THE_LIFT("The Lift"),
    THE_FORGE("The Forge"),
    FORGE_BASIN("Forge Basin"),
    LAVA_SPRINGS("Lava Springs"),
    PALACE_BRIDGE("Palace Bridge"),
    ROYAL_PALACE("Royal Palace"),
    ARISTOCRAT_PASSAGE("Aristocrat Passage"),
    HANGING_TERRACE("Hanging Terrace"),
    CLIFFSIDE_VEINS("Cliffside Veins"),
    RAMPARTS_QUARRY("Rampart's Quarry"),
    DIVANS_GATEWAY("Divan's Gateway"),
    FAR_RESERVE("Far Reserve"),
    GOBLIN_BURROWs("Goblin Burrows"),
    UPPER_MINES("Upper Mines"),
    ROYAL_MINES("Royal Mines"),
    MINERS_GUILD("Miner's Guild"),
    GREAT_ICE_WALL("Great Ice Wall"),
    THE_MIST("The Mist"),
    CC_MINECARTS_CO("C&C Minecarts Co."),
    GRAND_LIBRARY("Grand Library"),
    HANGING_COURT("Hanging Court"),

    /*
    Out of Bounds
    This is a valid location in Skyblock, it isn't a placeholder or a made up location.
    It actually displays when the player is out of bounds.
     */
    NONE("None"),

    // This is used when the mod is unable to retrieve the player's location from the sidebar.
    UNKNOWN("Unknown");

    /**
     * The name of this location as shown on the in-game scoreboard.
     */
    private final String scoreboardName;

    private final String discordIconKey;

    Location(String scoreboardName, String discordIconKey) {
        this.scoreboardName = scoreboardName;
        this.discordIconKey = discordIconKey;
    }

    Location(String scoreboardName) {
        this.scoreboardName = scoreboardName;

        Set<String> NO_DISCORD_RPC = Sets.newHashSet("ISLAND", "BAZAAR", "DEEP_CAVERNS", "GUNPOWDER_MINES", "LAPIS_QUARRY", "PIGMAN_DEN", "JERRYS_WORKSHOP", "JERRY_POND",
                "DWARVEN_MINES", "DWARVEN_VILLAGE", "GATES_TO_THE_MINES", "THE_LIFT", "THE_FORGE", "FORGE_BASIN", "LAVA_SPRINGS", "PALACE_BRIDGE", "ROYAL_PALACE",
                "ARISTOCRAT_PASSAGE", "HANGING_TERRACE", "CLIFFSIDE_VEINS", "RAMPARTS_QUARRY", "DIVANS_GATEWAY", "FAR_RESERVE", "GOBLIN_BURROWs", "UPPER_MINES",
                "MINERS_GUILD", "GREAT_ICE_WALL", "THE_MIST", "CC_MINECARTS_CO", "GRAND_LIBRARY", "HANGING_COURT", "ROYAL_MINES",
                "DESERT_SETTLEMENT", "TREASURE_HUNTER_CAMP", "OASIS", "MUSHROOM_GORGE", "GLOWING_MUSHROOM_CAVE", "OVERGROWN_MUSHROOM_CAVE", "JAKES_HOUSE", "SHEPHERDS_KEEP", "TRAPPERS_DEN");

        if (NO_DISCORD_RPC.contains(name())) {
            discordIconKey = "skyblock";
        } else {
            discordIconKey = name().toLowerCase().replace("_", "-");
        }
    }
}
