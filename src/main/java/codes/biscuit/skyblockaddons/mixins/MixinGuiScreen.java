package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Backpack;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

    @Inject(method = "renderToolTip", at = @At(value = "HEAD"), cancellable = true)
    private void shouldRenderRedirect(ItemStack stack, int x, int y, CallbackInfo ci) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (stack.getItem().equals(Items.SKULL) && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW)) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_HOLDING_SHIFT) && !GuiScreen.isShiftKeyDown()) {
                return;
            }
            Container playerContainer = Minecraft.getMinecraft().player.openContainer;
            if (playerContainer instanceof ContainerChest) { // Avoid showing backpack preview in auction stuff.
                IInventory chest = ((ContainerChest)playerContainer).getLowerChestInventory();
                if (chest.hasCustomName()) {
                    String chestName = chest.getDisplayName().getUnformattedText();
                    if (chestName.contains("Auction") || chestName.equals("Your Bids")) {
                        return;
                    }
                }
            }
            Backpack backpack = Backpack.getFromItem(stack);
            if (backpack != null) {
                backpack.setX(x);
                backpack.setY(y);
                main.getUtils().setBackpackToRender(backpack);
                main.getPlayerListener().onItemTooltip(new ItemTooltipEvent(stack, null, null, ITooltipFlag.TooltipFlags.NORMAL));
                ci.cancel();
            }
        }
    }
}
