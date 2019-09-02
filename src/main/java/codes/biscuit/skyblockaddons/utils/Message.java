package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("DeprecatedIsStillUsed")
public enum Message {
    LANGUAGE(MessageObject.ROOT, "language"),

    SETTING_MAGMA_BOSS_WARNING(MessageObject.SETTING, "magmaBossWarning"),
    SETTING_ITEM_DROP_CONFIRMATION(MessageObject.SETTING, "itemDropConfirmation"),
    SETTING_WARNING_TIME(MessageObject.SETTING, "warningTime"),
    SETTING_HIDE_SKELETON_HAT_BONES(MessageObject.SETTING, "hideSkeletonHatBones"),
    SETTING_SKELETON_HAT_BONES_BAR(MessageObject.SETTING, "skeletonHatBonesBar"),
    SETTING_HIDE_FOOD_AND_ARMOR(MessageObject.SETTING, "hideFoodAndArmor"),
    SETTING_FULL_INVENTORY_WARNING(MessageObject.SETTING, "fullInventoryWarning"),
    SETTING_MAGMA_BOSS_TIMER(MessageObject.SETTING, "magmaBossTimer"),
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
    SETTING_BACKPACK_STYLE(MessageObject.SETTING, "backpackStyle"),
    SETTING_SHOW_BACKPACK_PREVIEW(MessageObject.SETTING, "showBackpackPreview"),
    SETTING_HIDE_HEALTH_BAR(MessageObject.SETTING, "hideHealthBar"),
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
    SETTING_MANA_BAR(MessageObject.SETTING, "manaBar"),
    SETTING_HEALTH_BAR(MessageObject.SETTING, "healthBar"),
    SETTING_DEFENCE_ICON(MessageObject.SETTING, "defenceIcon"),
    SETTING_MANA_TEXT(MessageObject.SETTING, "manaText"),
    SETTING_HEALTH_TEXT(MessageObject.SETTING, "healthText"),
    SETTING_DEFENCE_TEXT(MessageObject.SETTING, "defenceText"),
    SETTING_DEFENCE_PERCENTAGE(MessageObject.SETTING, "defencePercentage"),
    SETTING_HEALTH_UPDATES(MessageObject.SETTING, "healthUpdates"),
    @Deprecated SETTING_ANCHOR_POINT(MessageObject.SETTING, "anchorPoint"),
    SETTING_HIDE_PLAYERS_IN_LOBBY(MessageObject.SETTING, "hidePlayersInLobby"),
    SETTING_TEXT_STYLE(MessageObject.SETTING, "textStyle"),
    SETTING_DARK_AUCTION_TIMER(MessageObject.SETTING, "darkAuctionTimer"),
    SETTING_ITEM_PICKUP_LOG(MessageObject.SETTING, "itemPickupLog"),
    SETTING_AVOID_PLACING_ENCHANTED_ITEMS(MessageObject.SETTING, "avoidPlacingEnchantedItems"),
    SETTING_AVOID_BREAKING_STEMS(MessageObject.SETTING, "avoidBreakingStems"),
    SETTING_MAGMA_BOSS_TIMER_COLOR(MessageObject.SETTING, "magmaBossTimerColor"),
    SETTING_DARK_AUCTION_TIMER_COLOR(MessageObject.SETTING, "darkAuctionTimerColor"),
    SETTING_STOP_BOW_CHARGE_FROM_RESETTING(MessageObject.SETTING, "stopBowChargeFromResetting"),
    SETTING_SHOW_MAGMA_TIMER_IN_OTHER_GAMES(MessageObject.SETTING, "showMagmaTimerInOtherGames"),
    SETTING_SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES(MessageObject.SETTING, "showDarkAuctionTimerInOtherGames"),
    SETTING_SHOW_ITEM_ANVIL_USES(MessageObject.SETTING, "showItemAnvilUses"),
    SETTING_PREVENT_MOVEMENT_ON_DEATH(MessageObject.SETTING, "preventMovementOnDeath"),

    BACKPACK_STYLE_GUI(MessageObject.STYLE, "inventory"),
    BACKPACK_STYLE_COMPACT(MessageObject.STYLE, "compact"),

    MESSAGE_DROP_CONFIRMATION(MessageObject.MESSAGES, "dropConfirmation"),
    MESSAGE_MAGMA_BOSS_WARNING(MessageObject.MESSAGES, "magmaBossWarning"),
    MESSAGE_FULL_INVENTORY(MessageObject.MESSAGES, "fullInventory"),
    MESSAGE_NEW_VERSION(MessageObject.MESSAGES, "newVersion"),
    MESSAGE_LABYMOD(MessageObject.MESSAGES, "labymod"),
    MESSAGE_DISCORD(MessageObject.MESSAGES, "discord"),
    MESSAGE_MINION_CANNOT_REACH(MessageObject.MESSAGES, "minionCannotReach"),
    MESSAGE_MINION_IS_FULL(MessageObject.MESSAGES, "minionIsFull"),
    MESSAGE_DEVELOPMENT_VERSION(MessageObject.MESSAGES, "developmentVersion"),
    MESSAGE_TYPE_ENCHANTMENTS(MessageObject.MESSAGES, "typeEnchantmentsHere"),
    MESSAGE_SEPARATE_ENCHANTMENTS(MessageObject.MESSAGES, "separateMultiple"),
    MESSAGE_ENCHANTS_TO_MATCH(MessageObject.MESSAGES, "enchantsToMatch"),
    MESSAGE_ENCHANTS_TO_EXCLUDE(MessageObject.MESSAGES, "enchantsToExclude"),
    MESSAGE_CANCELLED_STEM_BREAK(MessageObject.MESSAGES, "cancelledStemBreak"),

    @Deprecated ANCHOR_POINT_TOP_LEFT(MessageObject.ANCHOR_POINT, "topLeft"),
    @Deprecated ANCHOR_POINT_TOP_RIGHT(MessageObject.ANCHOR_POINT, "topRight"),
    @Deprecated ANCHOR_POINT_BOTTOM_LEFT(MessageObject.ANCHOR_POINT, "bottomLeft"),
    @Deprecated ANCHOR_POINT_BOTTOM_RIGHT(MessageObject.ANCHOR_POINT, "bottomRight"),
    @Deprecated ANCHOR_POINT_HEALTH_BAR(MessageObject.ANCHOR_POINT, "healthBar"),

    TEXT_STYLE_REGULAR(MessageObject.TEXT_STYLE, "regular"),
    TEXT_STYLE_BLACK_SHADOW(MessageObject.TEXT_STYLE, "blackShadow"),

    @Deprecated SETTING_MAKE_ENDERMEN_HOLDING_ITEMS_PINK(MessageObject.SETTING, "makeEndermenHoldingItemPink"), //removed

    INVENTORY_TYPE_ENCHANTS(MessageObject.INVENTORY_TYPE, "enchants"),
    INVENTORY_TYPE_REFORGES(MessageObject.INVENTORY_TYPE, "reforges");

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

    public String getMessage(String... variables) {
        String text;
        try {
            SkyblockAddons main = SkyblockAddons.getInstance();
            List<String> path = getMessageObject().getPath();
            JsonObject jsonObject = main.getConfigValues().getLanguageConfig();
            for (String part : path) {
                if (!part.equals("")) {
                    jsonObject = jsonObject.getAsJsonObject(part);
                }
            }
            text = jsonObject.get(getMemberName()).getAsString();
            if (text != null) {
                if (this == Message.SETTING_WARNING_TIME) {
                    text = text.replace("%time%", String.valueOf(main.getConfigValues().getWarningSeconds()));
                } else if (this == Message.SETTING_GUI_SCALE) {
                    text = text.replace("%scale%", variables[0]);
                } else if (this == Message.MESSAGE_NEW_VERSION) {
                    text = text.replace("%newestVersion%", variables[0]);
                } else if (this == Message.SETTING_BACKPACK_STYLE) {
                    text = text.replace("%style%", main.getConfigValues().getBackpackStyle().getMessage());
                } else if (this == Message.SETTING_TEXT_STYLE) {
                    text = text.replace("%style%", main.getConfigValues().getTextStyle().getMessage());
                } else if (this == Message.MESSAGE_DEVELOPMENT_VERSION) {
                    text = text.replace("%version%", variables[0]).replace("%newestVersion%", variables[1]);
                } else if (this == Message.LANGUAGE) {
                    text = "Language: " + text;
                } else if (this == Message.MESSAGE_MINION_CANNOT_REACH || this == Message.MESSAGE_TYPE_ENCHANTMENTS
                        || this == Message.MESSAGE_ENCHANTS_TO_MATCH || this == Message.MESSAGE_ENCHANTS_TO_EXCLUDE) {
                    text = text.replace("%type%", variables[0]);
                }// else if (this == Message.SETTING_ANCHOR_POINT) { //unused at the moment.
//                    Feature lastHovered = ButtonLocation.getLastHoveredFeature();
//                    if (lastHovered == null) {
//                        lastHovered = Feature.MANA_BAR;
//                    }
//                    text = text.replace("%setting%", lastHovered.getMessage());
//                    text = text.replace("%anchor%", main.getConfigValues().getAnchorPoint(lastHovered).getMessage());
//                }
            }
            if (text != null && main.getConfigValues().getLanguage() == Language.HEBREW) {
                text = main.getUtils().reverseText(text);
            }
        } catch (NullPointerException ex) { // In case I messed up some translation or something.
            ex.printStackTrace();
            text = "";
        }
        return text;
    }

    enum MessageObject {
        ROOT(""),
        SETTING("settings"),
        MESSAGES("messages"),
        STYLE("settings.backpackStyles"),
        INVENTORY_TYPE("messages.inventoryTypes"),
        TEXT_STYLE("settings.textStyles"),
        ANCHOR_POINT("settings.anchorPoints");

        private List<String> path;

        MessageObject(String path) {
            this.path = new LinkedList<>(Arrays.asList(path.split(Pattern.quote("."))));
        }

        public List<String> getPath() {
            return path;
        }
    }

}