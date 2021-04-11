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

    /**
     * This is the list of features in the mod that should be disabled. Features in this list will be disabled for all
     * versions of the mod v1.5.5 and above. The first key in this map is "all". It contains a list of features to be disabled
     * in all mod versions. Version numbers can be added as additional keys to disable features in specific mod versions.
     * An example of this is shown below:
     * <br><br>
     * {@code "1.5.5": [3]}
     * <br><br>
     * Versions must follow the semver format (e.g. {@code 1.6.0}) and cannot be pre-release versions (e.g. {@code 1.6.0-beta.10}).
     * Pre-release versions of the mod adhere to the disabled features list of their release version. For example, the version
     * {@code 1.6.0-beta.10} will adhere to the list with the key {@code 1.6.0}. Disabling features for unique pre-release
     * versions is not supported.
     */
    private HashMap<String, List<Integer>> disabledFeatures;

    private DropSettings dropSettings;

    private HashMap<String, Integer> specialEnchantments;

    private HashSet<String> legendarySeaCreatures;

    private HashSet<Pattern> hypixelBrands;

    @Getter
    public static class DropSettings {

        private ItemRarity minimumInventoryRarity;
        private ItemRarity minimumHotbarRarity;

        private List<String> dontDropTheseItems;

        private List<String> allowDroppingTheseItems;
    }
}
