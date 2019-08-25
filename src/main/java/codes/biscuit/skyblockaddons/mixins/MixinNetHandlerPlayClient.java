package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    /**
     *  These two injections make sure
     */
    @Inject(method = "handleSetSlot", at = @At(value = "HEAD"), cancellable = true)
    private void handleSetSlot(S2FPacketSetSlot packetIn, CallbackInfo ci) {
        if (packetIn != null) {
            ItemStack item = packetIn.func_149174_e();
            int windowID = packetIn.func_149175_c();
            int slot = packetIn.func_149173_d();
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (item != null && main != null && main.getUtils().isOnSkyblock() &&
                    main.getConfigValues().isEnabled(Feature.STOP_BOW_CHARGE_FROM_RESETTING) && windowID == 0) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc != null) {
                    EntityPlayer p = mc.thePlayer;
                    if (p != null) {
                        InventoryPlayer inventory = p.inventory;
                        if (inventory != null) {
                            if (slot-36 == inventory.currentItem && isShootingBow(item, mc, inventory.getCurrentItem())) {
                                ci.cancel();
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "handleWindowItems", at = @At(value = "HEAD"))
    private void handleSetSlot(S30PacketWindowItems packetIn, CallbackInfo ci) {
        if (packetIn != null) {
            ItemStack[] itemStacks = packetIn.getItemStacks();
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (itemStacks != null && itemStacks.length == 45 && main != null &&
                    main.getConfigValues().isEnabled(Feature.STOP_BOW_CHARGE_FROM_RESETTING) && main.getUtils().isOnSkyblock()) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc != null) {
                    EntityPlayer p = mc.thePlayer;
                    if (p != null) {
                        InventoryPlayer inventory = p.inventory;
                        if (inventory != null) {
                            int slot = 36 + inventory.currentItem;
                            ItemStack item = itemStacks[slot];
                            ItemStack currentItem = inventory.getCurrentItem();
                            if (isShootingBow(item, mc, currentItem)) {
                                itemStacks[slot] = currentItem;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isShootingBow(ItemStack itemStack, Minecraft mc, ItemStack currentItemStack) {
        if (itemStack != null && currentItemStack != null) {
            Item item = itemStack.getItem();
            Item currentItem = currentItemStack.getItem();
            return item != null && currentItem != null && item.equals(Items.bow) && currentItem.equals(Items.bow) &&
                    mc.gameSettings.keyBindUseItem.isKeyDown();
        }
        return false;
    }
}
