package codes.biscuit.skyblockaddons.misc.scheduler;

/**
 * This is a repeating scheduled task that runs a maximum of {@code RUN_LIMIT} times.
 */
public class LimitedRepeatingScheduledTask extends ScheduledTask {
    private final int RUN_LIMIT;

    private int runCount;

    /**
     * Creates a new Limited Repeating Scheduled Task.
     * This task is a repeating task that runs for a maximum of {@code runLimit} times.
     *
     * @param delay The delay (in ticks) to wait before the task is run.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param async If the task should be run asynchronously.
     * @param runLimit the maximum number of times this task should be run.
     */
    public LimitedRepeatingScheduledTask(int delay, int period, boolean async, int runLimit) {
        super(delay, period, async);
        runCount = 0;
        this.RUN_LIMIT = runLimit;
    }

    /**
     * Creates a new Limited Repeating Scheduled Task.
     * This task is a repeating task that runs for a maximum of {@code runLimit} times.
     *
     * @param task The task to run.
     * @param delay The delay (in ticks) to wait before the task is run.
     * @param period The delay (in ticks) to wait before calling the task again.
     * @param async If the task should be run asynchronously.
     * @param runLimit the maximum number of times this task should be run.
     */
    public LimitedRepeatingScheduledTask(SkyblockRunnable task, int delay, int period, boolean async, int runLimit) {
        super(task, delay, period, async);
        runCount = 0;
        this.RUN_LIMIT = runLimit;
    }

    /**
     * Starts the task. The run count is incremented every time the task runs until {@code RUN_LIMIT} is reached,
     * at which point the task is cancelled.
     */
    @Override
    public void start() {
        if (runCount < RUN_LIMIT) {
            runCount++;
            super.start();
        } else {
            cancel();
        }
    }
}
