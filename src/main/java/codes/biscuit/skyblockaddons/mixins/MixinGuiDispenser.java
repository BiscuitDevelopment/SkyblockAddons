package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiDispenser.class)
public abstract class MixinGuiDispenser extends GuiContainer {

    public MixinGuiDispenser(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, ClickType clickType) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        out:
        if (slotIn != null && main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                main.getUtils().isOnSkyblock()) {
            int slotNum = slotIn.slotNumber;
            if (slotNum > 8 && main.getConfigValues().getLockedSlots().contains(slotNum)) {
                main.getUtils().playSound("note.bass", 0.5);
                return;
            }
        }
        super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
    }
}
