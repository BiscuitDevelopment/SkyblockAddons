package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.constants.game.Rarity;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class OnlineData {

    private String bannerImageURL;
    private String bannerLink;
    private String videoLink;
    private String directDownload;

    private String updateJSON;
    private String languageJSONFormat;

    private Map<String, List<Integer>> disabledFeatures;

    private DropSettings dropSettings;

    @Getter
    public static class DropSettings {

        private Rarity minimumInventoryRarity;
        private Rarity minimumHotbarRarity;

        private List<String> dontDropTheseItems;

        private List<String> allowDroppingTheseItems;
    }
}
