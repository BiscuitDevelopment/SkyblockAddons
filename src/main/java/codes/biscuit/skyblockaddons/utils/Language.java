package codes.biscuit.skyblockaddons.utils;

public enum Language {

    // listed by popularity
    ENGLISH("en_us"),
    CHINESE_TRADITIONAL("zh_tw"),
    CHINESE_SIMPLIFIED("zh_cn"),
    SPANISH_MEXICO("es_mx"),
    SPANISH_SPAIN("es_es"),
    FRENCH("fr_fr"),
    ARABIC("ar_sa"),
    RUSSIAN("ru_ru"),
    PORTUGUESE_BRAZIL("pt_br"),
    PORTUGUESE_PORTUGAL("pt_pt"),
    GERMAN_GERMANY("de_de"),
    JAPANESE("ja_jp"),
    TURKISH("tr_tr"),
    KOREAN("ko_kr"),
    VIETNAMESE("vi_vn"),
    ITALIAN("it_it"),
    THAI("th_th"),
    FILIPINO("fil_ph"),

    //rest listed alphabetically
    BULGARIAN("bg_bg"),
    CROATIAN("hr_hr"),
    CZECH("cs_cz"),
    DANISH("da_dk"),
    DUTCH_NETHERLANDS("nl_nl"),
    FINNISH("fi_fi"),
    HEBREW("he_il"),
    HUNGARIAN("hu_hu"),
    IRISH("ga_ir"),
    LITHUANIAN("lt_lt"),
    NORWEGIAN("no_no"),
    POLISH("pl_pl"),
    ROMANIAN("ro_ro"),
    SERBIAN_LATIN("sr_cs"),
    SWEDISH("sv_se"),
    PIRATE_ENGLISH("en_pt"),
    BISCUITISH("bc_bc");

    private String path;

    Language(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getFlagPath() {
        return path.toLowerCase();
    }

//    public Language getNextLanguage() {
//        int nextType = ordinal()+1;
//        if (nextType > values().length-1) {
//            nextType = 0;
//        }
//        return values()[nextType];
//    }

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
