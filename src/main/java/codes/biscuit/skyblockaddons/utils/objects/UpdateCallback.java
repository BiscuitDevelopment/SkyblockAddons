package codes.biscuit.skyblockaddons.utils.objects;

@FunctionalInterface
public interface UpdateCallback<T> {

    void onUpdate(T updatedValue);
}
