package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.inventory.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiDispenser.class)
public abstract class MixinGuiDispenser extends GuiContainer {

    public MixinGuiDispenser(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        out:
        if (slotIn != null && main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                main.getUtils().isOnSkyblock()) {
            int slotNum = slotIn.slotNumber;
            if (slotNum < 9) break out; // for dispensers
            if (main.getConfigValues().getLockedSlots().contains(slotNum)) {
                main.getUtils().playSound("note.bass", 0.5);
                return;
            }
        }
        super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
    }
}
