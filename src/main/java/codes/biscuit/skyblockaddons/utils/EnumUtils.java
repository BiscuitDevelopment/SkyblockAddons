package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.core.Translations;
import lombok.Getter;
import net.minecraft.util.ResourceLocation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Set;

import static codes.biscuit.skyblockaddons.core.Message.*;

public class EnumUtils {

    public enum AnchorPoint {

        TOP_LEFT(0),
        TOP_RIGHT(1),
        BOTTOM_LEFT(2),
        BOTTOM_RIGHT(3),
        BOTTOM_MIDDLE(4);

        @Getter
        private final int id;

        AnchorPoint(int id) {
            this.id = id;
        }

        @SuppressWarnings("unused") // Accessed by reflection...
        public static AnchorPoint fromId(int id) {
            for (AnchorPoint feature : values()) {
                if (feature.getId() == id) {
                    return feature;
                }
            }
            return null;
        }

        public int getX(int maxX) {
            int x = 0;
            switch (this) {
                case TOP_RIGHT: case BOTTOM_RIGHT:
                    x = maxX;
                    break;
                case BOTTOM_MIDDLE:
                    x = maxX / 2;
                    break;

            }
            return x;
        }

        public int getY(int maxY) {
            int y = 0;
            switch (this) {
                case BOTTOM_LEFT: case BOTTOM_RIGHT: case BOTTOM_MIDDLE:
                    y = maxY;
                    break;

            }
            return y;
        }
    }

    public enum ButtonType {
        TOGGLE,
        SOLID,
        CHROMA_SLIDER
    }

    public enum BackpackStyle {
        GUI(BACKPACK_STYLE_REGULAR),
        BOX(BACKPACK_STYLE_COMPACT);

        private final Message message;

        BackpackStyle(Message message) {
            this.message = message;
        }

        public String getMessage() {
            return message.getMessage();
        }

        public BackpackStyle getNextType() {
            int nextType = ordinal() + 1;
            if (nextType > values().length - 1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    public enum PowerOrbDisplayStyle {
        DETAILED(Message.POWER_ORB_DISPLAY_STYLE_DETAILED),
        COMPACT(Message.POWER_ORB_DISPLAY_STYLE_COMPACT);

        private final Message message;

        PowerOrbDisplayStyle(Message message) {
            this.message = message;
        }

        public String getMessage() {
            return message.getMessage();
        }

        public PowerOrbDisplayStyle getNextType() {
            int nextType = ordinal() + 1;
            if (nextType > values().length - 1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    public enum TextStyle {
        STYLE_ONE(TEXT_STYLE_ONE),
        STYLE_TWO(TEXT_STYLE_TWO);

        private final Message message;

        TextStyle(Message message) {
            this.message = message;
        }

        public String getMessage() {
            return message.getMessage();
        }

        public TextStyle getNextType() {
            int nextType = ordinal() + 1;
            if (nextType > values().length - 1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    /** Different detection methods of the magma boss are more accurate than others, display how accurate the time is. */
    @Getter
    public enum MagmaTimerAccuracy {
        NO_DATA("N/A"),
        SPAWNED("NOW"),
        SPAWNED_PREDICTION("NOW"),
        EXACTLY(""),
        ABOUT("");

        private final String symbol;

        MagmaTimerAccuracy(String symbol) {
            this.symbol = symbol;
        }
    }

    @Getter
    public enum MagmaEvent {
        MAGMA_WAVE("magma"),
        BLAZE_WAVE("blaze"),
        BOSS_SPAWN("spawn"),
        BOSS_DEATH("death"),

        // Not actually an event
        PING("ping");

        // The event name used by InventiveTalent's API
        private final String inventiveTalentEvent;

        MagmaEvent(String inventiveTalentEvent) {
            this.inventiveTalentEvent = inventiveTalentEvent;
        }
    }

    public enum GuiTab {
        MAIN, GENERAL_SETTINGS
    }

    /**
     * Settings that modify the behavior of features- without technically being
     * a feature itself.
     * <p>
     * For the equivalent feature (that holds the state) use the ids instead of the enum directly
     * because the enum Feature depends on FeatureSetting, so FeatureSetting can't depend on Feature on creation.
     */
    public enum FeatureSetting {
        COLOR(SETTING_CHANGE_COLOR, -1),
        GUI_SCALE(SETTING_GUI_SCALE, -1),
        ENABLED_IN_OTHER_GAMES(SETTING_SHOW_IN_OTHER_GAMES, -1),
        REPEATING(SETTING_REPEATING, -1),
        TEXT_MODE(SETTING_TEXT_MODE, -1),
        DRAGONS_NEST_ONLY(SETTING_DRAGONS_NEST_ONLY, -1),
        USE_VANILLA_TEXTURE(SETTING_USE_VANILLA_TEXTURE, 17),
        BACKPACK_STYLE(SETTING_BACKPACK_STYLE, -1),
        SHOW_ONLY_WHEN_HOLDING_SHIFT(SETTING_SHOW_ONLY_WHEN_HOLDING_SHIFT, 18),
        MAKE_INVENTORY_COLORED(SETTING_MAKE_BACKPACK_INVENTORIES_COLORED, 43),
        POWER_ORB_DISPLAY_STYLE(SETTING_POWER_ORB_DISPLAY_STYLE, -1),
        CHANGE_BAR_COLOR_WITH_POTIONS(SETTING_CHANGE_BAR_COLOR_WITH_POTIONS, 46),
        ENABLE_MESSAGE_WHEN_ACTION_PREVENTED(SETTING_ENABLE_MESSAGE_WHEN_ACTION_PREVENTED, -1),
        HIDE_NIGHT_VISION_EFFECT(SETTING_HIDE_NIGHT_VISION_EFFECT_TIMER, 70),
        ENABLE_CAKE_BAG_PREVIEW(SETTING_SHOW_CAKE_BAG_PREVIEW, 71),
        SORT_TAB_EFFECT_TIMERS(SETTING_SORT_TAB_EFFECT_TIMERS, 74),
        ROTATE_MAP(SETTING_ROTATE_MAP, 100),
        CENTER_ROTATION_ON_PLAYER(SETTING_CENTER_ROTATION_ON_PLAYER, 101),
        MAP_ZOOM(SETTING_MAP_ZOOM, -1),
        COLOUR_BY_RARITY(SETTING_COLOR_BY_RARITY, -1),
        SHOW_PLAYER_HEADS_ON_MAP(SETTING_SHOW_PLAYER_HEAD_ON_MAP, 106),
        SHOW_GLOWING_ITEMS_ON_ISLAND(SETTING_SHOW_GLOWING_ITEMS_ON_ISLAND, 109),
        SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL(SETTING_SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL, 115),
        HIDE_WHEN_NOT_IN_CRYPTS(SETTING_HIDE_WHEN_NOT_IN_CRYPTS, 133),
        HIDE_WHEN_NOT_IN_SPIDERS_DEN(SETTING_HIDE_WHEN_NOT_IN_SPIDERS_DEN, 134),
        HIDE_WHEN_NOT_IN_CASTLE(SETTING_HIDE_WHEN_NOT_IN_CASTLE, 135),
        ENABLE_PERSONAL_COMPACTOR_PREVIEW(SETTING_SHOW_PERSONAL_COMPACTOR_PREVIEW, 137),

        SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP("settings.showSkillPercentageInstead", 144),
        SHOW_SKILL_XP_GAINED("settings.showSkillXPGained", 145),
        SHOW_SALVAGE_ESSENCES_COUNTER("settings.showSalvageEssencesCounter", 146),
        HEALING_CIRCLE_OPACITY("settings.healingCircleOpacity", 156),
        COOLDOWN_PREDICTION("settings.cooldownPrediction", 164),
        PERFECT_ENCHANT_COLOR("enchants.superTier", 165),
        GREAT_ENCHANT_COLOR("enchants.highTier", 166),
        GOOD_ENCHANT_COLOR("enchants.midTier", 167),
        POOR_ENCHANT_COLOR("enchants.lowTier", 168),
        COMMA_ENCHANT_COLOR("enchants.commas", 171),
        LEVEL_100_LEG_MONKEY("settings.legendaryMonkeyLevel100", 169),
        BIGGER_WAKE("settings.biggerWake", 170),
        HIGHLIGHT_ENCHANTMENTS("settings.highlightSpecialEnchantments", 153),
        HIDE_ENCHANTMENT_LORE("settings.hideEnchantDescription", 176),
        HIDE_GREY_ENCHANTS(Message.SETTING_HIDE_GREY_ENCHANTS, 87),
        ENCHANT_LAYOUT("enchantLayout.title", 0),
        TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR("settings.trevorTheTrapper.trackedEntityProximityIndicator", 173),
        TREVOR_HIGHLIGHT_TRACKED_ENTITY("settings.trevorTheTrapper.highlightTrackedEntity", 174),
        TREVOR_SHOW_QUEST_COOLDOWN("settings.trevorTheTrapper.showQuestCooldown", 175),
        SHOW_FETCHUR_ONLY_IN_DWARVENS(SETTING_SHOW_FETCHUR_IN_DWARVEN_ONLY, 179),
        SHOW_FETCHUR_ITEM_NAME(SETTING_SHOW_FETCHUR_ITEM_NAME, 180),
        SHOW_FETCHUR_INVENTORY_OPEN_ONLY(SETTING_SHOW_FETCHUR_INVENTORY_OPEN_ONLY, 181),
        WARN_WHEN_FETCHUR_CHANGES(SETTING_WARN_WHEN_FETCHUR_CHANGES, 182),


        DISCORD_RP_STATE((Message) null, 0),
        DISCORD_RP_DETAILS((Message) null, 0),
        ;

        private Message message;
        private final int featureEquivalent;
        private String messagePath;

        FeatureSetting(Message message, int featureEquivalent) {
            this.message = message;
            this.featureEquivalent = featureEquivalent;
        }

        FeatureSetting(String messagePath, int featureEquivalent) {
            this.messagePath = messagePath;
            this.featureEquivalent = featureEquivalent;
        }


        public Feature getFeatureEquivalent() {
            if (featureEquivalent == -1) return null;

            for (Feature feature : Feature.values()) {
                if (feature.getId() == featureEquivalent) {
                    return feature;
                }
            }
            return null;
        }

        public String getMessage(String... variables) {
            if (messagePath != null) {
                return Translations.getMessage(messagePath, (Object[]) variables);
            }

            return message.getMessage(variables);
        }
    }

    public enum FeatureCredit {
        // If you make a feature, feel free to add your name here with an associated website of your choice.

        INVENTIVE_TALENT("InventiveTalent", "inventivetalent.org", Feature.MAGMA_BOSS_TIMER),
        ORCHID_ALLOY("orchidalloy", "github.com/orchidalloy", Feature.SUMMONING_EYE_ALERT, Feature.FISHING_SOUND_INDICATOR, Feature.ENCHANTMENT_LORE_PARSING),
        HIGH_CRIT("HighCrit", "github.com/HighCrit", Feature.PREVENT_MOVEMENT_ON_DEATH),
        MOULBERRY("Moulberry", "github.com/Moulberry", Feature.DONT_RESET_CURSOR_INVENTORY),
        TOMOCRAFTER("tomocrafter", "github.com/tomocrafter", Feature.AVOID_BLINKING_NIGHT_VISION, Feature.SLAYER_INDICATOR, Feature.NO_ARROWS_LEFT_ALERT, Feature.BOSS_APPROACH_ALERT),
        DAPIGGUY("DaPigGuy", "github.com/DaPigGuy", Feature.MINION_DISABLE_LOCATION_WARNING),
        COMNIEMEER("comniemeer", "github.com/comniemeer", Feature.JUNGLE_AXE_COOLDOWN),
        KEAGEL("Keagel", "github.com/Keagel", Feature.ONLY_MINE_ORES_DEEP_CAVERNS, Feature.DISABLE_MAGICAL_SOUP_MESSAGES),
        SUPERHIZE("SuperHiZe", "github.com/superhize", Feature.SPECIAL_ZEALOT_ALERT),
        DIDI_SKYWALKER("DidiSkywalker", "twitter.com/didiskywalker", Feature.ITEM_PICKUP_LOG, Feature.HEALTH_UPDATES, Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS,
                Feature.CRAFTING_PATTERNS, Feature.POWER_ORB_STATUS_DISPLAY),
        GARY("GARY_", "github.com/occanowey", Feature.ONLY_MINE_VALUABLES_NETHER),
        P0KE("P0ke", "p0ke.dev", Feature.ZEALOT_COUNTER),
        BERISAN("Berisan", "github.com/Berisan", Feature.TAB_EFFECT_TIMERS),
        MYNAMEISJEFF("MyNameIsJeff", "github.com/My-Name-Is-Jeff", Feature.SHOW_BROKEN_FRAGMENTS),
        DJTHEREDSTONER("DJtheRedstoner", "github.com/DJtheRedstoner", Feature.LEGENDARY_SEA_CREATURE_WARNING, Feature.HIDE_SVEN_PUP_NAMETAGS),
        ANTONIO32A("Antonio32A", "github.com/Antonio32A", Feature.ONLY_BREAK_LOGS_PARK),
        CHARZARD("Charzard4261", "github.com/Charzard4261", Feature.DISABLE_TELEPORT_PAD_MESSAGES, Feature.BAIT_LIST, Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE,
                Feature.SHOW_ITEM_DUNGEON_FLOOR, Feature.SHOW_BASE_STAT_BOOST_PERCENTAGE, Feature.SHOW_RARITY_UPGRADED, Feature.REVENANT_SLAYER_TRACKER,
                Feature.TARANTULA_SLAYER_TRACKER, Feature.SVEN_SLAYER_TRACKER, Feature.DRAGON_STATS_TRACKER, Feature.SHOW_PERSONAL_COMPACTOR_PREVIEW, Feature.SHOW_STACKING_ENCHANT_PROGRESS,
                Feature.STOP_BONZO_STAFF_SOUNDS, Feature.DISABLE_MORT_MESSAGES, Feature.DISABLE_BOSS_MESSAGES),
        IHDEVELOPER("iHDeveloper", "github.com/iHDeveloper", Feature.SHOW_DUNGEON_MILESTONE, Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY, Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY,
                Feature.DUNGEONS_SECRETS_DISPLAY, Feature.SHOW_SALVAGE_ESSENCES_COUNTER, Feature.SHOW_SWORD_KILLS, Feature.ENCHANTMENTS_HIGHLIGHT),
        TIRELESS_TRAVELER("TirelessTraveler", "github.com/ILikePlayingGames", Feature.DUNGEON_DEATH_COUNTER),
        KAASBROODJU("kaasbroodju", "github.com/kaasbroodju", Feature.SKILL_PROGRESS_BAR, Feature.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP, Feature.SHOW_SKILL_XP_GAINED),
        PHOUBE("Phoube", "github.com/Phoube", Feature.HIDE_OTHER_PLAYERS_PRESENTS, Feature.EASIER_PRESENT_OPENING,
                Feature.SHOW_EXPERIMENTATION_TABLE_TOOLTIPS, Feature.ONLY_MINE_ORES_DWARVEN_MINES, Feature.DRILL_FUEL_BAR, Feature.DRILL_FUEL_TEXT,
                Feature.FISHING_PARTICLE_OVERLAY, Feature.COOLDOWN_PREDICTION, Feature.BIGGER_WAKE, Feature.TREVOR_HIGHLIGHT_TRACKED_ENTITY,
                Feature.TREVOR_SHOW_QUEST_COOLDOWN),
        PEDRO9558("Pedro9558", "github.com/Pedro9558", Feature.TREVOR_TRACKED_ENTITY_PROXIMITY_INDICATOR);

        private final Set<Feature> features;
        private final String author;
        private final String url;

        FeatureCredit(String author, String url, Feature... features) {
            this.features = EnumSet.of(features[0], features);
            this.author = author;
            this.url = url;
        }

        public static FeatureCredit fromFeature(Feature feature) {
            for (FeatureCredit credit : values()) {
                if (credit.features.contains(feature)) return credit;
            }
            return null;
        }

        public String getAuthor() {
            return "Contrib. " + author;
        }

        public String getUrl() {
            return "https://" + url;
        }
    }

    public enum DrawType {
        SKELETON_BAR,
        BAR,
        TEXT,
        PICKUP_LOG,
        DEFENCE_ICON,
        REVENANT_PROGRESS,
        POWER_ORB_DISPLAY,
        TICKER,
        BAIT_LIST_DISPLAY,
        TAB_EFFECT_TIMERS,
        DUNGEONS_MAP,
        SLAYER_TRACKERS,
        DRAGON_STATS_TRACKER,
        PROXIMITY_INDICATOR
    }

    @Getter
    public enum Social {
        YOUTUBE("youtube", "https://www.youtube.com/channel/UCYmE9-052frn0wQwqa6i8_Q"),
        DISCORD("discord", "https://biscuit.codes/discord"),
        GITHUB("github", "https://github.com/BiscuitDevelopment/SkyblockAddons"),
        PATREON("patreon", "https://www.patreon.com/biscuitdev");

        private final ResourceLocation resourceLocation;
        private URI url;

        Social(String resourcePath, String url) {
            this.resourceLocation = new ResourceLocation("skyblockaddons", "gui/" + resourcePath + ".png");
            try {
                this.url = new URI(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public enum GUIType {
        MAIN,
        EDIT_LOCATIONS,
        SETTINGS,
        WARP
    }

    public enum ChromaMode {
        ALL_SAME_COLOR(CHROMA_MODE_ALL_THE_SAME),
        FADE(CHROME_MODE_FADE);

        private final Message message;

        ChromaMode(Message message) {
            this.message = message;
        }

        public String getMessage() {
            return message.getMessage();
        }

        public ChromaMode getNextType() {
            int nextType = ordinal() + 1;
            if (nextType > values().length - 1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    @Getter
    public enum DiscordStatusEntry {
        DETAILS(0),
        STATE(1);

        private final int id;

        DiscordStatusEntry(int id) {
            this.id = id;
        }
    }

    //TODO Fix for Hypixel localization
    public enum SlayerQuest {
        REVENANT_HORROR("Revenant Horror"),
        TARANTULA_BROODFATHER("Tarantula Broodfather"),
        SVEN_PACKMASTER("Sven Packmaster");

        private final String scoreboardName;

        SlayerQuest(String scoreboardName) {
            this.scoreboardName = scoreboardName;
        }

        public static SlayerQuest fromName(String scoreboardName) {
            for (SlayerQuest slayerQuest : SlayerQuest.values()) {
                if (slayerQuest.scoreboardName.equals(scoreboardName)) {
                    return slayerQuest;
                }
            }

            return null;
        }
    }
}
