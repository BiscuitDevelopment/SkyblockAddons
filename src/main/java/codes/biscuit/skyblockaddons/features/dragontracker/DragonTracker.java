package codes.biscuit.skyblockaddons.features.dragontracker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.ItemDiff;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.skyblockdata.PetInfo;
import lombok.Getter;

import java.util.*;

public class DragonTracker {

    @Getter private static DragonTracker instance = new DragonTracker();

    @Getter private List<DragonType> recentDragons = new LinkedList<>();
    @Getter private Map<DragonsSince, Integer> dragonsSince = new EnumMap<>(DragonsSince.class);
    @Getter private int eyesPlaced = 0;

    private transient boolean contributedToCurrentDragon = false;
    private transient long lastDragonKilled = -1;
    private transient int eyesToPlace = 0;

    // Saves the last second of inventory differences
    private transient Map<Long, List<ItemDiff>> recentInventoryDifferences = new HashMap<>();

    public int getDragsSince(DragonsSince dragonsSince) {
        return this.dragonsSince.getOrDefault(dragonsSince, 0);
    }

    public void dragonSpawned(String dragonTypeText) {
        if (eyesToPlace > 0) {
            contributedToCurrentDragon = true;

            DragonType dragonType = DragonType.fromName(dragonTypeText);
            if (dragonType != null) {
                if (recentDragons.size() == 3) {
                    recentDragons.remove(0);
                }
                recentDragons.add(dragonType);
            }
            for (DragonsSince dragonsSince : DragonsSince.values()) {
                this.dragonsSince.put(dragonsSince, this.dragonsSince.getOrDefault(dragonsSince, 0) + 1);
            }
            if (dragonType == DragonType.SUPERIOR) {
                dragonsSince.put(DragonsSince.SUPERIOR, 0);
            }

            eyesPlaced += eyesToPlace;
            eyesToPlace = 0;

            SkyblockAddons.getInstance().getPersistentValues().saveValues();
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

                String skyBlockItemID = ItemUtils.getSkyBlockItemID(itemDifference.getExtraAttributes());
                switch (skyBlockItemID) {
                    case "ASPECT_OF_THE_DRAGON":
                        dragonsSince.put(DragonsSince.ASPECT_OF_THE_DRAGONS, 0);
                        SkyblockAddons.getInstance().getPersistentValues().saveValues();
                        break;
                    case "PET":
                        PetInfo petInfo = ItemUtils.getPetInfo(itemDifference.getExtraAttributes());
                        if (petInfo != null && "ENDER_DRAGON".equals(petInfo.getType())) {
                            dragonsSince.put(DragonsSince.ENDER_DRAGON_PET, 0);
                            SkyblockAddons.getInstance().getPersistentValues().saveValues();
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

    public static void setInstance(DragonTracker instance) {
        DragonTracker.instance = instance;
    }
}
