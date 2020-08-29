package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.ItemDiff;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.skyblockdata.Rune;
import lombok.Getter;

import java.util.*;

public class SlayerTracker {

    @Getter private static SlayerTracker instance = new SlayerTracker();

    private Map<SlayerBoss, Integer> slayerKills = new EnumMap<>(SlayerBoss.class);
    private Map<SlayerDrop, Integer> slayerDropCounts = new EnumMap<>(SlayerDrop.class);
    @Getter private SlayerBoss lastKilledBoss;

    // Saves the last second of inventory differences
    private transient Map<Long, List<ItemDiff>> recentInventoryDifferences = new HashMap<>();
    private transient long lastSlayerCompleted = -1;

    public int getSlayerKills(SlayerBoss slayerBoss) {
        return slayerKills.getOrDefault(slayerBoss, 0);
    }

    public int getDropCount(SlayerDrop slayerDrop) {
        return slayerDropCounts.getOrDefault(slayerDrop, 0);
    }
    /**
     * Adds a kill to the slayer type
     */
    public void completedSlayer(String slayerTypeText) {
        SlayerBoss slayerBoss = SlayerBoss.getFromMobType(slayerTypeText);
        if (slayerBoss != null) {
            slayerKills.put(slayerBoss, slayerKills.getOrDefault(slayerBoss, 0) + 1);
            lastKilledBoss = slayerBoss;
            lastSlayerCompleted = System.currentTimeMillis();

            SkyblockAddons.getInstance().getPersistentValues().saveValues();
        }
    }

    public void checkInventoryDifferenceForDrops(List<ItemDiff> newInventoryDifference) {
        recentInventoryDifferences.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getKey() > 1000);
        recentInventoryDifferences.put(System.currentTimeMillis(), newInventoryDifference);

        // They haven't killed a dragon recently OR the last killed dragon was over 30 seconds ago...
        if (lastKilledBoss == null || lastSlayerCompleted == -1 || System.currentTimeMillis() - lastSlayerCompleted > 30 * 1000) {
            return;
        }


        for (List<ItemDiff> inventoryDifference : recentInventoryDifferences.values()) {
            for (ItemDiff itemDifference : inventoryDifference) {
                if (itemDifference.getAmount() < 1) {
                    continue;
                }

                for (SlayerDrop drop : lastKilledBoss.getDrops()) {
                    if (drop.getSkyblockID().equals(ItemUtils.getSkyBlockItemID(itemDifference.getExtraAttributes()))) {

                        // If this is a rune and it doesn't match, continue
                        Rune rune = ItemUtils.getRuneData(itemDifference.getExtraAttributes());
                        if (drop.getRuneID() != null && (rune == null || rune.getType() == null || !rune.getType().equals(drop.getRuneID()))) {
                            continue;
                        }

                        slayerDropCounts.put(drop, slayerDropCounts.getOrDefault(drop, 0) + itemDifference.getAmount());
                    }
                }
            }
        }

        recentInventoryDifferences.clear();
    }

    public void setStatManually(String[] args) {
        SlayerBoss slayerBoss;
        try {
            slayerBoss = SlayerBoss.getFromMobType(args[1]);
        } catch (IllegalArgumentException ex) {
            slayerBoss = null;
        }

        if (slayerBoss != null) {
            if (args[2].equalsIgnoreCase("kills")) {
                try {
                    int count = Integer.parseInt(args[3]);
                    slayerKills.put(slayerBoss, count);
                    SkyblockAddons.getInstance().getUtils().sendMessage("Kills for slayer " + args[1] + " was set to " + args[3] + ".");
                    SkyblockAddons.getInstance().getPersistentValues().saveValues();
                    return;
                } catch (NumberFormatException ex) {
                    SkyblockAddons.getInstance().getUtils().sendErrorMessage(args[3] + " is not a valid number!");
                    return;
                }
            }

            SlayerDrop slayerDrop;
            try {
                slayerDrop = SlayerDrop.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException ex) {
                slayerDrop = null;
            }

            if (slayerDrop != null) {
                try {
                    int count = Integer.parseInt(args[3]);
                    slayerDropCounts.put(slayerDrop, count);
                    SkyblockAddons.getInstance().getUtils().sendMessage("Statistic " + args[2] + " for slayer " + args[1] + " was set to " + args[3] + ".");
                    SkyblockAddons.getInstance().getPersistentValues().saveValues();
                    return;
                } catch (NumberFormatException ex) {
                    SkyblockAddons.getInstance().getUtils().sendErrorMessage(args[3] + " is not a valid number!");
                    return;
                }
            }

            SkyblockAddons.getInstance().getUtils().sendErrorMessage(args[2] + " is not a valid statistic!");
            return;
        }

        SkyblockAddons.getInstance().getUtils().sendErrorMessage(args[1] + " is not a valid boss!");
    }

    public static void setInstance(SlayerTracker instance) {
        SlayerTracker.instance = instance;
    }
}
