package codes.biscuit.skyblockaddons.mixins.accessors;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiChest.class)
public interface AccessorGuiChest {

    @Accessor
    IInventory getLowerChestInventory();
}
