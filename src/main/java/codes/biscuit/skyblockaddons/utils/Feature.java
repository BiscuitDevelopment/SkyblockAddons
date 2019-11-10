package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLLog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings({"deprecation", "DeprecatedIsStillUsed"})
public enum Feature {

    MAGMA_WARNING(0, Message.SETTING_MAGMA_BOSS_WARNING, new GuiFeatureData(ConfigColor.RED), false),
    DROP_CONFIRMATION(1, Message.SETTING_ITEM_DROP_CONFIRMATION, true, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    DISABLE_EMBER_ROD(2, Message.SETTING_DISABLE_EMBER_ROD_ABILITY, false),
    SHOW_BACKPACK_PREVIEW(3, Message.SETTING_SHOW_BACKPACK_PREVIEW, false, EnumUtils.FeatureSetting.BACKPACK_STYLE, EnumUtils.FeatureSetting.SHOW_ONLY_WHEN_HOLDING_SHIFT, EnumUtils.FeatureSetting.MAKE_INVENTORY_COLORED),
    HIDE_BONES(4, Message.SETTING_HIDE_SKELETON_HAT_BONES, false),
    SKELETON_BAR(5, Message.SETTING_SKELETON_HAT_BONES_BAR, new GuiFeatureData(EnumUtils.DrawType.SKELETON_BAR, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -354, -47), false),
    HIDE_FOOD_ARMOR_BAR(6, Message.SETTING_HIDE_FOOD_AND_ARMOR, false),
    FULL_INVENTORY_WARNING(7, Message.SETTING_FULL_INVENTORY_WARNING, new GuiFeatureData(ConfigColor.RED), false),
    MAGMA_BOSS_TIMER(8, Message.SETTING_MAGMA_BOSS_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.GOLD, EnumUtils.AnchorPoint.TOP_RIGHT, -18, 13), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
//    HIDE_DURABILITY(9, Message.SETTING_HIDE_DURABILITY), // removed
    SHOW_ENCHANTMENTS_REFORGES(10, Message.SETTING_ENCHANTS_AND_REFORGES, false),
    MINION_STOP_WARNING(11, Message.SETTING_MINION_STOP_WARNING, new GuiFeatureData(ConfigColor.RED), true),
    HIDE_AUCTION_HOUSE_PLAYERS(12, Message.SETTING_AUCTION_HOUSE_PLAYERS, false),
    HIDE_HEALTH_BAR(13, Message.SETTING_HIDE_HEALTH_BAR, true),
    DOUBLE_DROP_IN_OTHER_GAMES(14, null, false),
    MINION_FULL_WARNING(15, Message.SETTING_FULL_MINION, new GuiFeatureData(ConfigColor.RED), false),
    IGNORE_ITEM_FRAME_CLICKS(16, Message.SETTING_IGNORE_ITEM_FRAME_CLICKS, true),
    USE_VANILLA_TEXTURE_DEFENCE(17, Message.SETTING_USE_VANILLA_TEXTURE, true),
    SHOW_BACKPACK_HOLDING_SHIFT(18, Message.SETTING_SHOW_ONLY_WHEN_HOLDING_SHIFT, true),
    MANA_BAR(19, Message.SETTING_MANA_BAR, new GuiFeatureData(EnumUtils.DrawType.BAR, ConfigColor.BLUE, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 50, -42, 5, 1), false),
    MANA_TEXT(20, Message.SETTING_MANA_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.BLUE, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 52, -44), false),
    HEALTH_BAR(21, Message.SETTING_HEALTH_BAR, new GuiFeatureData(EnumUtils.DrawType.BAR, ConfigColor.RED, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -50, -43, 8, 2), true, EnumUtils.FeatureSetting.CHANGE_BAR_COLOR_WITH_POTIONS),
    HEALTH_TEXT(22, Message.SETTING_HEALTH_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.RED, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -69, -44), false),
    DEFENCE_ICON(23, Message.SETTING_DEFENCE_ICON, new GuiFeatureData(EnumUtils.DrawType.DEFENCE_ICON, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 114, -18), false, EnumUtils.FeatureSetting.USE_VANILLA_TEXTURE),
    DEFENCE_TEXT(24, Message.SETTING_DEFENCE_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.GREEN, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 114, -17), false),
    DEFENCE_PERCENTAGE(25, Message.SETTING_DEFENCE_PERCENTAGE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.GREEN, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 114, -8), true),
    HEALTH_UPDATES(26, Message.SETTING_HEALTH_UPDATES, new GuiFeatureData(EnumUtils.DrawType.TEXT, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -19, -44), false), // Health updates all credit to DidiSkywalker#9975
    HIDE_PLAYERS_IN_LOBBY(27, Message.SETTING_HIDE_PLAYERS_IN_LOBBY, true),
    DARK_AUCTION_TIMER(28, Message.SETTING_DARK_AUCTION_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.GOLD, EnumUtils.AnchorPoint.TOP_RIGHT, -18, 29), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    ITEM_PICKUP_LOG(29, Message.SETTING_ITEM_PICKUP_LOG, new GuiFeatureData(EnumUtils.DrawType.PICKUP_LOG, EnumUtils.AnchorPoint.TOP_LEFT, 86, 17), false),
    AVOID_PLACING_ENCHANTED_ITEMS(30, Message.SETTING_AVOID_PLACING_ENCHANTED_ITEMS, false),
    STOP_BOW_CHARGE_FROM_RESETTING(31, Message.SETTING_STOP_BOW_CHARGE_FROM_RESETTING, false),
    AVOID_BREAKING_STEMS(32, Message.SETTING_AVOID_BREAKING_STEMS, false),
    SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES(33, null, false),
    SHOW_ITEM_ANVIL_USES(34, Message.SETTING_SHOW_ITEM_ANVIL_USES, new GuiFeatureData(ConfigColor.RED), false),
    PREVENT_MOVEMENT_ON_DEATH(35, Message.SETTING_PREVENT_MOVEMENT_ON_DEATH, true),
    SHOW_MAGMA_TIMER_IN_OTHER_GAMES(36, null, true),
    DONT_RESET_CURSOR_INVENTORY(37, Message.SETTING_DONT_RESET_CURSOR_INVENTORY, false),
    LOCK_SLOTS(38, Message.SETTING_LOCK_SLOTS, false),
    SUMMONING_EYE_ALERT(39, Message.SETTING_SUMMONING_EYE_ALERT, new GuiFeatureData(ConfigColor.RED), false),
    MAKE_ENDERCHESTS_GREEN_IN_END(40, Message.SETTING_MAKE_ENDERCHESTS_IN_END_GREEN, new GuiFeatureData(ConfigColor.GREEN), false), //TODO add color change
    DONT_OPEN_PROFILES_WITH_BOW(41, Message.SETTING_DONT_OPEN_PROFILES_WITH_BOW, false),
    STOP_DROPPING_SELLING_RARE_ITEMS(42, Message.SETTING_STOP_DROPPING_SELLING_RARE_ITEMS, new GuiFeatureData(ConfigColor.RED), false),
    MAKE_BACKPACK_INVENTORIES_COLORED(43, Message.SETTING_MAKE_BACKPACK_INVENTORIES_COLORED, false),
    @Deprecated AVOID_BREAKING_BOTTOM_SUGAR_CANE(44, Message.SETTING_AVOID_BREAKING_BOTTOM_SUGAR_CANE, false), //disallowed
    REPLACE_ROMAN_NUMERALS_WITH_NUMBERS(45, Message.SETTING_REPLACE_ROMAN_NUMERALS_WITH_NUMBERS, true),
    CHANGE_BAR_COLOR_FOR_POTIONS(46, Message.SETTING_CHANGE_BAR_COLOR_WITH_POTIONS, false),
    CRAFTING_PATTERNS(47, Message.SETTING_CRAFTING_PATTERNS, false),
    FISHING_SOUND_INDICATOR(48, Message.SETTING_FISHING_SOUND_INDICATOR, false),
    AVOID_BLINKING_NIGHT_VISION(49, Message.SETTING_AVOID_BLINKING_NIGHT_VISION, false),
    MINION_DISABLE_LOCATION_WARNING(50, Message.SETTING_DISABLE_MINION_LOCATION_WARNING, false),
    JUNGLE_AXE_COOLDOWN(51, Message.SETTING_JUNGLE_AXE_COOLDOWN, false),
    ORGANIZE_ENCHANTMENTS(52, Message.SETTING_ORGANIZE_ENCHANTMENTS, false),
    SHOW_ITEM_COOLDOWNS(53, Message.SETTING_SHOW_ITEM_COOLDOWNS, false),
    SKILL_DISPLAY(54, Message.SETTING_COLLECTION_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.AQUA, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 0, -66), false),
    SPEED_PERCENTAGE(55, Message.SETTING_SPEED_PERCENTAGE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.WHITE, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -110, -11), false),
    ONLY_MINE_ORES_DEEP_CAVERNS(56, Message.SETTING_ONLY_MINE_ORES_DEEP_CAVERNS, true),
    SLAYER_INDICATOR(57, Message.SETTING_SLAYER_INDICATOR, new GuiFeatureData(EnumUtils.DrawType.REVENANT_PROGRESS, ConfigColor.AQUA, EnumUtils.AnchorPoint.BOTTOM_RIGHT, -84, -29), true),
    SPECIAL_ZEALOT_ALERT(58, Message.SETTING_SPECIAL_ZEALOT_ALERT, new GuiFeatureData(ConfigColor.RED), false),
    POWER_ORB_STATUS_DISPLAY(59, Message.MESSAGE_DOWNLOAD_LINK, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.WHITE, EnumUtils.AnchorPoint.TOP_RIGHT, 10, 10), false),

    HIDE_GREY_ENCHANTS(100, null, false), // allow remote disabling this feature

    WARNING_TIME(-1, Message.SETTING_WARNING_DURATION, false),

    ADD(-1, null, false),
    SUBTRACT(-1, null, false),

    ANCHOR_POINT(-1, Message.SETTING_ANCHOR_POINT, false),

    LANGUAGE(-1, Message.LANGUAGE, false),
    EDIT_LOCATIONS(-1, Message.SETTING_EDIT_LOCATIONS, false),
    RESET_LOCATION(-1, Message.SETTING_RESET_LOCATIONS, false),
    TEXT_STYLE(-1, Message.SETTING_TEXT_STYLE, false);

    // Add any features to one of the following tab categories. They appear in the GUI in the order they are listed.

    private static final Set<Feature> FEATURES = new LinkedHashSet<>(Arrays.asList(SHOW_ENCHANTMENTS_REFORGES, SHOW_BACKPACK_PREVIEW, CRAFTING_PATTERNS,
            MINION_FULL_WARNING, FULL_INVENTORY_WARNING, IGNORE_ITEM_FRAME_CLICKS, HIDE_FOOD_ARMOR_BAR, HIDE_HEALTH_BAR,
            AVOID_BREAKING_STEMS, MAGMA_WARNING, HIDE_PLAYERS_IN_LOBBY, MINION_STOP_WARNING, SHOW_ITEM_ANVIL_USES, LOCK_SLOTS,
            DONT_OPEN_PROFILES_WITH_BOW, STOP_DROPPING_SELLING_RARE_ITEMS, MAKE_ENDERCHESTS_GREEN_IN_END, SPECIAL_ZEALOT_ALERT,
            FISHING_SOUND_INDICATOR, DONT_RESET_CURSOR_INVENTORY, REPLACE_ROMAN_NUMERALS_WITH_NUMBERS, DROP_CONFIRMATION, ORGANIZE_ENCHANTMENTS,
            JUNGLE_AXE_COOLDOWN, MINION_DISABLE_LOCATION_WARNING, SHOW_ITEM_COOLDOWNS, ONLY_MINE_ORES_DEEP_CAVERNS, SUMMONING_EYE_ALERT, AVOID_BREAKING_BOTTOM_SUGAR_CANE));

    private static Set<Feature> FIXES = new LinkedHashSet<>(Arrays.asList(HIDE_BONES, DISABLE_EMBER_ROD, HIDE_AUCTION_HOUSE_PLAYERS,
            STOP_BOW_CHARGE_FROM_RESETTING, AVOID_PLACING_ENCHANTED_ITEMS, PREVENT_MOVEMENT_ON_DEATH, AVOID_BLINKING_NIGHT_VISION));

    private static Set<Feature> GUI_FEATURES = new LinkedHashSet<>(Arrays.asList(MAGMA_BOSS_TIMER, MANA_BAR, MANA_TEXT, DEFENCE_ICON, DEFENCE_TEXT,
            DEFENCE_PERCENTAGE, HEALTH_BAR, HEALTH_TEXT, SKELETON_BAR, HEALTH_UPDATES, ITEM_PICKUP_LOG, DARK_AUCTION_TIMER, SKILL_DISPLAY, SPEED_PERCENTAGE, SLAYER_INDICATOR,
            POWER_ORB_STATUS_DISPLAY));

    private static Set<Feature> GENERAL_FEATURES = new LinkedHashSet<>(Arrays.asList(TEXT_STYLE, WARNING_TIME));

    private int id;
    private Message message;
    private Set<EnumUtils.FeatureSetting> settings;
    private GuiFeatureData guiFeatureData;
    private boolean defaultDisabled;

    Feature(int id, Message settingMessage, GuiFeatureData guiFeatureData, boolean defaultDisabled, EnumUtils.FeatureSetting... settings) { // color & gui scale settings added automatically
        this.id = id;
        this.message = settingMessage;
        this.settings = new HashSet<>(Arrays.asList(settings));
        this.guiFeatureData = guiFeatureData;
        this.defaultDisabled = defaultDisabled;
    }

    Feature(int id, Message settingMessage, boolean defaultDisabled, EnumUtils.FeatureSetting... settings) {
        this(id,settingMessage,null,defaultDisabled,settings);
    }

    public int getId() {
        return id;
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
        return GUI_FEATURES.contains(this);
    }

    public boolean isColorFeature() {
        return guiFeatureData != null && guiFeatureData.getDefaultColor() != null;
    }

    public boolean isDefaultDisabled() {
        return defaultDisabled;
    }

    public void draw(float scale, Minecraft mc, ButtonLocation buttonLocation) {
        if (guiFeatureData != null) {
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (guiFeatureData.getDrawType() == EnumUtils.DrawType.BAR) {
                main.getRenderListener().drawBar(this, scale, mc, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.SKELETON_BAR) {
                main.getRenderListener().drawSkeletonBar(scale, mc, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.TEXT) {
                main.getRenderListener().drawText(this, scale, mc, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.PICKUP_LOG) {
                main.getRenderListener().drawItemPickupLog(mc, scale, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.DEFENCE_ICON) {
                main.getRenderListener().drawIcon(scale, mc, buttonLocation);
            } else if (guiFeatureData.getDrawType() == EnumUtils.DrawType.REVENANT_PROGRESS) {
                main.getRenderListener().drawRevenantIndicator(scale, mc, buttonLocation);
            }
        }
    }

    public CoordsPair getDefaultCoordinates() {
        if (guiFeatureData != null) {
            CoordsPair coords = guiFeatureData.getDefaultPos();
            if (coords != null) return coords.cloneCoords();
        }
        return null;
    }

    public CoordsPair getDefaultBarSize() {
        if (guiFeatureData != null) {
            CoordsPair coords = guiFeatureData.getDefaultBarSize();
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

    public ConfigColor getDefaultColor() {
        if (guiFeatureData != null) {
            return guiFeatureData.getDefaultColor();
        }
        return null;
    }

    public Set<EnumUtils.FeatureSetting> getSettings() {
        return settings;
    }

    public static Set<Feature> getGuiFeatures() {
        return GUI_FEATURES;
    }

    public static Set<Feature> getFeatures() {
        return FEATURES;
    }

    public static Set<Feature> getFixes() {
        return FIXES;
    }

    public static Set<Feature> getGeneralFeatures() {
        return GENERAL_FEATURES;
    }
}
