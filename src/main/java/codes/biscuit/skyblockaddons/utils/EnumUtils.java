package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPattern;
import lombok.Getter;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

        @Getter private int id;

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

    public enum InventoryType {
        ENCHANTMENT_TABLE(INVENTORY_TYPE_ENCHANTS, "Enchant Item"),
        REFORGE_ANVIL(INVENTORY_TYPE_REFORGES, "Reforge Item"),
        BAKER(null, "Baker"),
        CRAFTING_TABLE(INVENTORY_TYPE_CRAFTING, CraftingPattern.CRAFTING_TABLE_DISPLAYNAME);

        /** The current inventory type. Can be null. */
        @Getter private static InventoryType currentInventoryType;

        /**
         * Resets the current inventory type
         */
        public static void resetCurrentInventoryType() {
            currentInventoryType = null;
        }

        /**
         * Get the inventory type based on an inventory name.
         * Stores the found type to access later with {@link #getCurrentInventoryType()}
         *
         * @param inventoryName Unformatted inventory name
         * @return Inventory type for that name or null
         */
        public static InventoryType getCurrentInventoryType(String inventoryName) {
            for (InventoryType inventoryType : values()) {
                if(inventoryType.inventoryName.equals(inventoryName)) {
                    currentInventoryType = inventoryType;
                    return inventoryType;
                }
            }
            return null;
        }

        private final Message message;
        private final String inventoryName;

        InventoryType(Message message, String inventoryName) {
            this.message = message;
            this.inventoryName = inventoryName;
        }

        public String getMessage() {
            return message.getMessage();
        }
    }

    public enum BackpackStyle {
        GUI(BACKPACK_STYLE_REGULAR),
        BOX(BACKPACK_STYLE_COMPACT);

        private Message message;

        BackpackStyle(Message message) {
            this.message = message;
        }

        public String getMessage() {
            return message.getMessage();
        }

        public BackpackStyle getNextType() {
            int nextType = ordinal()+1;
            if (nextType > values().length-1) {
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
            int nextType = ordinal()+1;
            if (nextType > values().length-1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    public enum TextStyle {
        STYLE_ONE(TEXT_STYLE_ONE),
        STYLE_TWO(TEXT_STYLE_TWO);

        private Message message;

        TextStyle(Message message) {
            this.message = message;
        }

        public String getMessage() {
            return message.getMessage();
        }

        public TextStyle getNextType() {
            int nextType = ordinal()+1;
            if (nextType > values().length-1) {
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

        private String symbol;

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
        private String inventiveTalentEvent;

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
     *
     * For the equivalent feature (that holds the state) use the ids instead of the enum directly
     * because the enum Feature depends on FeatureSetting, so FeatureSetting can't depend on Feature on creation.
     */
    public enum FeatureSetting {
        COLOR(SETTING_CHANGE_COLOR, -1),
        GUI_SCALE(SETTING_GUI_SCALE, -1),
        ENABLED_IN_OTHER_GAMES(SETTING_SHOW_IN_OTHER_GAMES, -1),
        REPEATING(SETTING_REPEATING, -1),
        USE_VANILLA_TEXTURE(SETTING_USE_VANILLA_TEXTURE, 17),
        BACKPACK_STYLE(SETTING_BACKPACK_STYLE, -1),
        SHOW_ONLY_WHEN_HOLDING_SHIFT(SETTING_SHOW_ONLY_WHEN_HOLDING_SHIFT, 18),
        MAKE_INVENTORY_COLORED(SETTING_MAKE_BACKPACK_INVENTORIES_COLORED, 43),
        POWER_ORB_DISPLAY_STYLE(SETTING_POWER_ORB_DISPLAY_STYLE, -1),
        CHANGE_BAR_COLOR_WITH_POTIONS(SETTING_CHANGE_BAR_COLOR_WITH_POTIONS, 46),
        ENABLE_MESSAGE_WHEN_ACTION_PREVENTED(SETTING_ENABLE_MESSAGE_WHEN_ACTION_PREVENTED, -1),
        HIDE_NIGHT_VISION_EFFECT(SETTING_HIDE_NIGHT_VISION_EFFECT_TIMER, 70),
        ENABLE_CAKE_BAG_PREVIEW(SETTING_SHOW_CAKE_BAG_PREVIEW, 71),
        ENABLE_BACKPACK_PREVIEW_AH(SETTING_SHOW_BACKPACK_PREVIEW_AH, 72),
        SORT_TAB_EFFECT_TIMERS(SETTING_SORT_TAB_EFFECT_TIMERS, 74),

        DISCORD_RP_STATE(null, 0),
        DISCORD_RP_DETAILS(null, 0);

        @Getter private Message message;
        private int featureEquivalent;

        FeatureSetting(Message message, int featureEquivalent) {
            this.message = message;
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
    }

    @SuppressWarnings("deprecation")
    public enum FeatureCredit {
        // If you make a feature, feel free to add your name here with an associated website of your choice.

        INVENTIVE_TALENT("InventiveTalent", "inventivetalent.org", Feature.MAGMA_BOSS_TIMER),
        FSCK("fsck", "github.com/fsckmc", Feature.AVOID_BREAKING_BOTTOM_SUGAR_CANE),
        ORCHID_ALLOY("orchidalloy", "github.com/orchidalloy", Feature.SUMMONING_EYE_ALERT, Feature.FISHING_SOUND_INDICATOR, Feature.ORGANIZE_ENCHANTMENTS),
        HIGH_CRIT("HighCrit", "github.com/HighCrit", Feature.PREVENT_MOVEMENT_ON_DEATH),
        MOULBERRY("Moulberry", "github.com/Moulberry", Feature.DONT_RESET_CURSOR_INVENTORY),
        TOMOCRAFTER("tomocrafter","github.com/tomocrafter", Feature.AVOID_BLINKING_NIGHT_VISION, Feature.SLAYER_INDICATOR, Feature.NO_ARROWS_LEFT_ALERT, Feature.BOSS_APPROACH_ALERT),
        DAPIGGUY("DaPigGuy", "github.com/DaPigGuy", Feature.MINION_DISABLE_LOCATION_WARNING),
        COMNIEMEER("comniemeer","github.com/comniemeer", Feature.JUNGLE_AXE_COOLDOWN),
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
        CHARZARD("Charzard4261", "github.com/Charzard4261", Feature.DISABLE_TELEPORT_PAD_MESSAGES, Feature.BAIT_LIST);

        private Set<Feature> features;
        private String author;
        private String url;

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
            return "https://"+url;
        }
    }

    public enum SkillType {
        FARMING("Farming", Items.golden_hoe),
        MINING("Mining", Items.diamond_pickaxe),
        COMBAT("Combat", Items.iron_sword),
        FORAGING("Foraging", Item.getItemFromBlock(Blocks.sapling)),
        FISHING("Fishing", Items.fishing_rod),
        ENCHANTING("Enchanting", Item.getItemFromBlock(Blocks.enchanting_table)),
        ALCHEMY("Alchemy", Items.brewing_stand),
        CARPENTRY("Carpentry", Item.getItemFromBlock(Blocks.crafting_table)),
        RUNECRAFTING("Runecrafting", Items.magma_cream),
        TAMING("Taming", Items.spawn_egg),
        DUNGEONEERING("Dungeoneering", Item.getItemFromBlock(Blocks.deadbush));

        private String skillName;
        @Getter private ItemStack item;

        SkillType(String skillName, Item item) {
            this.skillName = skillName;
            this.item = new ItemStack(item);
        }

        public static SkillType getFromString(String text) {
            for (SkillType skillType : values()) {
                if (skillType.skillName != null && skillType.skillName.equals(text)) {
                    return skillType;
                }
            }
            return null;
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
        TAB_EFFECT_TIMERS
    }

    @Getter
    public enum Social {
        YOUTUBE("youtube", "https://www.youtube.com/channel/UCYmE9-052frn0wQwqa6i8_Q"),
        DISCORD("discord", "https://biscuit.codes/discord"),
        GITHUB("github", "https://github.com/BiscuitDevelopment/SkyblockAddons"),
        PATREON("patreon", "https://www.patreon.com/biscuitdev");

        private ResourceLocation resourceLocation;
        private URI url;

        Social(String resourcePath, String url) {
            this.resourceLocation = new ResourceLocation("skyblockaddons", "gui/"+resourcePath+".png");
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

        private Message message;

        ChromaMode(Message message) {
            this.message = message;
        }

        public String getMessage() {
            return message.getMessage();
        }

        public ChromaMode getNextType() {
            int nextType = ordinal()+1;
            if (nextType > values().length-1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    @Getter
    public enum DiscordStatusEntry {
        DETAILS(0),
        STATE(1);

        private int id;

        DiscordStatusEntry(int id) {
            this.id = id;
        }
    }
}
