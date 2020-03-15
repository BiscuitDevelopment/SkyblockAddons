package codes.biscuit.skyblockaddons.utils.tasks;

import codes.biscuit.skyblockaddons.SkyblockAddons;

public class ResetTitleFeature implements Runnable {
    private SkyblockAddons main;

    public ResetTitleFeature(SkyblockAddons sba) {
        main = sba;
    }

    @Override
    public void run() {
        main.getRenderListener().setTitleFeature(null);
    }
}
