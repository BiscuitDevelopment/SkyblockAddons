package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreviewManager;
import net.minecraft.inventory.IInvBasic;
import net.minecraft.inventory.InventoryBasic;

/**
 * This listener is used for {@link codes.biscuit.skyblockaddons.core.Feature#SHOW_BACKPACK_PREVIEW}. Its
 * {@code onInventoryChanged} method is called when an item in the {@link InventoryBasic} it is listening
 * to changes.
 */
public class InventoryChangeListener implements IInvBasic {

    /**
     * This is called when an item in the {@code InventoryBasic} being listened to changes.
     *
     * @param inventory the {@code InventoryBasic} after the change
     */
    @Override
    public void onInventoryChanged(InventoryBasic inventory) {
        ContainerPreviewManager.onInventoryChange(inventory);
    }
}
