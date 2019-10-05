package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends GuiContainer {

    @Shadow
    private float oldMouseX;

    @Shadow
    private float oldMouseY;

    public MixinGuiInventory(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, ClickType clickType) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (slotIn != null && main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                main.getUtils().isOnSkyblock() && main.getConfigValues().getLockedSlots().contains(slotIn.slotNumber)) {
            main.getUtils().playSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
            return;
        }

        int j = guiLeft;
        int k = guiTop;
        boolean isOutsideGui = oldMouseX < j || oldMouseY < k || oldMouseX >= j + xSize || oldMouseY >= k + ySize;

        if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && mc.player.inventory.getItemStack() != null && isOutsideGui &&
                main.getInventoryUtils().shouldCancelDrop(mc.player.inventory.getItemStack()))
            return;

        super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
    }

}
