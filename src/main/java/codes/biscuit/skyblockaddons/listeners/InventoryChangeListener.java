package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreviewManager;
import lombok.NonNull;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;

/**
 * This listener is used for {@link GuiScreenListener}. Its
 * {@code onInventoryChanged} method is called when an item in the {@link InventoryBasic} it is listening
 * to changes.
 */
public class InventoryChangeListener implements IInvBasic {
    private final GuiScreenListener GUI_SCREEN_LISTENER;

    /**
     * Creates a new {@code InventoryChangeListener} with the given {@code GuiScreenListener} reference.
     *
     * @param guiScreenListener the {@code GuiScreenListener} reference
     */
    public InventoryChangeListener(@NonNull GuiScreenListener guiScreenListener) {
        GUI_SCREEN_LISTENER = guiScreenListener;
    }

    /**
     * This is called when an item in the {@code InventoryBasic} being listened to changes.
     *
     * @param inventory the {@code InventoryBasic} after the change
     */
    @Override
    public void onInventoryChanged(InventoryBasic inventory) {
        GUI_SCREEN_LISTENER.onInventoryChanged(inventory);
    }
}
