package codes.biscuit.skyblockaddons.utils.tasks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;

public class ShowInventoryFullWarning implements Runnable {
    private SkyblockAddons main;

    public ShowInventoryFullWarning(SkyblockAddons sba) {
        main = sba;
    }

    @Override
    public void run() {
        main.getUtils().playLoudSound("random.orb", 0.5);
        main.getRenderListener().setTitleFeature(Feature.FULL_INVENTORY_WARNING);
    }
}
