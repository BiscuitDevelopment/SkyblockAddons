package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.hooks.MinecraftHook;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayer {
    @Inject(method = "getCurrentItem", at = @At("HEAD"))
    public void updatedCurrentItem(CallbackInfoReturnable<ItemStack> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            int slot = mc.thePlayer.inventory.currentItem + 36;
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getConfigValues().getLockedSlots().contains(slot)
                    && (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)) {

                MinecraftHook.setLastLockedSlotItemChange(System.currentTimeMillis());
            }

            ItemStack heldItemStack = mc.thePlayer.getHeldItem();
            if (heldItemStack != null && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.getUtils().isInDungeon()
                    && !main.getUtils().getItemDropChecker().canDropItem(heldItemStack, true, false)) {

                MinecraftHook.setLastLockedSlotItemChange(System.currentTimeMillis());            }
        }
    }
}
