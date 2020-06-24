package codes.biscuit.skyblockaddons.scheduler;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Setter;

public abstract class SkyblockRunnable implements Runnable {

    @Setter private ScheduledTask thisTask;

    public void cancel() {
        SkyblockAddons.getInstance().getNewScheduler().cancel(thisTask);
    }
}
