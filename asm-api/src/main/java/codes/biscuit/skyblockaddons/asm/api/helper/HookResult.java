package codes.biscuit.skyblockaddons.asm.api.helper;

/**
 * A helper for getting result from hook to include in return or another function.
 *
 * @param <V> The type of the result we are getting from the hook
 *
 * @author iHDeveloper
 */
public class HookResult<V> {

    private boolean empty = false;
    private V value = null;

    /**
     * Check if the result is empty or not. Or, have the result been set before or not.
     *
     * @return Is the result empty or not
     */
    public boolean is() {
        return empty;
    }

    /**
     * Set the result to be nothing but it's not empty!
     */
    public void set() {
        set(null);
    }

    /**
     * Set the result to certain value. And, it's not empty anymore.
     *
     * @param value The value to include in the result
     */
    public void set(V value) {
        this.value = value;
        this.empty = true;
    }

    /**
     * Get the value from the result
     *
     * @return The value that's has been set by the hook
     */
    public V get() {
        return value;
    }

}
