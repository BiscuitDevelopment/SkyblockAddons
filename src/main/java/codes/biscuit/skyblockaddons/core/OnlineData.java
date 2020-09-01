package codes.biscuit.skyblockaddons.core;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class OnlineData {

    private String bannerImageURL;
    private String bannerLink;
    private String videoLink;

    private String latestVersion;
    private String latestBeta;
    private String directDownload;

    private String languageJSONFormat;

    private Map<String, List<Integer>> disabledFeatures;

    private DropSettings dropSettings;

    private Map<String, DungeonData> dungeons;

    @Getter
    public static class DropSettings {

        private ItemRarity minimumInventoryRarity;
        private ItemRarity minimumHotbarRarity;

        private List<String> dontDropTheseItems;

        private List<String> allowDroppingTheseItems;
    }

    @Getter
    public static class DungeonData {
        private Map<Integer, List<String>> bosses;
        private Map<String, String> bossDisplayName;
    }
}
