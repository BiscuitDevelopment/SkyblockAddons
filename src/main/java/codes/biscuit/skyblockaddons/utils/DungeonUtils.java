package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.DungeonClass;
import codes.biscuit.skyblockaddons.core.DungeonMilestone;
import codes.biscuit.skyblockaddons.core.DungeonPlayer;
import codes.biscuit.skyblockaddons.core.EssenceType;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.core.OnlineData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains a set of utility methods for Skyblock Dungeons.
 */
public class DungeonUtils {

    /** Represents unknown dungeon floor */
    public static final int FLOOR_NONE = -1;

    /** Represents the entrance of the dungeon */
    public static final int FLOOR_ENTRANCE = 0;

    private static final Pattern PATTERN_LOCATION = Pattern.compile("The Catacombs (E|F[0-9]+)");
    private static final Pattern PATTERN_MILESTONE = Pattern.compile("^.+?(Healer|Tank|Mage|Archer|Berserk) Milestone .+?([❶-❿]).+?(§r§.+\\d+) .+?");
    private static final Pattern PATTERN_COLLECTED_ESSENCES = Pattern.compile("§.+?(\\d+) (Wither|Spider|Undead|Dragon|Gold|Diamond|Ice) Essence");
    private static final Pattern PATTERN_BONUS_ESSENCE = Pattern.compile("^§.+?[^You] .+?found a .+?(Wither|Spider|Undead|Dragon|Gold|Diamond|Ice) Essence.+?");

    /** The last dungeon server the player played on */
    @Getter @Setter private String lastServerId;

    /** The latest milestone the player received during a dungeon game */
    @Getter @Setter private DungeonMilestone dungeonMilestone;

    /** The latest essences the player collected during a dungeon game */
    @Getter private final Map<EssenceType, Integer> collectedEssences = new EnumMap<>(EssenceType.class);

    /** The current teammates of the dungeon game */
    @Getter private final Map<String, DungeonPlayer> players = new HashMap<>();

    /** The current floor of the dungeon game */
    @Getter private int floor = FLOOR_NONE;

    /** The current bosses of the dungeon floor */
    @Getter private Set<String> bosses = new TreeSet<>();

    /** The display name map of dungeon boss */
    @Getter private Map<String, String> bossDisplayName = new HashMap<>();

    private boolean initialized = false;
    private EssenceType lastEssenceType;
    private int lastEssenceAmount;
    private int lastEssenceRepeat;

    /**
     * Initialize the data depending on the dungeon floor
     */
    public void init() {
        if (initialized)
            return;
        initialized = true;

        SkyblockAddons main = SkyblockAddons.getInstance();
        main.getUtils().setLocation(Location.DUNGEON_CATACOMBS);

        OnlineData.DungeonData dungeonData = main.getOnlineData().getDungeons().get("catacombs");
        bosses.addAll(dungeonData.getBosses().getOrDefault(floor, new ArrayList<>()));
        bossDisplayName.putAll(dungeonData.getBossDisplayName());
    }

    /**
     * Clear the dungeon game data. Called by {@link codes.biscuit.skyblockaddons.utils.Utils} each new game
     */
    public void reset() {
        if (!initialized)
            return;
        initialized = false;

        floor = FLOOR_NONE;
        dungeonMilestone = null;
        collectedEssences.clear();
        players.clear();
        bosses.clear();
        bossDisplayName.clear();
    }

    /**
     * Check if the dungeon game server change to call the reset method
     *
     * @param serverID The dungeon game server
     * @return If reset method should be called or not
     */
    public boolean requireReset(String serverID) {
        return initialized && lastServerId != null && !lastServerId.equals(serverID);
    }

    /**
     * Parses the skyblock location to identify the dungeon with its floor
     *
     * @param location The skyblock location
     * @return True if the location is in dungeons
     */
    public boolean parseLocation(String location) {
        Matcher matcher = PATTERN_LOCATION.matcher(location);
        if (!matcher.lookingAt())
            return false;

        String rawFloor = matcher.group(1);
        if (rawFloor.equals("E"))
            floor = FLOOR_ENTRANCE;
        else
            floor = Integer.parseInt(rawFloor.substring(1));
        return true;
    }

    /**
     * This method parses the class milestone attained from the chat message the player receives when they attain a milestone.
     *
     * @param message the chat message received
     * @return a {@code DungeonMilestone} object representing the milestone if one is found, or {@code null} if no milestone is found
     */
    public DungeonMilestone parseMilestone(String message) {
        Matcher matcher = PATTERN_MILESTONE.matcher(message);
        if (!matcher.lookingAt()) {
            return null;
        }

        DungeonClass dungeonClass = DungeonClass.fromDisplayName(matcher.group(1));
        return new DungeonMilestone(dungeonClass, matcher.group(2), matcher.group(3));
    }

    /**
     * This method parses the type and amount of essence the player collected from the action bar message that shows up
     * when an essence is collected. It then records the result in {@code collectedEssences}.
     *
     * @param message the action bar message to parse essence information from
     */
    public void parseCollectedEssence(String message) {
        Matcher matcher = PATTERN_COLLECTED_ESSENCES.matcher(message);

        while (matcher.find()) {

            int amount = Integer.parseInt(matcher.group(1));
            EssenceType essenceType = EssenceType.fromName(matcher.group(2));

            // Fix: Add x3 of the original collected
            // This happens because the action bar receives the collected essence 3 times
            if (lastEssenceType != null && lastEssenceAmount == amount && lastEssenceType == essenceType) {
                lastEssenceRepeat++;

                if (lastEssenceRepeat == 3) {
                    lastEssenceType = null; // Trigger a reset of the original collected essence in the third spam
                }
                continue; // Prevent the spam collected essence to be accounted
            }
            lastEssenceType = essenceType;
            lastEssenceAmount = amount;
            lastEssenceRepeat = 1;

            if (essenceType != null) {
                collectedEssences.put(essenceType, collectedEssences.getOrDefault(essenceType, 0) + amount);
            }
        }
    }

    /**
     * This method parses the type and amount of essence gained when a dungeon teammate finds a bonus essence.
     * This information is parsed from the given chat message. It then records the result in {@code collectedEssences}.
     *
     * @param message the chat message to parse essence information from
     */
    public void parseBonusEssence(String message) {
        Matcher matcher = PATTERN_BONUS_ESSENCE.matcher(message);

        if (matcher.matches()) {
            EssenceType essenceType = EssenceType.fromName(matcher.group(1));

            collectedEssences.put(essenceType, collectedEssences.getOrDefault(essenceType, 0) + 1);
        }
    }
}
