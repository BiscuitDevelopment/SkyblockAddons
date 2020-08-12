package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.DungeonClass;
import codes.biscuit.skyblockaddons.core.DungeonPlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter @Setter
public class DungeonUtils {
    private static final Pattern PATTERN_MILESTONE = Pattern.compile("^.+?(Healer|Tank|Mage|Archer|Berserk)?\\sMilestone\\s.+?([❶-❿]).+?(§r§.+[0-9]+)\\s.+?");

    private DungeonPlayer.Milestone milestone;

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
}
