package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.DungeonClass;
import codes.biscuit.skyblockaddons.core.DungeonMilestone;
import codes.biscuit.skyblockaddons.core.DungeonPlayer;
import codes.biscuit.skyblockaddons.core.EssenceType;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter @Setter
public class DungeonUtils {

    private static final Pattern PATTERN_MILESTONE = Pattern.compile("^.+?(Healer|Tank|Mage|Archer|Berserk)\\sMilestone\\s.+?([❶-❿]).+?(§r§.+[0-9]+)\\s.+?");
    private static final Pattern PATTERN_COLLECTED_ESSENCES = Pattern.compile("§.+?([0-9]+)\\s(Wither|Spider|Undead|Dragon|Gold|Diamond|Ice)\\sEssence");

    /** The last dungeon server the player played on */
    private String lastServerId;

    /** The latest milestone the player received during a dungeon game */
    private DungeonMilestone dungeonMilestone;

    /** The latest essences the player collected during a dungeon game */
    private Map<EssenceType, Integer> collectedEssences = new EnumMap<>(EssenceType.class);

    /** The current teammates of the dungeon game */
    private Map<String, DungeonPlayer> players = new HashMap<>();

    /**
     * Clear the dungeon game data. Called by {@link codes.biscuit.skyblockaddons.utils.Utils} each new game
     */
    public void reset() {
        dungeonMilestone = null;
        collectedEssences.clear();
        players.clear();
    }

    public DungeonMilestone parseMilestone(String message) {
        Matcher matcher = PATTERN_MILESTONE.matcher(message);
        if (!matcher.lookingAt()) {
            return null;
        }

        DungeonClass dungeonClass = DungeonClass.fromDisplayName(matcher.group(1));
        return new DungeonMilestone(dungeonClass, matcher.group(2), matcher.group(3));

    }

    public void parseEssence(String message) {
        Matcher matcher = PATTERN_COLLECTED_ESSENCES.matcher(message);

        while (matcher.find()) {

            int amount = Integer.parseInt(matcher.group(1));
            EssenceType essenceType = EssenceType.fromName(matcher.group(2));

            if (essenceType != null) {
                collectedEssences.put(essenceType, collectedEssences.getOrDefault(essenceType, 0) + amount);
            }
        }
    }
}
