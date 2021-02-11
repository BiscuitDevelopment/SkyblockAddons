package codes.biscuit.skyblockaddons.features.dragontracker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.ItemDiff;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.skyblockdata.PetInfo;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragonTracker {

    @Getter private static final List<DragonType> dummyDragons = Lists.newArrayList(DragonType.PROTECTOR, DragonType.SUPERIOR, DragonType.WISE);
    @Getter private static final DragonTracker instance = new DragonTracker();

    private transient boolean contributedToCurrentDragon = false;
    private transient long lastDragonKilled = -1;
    private transient int eyesToPlace = 0;

    // Saves the last second of inventory differences
    private transient Map<Long, List<ItemDiff>> recentInventoryDifferences = new HashMap<>();

    public int getDragsSince(DragonsSince dragonsSince) {
        DragonTrackerData dragonTrackerData = SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getDragonTracker();
        return dragonTrackerData.getDragonsSince().getOrDefault(dragonsSince, 0);
    }

    public List<DragonType> getRecentDragons() {
        return SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getDragonTracker().getRecentDragons();
    }

    public void dragonSpawned(String dragonTypeText) {
        if (eyesToPlace > 0) {
            contributedToCurrentDragon = true;

            DragonTrackerData dragonTrackerData = SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getDragonTracker();
            DragonType dragonType = DragonType.fromName(dragonTypeText);
            if (dragonType != null) {
                if (dragonTrackerData.getRecentDragons().size() == 3) {
                    dragonTrackerData.getRecentDragons().remove(0);
                }
                dragonTrackerData.getRecentDragons().add(dragonType);
            }
            for (DragonsSince dragonsSince : DragonsSince.values()) {
                dragonTrackerData.getDragonsSince().put(dragonsSince, dragonTrackerData.getDragonsSince().getOrDefault(dragonsSince, 0) + 1);
            }
            if (dragonType == DragonType.SUPERIOR) {
                dragonTrackerData.getDragonsSince().put(DragonsSince.SUPERIOR, 0);
            }

            dragonTrackerData.setEyesPlaced(dragonTrackerData.getEyesPlaced() + eyesToPlace);
            eyesToPlace = 0;

            SkyblockAddons.getInstance().getPersistentValuesManager().saveValues();
        }
    }

    public void dragonKilled() {
        if (!contributedToCurrentDragon) {
            return;
        }

        lastDragonKilled = System.currentTimeMillis();
        contributedToCurrentDragon = false;
    }

    public void checkInventoryDifferenceForDrops(List<ItemDiff> newInventoryDifference) {
        recentInventoryDifferences.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getKey() > 1000);
        recentInventoryDifferences.put(System.currentTimeMillis(), newInventoryDifference);

        // They haven't killed a dragon recently OR the last killed dragon was over 60 seconds ago...
        if (lastDragonKilled == -1 || System.currentTimeMillis() - lastDragonKilled > 60 * 1000) {
            return;
        }


        for (List<ItemDiff> inventoryDifference : recentInventoryDifferences.values()) {
            for (ItemDiff itemDifference : inventoryDifference) {
                if (itemDifference.getAmount() < 1) {
                    continue;
                }

                DragonTrackerData dragonTrackerData = SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getDragonTracker();
                String skyBlockItemID = ItemUtils.getSkyblockItemID(itemDifference.getExtraAttributes());
                switch (skyBlockItemID) {
                    case "ASPECT_OF_THE_DRAGON":
                        dragonTrackerData.getDragonsSince().put(DragonsSince.ASPECT_OF_THE_DRAGONS, 0);
                        SkyblockAddons.getInstance().getPersistentValuesManager().saveValues();
                        break;
                    case "PET":
                        PetInfo petInfo = ItemUtils.getPetInfo(itemDifference.getExtraAttributes());
                        if (petInfo != null && "ENDER_DRAGON".equals(petInfo.getType())) {
                            dragonTrackerData.getDragonsSince().put(DragonsSince.ENDER_DRAGON_PET, 0);
                            SkyblockAddons.getInstance().getPersistentValuesManager().saveValues();
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        recentInventoryDifferences.clear();
    }

    public void reset() {
        eyesToPlace = 0;
        contributedToCurrentDragon = false;
        lastDragonKilled = -1;
    }

    public void addEye() {
        eyesToPlace++;
    }

    public void removeEye() {
        eyesToPlace--;
    }
}
