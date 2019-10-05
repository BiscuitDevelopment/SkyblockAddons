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
    SETTING_WARNING_DURATION(MessageObject.SETTING, "warningDuration"),
    SETTING_HIDE_SKELETON_HAT_BONES(MessageObject.SETTING, "hideSkeletonHatBones"),
    SETTING_SKELETON_HAT_BONES_BAR(MessageObject.SETTING, "skeletonHatBonesBar"),
    SETTING_HIDE_FOOD_AND_ARMOR(MessageObject.SETTING, "hideFoodAndArmor"),
    SETTING_FULL_INVENTORY_WARNING(MessageObject.SETTING, "fullInventoryWarning"),
    SETTING_MAGMA_BOSS_TIMER(MessageObject.SETTING, "magmaBossTimer"),
    SETTING_DISABLE_EMBER_ROD_ABILITY(MessageObject.SETTING, "disableEmberRodAbility"),
    SETTING_EDIT_LOCATIONS(MessageObject.SETTING, "editLocations"),
    SETTING_GUI_SCALE(MessageObject.SETTING, "guiScale"),
    SETTING_RESET_LOCATIONS(MessageObject.SETTING, "resetLocations"),
    SETTING_SETTINGS(MessageObject.SETTING, "settings"),
    //    SETTING_HIDE_DURABILITY(MessageObject.SETTING, "hideDurability"), // removed
    SETTING_ENCHANTS_AND_REFORGES(MessageObject.SETTING, "showEnchantmentsReforges"),
    SETTING_MINION_STOP_WARNING(MessageObject.SETTING, "minionStopWarning"),
    SETTING_AUCTION_HOUSE_PLAYERS(MessageObject.SETTING, "hideAuctionHousePlayers"),
    SETTING_BACKPACK_STYLE(MessageObject.SETTING, "backpackStyle"),
    SETTING_SHOW_BACKPACK_PREVIEW(MessageObject.SETTING, "showBackpackPreview"),
    SETTING_HIDE_HEALTH_BAR(MessageObject.SETTING, "hideHealthBar"),
    SETTING_FULL_MINION(MessageObject.SETTING, "fullMinionWarning"),
    SETTING_IGNORE_ITEM_FRAME_CLICKS(MessageObject.SETTING, "ignoreItemFrameClicks"),
    SETTING_USE_VANILLA_TEXTURE(MessageObject.SETTING, "useVanillaTexture"),
    SETTING_SHOW_ONLY_WHEN_HOLDING_SHIFT(MessageObject.SETTING, "showOnlyWhenHoldingShift"),
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
    SETTING_STOP_BOW_CHARGE_FROM_RESETTING(MessageObject.SETTING, "stopBowChargeFromResetting"),
    SETTING_SHOW_ITEM_ANVIL_USES(MessageObject.SETTING, "showItemAnvilUses"),
    SETTING_PREVENT_MOVEMENT_ON_DEATH(MessageObject.SETTING, "preventMovementOnDeath"),
    SETTING_LOCK_SLOTS(MessageObject.SETTING, "lockSlots"),
    SETTING_LOCK_SLOT(MessageObject.SETTING, "lockSlot"),
    SETTING_DONT_RESET_CURSOR_INVENTORY(MessageObject.SETTING, "dontResetCursorInventory"),
    SETTING_SUMMONING_EYE_ALERT(MessageObject.SETTING, "summoningEyeAlert"),
    SETTING_CHANGE_COLOR(MessageObject.SETTING, "changeColor"),
    SETTING_SHOW_IN_OTHER_GAMES(MessageObject.SETTING, "showInOtherGames"),
    SETTING_DONT_OPEN_PROFILES_WITH_BOW(MessageObject.SETTING, "dontOpenProfileWithBow"),
    SETTING_MAKE_ENDERCHESTS_IN_END_GREEN(MessageObject.SETTING, "makeEnderchestsInEndGreen"),
    SETTING_STOP_DROPPING_SELLING_RARE_ITEMS(MessageObject.SETTING, "stopDroppingSellingRareItems"),
    SETTING_MAKE_BACKPACK_INVENTORIES_COLORED(MessageObject.SETTING, "makeBackpackInventoriesColored"),
    SETTING_AVOID_BREAKING_BOTTOM_SUGAR_CANE(MessageObject.SETTING, "avoidBreakingBottomSugarCane"),
    SETTING_REPLACE_ROMAN_NUMERALS_WITH_NUMBERS(MessageObject.SETTING, "replaceRomanNumeralsWithNumbers"),

    BACKPACK_STYLE_REGULAR(MessageObject.STYLE, "regular"),
    BACKPACK_STYLE_COMPACT(MessageObject.STYLE, "compact"),

    MESSAGE_DROP_CONFIRMATION(MessageObject.MESSAGES, "dropConfirmation"),
    MESSAGE_MAGMA_BOSS_WARNING(MessageObject.MESSAGES, "magmaBossWarning"),
    MESSAGE_FULL_INVENTORY(MessageObject.MESSAGES, "fullInventory"),
    //    MESSAGE_NEW_VERSION(MessageObject.MESSAGES, "newVersion"),
    MESSAGE_LABYMOD(MessageObject.MESSAGES, "labymod"),
    //    MESSAGE_DISCORD(MessageObject.MESSAGES, "discord"),
    MESSAGE_MINION_CANNOT_REACH(MessageObject.MESSAGES, "minionCannotReach"),
    MESSAGE_MINION_IS_FULL(MessageObject.MESSAGES, "minionIsFull"),
    //    MESSAGE_DEVELOPMENT_VERSION(MessageObject.MESSAGES, "developmentVersion"),
    MESSAGE_TYPE_ENCHANTMENTS(MessageObject.MESSAGES, "typeEnchantmentsHere"),
    MESSAGE_SEPARATE_ENCHANTMENTS(MessageObject.MESSAGES, "separateMultiple"),
    MESSAGE_ENCHANTS_TO_MATCH(MessageObject.MESSAGES, "enchantsToMatch"),
    MESSAGE_ENCHANTS_TO_EXCLUDE(MessageObject.MESSAGES, "enchantsToExclude"),
    MESSAGE_CANCELLED_STEM_BREAK(MessageObject.MESSAGES, "cancelledStemBreak"),
    MESSAGE_SLOT_LOCKED(MessageObject.MESSAGES, "slotLocked"),
    MESSAGE_SUMMONING_EYE_FOUND(MessageObject.MESSAGES, "summoningEyeFound"),
    MESSAGE_STOPPED_OPENING_PROFILE(MessageObject.MESSAGES, "cancelledProfileOpening"),
    MESSAGE_CANCELLED_DROPPING(MessageObject.MESSAGES, "cancelledDropping"),
    MESSAGE_CLICK_MORE_TIMES(MessageObject.MESSAGES, "clickMoreTimes"),
    MESSAGE_CANCELLED_CANE_BREAK(MessageObject.MESSAGES, "cancelledCaneBreak"),
    MESSAGE_NEW_UPDATE(MessageObject.MESSAGES, "newUpdateAvailable"),
    MESSAGE_VIEW_PATCH_NOTES(MessageObject.MESSAGES, "wantToViewPatchNotes"),
    MESSAGE_DOWNLOAD_LINK(MessageObject.MESSAGES, "downloadLink"),
    MESSAGE_DOWNLOAD_AUTOMATICALLY(MessageObject.MESSAGES, "downloadAutomatically"),
    MESSAGE_OPEN_MODS_FOLDER(MessageObject.MESSAGES, "openModFolder"),
    MESSAGE_JOIN_DISCORD(MessageObject.MESSAGES, "joinTheDiscord"),
    MESSAGE_DELETE_OLD_FILE(MessageObject.MESSAGES, "deleteOldFile"),

    @Deprecated ANCHOR_POINT_TOP_LEFT(MessageObject.ANCHOR_POINT, "topLeft"),
    @Deprecated ANCHOR_POINT_TOP_RIGHT(MessageObject.ANCHOR_POINT, "topRight"),
    @Deprecated ANCHOR_POINT_BOTTOM_LEFT(MessageObject.ANCHOR_POINT, "bottomLeft"),
    @Deprecated ANCHOR_POINT_BOTTOM_RIGHT(MessageObject.ANCHOR_POINT, "bottomRight"),
    @Deprecated ANCHOR_POINT_HEALTH_BAR(MessageObject.ANCHOR_POINT, "healthBar"),

    UPDATE_MESSAGE_MAJOR(MessageObject.UPDATE_MESSAGES, "majorAvailable"),
    UPDATE_MESSAGE_PATCH(MessageObject.UPDATE_MESSAGES, "patchAvailable"),
    UPDATE_MESSAGE_DOWNLOAD(MessageObject.UPDATE_MESSAGES, "downloading"),
    UPDATE_MESSAGE_FAILED(MessageObject.UPDATE_MESSAGES, "failed"),
    UPDATE_MESSAGE_DOWNLOAD_FINISHED(MessageObject.UPDATE_MESSAGES, "downloadFinished"),

    TEXT_STYLE_ONE(MessageObject.TEXT_STYLE, "one"),
    TEXT_STYLE_TWO(MessageObject.TEXT_STYLE, "two"),

    TAB_FEATURES(MessageObject.TAB, "features"),
    TAB_FIXES(MessageObject.TAB, "fixes"),
    TAB_GUI_FEATURES(MessageObject.TAB, "guiFeatures"),
    TAB_GENERAL_SETTINGS(MessageObject.TAB, "generalSettings"),

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
                if (!"".equals(part)) {
                    jsonObject = jsonObject.getAsJsonObject(part);
                }
            }
            text = jsonObject.get(getMemberName()).getAsString();
            if (text != null) {
                if (this == Message.SETTING_BACKPACK_STYLE) {
                    text = text.replace("%style%", main.getConfigValues().getBackpackStyle().getMessage());
                } else if (this == Message.SETTING_GUI_SCALE) {
                    text = text.replace("%scale%", variables[0]);
                } else if (this == Message.MESSAGE_NEW_UPDATE || this == UPDATE_MESSAGE_MAJOR || this == UPDATE_MESSAGE_PATCH) {
                    text = text.replace("%version%", variables[0]);
                    //} else if (this == Message.SETTING_BACKPACK_STYLE) {
                    //    text = text.replace("%style%", main.getConfigValues().getBackpackStyle().getMessage());
                } else if (this == Message.SETTING_TEXT_STYLE) {
                    text = text.replace("%style%", main.getConfigValues().getTextStyle().getMessage());
                } else if (this == Message.MESSAGE_MINION_CANNOT_REACH || this == Message.MESSAGE_TYPE_ENCHANTMENTS
                        || this == Message.MESSAGE_ENCHANTS_TO_MATCH || this == Message.MESSAGE_ENCHANTS_TO_EXCLUDE) {
                    text = text.replace("%type%", variables[0]);
                } else if (this == Message.MESSAGE_CLICK_MORE_TIMES) {
                    text = text.replace("%times%", variables[0]);
                } else if (this == Message.UPDATE_MESSAGE_DOWNLOAD) {
                    text = text.replace("%downloaded%", variables[0]).replace("%total%", variables[1]);
                } else if (this == Message.UPDATE_MESSAGE_DOWNLOAD_FINISHED) {
                    text = text.replace("%file%", variables[0]);
                }
                // else if (this == Message.SETTING_ANCHOR_POINT) { //unused at the moment.
//                    Feature lastHovered = ButtonLocation.getLastHoveredFeature();
//                    if (lastHovered == null) {
//                        lastHovered = Feature.MANA_BAR;
//                    }
//                    text = text.replace("%setting%", lastHovered.getMessage());
//                    text = text.replace("%anchor%", main.getConfigValues().getAnchorPoint(lastHovered).getMessage());
//                }
            }
            if (text != null && (main.getConfigValues().getLanguage() == Language.HEBREW || main.getConfigValues().getLanguage() == Language.ARABIC)) {
                text = main.getUtils().reverseText(text);
            }
        } catch (NullPointerException ex) { // In case I messed up some translation or something.
            ex.printStackTrace();
            text = memberName;
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
        TAB("settings.tab"),
        UPDATE_MESSAGES("messages.update"),
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