package codes.biscuit.skyblockaddons.core;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

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

    private HashSet<String> legendarySeaCreatures;

    private HashSet<Pattern> hypixelBrands;

    private HashSet<Pattern> hypixelDomains;

    @Getter
    public static class DropSettings {

        private ItemRarity minimumInventoryRarity;
        private ItemRarity minimumHotbarRarity;

        private List<String> dontDropTheseItems;

        private List<String> allowDroppingTheseItems;
    }
}
