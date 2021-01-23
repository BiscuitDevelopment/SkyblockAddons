package codes.biscuit.skyblockaddons.features.spookyevent;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Feature Rewrite
public class SpookyEventManager {

    private static final Pattern CANDY_PATTERN = Pattern.compile("Your Candy: (?<greenCandy>\\d+) Green, (?<purpleCandy>\\d+) Purple \\((?<points>\\d+) pts\\.\\)");
    @Getter private static final Map<CandyType, Integer> dummyCandyCounts = new HashMap<>();
    static {
        dummyCandyCounts.put(CandyType.GREEN, 12);
        dummyCandyCounts.put(CandyType.PURPLE, 34);
    }

    @Getter private static Map<CandyType, Integer> candyCounts = new HashMap<>();
    @Getter private static int points;
    static {
        reset();
    }

    public static void reset() {
        for (CandyType candyType : CandyType.values()) {
            candyCounts.put(candyType, 0);
        }
        points = 0;
    }

    public static boolean isActive() {
        return SpookyEventManager.getCandyCounts().get(CandyType.GREEN) != 0 || SpookyEventManager.getCandyCounts().get(CandyType.PURPLE) != 0;
    }

    public static void update(String strippedTabFooterString) {
        if (strippedTabFooterString == null) {
            reset();
            return;
        }

        try {
            Matcher matcher = CANDY_PATTERN.matcher(strippedTabFooterString);
            if (matcher.find()) {
                candyCounts.put(CandyType.GREEN, Integer.valueOf(matcher.group("greenCandy")));
                candyCounts.put(CandyType.PURPLE, Integer.valueOf(matcher.group("purpleCandy")));
                points = Integer.parseInt(matcher.group("points"));
            }
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("An error occurred while parsing the spooky event event text in the tab list!", ex);
        }
    }
}
