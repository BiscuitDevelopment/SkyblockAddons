package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Inject(method = "handleSetSlot", at = @At(value = "HEAD"), cancellable = true)
    private void handleSetSlot(S2FPacketSetSlot packetIn, CallbackInfo ci) {
        ItemStack item = packetIn.func_149174_e();
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (item != null && main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.STOP_BOW_CHARGE_FROM_RESETTING)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (packetIn.func_149175_c() == 0 && packetIn.func_149173_d()-36 == mc.thePlayer.inventory.currentItem
                    && item.getItem().equals(Items.bow) && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleWindowItems", at = @At(value = "HEAD"))
    private void handleSetSlot(S30PacketWindowItems packetIn, CallbackInfo ci) {
        ItemStack[] itemStacks = packetIn.getItemStacks();
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (itemStacks.length == 45 && main.getConfigValues().isEnabled(Feature.STOP_BOW_CHARGE_FROM_RESETTING)) {
            int slot = 36+Minecraft.getMinecraft().thePlayer.inventory.currentItem;
            ItemStack item = itemStacks[slot];
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer p =  mc.thePlayer;
            if (item != null && main.getUtils().isOnSkyblock() && item.getItem().equals(Items.bow) && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                itemStacks[slot] = p.inventory.getCurrentItem();
            }
        }
    }
}
