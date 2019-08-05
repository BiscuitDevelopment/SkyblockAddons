package codes.biscuit.skyblockaddons.utils;

public enum Language {

    ARABIC("ar_sa"),
    CHINESE_TRADITIONAL("zh_tw"),
    CHINESE_SIMPLIFIED("zh_cn"),
    DUTCH_NETHERLANDS("nl_nl"),
    ENGLISH("en_us"),
    FINNISH("fi_fi"),
    FRENCH("fr_fr"),
    GERMAN_GERMANY("de_de"),
    HEBREW("he_il"),
    HUNGARIAN("hu_hu"),
    JAPANESE("ja_jp"),
    POLISH("pl_pl"),
    PORTUGUESE("pt_br"),
    ROMANIAN("ro_ro"),
    RUSSIAN("ru_ru"),
    SPANISH_MEXICO("es_mx"),
    SPANISH_SPAIN("es_es");

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
