package codes.biscuit.skyblockaddons.features.spookyevent;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Feature Rewrite
public class SpookyEventManager {

    private static final Logger logger = SkyblockAddons.getLogger();

    private static final Pattern CANDY_PATTERN = Pattern.compile("Your Candy: (?<greenCandy>\\d+) Green, (?<purpleCandy>\\d+) Purple \\((?<points>\\d+) pts\\.\\)");
    @Getter private static final Map<CandyType, Integer> dummyCandyCounts = new HashMap<>();
    static {
        dummyCandyCounts.put(CandyType.GREEN, 12);
        dummyCandyCounts.put(CandyType.PURPLE, 34);
    }

    @Getter
    private static final Map<CandyType, Integer> candyCounts = new HashMap<>();
    @Getter
    private static int points;
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
            logger.error("An error occurred while parsing the spooky event event text in the tab list!", ex);
        }
    }

    /**
     * Temp function until feature re-write
     *
     * @param green
     * @param purple
     * @param pts
     */
    public static void update(int green, int purple, int pts) {
        candyCounts.put(CandyType.GREEN, green);
        candyCounts.put(CandyType.PURPLE, purple);
        points = pts;
    }
}
