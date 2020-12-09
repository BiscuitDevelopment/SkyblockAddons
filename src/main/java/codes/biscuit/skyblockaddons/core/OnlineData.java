package codes.biscuit.skyblockaddons.core;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;

@Getter
public class OnlineData {

    private String bannerImageURL;
    private String bannerLink;
    private String videoLink;

    private String latestVersion;
    private String latestBeta;
    private String directDownload;

    private String languageJSONFormat;

    private HashMap<String, List<Integer>> disabledFeatures;

    private DropSettings dropSettings;

    private HashMap<String, Integer> specialEnchantments;

    @Getter
    public static class DropSettings {

        private ItemRarity minimumInventoryRarity;
        private ItemRarity minimumHotbarRarity;

        private List<String> dontDropTheseItems;

        private List<String> allowDroppingTheseItems;
    }
}
