package codes.biscuit.skyblockaddons.gui.buttons;

@FunctionalInterface
public interface UpdateCallback<T> {

    void onUpdate(T updatedValue);
}
