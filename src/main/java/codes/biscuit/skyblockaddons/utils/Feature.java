package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;

import static codes.biscuit.skyblockaddons.utils.ConfigValues.Message.*;

public enum Feature {

    MAGMA_WARNING(0, ButtonType.REGULAR),
    DROP_CONFIRMATION(1, ButtonType.REGULAR),
    DISABLE_EMBER_ROD(2, ButtonType.REGULAR),
    SHOW_BACKPACK_PREVIEW(3, ButtonType.REGULAR),
    HIDE_BONES(4, ButtonType.REGULAR),
    SKELETON_BAR(5, ButtonType.REGULAR),
    HIDE_FOOD_ARMOR_BAR(6, ButtonType.REGULAR),
    FULL_INVENTORY_WARNING(7, ButtonType.REGULAR),
    MAGMA_BOSS_BAR(8, ButtonType.REGULAR),
    HIDE_DURABILITY(9, ButtonType.REGULAR),
    SHOW_ENCHANTMENTS_REFORGES(10, ButtonType.REGULAR),
    MINION_STOP_WARNING(11, ButtonType.REGULAR),
    HIDE_AUCTION_HOUSE_PLAYERS(12, ButtonType.REGULAR),
    HIDE_HEALTH_BAR(13, ButtonType.REGULAR),
    DISABLE_DOUBLE_DROP_AUTOMATICALLY(14, ButtonType.REGULAR),
    MINION_FULL_WARNING(15, ButtonType.REGULAR),
    IGNORE_ITEM_FRAME_CLICKS(16, ButtonType.REGULAR),

    MANA_BAR(-1, ButtonType.REGULAR),
    MANA_TEXT(-1, ButtonType.REGULAR),
    HEALTH_BAR(-1, ButtonType.REGULAR),
    HEALTH_TEXT(-1, ButtonType.REGULAR),
    DEFENCE_ICON(-1, ButtonType.REGULAR),
    DEFENCE_TEXT(-1, ButtonType.REGULAR),
    DEFENCE_PERCENTAGE(-1, ButtonType.REGULAR),

    WARNING_COLOR(-1, ButtonType.COLOR),
    CONFIRMATION_COLOR(-1, ButtonType.COLOR),
    MANA_TEXT_COLOR(-1, ButtonType.COLOR),
    MANA_BAR_COLOR(-1, ButtonType.COLOR),
    HEALTH_BAR_COLOR(-1, ButtonType.COLOR),
    HEALTH_TEXT_COLOR(-1, ButtonType.COLOR),
    DEFENCE_TEXT_COLOR(-1, ButtonType.COLOR),
    DEFENCE_PERCENTAGE_COLOR(-1, ButtonType.COLOR),

    WARNING_TIME(-1, ButtonType.NEUTRAL),

    ADD(-1, ButtonType.MODIFY),
    SUBTRACT(-1, ButtonType.MODIFY),

    LANGUAGE(-1, ButtonType.SOLID),
    EDIT_LOCATIONS(-1, ButtonType.SOLID),
    SETTINGS(-1, ButtonType.SOLID),
    RESET_LOCATION(-1, ButtonType.SOLID),
    BACKPACK_STYLE(-1, ButtonType.SOLID),
    NEXT_PAGE(-1, ButtonType.SOLID),
    PREVIOUS_PAGE(-1, ButtonType.SOLID);

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

    public ButtonType getButtonType() {
        return buttonType;
    }

    public enum ButtonType {
        REGULAR,
        COLOR,
        NEUTRAL,
        MODIFY,
        SOLID
    }

    public enum BarType {
        BAR_TEXT(BAR_TYPE_BAR_TEXT),
        TEXT(BAR_TYPE_TEXT),
        BAR(BAR_TYPE_BAR),
        OFF(BAR_TYPE_OFF);

        private ConfigValues.Message message;

        BarType(ConfigValues.Message message) {
            this.message = message;
        }

        public String getDisplayText() {
            return SkyblockAddons.getInstance().getConfigValues().getMessage(message);
        }

        public BarType getNextType() {
            int nextType = ordinal()+1;
            if (nextType > values().length-1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    public enum IconType {
        ICON_DEFENCE_PERCENTAGE(ICON_TYPE_ICON_DEFENCE_PERCENTAGE),

        ICON_DEFENCE(ICON_TYPE_ICON_DEFENCE),
        ICON_PERCENTAGE(ICON_TYPE_ICON_PERCENTAGE),
        DEFENCE_PERCENTAGE(ICON_TYPE_DEFENCE_PERCENTAGE),

        ICON(ICON_TYPE_ICON),
        DEFENCE(ICON_TYPE_DEFENCE),
        PERCENTAGE(ICON_TYPE_PERCENTAGE),
        OFF(ICON_TYPE_OFF);

        private ConfigValues.Message message;

        IconType(ConfigValues.Message message) {
            this.message = message;
        }

        public String getDisplayText() {
            return SkyblockAddons.getInstance().getConfigValues().getMessage(message);
        }

        public IconType getNextType() {
            int nextType = ordinal()+1;
            if (nextType > values().length-1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

//    public enum Accuracy {
//        RIVERS,
//        BLAZE,
//        MAGMA_CUBES,
//        MUSIC_DISC,
//        SPAWNED,
//    }

    public enum InventoryType {
        ENCHANTMENT_TABLE,
        REFORGE_ANVIL
    }

    public enum Backpack {
        SMALL("Small Backpack"),
        MEDIUM("Medium Backpack"),
        LARGE("Large Backpack");

        private String itemName;

        Backpack(String itemName) {
            this.itemName = itemName;
        }

        public String getItemName() {
            return itemName;
        }
    }

    public enum BackpackStyle {
        GUI(STYLE_GUI),
        BOX(STYLE_COMPACT);

        private ConfigValues.Message message;

        BackpackStyle(ConfigValues.Message message) {
            this.message = message;
        }

        public String getDisplayText() {
            return SkyblockAddons.getInstance().getConfigValues().getMessage(message);
        }

        public BackpackStyle getNextType() {
            int nextType = ordinal()+1;
            if (nextType > values().length-1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    public enum Location {
        ISLAND("Your Island"),
        BLAZING_FORTRESS("Blazing Fortress"),
        VILLAGE("Village"),
        AUCTION_HOUSE("Auction House");

        private String scoreboardName;

        Location(String scoreboardName) {
            this.scoreboardName = scoreboardName;
        }

        public String getScoreboardName() {
            return scoreboardName;
        }
    }
}
