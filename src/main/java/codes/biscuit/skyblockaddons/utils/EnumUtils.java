package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.text.WordUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

import static codes.biscuit.skyblockaddons.utils.Message.*;

public class EnumUtils {

    @SuppressWarnings("deprecation")
    public enum AnchorPoint {
        TOP_LEFT(0, ANCHOR_POINT_TOP_LEFT),
        TOP_RIGHT(1, ANCHOR_POINT_TOP_RIGHT),
        BOTTOM_LEFT(2, ANCHOR_POINT_BOTTOM_LEFT),
        BOTTOM_RIGHT(3, ANCHOR_POINT_BOTTOM_RIGHT),
        BOTTOM_MIDDLE(4, ANCHOR_POINT_HEALTH_BAR);

        private Message message;
        private int id;

        AnchorPoint(int id, Message message) {
            this.message = message;
            this.id = id;
        }

        public String getMessage() {
            return message.getMessage();
        }

        public int getId() {
            return id;
        }

        public static AnchorPoint fromId(int id) {
            for (AnchorPoint feature : values()) {
                if (feature.getId() == id) {
                    return feature;
                }
            }
            return null;
        }

        public int getX(int maxX) {
            int x;
            switch (this) {
                case TOP_RIGHT: case BOTTOM_RIGHT:
                    x = maxX;
                    break;
                case BOTTOM_MIDDLE:
                    x = maxX / 2;// - 91;
                    break;
                default: // or case TOP_LEFT: case BOTTOM_LEFT:
                    x = 0;

            }
            return x;
        }

        public int getY(int maxY) {
            int y;
            switch (this) {
                case BOTTOM_LEFT: case BOTTOM_RIGHT: case BOTTOM_MIDDLE:
                    y = maxY;
                    break;
//                case BOTTOM_MIDDLE:
//                    y = maxY - 39;
//                    break;
                default: // or case TOP_LEFT: case TOP_RIGHT:
                    y = 0;

            }
            return y;
        }

//        public AnchorPoint getNextType() {
//            int nextType = ordinal()+1;
//            if (nextType > values().length-1) {
//                nextType = 0;
//            }
//            return values()[nextType];
//        }
    }

    public enum ButtonType {
        TOGGLE,
//        COLOR,
        SOLID
    }

    public enum InventoryType {
        ENCHANTMENT_TABLE(INVENTORY_TYPE_ENCHANTS, "Enchant Item"),
        REFORGE_ANVIL(INVENTORY_TYPE_REFORGES, "Reforge Item"),
        CRAFTING_TABLE(INVENTORY_TYPE_CRAFTING, CraftingPattern.CRAFTING_TABLE_DISPLAYNAME);

        private static InventoryType currentInventoryType;

        /**
         * Resets the current inventory type
         */
        public static void resetCurrentInventoryType() {
            currentInventoryType = null;
        }

        /**
         * @return Current inventory type. Can be null
         */
        public static InventoryType getCurrentInventoryType() {
            return currentInventoryType;
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

    public enum TextStyle {
        REGULAR(TEXT_STYLE_ONE),
        BLACK_SHADOW(TEXT_STYLE_TWO);

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

    public enum Location {
        ISLAND("Your Island"),
        // Hub
        VILLAGE("Village"),
        AUCTION_HOUSE("Auction House"),
        BANK("Bank"),
        LIBRARY("Library"),
        COAL_MINE("Coal Mine"),
        GRAVEYARD("Graveyard"),
        COLOSSEUM("Colosseum"),
        WILDERNESS("Wilderness"),
        MOUNTAIN("Mountain"),
        WIZARD_TOWER("Wizard Tower"),
        RUINS("Ruins"),
        FOREST("Forest"),
        FARM("Farm"),
        FISHERMANS_HUT("Fisherman's Hut"),
        HIGH_LEVEL("High Level"),
        FLOWER_HOUSE("Flower House"),
        CANVAS_ROOM("Canvas Room"),
        TAVERN("Tavern"),
        // Floating Islands
        BIRCH_PARK("Birch Park"),
        SPRUCE_WOODS("Spruce Woods"),
        JUNGLE_ISLAND("Jungle Island"),
        SAVANNA_WOODLAND("Savanna Woodland"),
        DARK_THICKET("Dark Thicket"),

        GOLD_MINE("Gold Mine"),

        DEEP_CAVERNS("Deep Caverns"),
        GUNPOWDER_MINES("Gunpowder Mines"),
        LAPIS_QUARRY("Lapis Quarry"),
        PIGMAN_DEN("Pigmen's Den"),
        SLIMEHILL("Slimehill"),
        DIAMOND_RESERVE("Diamond Reserve"),
        OBSIDIAN_SANCTUARY("Obsidian Sanctuary"),
        THE_BARN("The Barn"),

        MUSHROOM_DESERT("Mushroom Desert"),

        SPIDERS_DEN("Spider's Den"),

        BLAZING_FORTRESS("Blazing Fortress"),

        THE_END("The End"),
        DRAGONS_NEST("Dragon's Nest");

        private String scoreboardName;

        Location(String scoreboardName) {
            this.scoreboardName = scoreboardName;
        }

        public String getScoreboardName() {
            return scoreboardName;
        }
    }

    // Different indicators of the magma boss are more accurate than others, display how accurate the time is.
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

        public String getSymbol() {
            return symbol;
        }
    }

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

        public String getInventiveTalentEvent() {
            return inventiveTalentEvent;
        }
    }

    public enum SkyblockNPC {
        AUCTION_MASTER(17.5,71,-78.5, false, Location.VILLAGE, Location.AUCTION_HOUSE),
        BANKER(20.5,71,-40.5, false, Location.VILLAGE, Location.BANK),
        BAKER(34.5, 71, -44.5, false, Location.VILLAGE),
        LOBBY_SELECTOR(-9,70,-79, false, Location.VILLAGE),
        LUMBER_MERCHANT(-18.5,70,-90, true, Location.VILLAGE),
        ADVENTURER(-18.5,70,-77, true, Location.VILLAGE),
        FISH_MERCHANT(-25.5,70,-77, true, Location.VILLAGE),
        ARMORSMITH(-25.5,70,-90, true, Location.VILLAGE),
        BLACKSMITH(-19.5,71,-124.5, false, Location.VILLAGE),
        BLACKSMITH_2(-39.5,77,-299.5, false, Location.GOLD_MINE),
        FARM_MERCHANT(-7,70,-48.5, true, Location.VILLAGE),
        MINE_MERCHANT(-19,70,-48.5, true, Location.VILLAGE),
        WEAPONSMITH(-19,70,-41.5, false, Location.VILLAGE),
        BUILDER(-7,70,-41.5, true, Location.VILLAGE),
        LIBRARIAN(17.5,71,-16.5, true, Location.VILLAGE, Location.LIBRARY),
        MARCO(9.5,71,-14, false, Location.VILLAGE, Location.FLOWER_HOUSE),
        ALCHEMIST(-33.5,73,-14.5, true, Location.VILLAGE),
        PAT(-129.5,73,-98.5, true, Location.GRAVEYARD),
        EVENT_MASTER(-61.5,71,-54.5, false, Location.COLOSSEUM, Location.VILLAGE),
        GOLD_FORGER(-27.5,74,-294.5, true, Location.GOLD_MINE),
        IRON_FORGER(-1.5,75,-307.5, false, Location.GOLD_MINE),
        RUSTY(-20,78,-326, false, Location.GOLD_MINE),
        MADDOX_THE_SLAYER(-87,66,-70, false, Location.VILLAGE, Location.TAVERN),
        SIRIUS(91.5,75,176.5, false, Location.WILDERNESS);

        private final AxisAlignedBB hideArea;
        private double x;
        private double y;
        private double z;
        private boolean isMerchant;
        Set<Location> locations;

        SkyblockNPC(double x, double y, double z, boolean isMerchant, Location... locations) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.isMerchant = isMerchant;
            int hideRadius = 4;
            this.hideArea = new AxisAlignedBB(x - hideRadius, y - hideRadius, z - hideRadius, x + hideRadius, y + hideRadius, z + hideRadius);
            this.locations = EnumSet.copyOf(Arrays.asList(locations));
        }

        public boolean isAtLocation(Location location) {
            return locations.contains(location);
        }

        public boolean isNearEntity(Entity entity) {
            Utils utils = SkyblockAddons.getInstance().getUtils();
            if (isAtLocation(utils.getLocation())) {
                double x = entity.posX;
                double y = entity.posY;
                double z = entity.posZ;

                return this.hideArea.isVecInside(new Vec3(x, y, z)) && (this.x != x || this.y != y || this.z != z) && utils.isNotNPC(entity);
            }
            return false;
        }

        public static boolean isNearNPC(Entity entity) {
            for (SkyblockNPC npc : values()) {
                if (npc.isNearEntity(entity))
                    return true;
            }
            return false;
        }

        public static boolean isMerchant(String name) {// inventory
            for (SkyblockNPC npc : values()) {
                if (npc.isMerchant) {
                    if (name.replaceAll(" ", "_").equalsIgnoreCase(npc.name())) {
                        return true;
                    }
                }
            }
            return name.contains("Merchant");
        }

    }

    public enum GuiTab {
        FEATURES, FIXES, GUI_FEATURES, GENERAL_SETTINGS;

        public Set<Feature> getFeatures() {
            switch (this) {
                case FEATURES: return Feature.getFeatures();
                case FIXES: return Feature.getFixes();
                case GUI_FEATURES: return Feature.getGuiFeatures();
                case GENERAL_SETTINGS: return Feature.getGeneralFeatures();
            }
            return null;
        }
    }

    public enum FeatureSetting {
        COLOR,
        GUI_SCALE,
        ENABLED_IN_OTHER_GAMES,
        USE_VANILLA_TEXTURE,
//        WARNING_TIME,
        BACKPACK_STYLE,
        SHOW_ONLY_WHEN_HOLDING_SHIFT,
        MAKE_INVENTORY_COLORED,
        CHANGE_BAR_COLOR_WITH_POTIONS
    }

    @SuppressWarnings("deprecation")
    public enum FeatureCredit {
        // If you make a feature, feel free to add your name here with an associated website of your choice.

        INVENTIVE_TALENT("InventiveTalent", "inventivetalent.org", Feature.MAGMA_BOSS_TIMER),
        FSCK("fsck", "github.com/fsckmc", Feature.AVOID_BREAKING_BOTTOM_SUGAR_CANE),
        ORCHID_ALLOY("orchidalloy", "github.com/orchidalloy", Feature.SUMMONING_EYE_ALERT, Feature.FISHING_SOUND_INDICATOR, Feature.ORGANIZE_ENCHANTMENTS),
        HIGH_CRIT("HighCrit", "github.com/HighCrit", Feature.PREVENT_MOVEMENT_ON_DEATH),
        MOULBERRY("Moulberry", "github.com/Moulberry", Feature.DONT_RESET_CURSOR_INVENTORY),
        TOMOCRAFTER("tomocrafter","github.com/tomocrafter", Feature.AVOID_BLINKING_NIGHT_VISION, Feature.SLAYER_INDICATOR),
        DAPIGGUY("DaPigGuy", "github.com/DaPigGuy", Feature.MINION_DISABLE_LOCATION_WARNING),
        COMNIEMEER("comniemeer","github.com/comniemeer", Feature.JUNGLE_AXE_COOLDOWN),
        KEAGEL("Keagel", "github.com/Keagel", Feature.ONLY_MINE_ORES_DEEP_CAVERNS),
        SUPERHIZE("SuperHiZe", "github.com/superhize", Feature.SPECIAL_ZEALOT_ALERT),
        DIDI_SKYWALKER("DidiSkywalker", "github.com/didiskywalker", Feature.ITEM_PICKUP_LOG, Feature.HEALTH_UPDATES, Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS, Feature.CRAFTING_PATTERNS),
        GARY("GARY_", "github.com/occanowey", Feature.ONLY_MINE_VALUABLES_NETHER);

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

    public enum Rarity {
        COMMON("f"),
        UNCOMMON("a"),
        RARE("9"),
        EPIC("5"),
        LEGENDARY("6"),
        SPECIAL("d");

        private String tag;

        Rarity(String s) {
            this.tag = "§"+s;
        }

        public static Rarity getRarity(ItemStack item) {
            if (item == null) return null;
            String itemName = item.getDisplayName();
            for(Rarity rarity: Rarity.values()) {
                if(itemName.startsWith(rarity.tag)) return rarity;
            }
            return null;
        }
    }

    public enum UpdateMessageType {
        MAJOR_AVAILABLE(UPDATE_MESSAGE_MAJOR),
        PATCH_AVAILABLE(UPDATE_MESSAGE_PATCH),
        DOWNLOADING(UPDATE_MESSAGE_DOWNLOAD),
        FAILED(UPDATE_MESSAGE_FAILED),
        DOWNLOAD_FINISHED(UPDATE_MESSAGE_DOWNLOAD_FINISHED),
        DEVELOPMENT(null);

        private Message message;

        UpdateMessageType(Message message) {
            this.message = message;
        }

        public String[] getMessages(String... variables) {
            if (this == DEVELOPMENT) return WordUtils.wrap("You are running a development version: "+SkyblockAddons.VERSION+
                    ". Please report any bugs that haven't been found yet. Thank you.", 36).replace("\r", "").split(Pattern.quote("\n"));
            String text = WordUtils.wrap(message.getMessage(variables), 36).replace("\r", "");
            return text.split(Pattern.quote("\n"));
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
        OTHER(null, null);

        private String skillName;
        private ItemStack item;

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
            return OTHER;
        }

        public ItemStack getItem() {
            return item;
        }
    }

    public enum DrawType {
        SKELETON_BAR,
        BAR,
        TEXT,
        PICKUP_LOG,
        DEFENCE_ICON,
        REVENANT_PROGRESS
    }

    public enum Social {
        YOUTUBE(new ResourceLocation("skyblockaddons", "youtube.png"), "https://www.youtube.com/channel/UCYmE9-052frn0wQwqa6i8_Q"),
        DISCORD(new ResourceLocation("skyblockaddons", "discord.png"), "https://discordapp.com/invite/PqTAEek"),
        GITHUB(new ResourceLocation("skyblockaddons", "github.png"), "https://github.com/BiscuitDevelopment/SkyblockAddons");

        private ResourceLocation resourceLocation;
        private URI url;

        Social(ResourceLocation resourceLocation, String url) {
            this.resourceLocation = resourceLocation;
            try {
                this.url = new URI(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        public ResourceLocation getResourceLocation() {
            return resourceLocation;
        }

        public URI getUrl() {
            return url;
        }
    }
}
