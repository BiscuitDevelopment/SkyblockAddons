package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import com.google.gson.JsonObject;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
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
    SETTING_REPEATING(MessageObject.SETTING, "repeating"),
    SETTING_MAGMA_BOSS_TIMER(MessageObject.SETTING, "magmaBossTimer"),
    SETTING_DISABLE_EMBER_ROD_ABILITY(MessageObject.SETTING, "disableEmberRodAbility"),
    SETTING_EDIT_LOCATIONS(MessageObject.SETTING, "editLocations"),
    SETTING_GUI_SCALE(MessageObject.SETTING, "guiScale"),
    SETTING_RESET_LOCATIONS(MessageObject.SETTING, "resetLocations"),
    SETTING_SETTINGS(MessageObject.SETTING, "settings"),
    SETTING_ENCHANTS_AND_REFORGES(MessageObject.SETTING, "showEnchantmentsReforges"),
    SETTING_MINION_STOP_WARNING(MessageObject.SETTING, "minionStopWarning"),
    SETTING_HIDE_PLAYERS_NEAR_NPCS(MessageObject.SETTING, "hidePlayersNearNPCs"),
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
    @Deprecated SETTING_AVOID_BREAKING_BOTTOM_SUGAR_CANE(MessageObject.SETTING, "avoidBreakingBottomSugarCane"), // disallowed
    SETTING_REPLACE_ROMAN_NUMERALS_WITH_NUMBERS(MessageObject.SETTING, "replaceRomanNumeralsWithNumbers"),
    SETTING_CHANGE_BAR_COLOR_WITH_POTIONS(MessageObject.SETTING, "changeBarColorForPotions"),
    SETTING_CRAFTING_PATTERNS(MessageObject.SETTING, "craftingPatterns"),
    SETTING_FISHING_SOUND_INDICATOR(MessageObject.SETTING, "soundIndicatorForFishing"),
    SETTING_AVOID_BLINKING_NIGHT_VISION(MessageObject.SETTING, "avoidBlinkingNightVision"),
    SETTING_DISABLE_MINION_LOCATION_WARNING(MessageObject.SETTING, "disableMinionLocationWarning"),
    SETTING_JUNGLE_AXE_COOLDOWN(MessageObject.SETTING, "jungleAxeCooldown"),
    SETTING_ORGANIZE_ENCHANTMENTS(MessageObject.SETTING, "organizeLongEnchantmentLists"),
    SETTING_SHOW_ITEM_COOLDOWNS(MessageObject.SETTING, "showItemCooldowns"),
    SETTING_COLLECTION_DISPLAY(MessageObject.SETTING, "collectionDisplay"),
    SETTING_SPEED_PERCENTAGE(MessageObject.SETTING, "speedPercentage"),
    SETTING_ONLY_MINE_ORES_DEEP_CAVERNS(MessageObject.SETTING, "onlyMineOresDeepCaverns"),
    SETTING_ENABLE_MESSAGE_WHEN_ACTION_PREVENTED(MessageObject.SETTING, "enableMessageWhenActionPrevented"),
    SETTING_SLAYER_INDICATOR(MessageObject.SETTING, "revenantIndicator"),
    SETTING_SPECIAL_ZEALOT_ALERT(MessageObject.SETTING, "specialZealotAlert"),
    SETTING_ONLY_MINE_VALUABLES_NETHER(MessageObject.SETTING, "onlyMineValuablesNether"),
    SETTING_DISABLE_MAGICAL_SOUP_MESSAGE(MessageObject.SETTING, "disableMagicalSoupMessage"),
    SETTING_HIDE_PET_HEALTH_BAR(MessageObject.SETTING, "hidePetHealthBar"),
    SETTING_POWER_ORB_DISPLAY(MessageObject.SETTING, "powerOrbDisplay"),
    SETTING_POWER_ORB_DISPLAY_STYLE(MessageObject.SETTING, "powerOrbDisplayStyle"),
    SETTING_ZEALOT_COUNTER(MessageObject.SETTING, "zealotCounter"),
    SETTING_TICKER_CHARGES_DISPLAY(MessageObject.SETTING, "tickerChargesDisplay"),
    SETTING_TAB_EFFECT_TIMERS(MessageObject.SETTING, "tabEffectTimers"),
    SETTING_HIDE_NIGHT_VISION_EFFECT_TIMER(MessageObject.SETTING, "hideNightVisionEffectTimer"),
    SETTING_NO_ARROWS_LEFT_ALERT(MessageObject.SETTING, "noArrowsLeftAlert"),
    SETTING_SHOW_CAKE_BAG_PREVIEW(MessageObject.SETTING, "showCakeBagPreview"),
    SETTING_SHOW_BACKPACK_PREVIEW_AH(MessageObject.SETTING, "showBackpackPreviewInAH"),
    SETTING_ENABLE_DEV_FEATURES(MessageObject.SETTING, "enableDevFeatures"),
    SETTING_CHROMA_SPEED(MessageObject.SETTING, "chromaSpeed"),
    SETTING_CHROMA_MODE(MessageObject.SETTING, "chromaMode"),
    SETTING_CHROMA_FADE_WIDTH(MessageObject.SETTING, "chromaFadeWidth"),
    SETTING_SORT_TAB_EFFECT_TIMERS(MessageObject.SETTING, "sortTabEffectTimers"),
    SETTING_SHOW_BROKEN_FRAGMENTS(MessageObject.SETTING, "showBrokenFragments"),
    SETTING_SKYBLOCK_ADDONS_BUTTON_IN_PAUSE_MENU(MessageObject.SETTING, "skyblockAddonsButtonInPauseMenu"),
    SETTING_SHOW_TOTAL_ZEALOT_COUNT(MessageObject.SETTING, "showTotalZealotCount"),
    SETTING_SHOW_SUMMONING_EYE_COUNT(MessageObject.SETTING, "showSummoningEyeCount"),
    SETTING_SHOW_AVERAGE_ZEALOTS_PER_EYE(MessageObject.SETTING, "showZealotsPerEye"),
    SETTING_TURN_BOW_GREEN_WHEN_USING_TOXIC_ARROW_POISON(MessageObject.SETTING, "turnBowGreenWhenUsingToxicArrowPoison"),
    SETTING_BIRCH_PARK_RAINMAKER_TIMER(MessageObject.SETTING, "birchParkRainmakerTimer"),
    SETTING_COMBAT_TIMER_DISPLAY(MessageObject.SETTING, "combatTimerDisplay"),
    SETTING_DISCORD_RP(MessageObject.SETTING, "discordRP"),
    SETTING_ENDSTONE_PROTECTOR_DISPLAY(MessageObject.SETTING, "endstoneProtectorDisplay"),
    SETTING_FANCY_WARP_MENU(MessageObject.SETTING, "fancyWarpMenu"),
    SETTING_DOUBLE_WARP(MessageObject.SETTING, "doubleWarp"),
    SETTING_ADVANCED_MODE(MessageObject.SETTING, "advancedMode"),
    SETTING_FREEZE_BACKPACK_PREVIEW(MessageObject.SETTING, "freezeBackpackPreview"),
    SETTING_HIDE_GREY_ENCHANTS(MessageObject.SETTING, "hideGreyEnchants"),
    SETTING_LEGENDARY_SEA_CREATURE_WARNING(MessageObject.SETTING, "legendarySeaCreatureWarning"),
    SETTING_ONLY_BREAK_LOGS_PARK(MessageObject.SETTING, "onlyBreakLogsPark"),
    SETTING_BOSS_APPROACH_ALERT(MessageObject.SETTING, "bossApproachAlert"),
    SETTING_DISABLE_TELEPORT_PAD_MESSAGES(MessageObject.SETTING, "disableTeleportPadMessages"),
    SETTING_BAIT_LIST(MessageObject.SETTING, "baitListDisplay"),
    SETTING_ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT(MessageObject.SETTING, "zealotCounterExplosiveBow"),
    SETTING_DISABLE_ENDERMAN_TELEPORTATION_EFFECT(MessageObject.SETTING, "disableEndermanTeleportation"),
    SETTING_CHANGE_ZEALOT_COLOR(MessageObject.SETTING, "changeZealotColor"),
    SETTING_HIDE_SVEN_PUP_NAMETAGS(MessageObject.SETTING, "hideSvenPupNametags"),
    SETTING_TURN_ALL_FEATURES_CHROMA(MessageObject.SETTING, "turnAllFeaturesChroma"),

    BACKPACK_STYLE_REGULAR(MessageObject.BACKPACK_STYLE, "regular"),
    BACKPACK_STYLE_COMPACT(MessageObject.BACKPACK_STYLE, "compact"),

    MESSAGE_ENCHANTS(MessageObject.MESSAGES, "enchants"),
    MESSAGE_REFORGES(MessageObject.MESSAGES, "reforges"),
    MESSAGE_DROP_CONFIRMATION(MessageObject.MESSAGES, "dropConfirmation"),
    MESSAGE_MAGMA_BOSS_WARNING(MessageObject.MESSAGES, "magmaBossWarning"),
    MESSAGE_FULL_INVENTORY(MessageObject.MESSAGES, "fullInventory"),
    MESSAGE_LABYMOD(MessageObject.MESSAGES, "labymod"),
    MESSAGE_MINION_CANNOT_REACH(MessageObject.MESSAGES, "minionCannotReach"),
    MESSAGE_MINION_IS_FULL(MessageObject.MESSAGES, "minionIsFull"),
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
    MESSAGE_CLICK_ONE_MORE_TIME(MessageObject.MESSAGES, "clickOneMoreTime"),
    MESSAGE_CANCELLED_CANE_BREAK(MessageObject.MESSAGES, "cancelledCaneBreak"),
    MESSAGE_VIEW_PATCH_NOTES(MessageObject.MESSAGES, "wantToViewPatchNotes"),
    MESSAGE_DOWNLOAD_LINK(MessageObject.MESSAGES, "downloadLink"),
    MESSAGE_DIRECT_DOWNLOAD(MessageObject.MESSAGES, "directDownload"),
    MESSAGE_DOWNLOAD_AUTOMATICALLY(MessageObject.MESSAGES, "downloadAutomatically"),
    MESSAGE_OPEN_MODS_FOLDER(MessageObject.MESSAGES, "openModFolder"),
    MESSAGE_JOIN_DISCORD(MessageObject.MESSAGES, "joinTheDiscord"),
    MESSAGE_FEATURE_DISABLED(MessageObject.MESSAGES, "featureDisabled"),
    MESSAGE_ANVIL_USES(MessageObject.MESSAGES, "anvilUses"),
    MESSAGE_CANCELLED_NON_ORES_BREAK(MessageObject.MESSAGES, "cancelledDeepCaverns"),
    MESSAGE_SPECIAL_ZEALOT_FOUND(MessageObject.MESSAGES, "specialZealotFound"),
    MESSAGE_BLOCK_INCOMPLETE_PATTERNS(MessageObject.MESSAGES, "blockIncompletePatterns"),
    MESSAGE_SEARCH_FEATURES(MessageObject.MESSAGES, "searchFeatures"),
    MESSAGE_DOWNLOADING_UPDATE(MessageObject.MESSAGES, "downloadingUpdateFile"),
    MESSAGE_ONLY_FEW_ARROWS_LEFT(MessageObject.MESSAGES, "onlyFewArrowsLeft"),
    MESSAGE_NO_ARROWS_LEFT(MessageObject.MESSAGES, "noArrowsLeft"),
    MESSAGE_CHOOSE_A_COLOR(MessageObject.MESSAGES, "chooseAColor"),
    MESSAGE_SELECTED_COLOR(MessageObject.MESSAGES, "selectedColor"),
    MESSAGE_SET_HEX_COLOR(MessageObject.MESSAGES, "setHexColor"),
    MESSAGE_RESCALE_FEATURES(MessageObject.MESSAGES, "rescaleFeatures"),
    MESSAGE_RESIZE_BARS(MessageObject.MESSAGES, "resizeBars"),
    MESSAGE_SHOW_COLOR_ICONS(MessageObject.MESSAGES, "showColorIcons"),
    MESSAGE_ENABLE_FEATURE_SNAPPING(MessageObject.MESSAGES, "enableFeatureSnapping"),
    MESSAGE_STAGE(MessageObject.MESSAGES, "stage"),
    MESSAGE_SWITCHED_SLOTS(MessageObject.MESSAGES, "switchedSlots"),
    MESSAGE_NEW_UPDATE(MessageObject.MESSAGES, "newUpdateAvailable"),
    MESSAGE_CLICK_TO_OPEN_LINK(MessageObject.MESSAGES, "clickToOpenLink"),
    MESSAGE_CLICK_TO_OPEN_FOLDER(MessageObject.MESSAGES, "clickToOpenFolder"),
    MESSAGE_FIRST_STATUS(MessageObject.MESSAGES, "firstStatus"),
    MESSAGE_SECOND_STATUS(MessageObject.MESSAGES, "secondStatus"),
    MESSAGE_FALLBACK_STATUS(MessageObject.MESSAGES, "fallbackStatus"),
    MESSAGE_LEGENDARY_SEA_CREATURE_WARNING(MessageObject.MESSAGES, "legendarySeaCreatureWarning"),
    MESSAGE_CANCELLED_NON_LOGS_BREAK(MessageObject.MESSAGES, "cancelledPark"),
    MESSAGE_BOSS_APPROACH_ALERT(MessageObject.MESSAGES, "bossApproaching"),
    MESSAGE_ENABLE_ALL(MessageObject.MESSAGES, "enableAll"),
    MESSAGE_DISABLE_ALL(MessageObject.MESSAGES, "disableAll"),
    MESSAGE_ENCHANTMENT_INCLUSION_EXAMPLE(MessageObject.MESSAGES, "enchantmentInclusionExample"),
    MESSAGE_ENCHANTMENT_EXCLUSION_EXAMPLE(MessageObject.MESSAGES, "enchantmentExclusionExample"),
    MESSAGE_REFORGE_INCLUSION_EXAMPLE(MessageObject.MESSAGES, "reforgeInclusionExample"),
    MESSAGE_REFORGE_EXCLUSION_EXAMPLE(MessageObject.MESSAGES, "reforgeExclusionExample"),
    MESSAGE_ONE_EFFECT_ACTIVE(MessageObject.MESSAGES, "effectActive"),
    MESSAGE_EFFECTS_ACTIVE(MessageObject.MESSAGES, "effectsActive"),

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

    @Deprecated TAB_FEATURES(MessageObject.TAB, "features"), // Tabs are no longer in use.
    @Deprecated TAB_FIXES(MessageObject.TAB, "fixes"),
    @Deprecated TAB_GUI_FEATURES(MessageObject.TAB, "guiFeatures"),
    TAB_GENERAL_SETTINGS(MessageObject.TAB, "generalSettings"),

    POWER_ORB_DISPLAY_STYLE_DETAILED(MessageObject.POWER_ORB_STYLE, "detailed"),
    POWER_ORB_DISPLAY_STYLE_COMPACT(MessageObject.POWER_ORB_STYLE, "compact"),

    CHROMA_MODE_ALL_THE_SAME(MessageObject.CHROMA_MODE, "allTheSame"),
    CHROME_MODE_FADE(MessageObject.CHROMA_MODE, "fade"),

    DISCORD_STATUS_NONE_TITLE(MessageObject.DISCORD_STATUS, "titleNone"),
    DISCORD_STATUS_NONE_DESCRIPTION(MessageObject.DISCORD_STATUS, "descriptionNone"),
    DISCORD_STATUS_LOCATION_TITLE(MessageObject.DISCORD_STATUS, "titleLocation"),
    DISCORD_STATUS_LOCATION_DESCRIPTION(MessageObject.DISCORD_STATUS, "descriptionLocation"),
    DISCORD_STATUS_PURSE_TITLE(MessageObject.DISCORD_STATUS, "titlePurse"),
    DISCORD_STATUS_PURSE_DESCRIPTION(MessageObject.DISCORD_STATUS, "descriptionPurse"),
    DISCORD_STATUS_STATS_TITLE(MessageObject.DISCORD_STATUS, "titleStats"),
    DISCORD_STATUS_STATS_DESCRIPTION(MessageObject.DISCORD_STATUS, "descriptionStats"),
    DISCORD_STATUS_ZEALOTS_TITLE(MessageObject.DISCORD_STATUS, "titleZealots"),
    DISCORD_STATUS_ZEALOTS_DESCRIPTION(MessageObject.DISCORD_STATUS, "descriptionZealots"),
    DISCORD_STATUS_ITEM_TITLE(MessageObject.DISCORD_STATUS, "titleItem"),
    DISCORD_STATUS_ITEM_DESCRIPTION(MessageObject.DISCORD_STATUS, "descriptionItem"),
    DISCORD_STATUS_TIME_TITLE(MessageObject.DISCORD_STATUS, "titleTime"),
    DISCORD_STATUS_TIME_DESCRIPTION(MessageObject.DISCORD_STATUS, "descriptionTime"),
    DISCORD_STATUS_PROFILE_TITLE(MessageObject.DISCORD_STATUS, "titleProfile"),
    DISCORD_STATUS_PROFILE_DESCRIPTION(MessageObject.DISCORD_STATUS, "descriptionProfile"),
    DISCORD_STATUS_CUSTOM(MessageObject.DISCORD_STATUS, "titleCustom"),
    DISCORD_STATUS_CUSTOM_DESCRIPTION(MessageObject.DISCORD_STATUS, "descriptionCustom"),
    DISCORD_STATUS_AUTO(MessageObject.DISCORD_STATUS, "titleAuto"),
    DISCORD_STATUS_AUTO_DESCRIPTION(MessageObject.DISCORD_STATUS, "descriptionAuto"),
    ;

    private MessageObject messageObject;
    private String memberName;

    Message(MessageObject messageObject, String memberName) {
        this.messageObject = messageObject;
        this.memberName = memberName;
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
                if (this == Message.SETTING_BACKPACK_STYLE) {
                    text = text.replace("%style%", main.getConfigValues().getBackpackStyle().getMessage());
                } else if(this == Message.SETTING_POWER_ORB_DISPLAY_STYLE) {
                    text = text.replace("%style%", main.getConfigValues().getPowerOrbDisplayStyle().getMessage());
                } else if (this == Message.SETTING_GUI_SCALE) {
                    text = text.replace("%scale%", variables[0]);
                } else if (this == MESSAGE_NEW_UPDATE || this == UPDATE_MESSAGE_MAJOR || this == UPDATE_MESSAGE_PATCH) {
                    text = text.replace("%version%", variables[0]);
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
                } else if (this == Message.MESSAGE_ANVIL_USES) {
                    text = text.replace("%uses%", main.getConfigValues().getRestrictedColor(Feature.SHOW_ITEM_ANVIL_USES)+variables[0]+ ColorCode.GRAY.toString());
                } else if (this == Message.MESSAGE_ONLY_FEW_ARROWS_LEFT) {
                    text = text.replace("%arrows%", variables[0]);
                } else if (this == Message.MESSAGE_STAGE) {
                    text = text.replace("%stage%", variables[0]);
                } else if (this == Message.MESSAGE_EFFECTS_ACTIVE) {
                    text = text.replace("%number%", variables[0]);
                }
            }
            if (text != null && (main.getConfigValues().getLanguage() == Language.HEBREW || main.getConfigValues().getLanguage() == Language.ARABIC) && !Minecraft.getMinecraft().fontRendererObj.getBidiFlag()) {
                text = bidiReorder(text);
            }
        } catch (NullPointerException ex) {
            text = memberName; // In case of fire...
        }
        return text;
    }

    private String bidiReorder(String text) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(ArabicShaping.LETTERS_SHAPE)).shape(text), Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
            bidi.setReorderingMode(Bidi.REORDER_DEFAULT);
            return bidi.writeReordered(Bidi.DO_MIRRORING);
        } catch (ArabicShapingException var3) {
            return text;
        }
    }

    @Getter
    enum MessageObject {
        ROOT(""),
        SETTING("settings"),
        MESSAGES("messages"),
        BACKPACK_STYLE("settings.backpackStyles"),
        POWER_ORB_STYLE("settings.powerOrbStyle"),
        TEXT_STYLE("settings.textStyles"),
        TAB("settings.tab"),
        UPDATE_MESSAGES("messages.update"),
        ANCHOR_POINT("settings.anchorPoints"),
        CHROMA_MODE("settings.chromaModes"),
        DISCORD_STATUS("discordStatus");

        private List<String> path;

        MessageObject(String path) {
            this.path = new LinkedList<>(Arrays.asList(path.split(Pattern.quote("."))));
        }
    }

}
