package codes.biscuit.skyblockaddons.utils;

public enum Message {
    LANGUAGE(MessageObject.ROOT, "language"),

    SETTING_MAGMA_BOSS_WARNING(MessageObject.SETTING, "magmaBossWarning"),
    SETTING_ITEM_DROP_CONFIRMATION(MessageObject.SETTING, "itemDropConfirmation"),
    SETTING_WARNING_TIME(MessageObject.SETTING, "warningTime"),
    SETTING_MANA_BAR(MessageObject.SETTING, "manaBar"),
    SETTING_HIDE_SKELETON_HAT_BONES(MessageObject.SETTING, "hideSkeletonHatBones"),
    SETTING_SKELETON_HAT_BONES_BAR(MessageObject.SETTING, "skeletonHatBonesBar"),
    SETTING_HIDE_FOOD_AND_ARMOR(MessageObject.SETTING, "hideFoodAndArmor"),
    SETTING_FULL_INVENTORY_WARNING(MessageObject.SETTING, "fullInventoryWarning"),
    SETTING_MAGMA_BOSS_HEALTH_BAR(MessageObject.SETTING, "magmaBossHealthBar"),
    SETTING_DISABLE_EMBER_ROD_ABILITY(MessageObject.SETTING, "disableEmberRodAbility"),
    SETTING_WARNING_COLOR(MessageObject.SETTING, "warningColor"),
    SETTING_CONFIRMATION_COLOR(MessageObject.SETTING, "confirmationColor"),
    SETTING_MANA_TEXT_COLOR(MessageObject.SETTING, "manaTextColor"),
    SETTING_MANA_BAR_COLOR(MessageObject.SETTING, "manaBarColor"),
    SETTING_EDIT_LOCATIONS(MessageObject.SETTING, "editLocations"),
    SETTING_GUI_SCALE(MessageObject.SETTING, "guiScale"),
    SETTING_RESET_LOCATIONS(MessageObject.SETTING, "resetLocations"),
    SETTING_SETTINGS(MessageObject.SETTING, "settings"),
    SETTING_EDIT_SETTINGS(MessageObject.SETTING, "openSettings"),
    SETTING_HIDE_DURABILITY(MessageObject.SETTING, "hideDurability"),
    SETTING_ENCHANTS_AND_REFORGES(MessageObject.SETTING, "showEnchantmentsReforges"),
    SETTING_MINION_STOP_WARNING(MessageObject.SETTING, "minionStopWarning"),
    SETTING_AUCTION_HOUSE_PLAYERS(MessageObject.SETTING, "hideAuctionHousePlayers"),
    SETTING_HEALTH_BAR(MessageObject.SETTING, "healthBar"),
    SETTING_DEFENCE_ICON(MessageObject.SETTING, "defenceIcon"),
    SETTING_BACKPACK_STYLE(MessageObject.SETTING, "backpackStyle"),
    SETTING_SHOW_BACKPACK_PREVIEW(MessageObject.SETTING, "showBackpackPreview"),
    SETTING_HIDE_HEALTH_VAR(MessageObject.SETTING, "hideHealthBar"),
    SETTING_HEALTH_TEXT_COLOR(MessageObject.SETTING, "healthTextColor"),
    SETTING_HEALTH_BAR_COLOR(MessageObject.SETTING, "healthBarColor"),
    SETTING_DEFENCE_TEXT_COLOR(MessageObject.SETTING, "defenceTextColor"),
    SETTING_DEFENCE_PERCENTAGE_COLOR(MessageObject.SETTING, "defencePercentageColor"),
    SETTING_DISABLE_DOUBLE_DROP(MessageObject.SETTING, "disableDoubleDropAutomatically"),
    SETTING_FULL_MINION(MessageObject.SETTING, "fullMinionWarning"),
    SETTING_NEXT_PAGE(MessageObject.SETTING, "nextPage"),
    SETTING_PREVIOUS_PAGE(MessageObject.SETTING, "previousPage"),
    SETTING_IGNORE_ITEM_FRAME_CLICKS(MessageObject.SETTING, "ignoreItemFrameClicks"),
    SETTING_USE_VANILLA_TEXTURE_DEFENCE(MessageObject.SETTING, "useVanillaTextureDefence"),
    SETTING_SHOW_BACKPACK_HOLDING_SHIFT(MessageObject.SETTING, "showBackpackHoldingShift"),

    STYLE_GUI(MessageObject.STYLE, "inventory"),
    STYLE_COMPACT(MessageObject.STYLE, "compact"),

    BAR_TYPE_BAR_TEXT(MessageObject.BAR_TYPE, "barAndText"),
    BAR_TYPE_BAR(MessageObject.BAR_TYPE, "bar"),
    BAR_TYPE_TEXT(MessageObject.BAR_TYPE, "text"),
    BAR_TYPE_OFF(MessageObject.BAR_TYPE, "off"),

    ICON_TYPE_ICON_DEFENCE_PERCENTAGE(MessageObject.ICON_TYPE, "iconDefenceAndPercentage"),
    ICON_TYPE_ICON_DEFENCE(MessageObject.ICON_TYPE, "iconAndDefence"),
    ICON_TYPE_ICON_PERCENTAGE(MessageObject.ICON_TYPE, "iconAndPercentage"),
    ICON_TYPE_DEFENCE_PERCENTAGE(MessageObject.ICON_TYPE, "defenceAndPercentage"),
    ICON_TYPE_DEFENCE(MessageObject.ICON_TYPE, "defence"),
    ICON_TYPE_PERCENTAGE(MessageObject.ICON_TYPE, "percentage"),
    ICON_TYPE_ICON(MessageObject.ICON_TYPE, "icon"),
    ICON_TYPE_OFF(MessageObject.ICON_TYPE, "off"),

    MESSAGE_DROP_CONFIRMATION(MessageObject.MESSAGES, "dropConfirmation"),
    MESSAGE_MAGMA_BOSS_WARNING(MessageObject.MESSAGES, "magmaBossWarning"),
    MESSAGE_FULL_INVENTORY(MessageObject.MESSAGES, "fullInventory"),
    MESSAGE_NEW_VERSION(MessageObject.MESSAGES, "newVersion"),
    MESSAGE_LABYMOD(MessageObject.MESSAGES, "labymod"),
    MESSAGE_DISCORD(MessageObject.MESSAGES, "discord"),
    MESSAGE_MINION_CANNOT_REACH(MessageObject.MESSAGES, "minionCannotReach"),
    MESSAGE_MINION_IS_FULL(MessageObject.MESSAGES, "minionIsFull"),
    MESSAGE_DEVELOPMENT_VERSION(MessageObject.MESSAGES, "developmentVersion");


    private MessageObject messageObject;
    private String memberName;

    Message(MessageObject messageObject, String memberName) {
        this.messageObject = messageObject;
        this.memberName = memberName;
    }

    public MessageObject getMessageObject() {
        return messageObject;
    }

    public String getMemberName() {
        return memberName;
    }

    enum MessageObject {
        ROOT,
        SETTING,
        BAR_TYPE,
        MESSAGES,
        STYLE,
        ICON_TYPE
    }

}