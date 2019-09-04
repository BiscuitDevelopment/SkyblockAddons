package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends GuiContainer {

    public MixinGuiInventory(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (slotIn != null && main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                main.getUtils().isOnSkyblock() && main.getConfigValues().getLockedSlots().contains(slotIn.slotNumber)) {
            main.getUtils().playSound("note.bass", 0.5);
            return;
        }
        super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
    }
}
