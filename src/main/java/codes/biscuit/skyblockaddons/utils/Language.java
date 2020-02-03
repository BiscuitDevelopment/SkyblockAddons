package codes.biscuit.skyblockaddons.utils;

import lombok.Getter;

@Getter
public enum Language {

    // listed by popularity
    ENGLISH("en_us"),
    CHINESE_TRADITIONAL("zh_TW"),
    CHINESE_SIMPLIFIED("zh_CN"),
    HINDI("hi_IN"),
    SPANISH_MEXICO("es_MX"),
    SPANISH_SPAIN("es_ES"),
    FRENCH("fr_FR"),
    ARABIC("ar_SA"),
    RUSSIAN("ru_RU"),
    PORTUGUESE_BRAZIL("pt_BR"),
    PORTUGUESE_PORTUGAL("pt_PT"),
    GERMAN_GERMANY("de_DE"),
    JAPANESE("ja_JP"),
    TURKISH("tr_TR"),
    KOREAN("ko_KR"),
    VIETNAMESE("vi_VN"),
    ITALIAN("it_IT"),
    THAI("th_TH"),
    FILIPINO("fil_PH"),

    //rest listed alphabetically
    BULGARIAN("bg_BG"),
    CROATIAN("hr_HR"),
    CZECH("cs_CZ"),
    DANISH("da_DK"),
    DUTCH_NETHERLANDS("nl_NL"),
    FINNISH("fi_FI"),
    HEBREW("he_IL"),
    HUNGARIAN("hu_HU"),
    IRISH("ga_IE"),
    LITHUANIAN("lt_LT"),
    NORWEGIAN("no_NO"),
    POLISH("pl_PL"),
    ROMANIAN("ro_RO"),
    SERBIAN_LATIN("sr_CS"),
    SWEDISH("sv_SE"),
    PIRATE_ENGLISH("en_PT"),
    BISCUITISH("bc_BC"),
    OWO("ow_Wo");

    private String path;

    Language(String path) {
        this.path = path;
    }

    /** Resources in later Minecraft versions can only use lowercase paths, so all of the flags are lowercase. */
    public String getFlagPath() {
        return path.toLowerCase();
    }

    /**
     * Find the corresponding {@link Language} to a given key string like {@code en_US}.
     * Case-insensitive.
     *
     * @param languageKey The lanugage key to look for.
     * @return The language if one was found, or null.
     */
    public static Language getFromPath(String languageKey) {
        for (Language language : values()) {
            String path = language.getPath();
            if (path != null && path.equalsIgnoreCase(languageKey)) {
                return language;
            }
        }
        return null;
    }
}
