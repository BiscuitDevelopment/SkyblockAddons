package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings({"deprecation", "DeprecatedIsStillUsed"})
public enum Feature {

    MAGMA_WARNING(0, Message.SETTING_MAGMA_BOSS_WARNING, new GuiFeatureData(ConfigColor.RED), false),
    DROP_CONFIRMATION(1, Message.SETTING_ITEM_DROP_CONFIRMATION, null, true, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    DISABLE_EMBER_ROD(2, Message.SETTING_DISABLE_EMBER_ROD_ABILITY, null, false),
    SHOW_BACKPACK_PREVIEW(3, Message.SETTING_SHOW_BACKPACK_PREVIEW, null, false, EnumUtils.FeatureSetting.BACKPACK_STYLE, EnumUtils.FeatureSetting.SHOW_ONLY_WHEN_HOLDING_SHIFT, EnumUtils.FeatureSetting.MAKE_INVENTORY_COLORED),
    HIDE_BONES(4, Message.SETTING_HIDE_SKELETON_HAT_BONES, null, false),
    SKELETON_BAR(5, Message.SETTING_SKELETON_HAT_BONES_BAR, new GuiFeatureData(EnumUtils.DrawType.SKELETON_BAR, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -354, -47), false),
    HIDE_FOOD_ARMOR_BAR(6, Message.SETTING_HIDE_FOOD_AND_ARMOR, null, false),
    FULL_INVENTORY_WARNING(7, Message.SETTING_FULL_INVENTORY_WARNING, new GuiFeatureData(ConfigColor.RED), false),
    MAGMA_BOSS_TIMER(8, Message.SETTING_MAGMA_BOSS_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.GOLD, EnumUtils.AnchorPoint.TOP_RIGHT, -18, 13), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
//    HIDE_DURABILITY(9, Message.SETTING_HIDE_DURABILITY), // removed
    SHOW_ENCHANTMENTS_REFORGES(10, Message.SETTING_ENCHANTS_AND_REFORGES, null, false),
    MINION_STOP_WARNING(11, Message.SETTING_MINION_STOP_WARNING, new GuiFeatureData(ConfigColor.RED), true),
    HIDE_AUCTION_HOUSE_PLAYERS(12, Message.SETTING_AUCTION_HOUSE_PLAYERS, null, false),
    HIDE_HEALTH_BAR(13, Message.SETTING_HIDE_HEALTH_BAR, null, true),
    DOUBLE_DROP_IN_OTHER_GAMES(14, null, null, false),
    MINION_FULL_WARNING(15, Message.SETTING_FULL_MINION, new GuiFeatureData(ConfigColor.RED), false),
    IGNORE_ITEM_FRAME_CLICKS(16, Message.SETTING_IGNORE_ITEM_FRAME_CLICKS, null, true),
    USE_VANILLA_TEXTURE_DEFENCE(17, Message.SETTING_USE_VANILLA_TEXTURE, null, true),
    SHOW_BACKPACK_HOLDING_SHIFT(18, Message.SETTING_SHOW_ONLY_WHEN_HOLDING_SHIFT, null, true),
    MANA_BAR(19, Message.SETTING_MANA_BAR, new GuiFeatureData(EnumUtils.DrawType.BAR, ConfigColor.BLUE, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 50, -42), false),
    MANA_TEXT(20, Message.SETTING_MANA_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.BLUE, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 52, -44), false),
    HEALTH_BAR(21, Message.SETTING_HEALTH_BAR, new GuiFeatureData(EnumUtils.DrawType.BAR, ConfigColor.RED, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -50, -43), true, EnumUtils.FeatureSetting.CHANGE_BAR_COLOR_WITH_POTIONS),
    HEALTH_TEXT(22, Message.SETTING_HEALTH_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.RED, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -69, -44), false),
    DEFENCE_ICON(23, Message.SETTING_DEFENCE_ICON, new GuiFeatureData(EnumUtils.DrawType.DEFENCE_ICON, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 114, -18), false, EnumUtils.FeatureSetting.USE_VANILLA_TEXTURE),
    DEFENCE_TEXT(24, Message.SETTING_DEFENCE_TEXT, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.GREEN, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 114, -17), false),
    DEFENCE_PERCENTAGE(25, Message.SETTING_DEFENCE_PERCENTAGE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.GREEN, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, 114, -8), true),
    HEALTH_UPDATES(26, Message.SETTING_HEALTH_UPDATES, new GuiFeatureData(EnumUtils.DrawType.TEXT, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -19, -44), false), // Health updates all credit to DidiSkywalker#9975
    HIDE_PLAYERS_IN_LOBBY(27, Message.SETTING_HIDE_PLAYERS_IN_LOBBY, null, true),
    DARK_AUCTION_TIMER(28, Message.SETTING_DARK_AUCTION_TIMER, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.GOLD, EnumUtils.AnchorPoint.TOP_RIGHT, -18, 29), false, EnumUtils.FeatureSetting.ENABLED_IN_OTHER_GAMES),
    ITEM_PICKUP_LOG(29, Message.SETTING_ITEM_PICKUP_LOG, new GuiFeatureData(EnumUtils.DrawType.PICKUP_LOG, EnumUtils.AnchorPoint.TOP_LEFT, 86, 17), false),
    AVOID_PLACING_ENCHANTED_ITEMS(30, Message.SETTING_AVOID_PLACING_ENCHANTED_ITEMS, null, false),
    STOP_BOW_CHARGE_FROM_RESETTING(31, Message.SETTING_STOP_BOW_CHARGE_FROM_RESETTING, null, false),
    AVOID_BREAKING_STEMS(32, Message.SETTING_AVOID_BREAKING_STEMS, null, false),
    SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES(33, null, null, false),
    SHOW_ITEM_ANVIL_USES(34, Message.SETTING_SHOW_ITEM_ANVIL_USES, new GuiFeatureData(ConfigColor.RED), false),
    PREVENT_MOVEMENT_ON_DEATH(35, Message.SETTING_PREVENT_MOVEMENT_ON_DEATH, null, true),
    SHOW_MAGMA_TIMER_IN_OTHER_GAMES(36, null, null, true),
    DONT_RESET_CURSOR_INVENTORY(37, Message.SETTING_DONT_RESET_CURSOR_INVENTORY, null, false),
    LOCK_SLOTS(38, Message.SETTING_LOCK_SLOTS, null, false),
    SUMMONING_EYE_ALERT(39, Message.SETTING_SUMMONING_EYE_ALERT, new GuiFeatureData(ConfigColor.RED), false),
    MAKE_ENDERCHESTS_GREEN_IN_END(40, Message.SETTING_MAKE_ENDERCHESTS_IN_END_GREEN, null, false), //todo allow changing color maybe?
    DONT_OPEN_PROFILES_WITH_BOW(41, Message.SETTING_DONT_OPEN_PROFILES_WITH_BOW, null, false),
    STOP_DROPPING_SELLING_RARE_ITEMS(42, Message.SETTING_STOP_DROPPING_SELLING_RARE_ITEMS, new GuiFeatureData(ConfigColor.RED), false),
    MAKE_BACKPACK_INVENTORIES_COLORED(43, Message.SETTING_MAKE_BACKPACK_INVENTORIES_COLORED, null, false),
    @Deprecated AVOID_BREAKING_BOTTOM_SUGAR_CANE(44, Message.SETTING_AVOID_BREAKING_BOTTOM_SUGAR_CANE, null, false), //disallowed
    REPLACE_ROMAN_NUMERALS_WITH_NUMBERS(45, Message.SETTING_REPLACE_ROMAN_NUMERALS_WITH_NUMBERS, null, true),
    CHANGE_BAR_COLOR_FOR_POTIONS(46, Message.SETTING_CHANGE_BAR_COLOR_WITH_POTIONS, null, false),
    CRAFTING_PATTERNS(47, Message.SETTING_CRAFTING_PATTERNS, null, false),
    FISHING_SOUND_INDICATOR(48, Message.SETTING_FISHING_SOUND_INDICATOR, null, false),
    AVOID_BLINKING_NIGHT_VISION(49, Message.SETTING_AVOID_BLINKING_NIGHT_VISION, null, false),
    MINION_DISABLE_LOCATION_WARNING(50, Message.SETTING_DISABLE_MINION_LOCATION_WARNING, null, false),
    JUNGLE_AXE_COOLDOWN(51, Message.SETTING_JUNGLE_AXE_COOLDOWN, null, false),
    ORGANIZE_ENCHANTMENTS(52, Message.SETTING_ORGANIZE_ENCHANTMENTS, null, false),
    SHOW_ITEM_COOLDOWNS(53, Message.SETTING_SHOW_ITEM_COOLDOWNS, null, false),
    COLLECTION_DISPLAY(54, Message.SETTING_COLLECTION_DISPLAY, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.AQUA, EnumUtils.AnchorPoint.TOP_RIGHT, -131, 13), false),
    SPEED_PERCENTAGE(55, Message.SETTING_SPEED_PERCENTAGE, new GuiFeatureData(EnumUtils.DrawType.TEXT, ConfigColor.WHITE, EnumUtils.AnchorPoint.BOTTOM_MIDDLE, -110, -11), false),

    HALLOWEEN(100, null, null, false),

    WARNING_TIME(-1, Message.SETTING_WARNING_DURATION, null, false),

    ADD(-1, null, null, false),
    SUBTRACT(-1, null, null, false),

    ANCHOR_POINT(-1, Message.SETTING_ANCHOR_POINT, null, false),

    LANGUAGE(-1, Message.LANGUAGE, null, false),
    EDIT_LOCATIONS(-1, Message.SETTING_EDIT_LOCATIONS, null, false),
    RESET_LOCATION(-1, Message.SETTING_RESET_LOCATIONS, null, false),
    TEXT_STYLE(-1, Message.SETTING_TEXT_STYLE, null, false);

    // Add any features to one of the following tab categories. They appear in the GUI in the order they are listed.

    private static final Set<Feature> FEATURES = new LinkedHashSet<>(Arrays.asList(Feature.SHOW_ENCHANTMENTS_REFORGES, Feature.SHOW_BACKPACK_PREVIEW, Feature.CRAFTING_PATTERNS,
            Feature.MINION_FULL_WARNING, Feature.FULL_INVENTORY_WARNING,
            Feature.IGNORE_ITEM_FRAME_CLICKS, Feature.HIDE_FOOD_ARMOR_BAR, Feature.HIDE_HEALTH_BAR,
            Feature.AVOID_BREAKING_STEMS, Feature.MAGMA_WARNING, Feature.HIDE_PLAYERS_IN_LOBBY, Feature.MINION_STOP_WARNING,
            Feature.SHOW_ITEM_ANVIL_USES, Feature.LOCK_SLOTS, Feature.DONT_OPEN_PROFILES_WITH_BOW, Feature.STOP_DROPPING_SELLING_RARE_ITEMS,
            Feature.MAKE_ENDERCHESTS_GREEN_IN_END, Feature.SUMMONING_EYE_ALERT, Feature.FISHING_SOUND_INDICATOR, Feature.DONT_RESET_CURSOR_INVENTORY,
            Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS, Feature.DROP_CONFIRMATION, Feature.ORGANIZE_ENCHANTMENTS, Feature.JUNGLE_AXE_COOLDOWN,
            Feature.MINION_DISABLE_LOCATION_WARNING, Feature.SHOW_ITEM_COOLDOWNS, Feature.AVOID_BREAKING_BOTTOM_SUGAR_CANE));

    private static Set<Feature> FIXES = new LinkedHashSet<>(Arrays.asList(Feature.HIDE_BONES, Feature.DISABLE_EMBER_ROD, Feature.HIDE_AUCTION_HOUSE_PLAYERS,
            Feature.STOP_BOW_CHARGE_FROM_RESETTING, Feature.AVOID_PLACING_ENCHANTED_ITEMS, Feature.PREVENT_MOVEMENT_ON_DEATH, Feature.AVOID_BLINKING_NIGHT_VISION));

    private static Set<Feature> GUI_FEATURES = new LinkedHashSet<>(Arrays.asList(Feature.MAGMA_BOSS_TIMER, Feature.MANA_BAR, Feature.MANA_TEXT, Feature.DEFENCE_TEXT, Feature.DEFENCE_PERCENTAGE,
            Feature.DEFENCE_ICON, Feature.HEALTH_BAR, Feature.HEALTH_TEXT, Feature.SKELETON_BAR, Feature.HEALTH_UPDATES,
            Feature.ITEM_PICKUP_LOG, Feature.DARK_AUCTION_TIMER, Feature.COLLECTION_DISPLAY, Feature.SPEED_PERCENTAGE));

    private static Set<Feature> GENERAL_FEATURES = new LinkedHashSet<>(Arrays.asList(Feature.TEXT_STYLE, Feature.WARNING_TIME));

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
            }
        }
    }

    public CoordsPair getDefaultCoordinates() {
        if (guiFeatureData != null) {
            return guiFeatureData.getDefaultPos();
        }
        return null;
    }

    public CoordsPair getDefaultBarSize() {
        if (guiFeatureData != null) {
            return guiFeatureData.getDefaultBarSize();
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
