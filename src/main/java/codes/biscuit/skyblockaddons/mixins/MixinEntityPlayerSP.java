package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    private Item lastItem = null;
    private long lastDrop = System.currentTimeMillis();

    @Inject(method = "dropOneItem", at = @At(value = "HEAD"), cancellable = true)
    private void dropOneItemConfirmation(boolean dropAll, CallbackInfoReturnable<EntityItem> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItemStack = mc.thePlayer.getHeldItem();
        if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getUtils().isOnSkyblock()) {
            int slot = mc.thePlayer.inventory.currentItem+36;
            if (main.getConfigValues().getLockedSlots().contains(slot)
                    && (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)) {
                main.getUtils().playSound("note.bass", 0.5);
                SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getColor(Feature.DROP_CONFIRMATION).getChatFormatting() +
                        Message.MESSAGE_SLOT_LOCKED.getMessage());
                cir.setReturnValue(null);
                return;
            }
        }
        if (heldItemStack != null) {
            EnumUtils.Rarity rarity = EnumUtils.Rarity.getRarity(heldItemStack);
            if (rarity != null && main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) &&
                    main.getUtils().cantDropItem(heldItemStack, rarity, true)) {
                SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getColor(Feature.STOP_DROPPING_SELLING_RARE_ITEMS).
                        getChatFormatting() + Message.MESSAGE_CANCELLED_DROPPING.getMessage());
                cir.setReturnValue(null);
                return;
            }
            if (main.getConfigValues().isEnabled(Feature.DROP_CONFIRMATION) && (main.getUtils().isOnSkyblock() ||
                    main.getConfigValues().isEnabled(Feature.DOUBLE_DROP_IN_OTHER_GAMES))) {
                Item heldItem = heldItemStack.getItem();
                if (lastItem != null && lastItem == heldItem && System.currentTimeMillis() - lastDrop < 3000) {
                    lastDrop = System.currentTimeMillis();
                } else {
                    SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getColor(Feature.DROP_CONFIRMATION).getChatFormatting() +
                            Message.MESSAGE_DROP_CONFIRMATION.getMessage());
                    lastItem = heldItem;
                    lastDrop = System.currentTimeMillis();
                    cir.setReturnValue(null);
                }
            }
        }
    }
}
