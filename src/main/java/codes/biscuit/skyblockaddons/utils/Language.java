package codes.biscuit.skyblockaddons.utils;

public enum Language {

    ARABIC("ar_SA"),
    BULGARIAN("bg_BG"),
    CHINESE_TRADITIONAL("zh_TW"),
    CHINESE_SIMPLIFIED("zh_CN"),
    CROATIAN("hr_HR"),
    CZECH("cs_CZ"),
    DUTCH_NETHERLANDS("nl_NL"),
    ENGLISH("en_us"),
    FILIPINO("fil_PH"),
    FINNISH("fi_FI"),
    FRENCH("fr_FR"),
    GERMAN_GERMANY("de_DE"),
    HEBREW("he_IL"),
    HUNGARIAN("hu_HU"),
    ITALIAN("it_IT"),
    JAPANESE("ja_JP"),
    PIRATE_ENGLISH("en_PT"),
    POLISH("pl_PL"),
    PORTUGUESE_BRAZIL("pt_BR"),
    PORTUGUESE_PORTUGAL("pt_PT"),
    ROMANIAN("ro_RO"),
    RUSSIAN("ru_RU"),
    SPANISH_MEXICO("es_MX"),
    SPANISH_SPAIN("es_ES"),
    SWEDISH("sv_SE"),
    TURKISH("tr_TR"),
    THAI("th_TH"),
    VIETNAMESE("vi_VN");

    private String path;

    Language(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Language getNextLanguage() {
        int nextType = ordinal()+1;
        if (nextType > values().length-1) {
            nextType = 0;
        }
        return values()[nextType];
    }

    public static Language getFromPath(String text) {
        for (Language language : values()) {
            String path = language.getPath();
            if (path != null && path.equals(text)) {
                return language;
            }
        }
        return null;
    }
}
