package codes.biscuit.skyblockaddons.utils;

public enum Feature {

    MAGMA_WARNING(0, ButtonType.REGULAR),
    DROP_CONFIRMATION(1, ButtonType.REGULAR),
    DISABLE_EMBER_ROD(2, ButtonType.REGULAR),
    MANA_BAR(3, ButtonType.REGULAR),
    BONES(4, ButtonType.REGULAR),
    SKELETON_BAR(5, ButtonType.REGULAR),
    HIDE_FOOD_ARMOR_BAR(6, ButtonType.REGULAR),

    WARNING_COLOR(-1, ButtonType.COLOR),
    CONFIRMATION_COLOR(-1, ButtonType.COLOR),
    MANA_TEXT_COLOR(-1, ButtonType.COLOR),
    MANA_BAR_COLOR(-1, ButtonType.COLOR),

    WARNING_TIME(-1, ButtonType.NEUTRAL),

    ADD(-1, ButtonType.MODIFY),
    SUBTRACT(-1, ButtonType.MODIFY),

    EDIT_LOCATIONS(-1, ButtonType.SOLID),
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
        BAR_TEXT("Bar & Text"),
        TEXT("Text"),
        BAR("Bar"),
        OFF("Off");

        private String displayText;

        ManaBarType(String displayText) {
            this.displayText = displayText;
        }

        public String getDisplayText() {
            return displayText;
        }

        public ManaBarType getNextType() {
            int nextType = ordinal()+1;
            if (nextType > values().length-1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    public ButtonType getButtonType() {
        return buttonType;
    }
}
