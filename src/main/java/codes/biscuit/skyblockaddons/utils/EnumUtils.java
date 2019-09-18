package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static codes.biscuit.skyblockaddons.utils.Message.*;

public class EnumUtils {

    @SuppressWarnings("deprecation")
    public enum AnchorPoint {
        TOP_LEFT(0, ANCHOR_POINT_TOP_LEFT),
        TOP_RIGHT(1, ANCHOR_POINT_TOP_RIGHT),
        BOTTOM_LEFT(2, ANCHOR_POINT_BOTTOM_LEFT),
        BOTTOM_RIGHT(3, ANCHOR_POINT_BOTTOM_RIGHT),
        HEALTH_BAR(4, ANCHOR_POINT_HEALTH_BAR);

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
                case HEALTH_BAR:
                    x = maxX / 2 - 91;
                    break;
                default: // or case TOP_LEFT: case BOTTOM_LEFT:
                    x = 0;

            }
            return x;
        }

        public int getY(int maxY) {
            int y;
            switch (this) {
                case BOTTOM_LEFT: case BOTTOM_RIGHT:
                    y = maxY;
                    break;
                case HEALTH_BAR:
                    y = maxY - 39;
                    break;
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
        ENCHANTMENT_TABLE(INVENTORY_TYPE_ENCHANTS),
        REFORGE_ANVIL(INVENTORY_TYPE_REFORGES);

        private Message message;

        InventoryType(Message message) {
            this.message = message;
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
        BLAZING_FORTRESS("Blazing Fortress"),
        VILLAGE("Village"),
        WILDERNESS("Wilderness"),
        BANK("Bank"),
        THE_END("The End"),
        DRAGONS_NEST("Dragon's Nest"),
        AUCTION_HOUSE("Auction House");

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
        AUCTION_MASTER(17.5,71,-78.5, Location.VILLAGE, Location.AUCTION_HOUSE),
        BANKER(20.5,71,-40.5, Location.VILLAGE, Location.BANK),
        LOBBY_SELECTOR(-9,70,-79, Location.VILLAGE),
        SIRIUS(91.5,75,176.5, Location.WILDERNESS);

        private final int hideRadius = 3;
        private AxisAlignedBB hideArea;
        private double x;
        private double y;
        private double z;
        Set<Location> locations;

        SkyblockNPC(double x, double y, double z, Location... locations) {
            this.x = x;
            this.y = y;
            this.z = z;
            hideArea = new AxisAlignedBB(x - hideRadius, y - hideRadius, z - hideRadius, x + hideRadius, y + hideRadius, z + hideRadius);
            this.locations = EnumSet.copyOf(Arrays.asList(locations));
        }

        public boolean isAtLocation(Location location) {
            return this.locations.contains(location);
        }

        public boolean isNearEntity(Entity entity) {
            if (this.locations.contains(SkyblockAddons.getInstance().getUtils().getLocation())) {
                double x = entity.posX;
                double y = entity.posY;
                double z = entity.posZ;

                return this.hideArea.contains(new Vec3d(x, y, z)) && (this.x != x || this.y != y || this.z != z);
            }

            return false;
        }

//        public static boolean isNPC(double x, double y, double z) {
//            for (SkyblockNPC npc : values()) {
//                if (npc.x == x && npc.y == y && npc.z == z) {
//                    return true;
//                }
//            }
//            return false;
//        }

        public static boolean isNearAnyNPC(Entity e) {
            for (SkyblockNPC npc : values()) {
                if (npc.locations.contains(SkyblockAddons.getInstance().getUtils().getLocation())) {
                    double x = e.posX;
                    double y = e.posY;
                    double z = e.posZ;

                    if (npc.hideArea.contains(new Vec3d(x, y, z)) && (npc.x != x || npc.y != y || npc.z != z))
                        return true;
                }
            }

            return false;
        }

    }

    public enum SkyblockAddonsGuiTab {
        FEATURES, FIXES, GUI_FEATURES, GENERAL_SETTINGS
    }

    public enum FeatureSetting {
        COLOR,
        GUI_SCALE,
        ENABLED_IN_OTHER_GAMES,
        USE_VANILLA_TEXTURE,
        //        WARNING_TIME,
        BACKPACK_STYLE,
        SHOW_ONLY_WHEN_HOLDING_SHIFT,
        MAKE_INVENTORY_COLORED
    }

    public enum Merchant {

        ADVENTURER("Adventurer"),
        BUILDER("Builder"),
        WEAPONSMITH("Weaponsmith"),
        ARMORSMITH("Armorsmith"),
        GOLD_FORGER("Gold Forger"),
        IRON_FORGER("Iron Forger");

        private String name;

        Merchant(String name) {
            this.name = name;
        }

        public static boolean isMerchant(String name) {
            for (Merchant merchant : values()) {
                if (name.equals(merchant.name)) {
                    return true;
                }
            }
            return name.contains("Merchant");
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
            this.tag = "\u00A7" + s;
        }

        public static Rarity getRarity(ItemStack item) {
            if (item == null) return null;
            String itemName = item.getDisplayName();
            for (Rarity rarity: Rarity.values()) {
                if(itemName.startsWith(rarity.tag)) return rarity;
            }
            return null;
        }

    }

}
