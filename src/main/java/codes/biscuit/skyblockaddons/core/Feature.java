package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.EntityOutlines.FeatureTrackerQuest;
import codes.biscuit.skyblockaddons.features.dungeonmap.DungeonMapManager;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.misc.SkyblockKeyBinding;
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
    SHOW_BACKPACK_PREVIEW(3, Message.SETTING_SHOW_BACKPACK_PREVIEW, false, EnumUtils.FeatureSetting.BACKPACK_STYLE, EnumUtils.FeatureSetting.SHOW_ONLY_WHEN_HOLDING_SHIFT, EnumUtils.FeatureSetting.MAKE_INVENTORY_COLORED, EnumUtils.FeatureSetting.ENABLE_CAKE_BAG_PREVIEW, EnumUtils.FeatureSetting.ENABLE_PERSONAL_COMPACTOR_PREVIEW, EnumUtils.FeatureSetting.SHOW_ENDER_CHEST_PREVIEW),
    HIDE_BONES(4, Message.SETTING_HIDE_SKELETON_HAT_BONES, false),
    SKELETON_BAR(5, Message.SETTING_SKELETON_HAT_BONES_BAR, new GuiFeatureData(EnumUtils.DrawType.SKELETON_BAR), false),
    HIDE_FOOD_ARMOR_BAR(6, Message.SETTING_HIDE_FOOD_AND_ARMOR, false),
    FULL_INVENTORY_WARNING(7, Message.SETTING_FULL_INVENTORY_WARNING, new GuiFeatureData(ColorCode.RED), false, EnumUtils.FeatureSetting.REPEATING),
    MAGMA_BOSS_TIMER(8, Message.SETTING_MAGMA_BOSS_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GOLD, false), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    SHOW_REFORGE_OVERLAY(10, Message.SETTING_ENCHANTS_AND_REFORGES, false),
    MINION_STOP_WARNING(11, Message.SETTING_MINION_STOP_WARNING, new GuiFeatureData(ColorCode.RED), true),
    HIDE_HEALTH_BAR(13, Message.SETTING_HIDE_HEALTH_BAR, true),
    DOUBLE_DROP_IN_OTHER_GAMES(14, null, false),
    MINION_FULL_WARNING(15, Message.SETTING_FULL_MINION, new GuiFeatureData(ColorCode.RED), false),
    USE_VANILLA_TEXTURE_DEFENCE(17, Message.SETTING_USE_VANILLA_TEXTURE, true),
    SHOW_BACKPACK_HOLDING_SHIFT(18, Message.SETTING_SHOW_ONLY_WHEN_HOLDING_SHIFT, true),
    MANA_BAR(19, Message.SETTING_MANA_BAR, new GuiFeatureData(EnumUtils.DrawType.BAR, ColorCode.BLUE), false),
    MANA_TEXT(20, Message.SETTING_MANA_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.BLUE), false),
    HEALTH_BAR(21, Message.SETTING_HEALTH_BAR, new GuiFeatureData(EnumUtils.DrawType.BAR, ColorCode.RED), true, EnumUtils.FeatureSetting.CHANGE_BAR_COLOR_WITH_POTIONS, EnumUtils.FeatureSetting.HEALTH_PREDICTION),
    HEALTH_TEXT(22, Message.SETTING_HEALTH_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.RED), false),
    DEFENCE_ICON(23, Message.SETTING_DEFENCE_ICON, new GuiFeatureData(EnumUtils.DrawType.DEFENCE_ICON), false, EnumUtils.FeatureSetting.USE_VANILLA_TEXTURE),
    DEFENCE_TEXT(24, Message.SETTING_DEFENCE_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GREEN), false, EnumUtils.FeatureSetting.OTHER_DEFENCE_STATS),
    DEFENCE_PERCENTAGE(25, Message.SETTING_DEFENCE_PERCENTAGE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GREEN), true),
    HEALTH_UPDATES(26, Message.SETTING_HEALTH_UPDATES, new GuiFeatureData(EnumUtils.DrawType.TEXT), false), // Health updates all credit to DidiSkywalker#9975
    HIDE_PLAYERS_IN_LOBBY(27, Message.SETTING_HIDE_PLAYERS_IN_LOBBY, true),
    DARK_AUCTION_TIMER(28, Message.SETTING_DARK_AUCTION_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GOLD), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    ITEM_PICKUP_LOG(29, Message.SETTING_ITEM_PICKUP_LOG, new GuiFeatureData(EnumUtils.DrawType.PICKUP_LOG), false),
    SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES(33, null, false),
    SHOW_ITEM_ANVIL_USES(34, Message.SETTING_SHOW_ITEM_ANVIL_USES, new GuiFeatureData(ColorCode.RED, true), false),
    SHOW_MAGMA_TIMER_IN_OTHER_GAMES(36, null, true),
    DONT_RESET_CURSOR_INVENTORY(37, Message.SETTING_DONT_RESET_CURSOR_INVENTORY, false),
    LOCK_SLOTS(38, Message.SETTING_LOCK_SLOTS, false),
    SUMMONING_EYE_ALERT(39, Message.SETTING_SUMMONING_EYE_ALERT, new GuiFeatureData(ColorCode.RED), false),
    MAKE_ENDERCHESTS_GREEN_IN_END(40, Message.SETTING_MAKE_ENDERCHESTS_IN_END_GREEN, new GuiFeatureData(ColorCode.GREEN), false),
    STOP_DROPPING_SELLING_RARE_ITEMS(42, Message.SETTING_STOP_DROPPING_SELLING_RARE_ITEMS, new GuiFeatureData(ColorCode.RED, true), false),
    MAKE_BACKPACK_INVENTORIES_COLORED(43, Message.SETTING_MAKE_BACKPACK_INVENTORIES_COLORED, false),
    REPLACE_ROMAN_NUMERALS_WITH_NUMBERS(45, Message.SETTING_REPLACE_ROMAN_NUMERALS_WITH_NUMBERS, true, EnumUtils.FeatureSetting.DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME),
    CHANGE_BAR_COLOR_FOR_POTIONS(46, Message.SETTING_CHANGE_BAR_COLOR_WITH_POTIONS, false),
    //CRAFTING_PATTERNS(47, Message.SETTING_CRAFTING_PATTERNS, false),
    FISHING_SOUND_INDICATOR(48, Message.SETTING_FISHING_SOUND_INDICATOR, false),
    AVOID_BLINKING_NIGHT_VISION(49, Message.SETTING_AVOID_BLINKING_NIGHT_VISION, false),
    MINION_DISABLE_LOCATION_WARNING(50, Message.SETTING_DISABLE_MINION_LOCATION_WARNING, false),
    ENCHANTMENT_LORE_PARSING(52, "settings.enchantmentLoreParsing", null, false, EnumUtils.FeatureSetting.HIGHLIGHT_ENCHANTMENTS, EnumUtils.FeatureSetting.PERFECT_ENCHANT_COLOR, EnumUtils.FeatureSetting.GREAT_ENCHANT_COLOR, EnumUtils.FeatureSetting.GOOD_ENCHANT_COLOR, EnumUtils.FeatureSetting.POOR_ENCHANT_COLOR, EnumUtils.FeatureSetting.COMMA_ENCHANT_COLOR, EnumUtils.FeatureSetting.ENCHANT_LAYOUT, EnumUtils.FeatureSetting.HIDE_ENCHANTMENT_LORE, EnumUtils.FeatureSetting.HIDE_GREY_ENCHANTS),
    SHOW_ITEM_COOLDOWNS(53, Message.SETTING_SHOW_ITEM_COOLDOWNS, false),
    SKILL_DISPLAY(54, Message.SETTING_COLLECTION_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.AQUA), false, EnumUtils.FeatureSetting.SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL, EnumUtils.FeatureSetting.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP, EnumUtils.FeatureSetting.SHOW_SKILL_XP_GAINED, EnumUtils.FeatureSetting.ABBREVIATE_SKILL_XP_DENOMINATOR),
    SPEED_PERCENTAGE(55, Message.SETTING_SPEED_PERCENTAGE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.WHITE), false),
    SLAYER_INDICATOR(57, Message.SETTING_SLAYER_INDICATOR, new GuiFeatureData(EnumUtils.DrawType.REVENANT_PROGRESS, ColorCode.AQUA), true),
    SPECIAL_ZEALOT_ALERT(58, Message.SETTING_SPECIAL_ZEALOT_ALERT, new GuiFeatureData(ColorCode.RED), false),
    ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS(60, null, false),
    ENABLE_MESSAGE_WHEN_BREAKING_STEMS(61, null, false),
    ENABLE_MESSAGE_WHEN_MINING_NETHER(62, null, false),
    HIDE_PET_HEALTH_BAR(63, Message.SETTING_HIDE_PET_HEALTH_BAR, false),
    // Release v1.4
    DISABLE_MAGICAL_SOUP_MESSAGES(64, Message.SETTING_DISABLE_MAGICAL_SOUP_MESSAGE, true),
    POWER_ORB_STATUS_DISPLAY(65, Message.SETTING_POWER_ORB_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.POWER_ORB_DISPLAY, null), false, EnumUtils.FeatureSetting.POWER_ORB_DISPLAY_STYLE),
    ZEALOT_COUNTER(66, Message.SETTING_ZEALOT_COUNTER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), false, EnumUtils.FeatureSetting.DRAGONS_NEST_ONLY),
    TICKER_CHARGES_DISPLAY(67, Message.SETTING_TICKER_CHARGES_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TICKER, null), false),
    TAB_EFFECT_TIMERS(68, Message.SETTING_TAB_EFFECT_TIMERS, new GuiFeatureData(EnumUtils.DrawType.TAB_EFFECT_TIMERS, ColorCode.WHITE), true, EnumUtils.FeatureSetting.HIDE_NIGHT_VISION_EFFECT, EnumUtils.FeatureSetting.SORT_TAB_EFFECT_TIMERS),
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
    HIDE_GREY_ENCHANTS(87, Message.SETTING_HIDE_GREY_ENCHANTS, false),
    LEGENDARY_SEA_CREATURE_WARNING(88, Message.SETTING_LEGENDARY_SEA_CREATURE_WARNING, new GuiFeatureData(ColorCode.RED), false),
    ENABLE_MESSAGE_WHEN_BREAKING_PARK(90, null, false),
    BOSS_APPROACH_ALERT(91, Message.SETTING_BOSS_APPROACH_ALERT, false, EnumUtils.FeatureSetting.REPEATING),
    DISABLE_TELEPORT_PAD_MESSAGES(92, Message.SETTING_DISABLE_TELEPORT_PAD_MESSAGES, false),
    BAIT_LIST(93, Message.SETTING_BAIT_LIST, new GuiFeatureData(EnumUtils.DrawType.BAIT_LIST_DISPLAY, ColorCode.AQUA), true),
    ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT(94, Message.SETTING_ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT, true),
    DISABLE_ENDERMAN_TELEPORTATION_EFFECT(95, Message.SETTING_DISABLE_ENDERMAN_TELEPORTATION_EFFECT, true),
    CHANGE_ZEALOT_COLOR(96, Message.SETTING_CHANGE_ZEALOT_COLOR, new GuiFeatureData(ColorCode.LIGHT_PURPLE), true),
    HIDE_SVEN_PUP_NAMETAGS(97, Message.SETTING_HIDE_SVEN_PUP_NAMETAGS, true),
    REPEAT_SLAYER_BOSS_WARNING(98, null, true),
    // Release v1.5
    DUNGEONS_MAP_DISPLAY(99, Message.SETTING_DUNGEON_MAP_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.DUNGEONS_MAP, ColorCode.BLACK), false, EnumUtils.FeatureSetting.ROTATE_MAP, EnumUtils.FeatureSetting.CENTER_ROTATION_ON_PLAYER, EnumUtils.FeatureSetting.SHOW_PLAYER_HEADS_ON_MAP, EnumUtils.FeatureSetting.MAP_ZOOM, EnumUtils.FeatureSetting.CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD),
    ROTATE_MAP(100, Message.SETTING_ROTATE_MAP, false),
    CENTER_ROTATION_ON_PLAYER(101, Message.SETTING_CENTER_ROTATION_ON_PLAYER, false),
    MAP_ZOOM(-1, Message.SETTING_MAP_ZOOM, false),
    MAKE_DROPPED_ITEMS_GLOW(102, Message.SETTING_GLOWING_DROPPED_ITEMS, false, EnumUtils.FeatureSetting.SHOW_GLOWING_ITEMS_ON_ISLAND),
    MAKE_DUNGEON_TEAMMATES_GLOW(103, Message.SETTING_GLOWING_DUNGEON_TEAMMATES, false),
    SHOW_BASE_STAT_BOOST_PERCENTAGE(104, Message.SETTING_SHOW_BASE_STAT_BOOST_PERCENTAGE, new GuiFeatureData(ColorCode.RED, true), false, EnumUtils.FeatureSetting.COLOUR_BY_RARITY),
    BASE_STAT_BOOST_COLOR_BY_RARITY(105, Message.SETTING_COLOR_BY_RARITY, null, true),
    SHOW_PLAYER_HEADS_ON_MAP(106, Message.SETTING_SHOW_PLAYER_HEAD_ON_MAP, null, true),
    SHOW_HEALING_CIRCLE_WALL(107, Message.SETTING_SHOW_HEALING_CIRCLE_WALL, new GuiFeatureData(ColorCode.GREEN, false), true, EnumUtils.FeatureSetting.HEALING_CIRCLE_OPACITY),
    SHOW_CRITICAL_DUNGEONS_TEAMMATES(108, Message.SETTING_SHOW_CRITICAL_TEAMMATES, null, true),
    SHOW_GLOWING_ITEMS_ON_ISLAND(109, Message.SETTING_SHOW_GLOWING_ITEMS_ON_ISLAND, null, false),
    SHOW_ITEM_DUNGEON_FLOOR(110, Message.SETTING_SHOW_ITEM_DUNGEON_FLOOR, new GuiFeatureData(ColorCode.RED, true), false),
    SHOW_DUNGEON_MILESTONE(111, Message.SETTING_SHOW_DUNGEON_MILESTONE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.YELLOW), false),
    DUNGEONS_COLLECTED_ESSENCES_DISPLAY(112, Message.SETTING_DUNGEONS_COLLECTED_ESSENCES_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.YELLOW), false, EnumUtils.FeatureSetting.SHOW_SALVAGE_ESSENCES_COUNTER, EnumUtils.FeatureSetting.RESET_SALVAGED_ESSENCES_AFTER_LEAVING_MENU),
    STOP_BONZO_STAFF_SOUNDS(113, Message.SETTING_BONZO_STAFF_SOUNDS, null, true),
    SHOW_RARITY_UPGRADED(114, Message.SETTING_SHOW_RARITY_UPGRADED, new GuiFeatureData(ColorCode.LIGHT_PURPLE, true), false),
    SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL(115, null, true),
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
    SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY(140, "settings.dungeonsTeammateNameOverlay", null, false),
    SHOW_STACKING_ENCHANT_PROGRESS(141, "settings.stackingEnchantProgress", new GuiFeatureData(ColorCode.RED, true), false),
    DUNGEONS_SECRETS_DISPLAY(142, "settings.dungeonsSecretsDisplay", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GRAY), false),
    SKILL_PROGRESS_BAR(143, "settings.skillProgressBar", new GuiFeatureData(EnumUtils.DrawType.BAR, ColorCode.GREEN), true),
    SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP(144, null, true),
    SHOW_SKILL_XP_GAINED(145, null, false),
    SHOW_SALVAGE_ESSENCES_COUNTER(146, null, false),
    DISABLE_MORT_MESSAGES(147, "settings.disableMortMessages", null, false),
    DISABLE_BOSS_MESSAGES(148, "settings.disableBossMessages", null, false),
    SHOW_SWORD_KILLS(149, "settings.showSwordKills", new GuiFeatureData(ColorCode.RED, true), false),
    HIDE_OTHER_PLAYERS_PRESENTS(150, "settings.hideOtherPlayersPresents", null, false),
    COMPACT_TAB_LIST(152, "settings.compactTabList", null, false),
    ENCHANTMENTS_HIGHLIGHT(153, "settings.highlightSpecialEnchantments", null, false),
    CANDY_POINTS_COUNTER(155, "settings.candyPointsCounter", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GOLD), false),
    HEALING_CIRCLE_OPACITY(156, "settings.healingCircleOpacity", null, false),
    USE_NEW_CHROMA_EFFECT(157, "settings.useNewChromaEffect", null, false),
    SHOW_EXPERIMENTATION_TABLE_TOOLTIPS(158, "settings.showExperimentationTableTooltips", null, true),
    DRILL_FUEL_BAR(160, "settings.drillFuelBar", new GuiFeatureData(EnumUtils.DrawType.BAR, ColorCode.DARK_GREEN), false),
    DRILL_FUEL_TEXT(161, "settings.drillFuelNumber", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_GREEN), false),
    FISHING_PARTICLE_OVERLAY(162, "settings.fishingParticleOverlay", new GuiFeatureData(ColorCode.WHITE), false, EnumUtils.FeatureSetting.BIGGER_WAKE),
    COOLDOWN_PREDICTION(164, "settings.cooldownPrediction", null, false),
    ENCHANTMENT_PERFECT_COLOR(165, "enchants.superTier", new GuiFeatureData(ColorCode.CHROMA, true), false),
    ENCHANTMENT_GREAT_COLOR(166, "enchants.highTier", new GuiFeatureData(ColorCode.GOLD, true), false),
    ENCHANTMENT_GOOD_COLOR(167, "enchants.midTier", new GuiFeatureData(ColorCode.BLUE, true), false),
    ENCHANTMENT_POOR_COLOR(168, "enchants.lowTier", new GuiFeatureData(ColorCode.GRAY, true), false),
    LEG_MONKEY_LEVEL_100(169, "settings.legendaryMonkeyLevel100", null, true),
    BIGGER_WAKE(170, "settings.biggerWake", null, false),
    ENCHANTMENT_COMMA_COLOR(171, "enchants.commas", new GuiFeatureData(ColorCode.BLUE, true), false),
    REFORGE_FILTER(172, "settings.reforgeFilter", null, false),
    TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR(173, "settings.trevorTheTrapper.trackedEntityProximityIndicator", new GuiFeatureData(EnumUtils.DrawType.PROXIMITY_INDICATOR, null), false),
    TREVOR_HIGHLIGHT_TRACKED_ENTITY(174, "settings.trevorTheTrapper.highlightTrackedEntity", null, false),
    TREVOR_SHOW_QUEST_COOLDOWN(175, "settings.trevorTheTrapper.showQuestCooldown", null, false),
    HIDE_ENCHANT_DESCRIPTION(176, "settings.hideEnchantDescription", null, true),
    TREVOR_THE_TRAPPER_FEATURES(177, "settings.trevorTheTrapper.title", null, false, EnumUtils.FeatureSetting.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR, EnumUtils.FeatureSetting.TREVOR_HIGHLIGHT_TRACKED_ENTITY, EnumUtils.FeatureSetting.TREVOR_SHOW_QUEST_COOLDOWN),
    FETCHUR_TODAY(178, Message.SETTING_SHOW_FETCHUR_TODAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GREEN), false, EnumUtils.FeatureSetting.SHOW_FETCHUR_ITEM_NAME, EnumUtils.FeatureSetting.SHOW_FETCHUR_ONLY_IN_DWARVENS, EnumUtils.FeatureSetting.SHOW_FETCHUR_INVENTORY_OPEN_ONLY, EnumUtils.FeatureSetting.WARN_WHEN_FETCHUR_CHANGES),
    SHOW_FETCHUR_ONLY_IN_DWARVENS(179, Message.SETTING_SHOW_FETCHUR_IN_DWARVEN_ONLY, true),
    SHOW_FETCHUR_ITEM_NAME(180, Message.SETTING_SHOW_FETCHUR_TODAY, true),
    SHOW_FETCHUR_INVENTORY_OPEN_ONLY(181, Message.SETTING_SHOW_FETCHUR_INVENTORY_OPEN_ONLY, true),
    WARN_WHEN_FETCHUR_CHANGES(182, Message.SETTING_WARN_WHEN_FETCHUR_CHANGES, new GuiFeatureData(ColorCode.RED), true),
    STOP_RAT_SOUNDS(183, Message.SETTING_STOP_RAT_SOUNDS, true, EnumUtils.FeatureSetting.STOP_ONLY_RAT_SQUEAK),
    STOP_ONLY_RAT_SQUEAK(184, Message.SETTING_STOP_ONLY_RAT_SQUEAK, true),
    SHOW_ENDER_CHEST_PREVIEW(185, "settings.showEnderChestPreview", null, false),
    VOIDGLOOM_SLAYER_TRACKER(186, "settings.voidgloomSlayerTracker", new GuiFeatureData(EnumUtils.DrawType.SLAYER_TRACKERS, ColorCode.WHITE), false, EnumUtils.FeatureSetting.COLOUR_BY_RARITY, EnumUtils.FeatureSetting.TEXT_MODE, EnumUtils.FeatureSetting.HIDE_WHEN_NOT_IN_END),
    HIDE_WHEN_NOT_IN_END(187, null, false),
    ENDERMAN_COLOR_BY_RARITY(188, null, false),
    ENDERMAN_TEXT_MODE(189, null, true),
    HIDE_PLAYERS_NEAR_NPCS(190, Message.SETTING_HIDE_PLAYERS_NEAR_NPCS, false),
    OVERFLOW_MANA(191, "settings.showOverflowManaNumber", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_AQUA), false),
    DOUBLE_WARP(192, Message.SETTING_DOUBLE_WARP, true),
    JUNGLE_AXE_COOLDOWN(193, Message.SETTING_JUNGLE_AXE_COOLDOWN, true, EnumUtils.FeatureSetting.COOLDOWN_PREDICTION, EnumUtils.FeatureSetting.LEVEL_100_LEG_MONKEY),
    HEALTH_PREDICTION(194, "settings.vanillaHealthPrediction", null, true),
    DISABLE_EMPTY_GLASS_PANES(195, "settings.hideMenuGlassPanes", null, false),
    ENTITY_OUTLINES(196, "settings.entityOutlines", null, false, EnumUtils.FeatureSetting.DUNGEON_PLAYER_GLOW, EnumUtils.FeatureSetting.ITEM_GLOW, EnumUtils.FeatureSetting.TREVOR_HIGHLIGHT_TRACKED_ENTITY),
    EFFECTIVE_HEALTH_TEXT(197, "settings.effectiveHealthNumber", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.DARK_GREEN), false),
    ABBREVIATE_SKILL_XP_DENOMINATOR(198, "settings.abbreviateSkillXpDenominator", null, true),
    OTHER_DEFENCE_STATS(199, "settings.otherDefenseStats", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GREEN), false),
    // Release v1.6
    PREVENT_MOVEMENT_ON_DEATH(200, Message.SETTING_PREVENT_MOVEMENT_ON_DEATH, true),
    HIDE_SPAWN_POINT_PLAYERS(201, "settings.hideSpawnPointPlayers", null, true),
    SPIRIT_SCEPTRE_DISPLAY(202, "settings.showSpiritSceptreDisplay", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GRAY), false, EnumUtils.FeatureSetting.DISABLE_SPIRIT_SCEPTRE_MESSAGES),
    DISABLE_SPIRIT_SCEPTRE_MESSAGES(203, null, false),
    FARM_EVENT_TIMER(204, "settings.jacobsContestTimer", new GuiFeatureData(EnumUtils.DrawType.TEXT, ColorCode.GOLD), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    SHOW_FARM_EVENT_TIMER_IN_OTHER_GAMES(205, null, false),
    OUTBID_ALERT_SOUND(206, Message.SETTING_OUTBID_ALERT_SOUND,true, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    BROOD_MOTHER_ALERT(207, Message.SETTING_BROOD_MOTHER_WARNING, false),
    BAL_BOSS_ALERT(208, Message.SETTING_BAL_BOSS_WARNING, false),
    OUTBID_ALERT_SOUND_IN_OTHER_GAMES(209, null, false),
    DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME(210, "settings.dontReplaceRomanNumeralsInItemNames", null, true),
    BACKPACK_OPENING_SOUND(211, "settings.backpackOpeningSound", null, false),
    DEVELOPER_MODE(212, "settings.devMode", null, true),
    SHOW_SKYBLOCK_ITEM_ID(213, "settings.showSkyblockItemId", null, true),
    RESET_SALVAGED_ESSENCES_AFTER_LEAVING_MENU(214, "settings.resetSalvagedEssencesAfterLeavingMenu", null, false),
    CHANGE_DUNGEON_MAP_ZOOM_WITH_KEYBOARD(215, "settings.changeDungeonMapZoomWithKeyboard", null, false),

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
    SHOW_FEATURE_NAMES_ON_HOVER(-1, "settings.showFeatureNamesOnHover", null,true),
    GENERAL_SETTINGS(-1, Message.TAB_GENERAL_SETTINGS, false),
    TEXT_STYLE(-1, Message.SETTING_TEXT_STYLE, false),
    CHROMA_SPEED(-1, Message.SETTING_CHROMA_SPEED, false),
    CHROMA_MODE(-1, Message.SETTING_CHROMA_MODE, false),
    @Deprecated CHROMA_FADE_WIDTH(-1, Message.SETTING_CHROMA_FADE_WIDTH, false),
    CHROMA_SIZE(-1, "settings.chromaSize", null, false),
    CHROMA_SATURATION(-1, "settings.chromaSaturation", null, false),
    CHROMA_BRIGHTNESS(-1, "settings.chromaBrightness", null, false),
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
            SHOW_PLAYER_HEADS_ON_MAP, SHOW_GLOWING_ITEMS_ON_ISLAND, SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL, REVENANT_COLOR_BY_RARITY,
            TARANTULA_COLOR_BY_RARITY, SVEN_COLOR_BY_RARITY, REVENANT_TEXT_MODE, TARANTULA_TEXT_MODE, SVEN_TEXT_MODE,
            DRAGON_STATS_TRACKER_COLOR_BY_RARITY, HIDE_WHEN_NOT_IN_CASTLE, HIDE_WHEN_NOT_IN_SPIDERS_DEN,
            HIDE_WHEN_NOT_IN_CRYPTS, SHOW_PERSONAL_COMPACTOR_PREVIEW, SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP, SHOW_SKILL_XP_GAINED,
            SHOW_SALVAGE_ESSENCES_COUNTER, HEALING_CIRCLE_OPACITY, COOLDOWN_PREDICTION, ENCHANTMENTS_HIGHLIGHT,
            ENCHANTMENT_COMMA_COLOR, ENCHANTMENT_PERFECT_COLOR, ENCHANTMENT_GREAT_COLOR, ENCHANTMENT_GOOD_COLOR,
            ENCHANTMENT_POOR_COLOR, BIGGER_WAKE, LEG_MONKEY_LEVEL_100, HIDE_ENCHANT_DESCRIPTION, HIDE_GREY_ENCHANTS,
            TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR, TREVOR_HIGHLIGHT_TRACKED_ENTITY, TREVOR_SHOW_QUEST_COOLDOWN,
            SHOW_FETCHUR_ONLY_IN_DWARVENS, SHOW_FETCHUR_ITEM_NAME, SHOW_FETCHUR_INVENTORY_OPEN_ONLY, WARN_WHEN_FETCHUR_CHANGES,
            STOP_ONLY_RAT_SQUEAK, SHOW_ENDER_CHEST_PREVIEW, HEALTH_PREDICTION, ABBREVIATE_SKILL_XP_DENOMINATOR, OTHER_DEFENCE_STATS,
            DISABLE_SPIRIT_SCEPTRE_MESSAGES, OUTBID_ALERT_SOUND_IN_OTHER_GAMES, DONT_REPLACE_ROMAN_NUMERALS_IN_ITEM_NAME,
            RESET_SALVAGED_ESSENCES_AFTER_LEAVING_MENU);

    /**
     * Features that are considered gui ones. This is used for examnple when saving the config to ensure that these features'
     * coordinates and colors are handled properly.
     */
    @Getter
    private static final Set<Feature> guiFeatures = new LinkedHashSet<>(Arrays.asList(DRILL_FUEL_BAR, SKILL_PROGRESS_BAR, MANA_BAR, HEALTH_BAR,
            MAGMA_BOSS_TIMER, MANA_TEXT, OVERFLOW_MANA, DEFENCE_ICON, DEFENCE_TEXT, EFFECTIVE_HEALTH_TEXT,
            DEFENCE_PERCENTAGE, HEALTH_TEXT, SKELETON_BAR, HEALTH_UPDATES, ITEM_PICKUP_LOG, DARK_AUCTION_TIMER, SKILL_DISPLAY, SPEED_PERCENTAGE,
            SLAYER_INDICATOR, POWER_ORB_STATUS_DISPLAY, ZEALOT_COUNTER, TICKER_CHARGES_DISPLAY, TAB_EFFECT_TIMERS, SHOW_TOTAL_ZEALOT_COUNT, SHOW_SUMMONING_EYE_COUNT,
            SHOW_AVERAGE_ZEALOTS_PER_EYE, BIRCH_PARK_RAINMAKER_TIMER, COMBAT_TIMER_DISPLAY, ENDSTONE_PROTECTOR_DISPLAY, BAIT_LIST, DUNGEONS_MAP_DISPLAY, SHOW_DUNGEON_MILESTONE,
            DUNGEONS_COLLECTED_ESSENCES_DISPLAY, REVENANT_SLAYER_TRACKER, TARANTULA_SLAYER_TRACKER, SVEN_SLAYER_TRACKER, DRAGON_STATS_TRACKER, DUNGEON_DEATH_COUNTER,
            ROCK_PET_TRACKER, DOLPHIN_PET_TRACKER, DUNGEONS_SECRETS_DISPLAY, CANDY_POINTS_COUNTER, DRILL_FUEL_TEXT,
            TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR, FETCHUR_TODAY, VOIDGLOOM_SLAYER_TRACKER, OTHER_DEFENCE_STATS, SPIRIT_SCEPTRE_DISPLAY, FARM_EVENT_TIMER));

    /**
     * These are features that are displayed separate, on the general tab.
     */
    @Getter
    private static final Set<Feature> generalTabFeatures = new LinkedHashSet<>(Arrays.asList(TEXT_STYLE, WARNING_TIME, CHROMA_SPEED, CHROMA_MODE,
            CHROMA_SIZE, TURN_ALL_FEATURES_CHROMA, CHROMA_SATURATION, CHROMA_BRIGHTNESS, USE_NEW_CHROMA_EFFECT, DEVELOPER_MODE));

    private static final int ID_AT_PREVIOUS_UPDATE = 199;

    private final int id;
    private Message message;
    private final List<EnumUtils.FeatureSetting> settings;
    private final GuiFeatureData guiFeatureData;
    private final boolean defaultDisabled;
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

    /**
     * Called when a features enable state is changed.
     */
    public void onToggle() {
        if (this.id == DEVELOPER_MODE.id) {
            SkyblockAddons main = SkyblockAddons.getInstance();
            SkyblockKeyBinding devModeKeyBinding = main.getDeveloperCopyNBTKey();

            if (main.getConfigValues().isEnabled(DEVELOPER_MODE)) {
                devModeKeyBinding.register();
            } else if (devModeKeyBinding.isRegistered()) {
                devModeKeyBinding.deRegister();
            }
        }
    }

    /**
     * Sets whether the current feature is enabled.
     *
     * @param enabled {@code true} to enable the feature, {@code false} to disable it
     */
    public void setEnabled(boolean enabled) {
        if (enabled) {
            SkyblockAddons.getInstance().getConfigValues().getDisabledFeatures().remove(this);
        } else {
            SkyblockAddons.getInstance().getConfigValues().getDisabledFeatures().add(this);
        }
        onToggle();
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
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.TICKER) {
                main.getRenderListener().drawScorpionFoilTicker(mc, scale, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.PROXIMITY_INDICATOR) {
                FeatureTrackerQuest.drawTrackerLocationIndicator(mc, scale, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.TAB_EFFECT_TIMERS) {
                main.getRenderListener().drawPotionEffectTimers(scale, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.BAIT_LIST_DISPLAY) {
                main.getRenderListener().drawBaitList(mc, scale, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.DUNGEONS_MAP) {
                DungeonMapManager.drawDungeonsMap(mc, scale, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.SLAYER_TRACKERS) {
                main.getRenderListener().drawSlayerTrackers(this, mc, scale, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.DRAGON_STATS_TRACKER) {
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
        return id > ID_AT_PREVIOUS_UPDATE;
    }
}
