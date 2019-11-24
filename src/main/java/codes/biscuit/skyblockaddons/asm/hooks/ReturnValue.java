package codes.biscuit.skyblockaddons.asm.hooks;

public class ReturnValue<R> {

    private boolean cancelled = false;

    private R returnValue;

    public void cancel() {
        cancel(null);
    }

    public void cancel(R returnValue) {
        cancelled = true;
        this.returnValue = returnValue;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public R getReturnValue() {
        return returnValue;
    }
}
