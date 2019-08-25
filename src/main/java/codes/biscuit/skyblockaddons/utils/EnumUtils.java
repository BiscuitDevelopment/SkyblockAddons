package codes.biscuit.skyblockaddons.utils;

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

        public AnchorPoint getNextType() {
            int nextType = ordinal()+1;
            if (nextType > values().length-1) {
                nextType = 0;
            }
            return values()[nextType];
        }
    }

    public enum ButtonType {
        TOGGLE,
        COLOR,
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

    public enum Backpack {
        SMALL("Small Backpack"),
        MEDIUM("Medium Backpack"),
        LARGE("Large Backpack");

        private String itemName;

        Backpack(String itemName) {
            this.itemName = itemName;
        }

        public String getItemName() {
            return itemName;
        }
    }

    public enum BackpackStyle {
        GUI(BACKPACK_STYLE_GUI),
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
        REGULAR(TEXT_STYLE_REGULAR),
        BLACK_SHADOW(TEXT_STYLE_BLACK_SHADOW);

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
}
