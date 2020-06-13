package codes.biscuit.skyblockaddons.scheduler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class NewScheduler {

    private static final NewScheduler INSTANCE = new NewScheduler();
    private final List<ScheduledTask> activeTasks = new ArrayList<>();
    private final List<ScheduledTask> pendingTasks = new ArrayList<>();
    private final Object anchor = new Object();
    private final Thread taskCleaner;
    private volatile long currentTicks = 0;
    private volatile long totalTicks = 0;

    public NewScheduler() {
        this.taskCleaner = new Thread(() -> {
            this.pendingTasks.removeIf(ScheduledTask::isCompleted);
            this.activeTasks.removeIf(ScheduledTask::isCompleted);

            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) { }
        });
        this.taskCleaner.start();
    }

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
                List<ScheduledTask> removeTasks = new ArrayList<>();
                for (ScheduledTask scheduledTask : this.pendingTasks) {
                    if (this.getTotalTicks() >= (scheduledTask.getAddedTicks() + scheduledTask.getDelay())) {
                        if (!scheduledTask.isCanceled()) {
                            this.activeTasks.add(scheduledTask);
                            scheduledTask.start();

                            if (!scheduledTask.isCompleted() && scheduledTask.getPeriod() > 0) {
                                scheduledTask.setDelay(scheduledTask.getPeriod());
                                this.pendingTasks.add(scheduledTask);
                            } else
                                removeTasks.add(scheduledTask);
                        }
                    }
                }

                this.pendingTasks.removeIf(removeTasks::contains);
            }
        }
    }

    public synchronized void cancel(int id) {
        pendingTasks.forEach(scheduledTask -> {
            if (scheduledTask.getId() == id)
                scheduledTask.cancel();
        });

        activeTasks.forEach(scheduledTask -> {
            if (scheduledTask.getId() == id)
                scheduledTask.cancel();
        });
    }

    public void cancel(ScheduledTask task) {
        task.cancel();
    }

    public static NewScheduler getInstance() {
        return INSTANCE;
    }

    /**
     * Repeats a task (synchronously) every tick.<br><br>
     *
     * Warning: This method is ran on the main thread, don't do anything heavy.
     * @param task The task to run.
     * @return The scheduled task.
     */
    public ScheduledTask repeat(Runnable task) {
        return this.schedule(task, 0, 1);
    }

    /**
     * Repeats a task (asynchronously) every tick.
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    public ScheduledTask repeatAsync(Runnable task) {
        return this.runAsync(task, 0, 1);
    }

    /**
     * Runs a task (asynchronously) on the next tick.
     *
     * @param task The task to run.
     * @return The scheduled task.
     */
    public ScheduledTask runAsync(Runnable task) {
        return this.runAsync(task, 0);
    }

    /**
     * Runs a task (asynchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @return The scheduled task.
     */
    public ScheduledTask runAsync(Runnable task, int delay) {
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
    public ScheduledTask runAsync(Runnable task, int delay, int period) {
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
    public ScheduledTask schedule(Runnable task) {
        return this.schedule(task, 0);
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @return The scheduled task.
     */
    public ScheduledTask schedule(Runnable task, int delay) {
        return this.schedule(task, delay, 0);
    }

    /**
     * Runs a task (synchronously) on the next tick.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is ran.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @return The scheduled task.
     */
    public ScheduledTask schedule(Runnable task, int delay, int period) {
        ScheduledTask scheduledTask = new ScheduledTask(task, delay, period, false);
        this.pendingTasks.add(scheduledTask);
        return scheduledTask;
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
