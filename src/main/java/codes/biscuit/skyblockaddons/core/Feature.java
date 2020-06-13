package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.utils.IntPair;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.FloatPair;
import codes.biscuit.skyblockaddons.utils.GuiFeatureData;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.*;

@Getter
@SuppressWarnings({"DeprecatedIsStillUsed"})
public enum Feature {

    MAGMA_WARNING(0, Message.SETTING_MAGMA_BOSS_WARNING, new GuiFeatureData(ChatFormatting.RED), false),
    DROP_CONFIRMATION(1, Message.SETTING_ITEM_DROP_CONFIRMATION, new GuiFeatureData(ChatFormatting.RED, true), true, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    DISABLE_EMBER_ROD(2, Message.SETTING_DISABLE_EMBER_ROD_ABILITY, false),
    SHOW_BACKPACK_PREVIEW(3, Message.SETTING_SHOW_BACKPACK_PREVIEW, false, EnumUtils.FeatureSetting.BACKPACK_STYLE, EnumUtils.FeatureSetting.SHOW_ONLY_WHEN_HOLDING_SHIFT, EnumUtils.FeatureSetting.MAKE_INVENTORY_COLORED, EnumUtils.FeatureSetting.ENABLE_CAKE_BAG_PREVIEW, EnumUtils.FeatureSetting.ENABLE_BACKPACK_PREVIEW_AH),
    HIDE_BONES(4, Message.SETTING_HIDE_SKELETON_HAT_BONES, false),
    SKELETON_BAR(5, Message.SETTING_SKELETON_HAT_BONES_BAR, new GuiFeatureData(EnumUtils.DrawType.SKELETON_BAR, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 158, -11), false),
    HIDE_FOOD_ARMOR_BAR(6, Message.SETTING_HIDE_FOOD_AND_ARMOR, false),
    FULL_INVENTORY_WARNING(7, Message.SETTING_FULL_INVENTORY_WARNING, new GuiFeatureData(ChatFormatting.RED), false, EnumUtils.FeatureSetting.REPEATING),
    MAGMA_BOSS_TIMER(8, Message.SETTING_MAGMA_BOSS_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.GOLD, EnumUtils.AnchorPoint.TOP_RIGHT, false, -30, 11), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
//    HIDE_DURABILITY(9, Message.SETTING_HIDE_DURABILITY), // Removed, added to Skyblock.
    SHOW_ENCHANTMENTS_REFORGES(10, Message.SETTING_ENCHANTS_AND_REFORGES, false),
    MINION_STOP_WARNING(11, Message.SETTING_MINION_STOP_WARNING, new GuiFeatureData(ChatFormatting.RED), true),
    HIDE_PLAYERS_NEAR_NPCS(12, Message.SETTING_HIDE_PLAYERS_NEAR_NPCS, false),
    HIDE_HEALTH_BAR(13, Message.SETTING_HIDE_HEALTH_BAR, true),

    DOUBLE_DROP_IN_OTHER_GAMES(14, null, false),

    MINION_FULL_WARNING(15, Message.SETTING_FULL_MINION, new GuiFeatureData(ChatFormatting.RED), false),
    IGNORE_ITEM_FRAME_CLICKS(16, Message.SETTING_IGNORE_ITEM_FRAME_CLICKS, true),
    USE_VANILLA_TEXTURE_DEFENCE(17, Message.SETTING_USE_VANILLA_TEXTURE, true),
    SHOW_BACKPACK_HOLDING_SHIFT(18, Message.SETTING_SHOW_ONLY_WHEN_HOLDING_SHIFT, true),
    MANA_BAR(19, Message.SETTING_MANA_BAR, new GuiFeatureData(EnumUtils.DrawType.BAR, ChatFormatting.BLUE, EnumUtils.AnchorPoint.BOTTOM_MIDDLE,49, -44, 5, 1), false),
    MANA_TEXT(20, Message.SETTING_MANA_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.BLUE, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 49, -47), false),
    HEALTH_BAR(21, Message.SETTING_HEALTH_BAR, new GuiFeatureData(EnumUtils.DrawType.BAR, ChatFormatting.RED, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -48, -44, 8, 2), true, EnumUtils.FeatureSetting.CHANGE_BAR_COLOR_WITH_POTIONS),
    HEALTH_TEXT(22, Message.SETTING_HEALTH_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.RED, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -66, -46), false),
    DEFENCE_ICON(23, Message.SETTING_DEFENCE_ICON, new GuiFeatureData(EnumUtils.DrawType.DEFENCE_ICON, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 114, -18), false, EnumUtils.FeatureSetting.USE_VANILLA_TEXTURE),
    DEFENCE_TEXT(24, Message.SETTING_DEFENCE_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.GREEN, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 114, -17), false),
    DEFENCE_PERCENTAGE(25, Message.SETTING_DEFENCE_PERCENTAGE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.GREEN, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 114, -8), true),
    HEALTH_UPDATES(26, Message.SETTING_HEALTH_UPDATES, new GuiFeatureData(EnumUtils.DrawType.TEXT, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -19, -44), false), // Health updates all credit to DidiSkywalker#9975
    HIDE_PLAYERS_IN_LOBBY(27, Message.SETTING_HIDE_PLAYERS_IN_LOBBY, true),
    DARK_AUCTION_TIMER(28, Message.SETTING_DARK_AUCTION_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.GOLD, EnumUtils.AnchorPoint.TOP_RIGHT, -26, 28), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    ITEM_PICKUP_LOG(29, Message.SETTING_ITEM_PICKUP_LOG, new GuiFeatureData(EnumUtils.DrawType.PICKUP_LOG, EnumUtils.AnchorPoint.TOP_LEFT, 86, 17), false),
    AVOID_PLACING_ENCHANTED_ITEMS(30, Message.SETTING_AVOID_PLACING_ENCHANTED_ITEMS, false),
    STOP_BOW_CHARGE_FROM_RESETTING(31, Message.SETTING_STOP_BOW_CHARGE_FROM_RESETTING, false),
    AVOID_BREAKING_STEMS(32, Message.SETTING_AVOID_BREAKING_STEMS, new GuiFeatureData(ChatFormatting.RED, true), false, EnumUtils.FeatureSetting.ENABLE_MESSAGE_WHEN_ACTION_PREVENTED),
    SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES(33, null, false),
    SHOW_ITEM_ANVIL_USES(34, Message.SETTING_SHOW_ITEM_ANVIL_USES, new GuiFeatureData(ChatFormatting.RED, true), false),
    PREVENT_MOVEMENT_ON_DEATH(35, Message.SETTING_PREVENT_MOVEMENT_ON_DEATH, true),

    SHOW_MAGMA_TIMER_IN_OTHER_GAMES(36, null, true),

    DONT_RESET_CURSOR_INVENTORY(37, Message.SETTING_DONT_RESET_CURSOR_INVENTORY, false),
    LOCK_SLOTS(38, Message.SETTING_LOCK_SLOTS, false),
    SUMMONING_EYE_ALERT(39, Message.SETTING_SUMMONING_EYE_ALERT, new GuiFeatureData(ChatFormatting.RED), false),
    MAKE_ENDERCHESTS_GREEN_IN_END(40, Message.SETTING_MAKE_ENDERCHESTS_IN_END_GREEN, new GuiFeatureData(ChatFormatting.GREEN), false),
    DONT_OPEN_PROFILES_WITH_BOW(41, Message.SETTING_DONT_OPEN_PROFILES_WITH_BOW, new GuiFeatureData(ChatFormatting.RED, true), false),
    STOP_DROPPING_SELLING_RARE_ITEMS(42, Message.SETTING_STOP_DROPPING_SELLING_RARE_ITEMS, new GuiFeatureData(ChatFormatting.RED, true), false),
    MAKE_BACKPACK_INVENTORIES_COLORED(43, Message.SETTING_MAKE_BACKPACK_INVENTORIES_COLORED, false),
    @Deprecated AVOID_BREAKING_BOTTOM_SUGAR_CANE(44, Message.SETTING_AVOID_BREAKING_BOTTOM_SUGAR_CANE, false), // Disallowed by the Hypixel admins.
    REPLACE_ROMAN_NUMERALS_WITH_NUMBERS(45, Message.SETTING_REPLACE_ROMAN_NUMERALS_WITH_NUMBERS, true),
    CHANGE_BAR_COLOR_FOR_POTIONS(46, Message.SETTING_CHANGE_BAR_COLOR_WITH_POTIONS, false),
    CRAFTING_PATTERNS(47, Message.SETTING_CRAFTING_PATTERNS, false),
    FISHING_SOUND_INDICATOR(48, Message.SETTING_FISHING_SOUND_INDICATOR, false),
    AVOID_BLINKING_NIGHT_VISION(49, Message.SETTING_AVOID_BLINKING_NIGHT_VISION, false),
    MINION_DISABLE_LOCATION_WARNING(50, Message.SETTING_DISABLE_MINION_LOCATION_WARNING, false),
    JUNGLE_AXE_COOLDOWN(51, Message.SETTING_JUNGLE_AXE_COOLDOWN, true),
    ORGANIZE_ENCHANTMENTS(52, Message.SETTING_ORGANIZE_ENCHANTMENTS, false),
    SHOW_ITEM_COOLDOWNS(53, Message.SETTING_SHOW_ITEM_COOLDOWNS, false),
    SKILL_DISPLAY(54, Message.SETTING_COLLECTION_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.AQUA, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 0, -66), false),
    SPEED_PERCENTAGE(55, Message.SETTING_SPEED_PERCENTAGE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.WHITE, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -110, -11), false),
    ONLY_MINE_ORES_DEEP_CAVERNS(56, Message.SETTING_ONLY_MINE_ORES_DEEP_CAVERNS, new GuiFeatureData(ChatFormatting.RED, true),true, EnumUtils.FeatureSetting.ENABLE_MESSAGE_WHEN_ACTION_PREVENTED),
    SLAYER_INDICATOR(57, Message.SETTING_SLAYER_INDICATOR, new GuiFeatureData(EnumUtils.DrawType.REVENANT_PROGRESS, ChatFormatting.AQUA, EnumUtils.AnchorPoint.BOTTOM_RIGHT, -84, -29), true),
    SPECIAL_ZEALOT_ALERT(58, Message.SETTING_SPECIAL_ZEALOT_ALERT, new GuiFeatureData(ChatFormatting.RED), false),
    ONLY_MINE_VALUABLES_NETHER(59, Message.SETTING_ONLY_MINE_VALUABLES_NETHER, new GuiFeatureData(ChatFormatting.RED, true), true, EnumUtils.FeatureSetting.ENABLE_MESSAGE_WHEN_ACTION_PREVENTED),

    ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS(60, null, false),
    ENABLE_MESSAGE_WHEN_BREAKING_STEMS(61, null, false),
    ENABLE_MESSAGE_WHEN_MINING_NETHER(62, null, false),

    HIDE_PET_HEALTH_BAR(63, Message.SETTING_HIDE_PET_HEALTH_BAR, false),
    DISABLE_MAGICAL_SOUP_MESSAGES(64, Message.SETTING_DISABLE_MAGICAL_SOUP_MESSAGE, true),
    POWER_ORB_STATUS_DISPLAY(65, Message.SETTING_POWER_ORB_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.POWER_ORB_DISPLAY, null, EnumUtils.AnchorPoint.TOP_LEFT, 36, 49), false, EnumUtils.FeatureSetting.POWER_ORB_DISPLAY_STYLE),
    ZEALOT_COUNTER(66, Message.SETTING_ZEALOT_COUNTER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.DARK_AQUA, EnumUtils.AnchorPoint.TOP_RIGHT, -28, 45), false),
    TICKER_CHARGES_DISPLAY(67, Message.SETTING_TICKER_CHARGES_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TICKER, null, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 158, -27), false),
    TAB_EFFECT_TIMERS(68, Message.SETTING_TAB_EFFECT_TIMERS, new GuiFeatureData(EnumUtils.DrawType.TAB_EFFECT_TIMERS, ChatFormatting.WHITE, EnumUtils.AnchorPoint.TOP_LEFT, 81, 83), false, EnumUtils.FeatureSetting.HIDE_NIGHT_VISION_EFFECT, EnumUtils.FeatureSetting.SORT_TAB_EFFECT_TIMERS),
    NO_ARROWS_LEFT_ALERT(69, Message.SETTING_NO_ARROWS_LEFT_ALERT, new GuiFeatureData(ChatFormatting.RED), false),
    HIDE_NIGHT_VISION_EFFECT_TIMER(70, Message.SETTING_HIDE_NIGHT_VISION_EFFECT_TIMER, true),
    CAKE_BAG_PREVIEW(71, Message.SETTING_SHOW_CAKE_BAG_PREVIEW, true),
    BACKPACK_PREVIEW_AH(72, Message.SETTING_SHOW_BACKPACK_PREVIEW_AH, true),

    REPEAT_FULL_INVENTORY_WARNING(73, null, true),

    SORT_TAB_EFFECT_TIMERS(74, Message.SETTING_SORT_TAB_EFFECT_TIMERS, false),
    SHOW_BROKEN_FRAGMENTS(75, Message.SETTING_SHOW_BROKEN_FRAGMENTS, new GuiFeatureData(ChatFormatting.RED, true), false),
    SKYBLOCK_ADDONS_BUTTON_IN_PAUSE_MENU(76, Message.SETTING_SKYBLOCK_ADDONS_BUTTON_IN_PAUSE_MENU, false),
    SHOW_TOTAL_ZEALOT_COUNT(77, Message.SETTING_SHOW_TOTAL_ZEALOT_COUNT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.DARK_AQUA, EnumUtils.AnchorPoint.TOP_RIGHT, -22, 62), true),
    SHOW_SUMMONING_EYE_COUNT(78, Message.SETTING_SHOW_SUMMONING_EYE_COUNT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.DARK_AQUA, EnumUtils.AnchorPoint.TOP_RIGHT, -28, 79), true),
    SHOW_AVERAGE_ZEALOTS_PER_EYE(79, Message.SETTING_SHOW_AVERAGE_ZEALOTS_PER_EYE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.DARK_AQUA, EnumUtils.AnchorPoint.TOP_RIGHT, -28, 96), true),
    TURN_BOW_GREEN_WHEN_USING_TOXIC_ARROW_POISON(80, Message.SETTING_TURN_BOW_GREEN_WHEN_USING_TOXIC_ARROW_POISON, false),
    BIRCH_PARK_RAINMAKER_TIMER(81, Message.SETTING_BIRCH_PARK_RAINMAKER_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.DARK_AQUA, EnumUtils.AnchorPoint.TOP_RIGHT, -118 , 11), false),
    COMBAT_TIMER_DISPLAY(82, Message.SETTING_COMBAT_TIMER_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.RED, EnumUtils.AnchorPoint.TOP_RIGHT, -261, 18), false),
    DISCORD_RPC(83, Message.SETTING_DISCORD_RP, true, EnumUtils.FeatureSetting.DISCORD_RP_DETAILS, EnumUtils.FeatureSetting.DISCORD_RP_STATE),
    ENDSTONE_PROTECTOR_DISPLAY(84, Message.SETTING_ENDSTONE_PROTECTOR_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ChatFormatting.WHITE, EnumUtils.AnchorPoint.TOP_RIGHT, -184, 11), false),
    FANCY_WARP_MENU(85, Message.SETTING_FANCY_WARP_MENU, false),
    DOUBLE_WARP(86, Message.SETTING_DOUBLE_WARP, true),
    HIDE_GREY_ENCHANTS(87, Message.SETTING_HIDE_GREY_ENCHANTS, false),
    LEGENDARY_SEA_CREATURE_WARNING(88, Message.SETTING_LEGENDARY_SEA_CREATURE_WARNING, new GuiFeatureData(ChatFormatting.RED), false),
    ONLY_BREAK_LOGS_PARK(84, Message.SETTING_ONLY_BREAK_LOGS_PARK, new GuiFeatureData(ChatFormatting.RED, true), false, EnumUtils.FeatureSetting.ENABLE_MESSAGE_WHEN_ACTION_PREVENTED),
    ENABLE_MESSAGE_WHEN_BREAKING_PARK(85, null, false),
    BOSS_APPROACH_ALERT(86, Message.SETTING_BOSS_APPROACH_ALERT, false),
    HIDE_SVEN_PUP_NAMETAGS(87, Message.SETTING_HIDE_SVEN_PUP_NAMETAGS, true),

    WARNING_TIME(-1, Message.SETTING_WARNING_DURATION, false),

    WARP_ADVANCED_MODE(-1, Message.SETTING_ADVANCED_MODE, true),

    ADD(-1, null, false),
    SUBTRACT(-1, null, false),

    LANGUAGE(-1, Message.LANGUAGE, false),
    EDIT_LOCATIONS(-1, Message.SETTING_EDIT_LOCATIONS, false),
    RESET_LOCATION(-1, Message.SETTING_RESET_LOCATIONS, false),
    RESCALE_FEATURES(-1, Message.MESSAGE_RESCALE_FEATURES, false),
    SHOW_COLOR_ICONS(-1, Message.MESSAGE_SHOW_COLOR_ICONS, false),
    RESIZE_BARS(-1, Message.MESSAGE_RESIZE_BARS, false),
    ENABLE_FEATURE_SNAPPING(-1, Message.MESSAGE_ENABLE_FEATURE_SNAPPING, false),
    GENERAL_SETTINGS(-1, Message.TAB_GENERAL_SETTINGS, false),
    TEXT_STYLE(-1, Message.SETTING_TEXT_STYLE, false),
    CHROMA_SPEED(-1, Message.SETTING_CHROMA_SPEED, false),
    CHROMA_MODE(-1, Message.SETTING_CHROMA_MODE, false),
    CHROMA_FADE_WIDTH(-1, Message.SETTING_CHROMA_FADE_WIDTH, false);

    /**
     * These are "features" that are not actually features, but just hold the place of a setting. If you are adding any new settings and create
     * a feature here, make sure to add it!
     */
    private static final Set<Feature> SETTINGS = Sets.newHashSet(DOUBLE_DROP_IN_OTHER_GAMES,
            USE_VANILLA_TEXTURE_DEFENCE, SHOW_BACKPACK_HOLDING_SHIFT, SHOW_MAGMA_TIMER_IN_OTHER_GAMES,
            MAKE_BACKPACK_INVENTORIES_COLORED, CHANGE_BAR_COLOR_FOR_POTIONS, ENABLE_MESSAGE_WHEN_BREAKING_STEMS,
            ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS, ENABLE_MESSAGE_WHEN_MINING_NETHER, HIDE_NIGHT_VISION_EFFECT_TIMER,
            CAKE_BAG_PREVIEW, BACKPACK_PREVIEW_AH, REPEAT_FULL_INVENTORY_WARNING, SORT_TAB_EFFECT_TIMERS, DOUBLE_WARP);

    /**
     * Features that are considered gui ones. This is used for examnple when saving the config to ensure that these features'
     * coordinates and colors are handled properly.
     */
    @Getter private static Set<Feature> guiFeatures = new LinkedHashSet<>(Arrays.asList(MAGMA_BOSS_TIMER, MANA_BAR, MANA_TEXT, DEFENCE_ICON, DEFENCE_TEXT,
            DEFENCE_PERCENTAGE, HEALTH_BAR, HEALTH_TEXT, SKELETON_BAR, HEALTH_UPDATES, ITEM_PICKUP_LOG, DARK_AUCTION_TIMER, SKILL_DISPLAY, SPEED_PERCENTAGE, SLAYER_INDICATOR,
            POWER_ORB_STATUS_DISPLAY, ZEALOT_COUNTER, TICKER_CHARGES_DISPLAY, TAB_EFFECT_TIMERS, SHOW_TOTAL_ZEALOT_COUNT, SHOW_SUMMONING_EYE_COUNT,
            SHOW_AVERAGE_ZEALOTS_PER_EYE, BIRCH_PARK_RAINMAKER_TIMER, COMBAT_TIMER_DISPLAY, ENDSTONE_PROTECTOR_DISPLAY));

    private static final int ID_AT_PREVIOUS_UPDATE = 63;

    /**
     * These are features that are displayed separate, on the general tab.
     */
    @Getter private static Set<Feature> generalTabFeatures = new LinkedHashSet<>(Arrays.asList(TEXT_STYLE, WARNING_TIME, CHROMA_SPEED, CHROMA_MODE, CHROMA_FADE_WIDTH));

    private int id;
    private Message message;
    private List<EnumUtils.FeatureSetting> settings;
    private GuiFeatureData guiFeatureData;
    private boolean defaultDisabled;

    Feature(int id, Message settingMessage, GuiFeatureData guiFeatureData, boolean defaultDisabled, EnumUtils.FeatureSetting... settings) { // color & gui scale settings added automatically
        this.id = id;
        this.message = settingMessage;
        this.settings = new ArrayList<>(Arrays.asList(settings));
        this.guiFeatureData = guiFeatureData;
        this.defaultDisabled = defaultDisabled;
    }

    Feature(int id, Message settingMessage, boolean defaultDisabled, EnumUtils.FeatureSetting... settings) {
        this(id, settingMessage,null, defaultDisabled, settings);
    }

    public boolean isActualFeature() {
        return id != -1 && message != null && getMessage() != null && !SETTINGS.contains(this);
    }

    public String getMessage(String... variables) {
        return message.getMessage(variables);
    }

    public static Feature fromId(int id) {
        for (Feature feature : values()) {
            if (feature.getId() == id) {
                return feature;
            }
        }
        return null;
    }

    public boolean isGuiFeature() {
        return guiFeatures.contains(this);
    }

    public boolean isColorFeature() {
        return guiFeatureData != null && guiFeatureData.getDefaultColor() != null;
    }

    public void draw(float scale, Minecraft mc, ButtonLocation buttonLocation) {
        if (guiFeatureData != null) {
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (guiFeatureData.getDrawType() == EnumUtils.DrawType.BAR) {
                main.getRenderListener().drawBar(this, scale, mc, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.SKELETON_BAR) {
                main.getRenderListener().drawSkeletonBar(mc, scale, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.TEXT) {
                main.getRenderListener().drawText(this, scale, mc, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.PICKUP_LOG) {
                main.getRenderListener().drawItemPickupLog(scale, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.DEFENCE_ICON) {
                main.getRenderListener().drawIcon(scale, mc, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.REVENANT_PROGRESS) {
                main.getRenderListener().drawRevenantIndicator(scale, mc, buttonLocation);
            } else if(guiFeatureData.getDrawType() == EnumUtils.DrawType.POWER_ORB_DISPLAY) {
                main.getRenderListener().drawPowerOrbStatus(mc, scale, buttonLocation);
            } else if(guiFeatureData.getDrawType() == EnumUtils.DrawType.TICKER) {
                main.getRenderListener().drawScorpionFoilTicker(mc, scale, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.TAB_EFFECT_TIMERS){
                main.getRenderListener().drawPotionEffectTimers(scale, buttonLocation);
            }
        }
    }

    public FloatPair getDefaultCoordinates() {
        if (guiFeatureData != null) {
            FloatPair coords = guiFeatureData.getDefaultPos();
            if (coords != null) return coords.cloneCoords();
        }
        return null;
    }

    public IntPair getDefaultBarSize() {
        if (guiFeatureData != null) {
            IntPair coords = guiFeatureData.getDefaultBarSize();
            if (coords != null) return coords.cloneCoords();
        }
        return null;
    }

    public EnumUtils.AnchorPoint getAnchorPoint() {
        if (guiFeatureData != null) {
            return guiFeatureData.getDefaultAnchor();
        }
        return null;
    }

    public ChatFormatting getDefaultColor() {
        if (guiFeatureData != null) {
            return guiFeatureData.getDefaultColor();
        }
        return null;
    }

    public boolean isNew() {
        return id > ID_AT_PREVIOUS_UPDATE && this != HIDE_GREY_ENCHANTS;
    }
}
