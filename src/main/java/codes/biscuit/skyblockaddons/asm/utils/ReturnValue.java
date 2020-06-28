package codes.biscuit.skyblockaddons.asm.utils;

public class ReturnValue<R> {

    private boolean cancelled;

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
