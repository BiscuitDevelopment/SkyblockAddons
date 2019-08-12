package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;

import static codes.biscuit.skyblockaddons.utils.Message.*;

public class EnumUtils {

    public enum AnchorPoint {
        TOP_LEFT(SETTING_NEXT_PAGE),
        TOP_RIGHT(SETTING_NEXT_PAGE),
        BOTTOM_LEFT(SETTING_NEXT_PAGE),
        BOTTOM_RIGHT(SETTING_NEXT_PAGE),
        HEALTH_BAR(SETTING_NEXT_PAGE);

        private Message message;

        AnchorPoint(Message message) {
            this.message = message;
        }

        //TODO actually add translation entries for this and stuff
        public String getDisplayText() {
            return SkyblockAddons.getInstance().getConfigValues().getMessage(message);
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

        public Message getMessage() {
            return message;
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
        GUI(STYLE_GUI),
        BOX(STYLE_COMPACT);

        private Message message;

        BackpackStyle(Message message) {
            this.message = message;
        }

        public String getDisplayText() {
            return SkyblockAddons.getInstance().getConfigValues().getMessage(message);
        }

        public BackpackStyle getNextType() {
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
}
