package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.DungeonClass;
import codes.biscuit.skyblockaddons.core.DungeonPlayer;
import lombok.Getter;
import lombok.Setter;

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
    private DungeonPlayer.Milestone milestone;

    /** The latest essences the player collected during a dungeon game */
    private DungeonPlayer.CollectedEssences collectedEssences;

    /** The current teammates of the dungeon game */
    private Map<String, DungeonPlayer> players = new HashMap<>();

    /**
     * Clear the dungeon game data. Called by {@link codes.biscuit.skyblockaddons.utils.Utils} each new game
     */
    public void reset() {
        milestone = null;
        collectedEssences.reset();
        players.clear();
    }

    public DungeonPlayer.Milestone parseMilestone(String message) {
        Matcher matcher = PATTERN_MILESTONE.matcher(message);
        if (!matcher.lookingAt()) {
            return null;
        }

        DungeonPlayer.Milestone milestone = new DungeonPlayer.Milestone();
        String name = matcher.group(1).toUpperCase();
        if (name.equals("BERSERK"))
            name += "ER";
        milestone.setDungeonClass(DungeonClass.valueOf(name));
        milestone.setLevel(matcher.group(2));
        milestone.setValue(matcher.group(3));
        return milestone;
    }

    public void parseEssence(String message) {
        Matcher matcher = PATTERN_COLLECTED_ESSENCES.matcher(message);
        if (collectedEssences == null) {
            collectedEssences = DungeonPlayer.CollectedEssences.empty();
        }

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String type = matcher.group(2);

            switch (type) {
                case "Wither":
                    collectedEssences.addWither(value);
                    break;
                case "Spider":
                    collectedEssences.addSpider(value);
                    break;
                case "Undead":
                    collectedEssences.addUndead(value);
                    break;
                case "Dragon":
                    collectedEssences.addDragon(value);
                    break;
                case "Gold":
                    collectedEssences.addGold(value);
                    break;
                case "Diamond":
                    collectedEssences.addDiamond(value);
                    break;
                case "Ice":
                    collectedEssences.addIce(value);
                    break;
                default:
                    break;
            }
        }
    }
}
