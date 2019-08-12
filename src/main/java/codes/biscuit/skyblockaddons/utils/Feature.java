package codes.biscuit.skyblockaddons.utils;

public enum Feature {

    MAGMA_WARNING(0),
    DROP_CONFIRMATION(1),
    DISABLE_EMBER_ROD(2),
    SHOW_BACKPACK_PREVIEW(3),
    HIDE_BONES(4),
    SKELETON_BAR(5),
    HIDE_FOOD_ARMOR_BAR(6),
    FULL_INVENTORY_WARNING(7),
    MAGMA_BOSS_BAR(8),
    HIDE_DURABILITY(9),
    SHOW_ENCHANTMENTS_REFORGES(10),
    MINION_STOP_WARNING(11),
    HIDE_AUCTION_HOUSE_PLAYERS(12),
    HIDE_HEALTH_BAR(13),
    MINION_FULL_WARNING(15),
    IGNORE_ITEM_FRAME_CLICKS(16),
    MANA_BAR(19),
    MANA_TEXT(20),
    HEALTH_BAR(21),
    HEALTH_TEXT(22),
    DEFENCE_ICON(23),
    DEFENCE_TEXT(24),
    DEFENCE_PERCENTAGE(25),
    HEALTH_UPDATES(26), // Health updates all credit to DidiSkywalker#9975

    DISABLE_DOUBLE_DROP_AUTOMATICALLY(14),
    USE_VANILLA_TEXTURE_DEFENCE(17),
    SHOW_BACKPACK_HOLDING_SHIFT(18),

    WARNING_TIME(-1),

    ADD(-1),
    SUBTRACT(-1),

    LANGUAGE(-1),
    EDIT_LOCATIONS(-1),
    SETTINGS(-1),
    RESET_LOCATION(-1),
    BACKPACK_STYLE(-1),
    NEXT_PAGE(-1),
    PREVIOUS_PAGE(-1);

    private int id;

    Feature(int id) {
        this.id = id;
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

    //    public enum Accuracy {
//        RIVERS,
//        BLAZE,
//        MAGMA_CUBES,
//        MUSIC_DISC,
//        SPAWNED,
//    }

}
