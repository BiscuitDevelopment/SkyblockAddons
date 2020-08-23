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

public class DungeonUtils {

    private static final Pattern PATTERN_MILESTONE = Pattern.compile("^.+?(Healer|Tank|Mage|Archer|Berserk)\\sMilestone\\s.+?([❶-❿]).+?(§r§.+[0-9]+)\\s.+?");
    private static final Pattern PATTERN_COLLECTED_ESSENCES = Pattern.compile("§.+?([0-9]+)\\s(Wither|Spider|Undead|Dragon|Gold|Diamond|Ice)\\sEssence");
    private static final Pattern PATTERN_GAIN_ESSENCE = Pattern.compile("^§.+?[^You]\\s.+?found\\sa\\s.+?(Wither|Spider|Undead|Dragon|Gold|Diamond|Ice)\\sEssence.+?");

    /** The last dungeon server the player played on */
    @Getter @Setter private String lastServerId;

    /** The latest milestone the player received during a dungeon game */
    @Getter @Setter private DungeonMilestone dungeonMilestone;

    /** The latest essences the player collected during a dungeon game */
    @Getter @Setter private Map<EssenceType, Integer> collectedEssences = new EnumMap<>(EssenceType.class);

    /** The current teammates of the dungeon game */
    @Getter @Setter private Map<String, DungeonPlayer> players = new HashMap<>();

    private EssenceType lastEssenceType;
    private int lastEssenceAmount;
    private int lastEssenceRepeat;

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

    public void parseEssence(String message, boolean gain) {
        Matcher matcher;
        if (gain) {
            matcher = PATTERN_GAIN_ESSENCE.matcher(message);

            if (matcher.matches()) {
                EssenceType essenceType = EssenceType.fromName(matcher.group(1));

                collectedEssences.put(essenceType, collectedEssences.getOrDefault(essenceType, 0) + 1);
            }
            return;
        }
        matcher = PATTERN_COLLECTED_ESSENCES.matcher(message);

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
}
