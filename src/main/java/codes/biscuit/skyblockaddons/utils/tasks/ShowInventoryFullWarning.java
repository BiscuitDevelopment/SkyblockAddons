package codes.biscuit.skyblockaddons.utils.tasks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;

public class ShowInventoryFullWarning implements Runnable {
    private Minecraft mc;
    private SkyblockAddons main;

    public ShowInventoryFullWarning() {
        mc = Minecraft.getMinecraft();
        main = SkyblockAddons.getInstance();
    }

    @Override
    public void run() {
        // Here I'm using the Minecraft task scheduler to avoid concurrency-related crashes since this task is being scheduled in another thread.
        mc.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                main.getUtils().playLoudSound("random.orb", 0.5);
            }
        });
        mc.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                main.getRenderListener().setTitleFeature(Feature.FULL_INVENTORY_WARNING);
            }
        });
    }
}
