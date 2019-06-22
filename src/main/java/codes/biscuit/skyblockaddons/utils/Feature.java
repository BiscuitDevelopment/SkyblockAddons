package codes.biscuit.skyblockaddons.utils;

public enum Feature {

    MAGMA_WARNING(0, ButtonType.REGULAR),
    DROP_CONFIRMATION(1, ButtonType.REGULAR),
    DISABLE_EMBER_ROD(2, ButtonType.REGULAR),

    WARNING_COLOR(-1, ButtonType.COLOR),
    CONFIRMATION_COLOR(-1, ButtonType.COLOR),

    WARNING_TIME(-1, ButtonType.NEUTRAL),

    ADD(-1, ButtonType.MODIFY),
    SUBTRACT(-1, ButtonType.MODIFY);

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
        MODIFY
    }

    public ButtonType getButtonType() {
        return buttonType;
    }
}
