package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.features.ItemDiff;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.skyblockdata.Rune;
import lombok.Getter;
import net.minecraft.command.ICommandSender;

import java.util.*;

public class SlayerTracker {

    @Getter private static SlayerTracker instance;

    private Map<SlayerBoss, Integer> slayerKills = new EnumMap<>(SlayerBoss.class);
    private Map<SlayerDrop, Integer> slayerDropCounts = new EnumMap<>(SlayerDrop.class);
    @Getter private SlayerBoss lastKilledBoss;

    // Saves the last second of inventory differences
    private transient Map<Long, List<ItemDiff>> recentInventoryDifferences = new HashMap<>();
    private transient long lastSlayerCompleted = -1;

    public SlayerTracker() {
        instance = this;
    }

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

            SkyblockAddons.getInstance().getPersistentValuesManager().saveValues();
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

    /**
     * Sets the value of a specific slayer stat
     * <p>
     * This method is called from {@link codes.biscuit.skyblockaddons.commands.SkyblockAddonsCommand#processCommand(ICommandSender, String[])}
     * when the player runs the command to change the slayer tracker stats.
     *
     * @param args the arguments provided when the player executed the command
     */
    public void setStatManually(String[] args) {
        SlayerBoss slayerBoss = SlayerBoss.getFromMobType(args[1]);

        if (slayerBoss == null) {
            throw new IllegalArgumentException(Translations.getMessage("commandUsage.sba.slayer.invalidBoss", args[1]));
        }

        if (args[2].equalsIgnoreCase("kills")) {
            int count = Integer.parseInt(args[3]);
            slayerKills.put(slayerBoss, count);
            SkyblockAddons.getInstance().getUtils().sendMessage(Translations.getMessage(
                    "commandUsage.sba.slayer.killsSet", args[1], args[3]));
            SkyblockAddons.getInstance().getPersistentValuesManager().saveValues();
            return;
        }

        SlayerDrop slayerDrop;
        try {
            slayerDrop = SlayerDrop.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException ex) {
            slayerDrop = null;
        }

        if (slayerDrop != null) {
            int count = Integer.parseInt(args[3]);
            slayerDropCounts.put(slayerDrop, count);
            SkyblockAddons.getInstance().getUtils().sendMessage(Translations.getMessage(
                    "commandUsage.sba.slayer.statSet", args[2], args[1], args[3]));
            SkyblockAddons.getInstance().getPersistentValuesManager().saveValues();
            return;
        }

        throw new IllegalArgumentException(Translations.getMessage("commandUsage.sba.slayer.invalidStat", args[1]));
    }

    public void setKillCount(SlayerBoss slayerBoss, int kills) {
        this.slayerKills.put(slayerBoss, kills);
    }

    public static void setInstance(SlayerTracker instance) {
        SlayerTracker.instance = instance;
    }
}
