package codes.biscuit.skyblockaddons.core;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Set;

/**
 * Contains all of the Skyblock locations (I hope).
 */
@Getter
public enum Location {
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

    // The Park
    BIRCH_PARK("Birch Park"),
    SPRUCE_WOODS("Spruce Woods"),
    SAVANNA_WOODLAND("Savanna Woodland"),
    DARK_THICKET("Dark Thicket"),
    JUNGLE_ISLAND("Jungle Island"),

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

    MUSHROOM_DESERT("Mushroom Desert"),

    SPIDERS_DEN("Spider's Den"),

    BLAZING_FORTRESS("Blazing Fortress"),

    // The End
    THE_END("The End"),
    DRAGONS_NEST("Dragon's Nest"),

    // Jerry's workshop
    JERRY_POND("Jerry Pond"), // TODO RPC
    JERRYS_WORKSHOP("Jerry's Workshop"), // TODO RPC

    // Dungeons
    DUNGEON("Dungeon"), // TODO RPC
    DUNGEON_HUB("Dungeon Hub"), // TODO RPC

    /*
    Out of Bounds
    This is a valid location in Skyblock, it isn't a placeholder or a made up location.
    It actually displays when the player is out of bounds.
     */
    NONE("None"),

    // This is used when the mod is unable to retrieve the player's location from the sidebar.
    UNKNOWN("Unknown");

    /** The name of this location as shown on the in-game scoreboard. */
    private String scoreboardName;

    private String discordIconKey;

    Location(String scoreboardName, String discordIconKey) {
        this.scoreboardName = scoreboardName;
        this.discordIconKey = discordIconKey;
    }

    Location(String scoreboardName) {
        this.scoreboardName = scoreboardName;

        Set<String> NO_DISCORD_RPC = Sets.newHashSet("ISLAND", "BAZAAR", "DEEP_CAVERNS", "GUNPOWDER_MINES", "LAPIS_QUARRY", "PIGMAN_DEN", "JERRYS_WORKSHOP", "JERRY_POND");

        if (NO_DISCORD_RPC.contains(name())) {
            discordIconKey = "skyblock";
        } else {
            discordIconKey = name().toLowerCase().replace("_", "-");
        }
    }
}
