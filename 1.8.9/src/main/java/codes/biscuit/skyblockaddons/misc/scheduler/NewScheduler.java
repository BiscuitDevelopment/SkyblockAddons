package codes.biscuit.skyblockaddons.misc.scheduler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class NewScheduler {

    private final List<ScheduledTask> queuedTasks = new ArrayList<>();
    private final List<ScheduledTask> pendingTasks = new ArrayList<>();
    private final Object anchor = new Object();
    private volatile long currentTicks = 0;
    private volatile long totalTicks = 0;

    public synchronized long getCurrentTicks() {
        return this.currentTicks;
    }

    public synchronized long getTotalTicks() {
        return this.totalTicks;
    }

    @SubscribeEvent
    public void ticker(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            synchronized (this.anchor) {
                this.totalTicks++;
                this.currentTicks++;
            }

            if (Minecraft.getMinecraft() != null) {
                this.pendingTasks.removeIf(ScheduledTask::isCanceled);

                this.pendingTasks.addAll(queuedTasks);
                queuedTasks.clear();

                try {
                    for (ScheduledTask scheduledTask : this.pendingTasks) {
                        if (this.getTotalTicks() >= (scheduledTask.getAddedTicks() + scheduledTask.getDelay())) {
                            scheduledTask.start();

                            if (scheduledTask.isRepeating()) {
                                if (!scheduledTask.isCanceled()) {
                                    scheduledTask.setDelay(scheduledTask.getPeriod());
                                }
                            } else {
                                scheduledTask.cancel();
                            }
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public synchronized void cancel(int id) {
        pendingTasks.forEach(scheduledTask -> {
            if (scheduledTask.getId() == id)
                scheduledTask.cancel();
        });
    }

    public void cancel(ScheduledTask task) {
        task.cancel();
    }
    /**
     * Repeats a task (synchronously) every tick.<br><br>
     *
     * Warning: This method is ran on the main thread, don't do anything heavy.
     * @param task The task to run.
     * @return The scheduled task.
     */
    public ScheduledTask repeat(SkyblockRunnable task) {
        return this.scheduleRepeatingTask(task, 0, 1);
    }

    /**
     * Repeats a task (asynchronously) every tick.
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    public ScheduledTask repeatAsync(SkyblockRunnable task) {
        return this.runAsync(task, 0, 1);
    }

    /**
     * Runs a task (asynchronously) on the next tick.
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    public ScheduledTask runAsync(SkyblockRunnable task) {
        return this.runAsync(task, 0);
    }

    /**
     * Runs a task (asynchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @return The scheduled task.
     */
    public ScheduledTask runAsync(SkyblockRunnable task, int delay) {
        return this.runAsync(task, delay, 0);
    }

    /**
     * Runs a task (asynchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @return The scheduled task.
     */
    public ScheduledTask runAsync(SkyblockRunnable task, int delay, int period) {
        ScheduledTask scheduledTask = new ScheduledTask(task, delay, period, true);
        this.pendingTasks.add(scheduledTask);
        return scheduledTask;
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    public ScheduledTask scheduleTask(SkyblockRunnable task) {
        return this.scheduleDelayedTask(task, 0);
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @return The scheduled task.
     */
    public ScheduledTask scheduleDelayedTask(SkyblockRunnable task, int delay) {
        return this.scheduleRepeatingTask(task, delay, 0);
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @return The scheduled task.
     */
    public ScheduledTask scheduleRepeatingTask(SkyblockRunnable task, int delay, int period) {
        return this.scheduleRepeatingTask(task, delay, period, false);
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param queued Whether or not to queue this task to be run next loop
     *               (to be used for scheduling tasks directly from a synchronous task.
     * @return The scheduled task.
     */
    public ScheduledTask scheduleRepeatingTask(SkyblockRunnable task, int delay, int period, boolean queued) {
        ScheduledTask scheduledTask = new ScheduledTask(task, delay, period, false);
        if (queued) {
            this.queuedTasks.add(scheduledTask);
        } else {
            this.pendingTasks.add(scheduledTask);
        }
        return scheduledTask;
    }

    /**
     * Runs a task  on the next tick.
     *
     * @param scheduledTask The ScheduledTask to run.
     */
    public void schedule(ScheduledTask scheduledTask) {
        this.pendingTasks.add(scheduledTask);
    }

    /**
     * Causes the currently executing thread to sleep (temporarily cease execution) for the specified number of milliseconds, subject to the precision and accuracy of system timers and schedulers. The thread does not lose ownership of any monitors.
     *
     * @param millis the length of time to sleep in milliseconds
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) { }
    }
}
