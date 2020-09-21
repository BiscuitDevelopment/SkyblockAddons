package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.util.*;

@Getter
public enum Feature {

    MAGMA_WARNING(0, Message.SETTING_MAGMA_BOSS_WARNING, new GuiFeatureData(ColorCode.RED), false),
    DROP_CONFIRMATION(1, Message.SETTING_ITEM_DROP_CONFIRMATION, new GuiFeatureData(ColorCode.RED, true), true, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    DISABLE_EMBER_ROD(2, Message.SETTING_DISABLE_EMBER_ROD_ABILITY, false),
    SHOW_BACKPACK_PREVIEW(3, Message.SETTING_SHOW_BACKPACK_PREVIEW, false, EnumUtils.FeatureSetting.BACKPACK_STYLE, EnumUtils.FeatureSetting.SHOW_ONLY_WHEN_HOLDING_SHIFT, EnumUtils.FeatureSetting.MAKE_INVENTORY_COLORED, EnumUtils.FeatureSetting.ENABLE_CAKE_BAG_PREVIEW, EnumUtils.FeatureSetting.ENABLE_PERSONAL_COMPACTOR_PREVIEW),
    HIDE_BONES(4, Message.SETTING_HIDE_SKELETON_HAT_BONES, false),
    SKELETON_BAR(5, Message.SETTING_SKELETON_HAT_BONES_BAR, new GuiFeatureData(EnumUtils.DrawType.SKELETON_BAR), false),
    HIDE_FOOD_ARMOR_BAR(6, Message.SETTING_HIDE_FOOD_AND_ARMOR, false),
    FULL_INVENTORY_WARNING(7, Message.SETTING_FULL_INVENTORY_WARNING, new GuiFeatureData(ColorCode.RED), false, EnumUtils.FeatureSetting.REPEATING),
    MAGMA_BOSS_TIMER(8, Message.SETTING_MAGMA_BOSS_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GOLD, false), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    SHOW_ENCHANTMENTS_REFORGES(10, Message.SETTING_ENCHANTS_AND_REFORGES, false),
    MINION_STOP_WARNING(11, Message.SETTING_MINION_STOP_WARNING, new GuiFeatureData(ColorCode.RED), true),
    HIDE_PLAYERS_NEAR_NPCS(12, Message.SETTING_HIDE_PLAYERS_NEAR_NPCS, false),
    HIDE_HEALTH_BAR(13, Message.SETTING_HIDE_HEALTH_BAR, true),

    DOUBLE_DROP_IN_OTHER_GAMES(14, null, false),

    MINION_FULL_WARNING(15, Message.SETTING_FULL_MINION, new GuiFeatureData(ColorCode.RED), false),
    IGNORE_ITEM_FRAME_CLICKS(16, Message.SETTING_IGNORE_ITEM_FRAME_CLICKS, true),
    USE_VANILLA_TEXTURE_DEFENCE(17, Message.SETTING_USE_VANILLA_TEXTURE, true),
    SHOW_BACKPACK_HOLDING_SHIFT(18, Message.SETTING_SHOW_ONLY_WHEN_HOLDING_SHIFT, true),
    MANA_BAR(19, Message.SETTING_MANA_BAR, new GuiFeatureData(EnumUtils.DrawType.BAR, ColorCode.BLUE), false),
    MANA_TEXT(20, Message.SETTING_MANA_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.BLUE), false),
    HEALTH_BAR(21, Message.SETTING_HEALTH_BAR, new GuiFeatureData(EnumUtils.DrawType.BAR, ColorCode.RED), true, EnumUtils.FeatureSetting.CHANGE_BAR_COLOR_WITH_POTIONS),
    HEALTH_TEXT(22, Message.SETTING_HEALTH_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.RED), false),
    DEFENCE_ICON(23, Message.SETTING_DEFENCE_ICON, new GuiFeatureData(EnumUtils.DrawType.DEFENCE_ICON), false, EnumUtils.FeatureSetting.USE_VANILLA_TEXTURE),
    DEFENCE_TEXT(24, Message.SETTING_DEFENCE_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GREEN), false),
    DEFENCE_PERCENTAGE(25, Message.SETTING_DEFENCE_PERCENTAGE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GREEN), true),
    HEALTH_UPDATES(26, Message.SETTING_HEALTH_UPDATES, new GuiFeatureData(EnumUtils.DrawType.TEXT), false), // Health updates all credit to DidiSkywalker#9975
    HIDE_PLAYERS_IN_LOBBY(27, Message.SETTING_HIDE_PLAYERS_IN_LOBBY, true),
    DARK_AUCTION_TIMER(28, Message.SETTING_DARK_AUCTION_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GOLD), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    ITEM_PICKUP_LOG(29, Message.SETTING_ITEM_PICKUP_LOG, new GuiFeatureData(EnumUtils.DrawType.PICKUP_LOG), false),
    AVOID_PLACING_ENCHANTED_ITEMS(30, Message.SETTING_AVOID_PLACING_ENCHANTED_ITEMS, false),
    AVOID_BREAKING_STEMS(32, Message.SETTING_AVOID_BREAKING_STEMS, new GuiFeatureData(ColorCode.RED, true), false, EnumUtils.FeatureSetting.ENABLE_MESSAGE_WHEN_ACTION_PREVENTED),
    SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES(33, null, false),
    SHOW_ITEM_ANVIL_USES(34, Message.SETTING_SHOW_ITEM_ANVIL_USES, new GuiFeatureData(ColorCode.RED, true), false),
    PREVENT_MOVEMENT_ON_DEATH(35, Message.SETTING_PREVENT_MOVEMENT_ON_DEATH, true),

    SHOW_MAGMA_TIMER_IN_OTHER_GAMES(36, null, true),

    DONT_RESET_CURSOR_INVENTORY(37, Message.SETTING_DONT_RESET_CURSOR_INVENTORY, false),
    LOCK_SLOTS(38, Message.SETTING_LOCK_SLOTS, false),
    SUMMONING_EYE_ALERT(39, Message.SETTING_SUMMONING_EYE_ALERT, new GuiFeatureData(ColorCode.RED), false),
    MAKE_ENDERCHESTS_GREEN_IN_END(40, Message.SETTING_MAKE_ENDERCHESTS_IN_END_GREEN, new GuiFeatureData(ColorCode.GREEN), false),
    STOP_DROPPING_SELLING_RARE_ITEMS(42, Message.SETTING_STOP_DROPPING_SELLING_RARE_ITEMS, new GuiFeatureData(ColorCode.RED, true), false),
    MAKE_BACKPACK_INVENTORIES_COLORED(43, Message.SETTING_MAKE_BACKPACK_INVENTORIES_COLORED, false),
    REPLACE_ROMAN_NUMERALS_WITH_NUMBERS(45, Message.SETTING_REPLACE_ROMAN_NUMERALS_WITH_NUMBERS, true),
    CHANGE_BAR_COLOR_FOR_POTIONS(46, Message.SETTING_CHANGE_BAR_COLOR_WITH_POTIONS, false),
    CRAFTING_PATTERNS(47, Message.SETTING_CRAFTING_PATTERNS, false),
    FISHING_SOUND_INDICATOR(48, Message.SETTING_FISHING_SOUND_INDICATOR, false),
    AVOID_BLINKING_NIGHT_VISION(49, Message.SETTING_AVOID_BLINKING_NIGHT_VISION, false),
    MINION_DISABLE_LOCATION_WARNING(50, Message.SETTING_DISABLE_MINION_LOCATION_WARNING, false),
    JUNGLE_AXE_COOLDOWN(51, Message.SETTING_JUNGLE_AXE_COOLDOWN, true),
    ORGANIZE_ENCHANTMENTS(52, Message.SETTING_ORGANIZE_ENCHANTMENTS, false),
    SHOW_ITEM_COOLDOWNS(53, Message.SETTING_SHOW_ITEM_COOLDOWNS, false),
    SKILL_DISPLAY(54, Message.SETTING_COLLECTION_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.AQUA), false, EnumUtils.FeatureSetting.EXPANDED),
    SPEED_PERCENTAGE(55, Message.SETTING_SPEED_PERCENTAGE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.WHITE), false),
    ONLY_MINE_ORES_DEEP_CAVERNS(56, Message.SETTING_ONLY_MINE_ORES_DEEP_CAVERNS, new GuiFeatureData(ColorCode.RED, true),true, EnumUtils.FeatureSetting.ENABLE_MESSAGE_WHEN_ACTION_PREVENTED),
    SLAYER_INDICATOR(57, Message.SETTING_SLAYER_INDICATOR, new GuiFeatureData(EnumUtils.DrawType.REVENANT_PROGRESS, ColorCode.AQUA), true),
    SPECIAL_ZEALOT_ALERT(58, Message.SETTING_SPECIAL_ZEALOT_ALERT, new GuiFeatureData(ColorCode.RED), false),
    ONLY_MINE_VALUABLES_NETHER(59, Message.SETTING_ONLY_MINE_VALUABLES_NETHER, new GuiFeatureData(ColorCode.RED, true), true, EnumUtils.FeatureSetting.ENABLE_MESSAGE_WHEN_ACTION_PREVENTED),

    ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS(60, null, false),
    ENABLE_MESSAGE_WHEN_BREAKING_STEMS(61, null, false),
    ENABLE_MESSAGE_WHEN_MINING_NETHER(62, null, false),

    HIDE_PET_HEALTH_BAR(63, Message.SETTING_HIDE_PET_HEALTH_BAR, false),
    DISABLE_MAGICAL_SOUP_MESSAGES(64, Message.SETTING_DISABLE_MAGICAL_SOUP_MESSAGE, true),
    POWER_ORB_STATUS_DISPLAY(65, Message.SETTING_POWER_ORB_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.POWER_ORB_DISPLAY, null), false, EnumUtils.FeatureSetting.POWER_ORB_DISPLAY_STYLE),
    ZEALOT_COUNTER(66, Message.SETTING_ZEALOT_COUNTER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), false, EnumUtils.FeatureSetting.DRAGONS_NEST_ONLY),
    TICKER_CHARGES_DISPLAY(67, Message.SETTING_TICKER_CHARGES_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TICKER, null), false),
    TAB_EFFECT_TIMERS(68, Message.SETTING_TAB_EFFECT_TIMERS, new GuiFeatureData(EnumUtils.DrawType.TAB_EFFECT_TIMERS, ColorCode.WHITE), false, EnumUtils.FeatureSetting.HIDE_NIGHT_VISION_EFFECT, EnumUtils.FeatureSetting.SORT_TAB_EFFECT_TIMERS),
    NO_ARROWS_LEFT_ALERT(69, Message.SETTING_NO_ARROWS_LEFT_ALERT, new GuiFeatureData(ColorCode.RED), false),
    HIDE_NIGHT_VISION_EFFECT_TIMER(70, Message.SETTING_HIDE_NIGHT_VISION_EFFECT_TIMER, true),
    CAKE_BAG_PREVIEW(71, Message.SETTING_SHOW_CAKE_BAG_PREVIEW, true),

    REPEAT_FULL_INVENTORY_WARNING(73, null, true),

    SORT_TAB_EFFECT_TIMERS(74, Message.SETTING_SORT_TAB_EFFECT_TIMERS, false),
    SHOW_BROKEN_FRAGMENTS(75, Message.SETTING_SHOW_BROKEN_FRAGMENTS, new GuiFeatureData(ColorCode.RED, true), false),
    SKYBLOCK_ADDONS_BUTTON_IN_PAUSE_MENU(76, Message.SETTING_SKYBLOCK_ADDONS_BUTTON_IN_PAUSE_MENU, false),
    SHOW_TOTAL_ZEALOT_COUNT(77, Message.SETTING_SHOW_TOTAL_ZEALOT_COUNT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), true, EnumUtils.FeatureSetting.DRAGONS_NEST_ONLY),
    SHOW_SUMMONING_EYE_COUNT(78, Message.SETTING_SHOW_SUMMONING_EYE_COUNT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), true, EnumUtils.FeatureSetting.DRAGONS_NEST_ONLY),
    SHOW_AVERAGE_ZEALOTS_PER_EYE(79, Message.SETTING_SHOW_AVERAGE_ZEALOTS_PER_EYE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), true, EnumUtils.FeatureSetting.DRAGONS_NEST_ONLY),
    TURN_BOW_GREEN_WHEN_USING_TOXIC_ARROW_POISON(80, Message.SETTING_TURN_BOW_GREEN_WHEN_USING_TOXIC_ARROW_POISON, false),
    BIRCH_PARK_RAINMAKER_TIMER(81, Message.SETTING_BIRCH_PARK_RAINMAKER_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), false),
    COMBAT_TIMER_DISPLAY(82, Message.SETTING_COMBAT_TIMER_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.RED), false),
    DISCORD_RPC(83, Message.SETTING_DISCORD_RP, true, EnumUtils.FeatureSetting.DISCORD_RP_DETAILS, EnumUtils.FeatureSetting.DISCORD_RP_STATE),
    ENDSTONE_PROTECTOR_DISPLAY(84, Message.SETTING_ENDSTONE_PROTECTOR_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.WHITE), false),
    FANCY_WARP_MENU(85, Message.SETTING_FANCY_WARP_MENU, false),
    DOUBLE_WARP(86, Message.SETTING_DOUBLE_WARP, true),
    HIDE_GREY_ENCHANTS(87, Message.SETTING_HIDE_GREY_ENCHANTS, false),
    LEGENDARY_SEA_CREATURE_WARNING(88, Message.SETTING_LEGENDARY_SEA_CREATURE_WARNING, new GuiFeatureData(ColorCode.RED), false),
    ONLY_BREAK_LOGS_PARK(89, Message.SETTING_ONLY_BREAK_LOGS_PARK, new GuiFeatureData(ColorCode.RED, true), false, EnumUtils.FeatureSetting.ENABLE_MESSAGE_WHEN_ACTION_PREVENTED),
    ENABLE_MESSAGE_WHEN_BREAKING_PARK(90, null, false),
    BOSS_APPROACH_ALERT(91, Message.SETTING_BOSS_APPROACH_ALERT, false, EnumUtils.FeatureSetting.REPEATING),
    DISABLE_TELEPORT_PAD_MESSAGES(92, Message.SETTING_DISABLE_TELEPORT_PAD_MESSAGES, false),
    BAIT_LIST(93, Message.SETTING_BAIT_LIST, new GuiFeatureData(EnumUtils.DrawType.BAIT_LIST_DISPLAY, ColorCode.AQUA), true),
    ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT(94, Message.SETTING_ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT, true),
    DISABLE_ENDERMAN_TELEPORTATION_EFFECT(95, Message.SETTING_DISABLE_ENDERMAN_TELEPORTATION_EFFECT, true),
    CHANGE_ZEALOT_COLOR(96, Message.SETTING_CHANGE_ZEALOT_COLOR, new GuiFeatureData(ColorCode.LIGHT_PURPLE), true),
    HIDE_SVEN_PUP_NAMETAGS(97, Message.SETTING_HIDE_SVEN_PUP_NAMETAGS, true),
    REPEAT_SLAYER_BOSS_WARNING(98, null, true),
    DUNGEONS_MAP_DISPLAY(99, Message.SETTING_DUNGEON_MAP_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.DUNGEONS_MAP, ColorCode.BLACK), false, EnumUtils.FeatureSetting.ROTATE_MAP, EnumUtils.FeatureSetting.CENTER_ROTATION_ON_PLAYER, EnumUtils.FeatureSetting.SHOW_PLAYER_HEADS_ON_MAP, EnumUtils.FeatureSetting.MAP_ZOOM),
    ROTATE_MAP(100, Message.SETTING_ROTATE_MAP, false),
    CENTER_ROTATION_ON_PLAYER(101, Message.SETTING_CENTER_ROTATION_ON_PLAYER, false),
    MAP_ZOOM(-1, Message.SETTING_MAP_ZOOM, false),
    MAKE_DROPPED_ITEMS_GLOW(102, Message.SETTING_GLOWING_DROPPED_ITEMS, false, EnumUtils.FeatureSetting.SHOW_GLOWING_ITEMS_ON_ISLAND),
    MAKE_DUNGEON_TEAMMATES_GLOW(103, Message.SETTING_GLOWING_DUNGEON_TEAMMATES, false),
    SHOW_BASE_STAT_BOOST_PERCENTAGE(104, Message.SETTING_SHOW_BASE_STAT_BOOST_PERCENTAGE, new GuiFeatureData(ColorCode.RED, true), false, EnumUtils.FeatureSetting.COLOUR_BY_RARITY),
    BASE_STAT_BOOST_COLOR_BY_RARITY(105, Message.SETTING_COLOR_BY_RARITY, null, true),
    SHOW_PLAYER_HEADS_ON_MAP(106, Message.SETTING_SHOW_PLAYER_HEAD_ON_MAP, null, true),
    SHOW_HEALING_CIRCLE_WALL(107, Message.SETTING_SHOW_HEALING_CIRCLE_WALL, new GuiFeatureData(ColorCode.GREEN, false), true),
    SHOW_CRITICAL_DUNGEONS_TEAMMATES(108, Message.SETTING_SHOW_CRITICAL_TEAMMATES, null, true),
    SHOW_GLOWING_ITEMS_ON_ISLAND(109, Message.SETTING_SHOW_GLOWING_ITEMS_ON_ISLAND, null, false),
    SHOW_ITEM_DUNGEON_FLOOR(110, Message.SETTING_SHOW_ITEM_DUNGEON_FLOOR, new GuiFeatureData(ColorCode.RED, true), false),
    SHOW_DUNGEON_MILESTONE(111, Message.SETTING_SHOW_DUNGEON_MILESTONE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.YELLOW), false),
    DUNGEONS_COLLECTED_ESSENCES_DISPLAY(112, Message.SETTING_DUNGEONS_COLLECTED_ESSENCES_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.YELLOW), false),
    STOP_BONZO_STAFF_SOUNDS(113, Message.SETTING_BONZO_STAFF_SOUNDS, null, true),
    SHOW_RARITY_UPGRADED(114, Message.SETTING_SHOW_RARITY_UPGRADED, new GuiFeatureData(ColorCode.LIGHT_PURPLE, true), false),

    ACTIONS_UNTIL_NEXT_LEVEL(115, null, true),

    REVENANT_SLAYER_TRACKER(116, Message.SETTING_REVENANT_SLAYER_TRACKER, new GuiFeatureData(EnumUtils.DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false, EnumUtils.FeatureSetting.COLOUR_BY_RARITY, EnumUtils.FeatureSetting.TEXT_MODE, EnumUtils.FeatureSetting.HIDE_WHEN_NOT_IN_CRYPTS),
    TARANTULA_SLAYER_TRACKER(117, Message.SETTING_TARANTULA_SLAYER_TRACKER, new GuiFeatureData(EnumUtils.DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false, EnumUtils.FeatureSetting.COLOUR_BY_RARITY, EnumUtils.FeatureSetting.TEXT_MODE, EnumUtils.FeatureSetting.HIDE_WHEN_NOT_IN_SPIDERS_DEN),
    SVEN_SLAYER_TRACKER(118, Message.SETTING_SVEN_SLAYER_TRACKER, new GuiFeatureData(EnumUtils.DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false, EnumUtils.FeatureSetting.COLOUR_BY_RARITY, EnumUtils.FeatureSetting.TEXT_MODE, EnumUtils.FeatureSetting.HIDE_WHEN_NOT_IN_CASTLE),

    REVENANT_COLOR_BY_RARITY(119, null, false),
    TARANTULA_COLOR_BY_RARITY(120, null, false),
    SVEN_COLOR_BY_RARITY(121, null, false),

    REVENANT_TEXT_MODE(122, null, true),
    TARANTULA_TEXT_MODE(123, null, true),
    SVEN_TEXT_MODE(124, null, true),

    DRAGON_STATS_TRACKER(125, Message.SETTING_DRAGON_STATS_TRACKER, new GuiFeatureData(EnumUtils.DrawType.DRAGON_STATS_TRACKER, ColorCode.WHITE), true, EnumUtils.FeatureSetting.COLOUR_BY_RARITY, EnumUtils.FeatureSetting.DRAGONS_NEST_ONLY),
    DRAGON_STATS_TRACKER_COLOR_BY_RARITY(126, null, false),
    DRAGON_STATS_TRACKER_TEXT_MODE(127, null, false),
    DRAGON_STATS_TRACKER_NEST_ONLY(128, null, false),

    ZEALOT_COUNTER_NEST_ONLY(129, null, false),
    SHOW_TOTAL_ZEALOT_COUNT_NEST_ONLY(130, null, false),
    SHOW_SUMMONING_EYE_COUNT_NEST_ONLY(131, null, false),
    SHOW_AVERAGE_ZEALOTS_PER_EYE_NEST_ONLY(132, null, false),

    HIDE_WHEN_NOT_IN_CRYPTS(133, null, false),
    HIDE_WHEN_NOT_IN_SPIDERS_DEN(134, null, false),
    HIDE_WHEN_NOT_IN_CASTLE(135, null, false),

    DUNGEON_DEATH_COUNTER(136, Message.SETTING_DUNGEON_DEATH_COUNTER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.RED), true),
    SHOW_PERSONAL_COMPACTOR_PREVIEW(137, null, false),

    ROCK_PET_TRACKER(138, "settings.rockPetTracker", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GRAY), true),
    DOLPHIN_PET_TRACKER(139, "settings.dolphinPetTracker", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.AQUA), true),

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
    CHROMA_FADE_WIDTH(-1, Message.SETTING_CHROMA_FADE_WIDTH, false),
    TURN_ALL_FEATURES_CHROMA(-1, Message.SETTING_TURN_ALL_FEATURES_CHROMA, false);

    /**
     * These are "features" that are not actually features, but just hold the place of a setting. If you are adding any new settings and create
     * a feature here, make sure to add it!
     */
    private static final Set<Feature> SETTINGS = Sets.newHashSet(DOUBLE_DROP_IN_OTHER_GAMES,
            USE_VANILLA_TEXTURE_DEFENCE, SHOW_BACKPACK_HOLDING_SHIFT, SHOW_MAGMA_TIMER_IN_OTHER_GAMES,
            MAKE_BACKPACK_INVENTORIES_COLORED, CHANGE_BAR_COLOR_FOR_POTIONS, ENABLE_MESSAGE_WHEN_BREAKING_STEMS,
            ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS, ENABLE_MESSAGE_WHEN_MINING_NETHER, HIDE_NIGHT_VISION_EFFECT_TIMER,
            CAKE_BAG_PREVIEW, REPEAT_FULL_INVENTORY_WARNING, SORT_TAB_EFFECT_TIMERS, DOUBLE_WARP,
            REPEAT_SLAYER_BOSS_WARNING, ROTATE_MAP, CENTER_ROTATION_ON_PLAYER, MAP_ZOOM, BASE_STAT_BOOST_COLOR_BY_RARITY,
            SHOW_PLAYER_HEADS_ON_MAP, SHOW_GLOWING_ITEMS_ON_ISLAND, ACTIONS_UNTIL_NEXT_LEVEL, REVENANT_COLOR_BY_RARITY,
            TARANTULA_COLOR_BY_RARITY, SVEN_COLOR_BY_RARITY, REVENANT_TEXT_MODE, TARANTULA_TEXT_MODE, SVEN_TEXT_MODE,
            DRAGON_STATS_TRACKER_COLOR_BY_RARITY, HIDE_WHEN_NOT_IN_CASTLE, HIDE_WHEN_NOT_IN_SPIDERS_DEN,
            HIDE_WHEN_NOT_IN_CRYPTS, SHOW_PERSONAL_COMPACTOR_PREVIEW);

    /**
     * Features that are considered gui ones. This is used for examnple when saving the config to ensure that these features'
     * coordinates and colors are handled properly.
     */
    @Getter private static final Set<Feature> guiFeatures = new LinkedHashSet<>(Arrays.asList(MAGMA_BOSS_TIMER, MANA_BAR, MANA_TEXT, DEFENCE_ICON, DEFENCE_TEXT,
            DEFENCE_PERCENTAGE, HEALTH_BAR, HEALTH_TEXT, SKELETON_BAR, HEALTH_UPDATES, ITEM_PICKUP_LOG, DARK_AUCTION_TIMER, SKILL_DISPLAY, SPEED_PERCENTAGE,
            SLAYER_INDICATOR, POWER_ORB_STATUS_DISPLAY, ZEALOT_COUNTER, TICKER_CHARGES_DISPLAY, TAB_EFFECT_TIMERS, SHOW_TOTAL_ZEALOT_COUNT, SHOW_SUMMONING_EYE_COUNT,
            SHOW_AVERAGE_ZEALOTS_PER_EYE, BIRCH_PARK_RAINMAKER_TIMER, COMBAT_TIMER_DISPLAY, ENDSTONE_PROTECTOR_DISPLAY, BAIT_LIST, DUNGEONS_MAP_DISPLAY, SHOW_DUNGEON_MILESTONE,
            DUNGEONS_COLLECTED_ESSENCES_DISPLAY, REVENANT_SLAYER_TRACKER, TARANTULA_SLAYER_TRACKER, SVEN_SLAYER_TRACKER, DRAGON_STATS_TRACKER, DUNGEON_DEATH_COUNTER,
            ROCK_PET_TRACKER, DOLPHIN_PET_TRACKER));

    /**
     * These are features that are displayed separate, on the general tab.
     */
    @Getter private static final Set<Feature> generalTabFeatures = new LinkedHashSet<>(Arrays.asList(TEXT_STYLE, WARNING_TIME, CHROMA_SPEED, CHROMA_MODE,
            CHROMA_FADE_WIDTH, TURN_ALL_FEATURES_CHROMA));

    private static final int ID_AT_PREVIOUS_UPDATE = 63;

    private int id;
    private Message message;
    private List<EnumUtils.FeatureSetting> settings;
    private GuiFeatureData guiFeatureData;
    private boolean defaultDisabled;
    private String messagePath;

    Feature(int id, Message settingMessage, GuiFeatureData guiFeatureData, boolean defaultDisabled, EnumUtils.FeatureSetting... settings) { // color & gui scale settings added automatically
        this.id = id;
        this.message = settingMessage;
        this.settings = new ArrayList<>(Arrays.asList(settings));
        this.guiFeatureData = guiFeatureData;
        this.defaultDisabled = defaultDisabled;

        Set<Integer> registeredFeatureIDs = SkyblockAddons.getInstance().getRegisteredFeatureIDs();
        if (id != -1 && registeredFeatureIDs.contains(id)) {
            throw new RuntimeException("Multiple features have the same IDs!");
        } else {
            registeredFeatureIDs.add(id);
        }
    }

    Feature(int id, String messagePath, GuiFeatureData guiFeatureData, boolean defaultDisabled, EnumUtils.FeatureSetting... settings) {
        this.id = id;
        this.messagePath = messagePath;
        this.settings = new ArrayList<>(Arrays.asList(settings));
        this.guiFeatureData = guiFeatureData;
        this.defaultDisabled = defaultDisabled;

        Set<Integer> registeredFeatureIDs = SkyblockAddons.getInstance().getRegisteredFeatureIDs();
        if (id != -1 && registeredFeatureIDs.contains(id)) {
            throw new RuntimeException("Multiple features have the same IDs!");
        } else {
            registeredFeatureIDs.add(id);
        }
    }

    Feature(int id, Message settingMessage, boolean defaultDisabled, EnumUtils.FeatureSetting... settings) {
        this(id, settingMessage,null, defaultDisabled, settings);
    }

    public boolean isActualFeature() {
        return id != -1 && getMessage() != null && !SETTINGS.contains(this);
    }

    public String getMessage(String... variables) {
        if (message != null) {
            return message.getMessage(variables);
        } else if (messagePath != null) {
            return Translations.getMessage(messagePath, (Object[]) variables);
        }
        return null;
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
            } else if(guiFeatureData.getDrawType() == EnumUtils.DrawType.BAIT_LIST_DISPLAY) {
                main.getRenderListener().drawBaitList(mc, scale, buttonLocation);
            } else if(guiFeatureData.getDrawType() == EnumUtils.DrawType.DUNGEONS_MAP) {
                main.getRenderListener().drawDungeonsMap(mc, scale, buttonLocation);
            } else if(guiFeatureData.getDrawType() == EnumUtils.DrawType.SLAYER_TRACKERS) {
                main.getRenderListener().drawSlayerTrackers(this, mc, scale, buttonLocation);
            } else if(guiFeatureData.getDrawType() == EnumUtils.DrawType.DRAGON_STATS_TRACKER) {
                main.getRenderListener().drawDragonTrackers(mc, scale, buttonLocation);
            }
        }
    }

    public ColorCode getDefaultColor() {
        if (guiFeatureData != null) {
            return guiFeatureData.getDefaultColor();
        }
        return null;
    }

    public boolean isNew() {
        return id > ID_AT_PREVIOUS_UPDATE && this != HIDE_GREY_ENCHANTS;
    }
}
