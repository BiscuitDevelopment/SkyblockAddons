package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;

public enum Feature {

    MAGMA_WARNING(0, ButtonType.REGULAR),
    DROP_CONFIRMATION(1, ButtonType.REGULAR),
    DISABLE_EMBER_ROD(2, ButtonType.REGULAR),
    MANA_BAR(3, ButtonType.REGULAR),
    HIDE_BONES(4, ButtonType.REGULAR),
    SKELETON_BAR(5, ButtonType.REGULAR),
    HIDE_FOOD_ARMOR_BAR(6, ButtonType.REGULAR),
    FULL_INVENTORY_WARNING(7, ButtonType.REGULAR),
    MAGMA_BOSS_BAR(8, ButtonType.REGULAR),
    HIDE_DURABILITY(9, ButtonType.REGULAR),
    SHOW_ENCHANTMENTS_REFORGES(10, ButtonType.REGULAR),
    MINION_STOP_WARNING(11, ButtonType.REGULAR),

    WARNING_COLOR(-1, ButtonType.COLOR),
    CONFIRMATION_COLOR(-1, ButtonType.COLOR),
    MANA_TEXT_COLOR(-1, ButtonType.COLOR),
    MANA_BAR_COLOR(-1, ButtonType.COLOR),

    WARNING_TIME(-1, ButtonType.NEUTRAL),
    MANA_TEXT(-1, ButtonType.REGULAR),

    ADD(-1, ButtonType.MODIFY),
    SUBTRACT(-1, ButtonType.MODIFY),

    LANGUAGE(-1, ButtonType.SOLID),
    EDIT_LOCATIONS(-1, ButtonType.SOLID),
    SETTINGS(-1, ButtonType.SOLID),
    RESET_LOCATION(-1, ButtonType.SOLID);

    private int id;
    private ButtonType buttonType;

    Feature(int id, ButtonType buttonType) {
        this.id = id;
        this.buttonType = buttonType;
    }

    public int getId() {
        return id;
    }

    public static Feature fromId(int id) {
        for (Feature feature : values()) {
            if (feature.getId() == id) {
                return feature;
            }
        }
        return null;
    }

    public enum ButtonType {
        REGULAR,
        COLOR,
        NEUTRAL,
        MODIFY,
        SOLID
    }

    public enum ManaBarType {
        BAR_TEXT,
        TEXT,
        BAR,
        OFF;

        public String getDisplayText() {
            switch (this) {
                case BAR_TEXT:
                    return SkyblockAddons.INSTANCE.getConfigValues().getMessage(ConfigValues.Message.MANA_BAR_TYPE_BAR_TEXT);
                case TEXT:
                    return SkyblockAddons.INSTANCE.getConfigValues().getMessage(ConfigValues.Message.MANA_BAR_TYPE_TEXT);
                case BAR:
                    return SkyblockAddons.INSTANCE.getConfigValues().getMessage(ConfigValues.Message.MANA_BAR_TYPE_BAR);
                default:
                    return SkyblockAddons.INSTANCE.getConfigValues().getMessage(ConfigValues.Message.MANA_BAR_TYPE_OFF);
            }
        }

        public ManaBarType getNextType() {
            int nextType = ordinal()+1;
            if (nextType > values().length-1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    public enum Language {
        DUTCH_NETHERLANDS("nl_NL"),
        ENGLISH("en_US"),
        FRENCH("fr_FR"),
        GERMAN_GERMANY("de_DE"),
        JAPANESE("ja_JP"),
        POLISH("nl_NL"),
        RUSSIAN("ru_RU"),
        SPANISH_MEXICO("es_MX"),
        SPANISH_SPAIN("es_ES");

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

    public ButtonType getButtonType() {
        return buttonType;
    }
}
