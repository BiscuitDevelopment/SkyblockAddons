package codes.biscuit.skyblockaddons.utils;

@FunctionalInterface
public interface UpdateCallback<T> {

    void onUpdate(T updatedValue);
}
