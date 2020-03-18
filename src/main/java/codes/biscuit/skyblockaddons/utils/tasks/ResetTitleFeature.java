package codes.biscuit.skyblockaddons.utils.tasks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.Minecraft;

public class ResetTitleFeature implements Runnable {
    private Minecraft mc;
    private SkyblockAddons main;

    public ResetTitleFeature() {
        mc = Minecraft.getMinecraft();
        main = SkyblockAddons.getInstance();
    }

    @Override
    public void run() {
        // Here I'm using the Minecraft task scheduler to avoid concurrency-related crashes since this task is being scheduled in another thread.
        mc.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                main.getRenderListener().setTitleFeature(null);
            }
        });
    }
}
