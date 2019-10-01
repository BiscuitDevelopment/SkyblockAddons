package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    private long lastStemMessage = -1;
    private long lastProfileMessage = -1;

    /**
     * Cancels stem breaks if holding an item, to avoid accidental breaking.
     */
    @Inject(method = "clickBlock", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onPlayerDamageBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = mc.thePlayer;
        ItemStack heldItem = p.getHeldItem();
        Block block = mc.theWorld.getBlockState(loc).getBlock();
        if (heldItem != null) {
            if (main.getConfigValues().isEnabled(Feature.AVOID_BREAKING_STEMS) && (block.equals(Blocks.melon_stem) || block.equals(Blocks.pumpkin_stem))) {
                if (System.currentTimeMillis()- lastStemMessage > 20000) {
                    lastStemMessage = System.currentTimeMillis();
                    main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.AVOID_BREAKING_STEMS).getChatFormatting()+Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                }
                cir.setReturnValue(false);
                return;
            }
        }
        if (main.getConfigValues().isEnabled(Feature.AVOID_BREAKING_BOTTOM_SUGAR_CANE) && main.getUtils().getLocation() == EnumUtils.Location.ISLAND
                && (block.equals(Blocks.reeds) && mc.theWorld.getBlockState(loc.down()).getBlock() != Blocks.reeds)) {
            if (heldItem == null || heldItem.getItem().equals(Items.reeds) || heldItem.getItem().equals(Items.diamond_hoe)
                    || heldItem.getItem().equals(Items.iron_hoe) || heldItem.getItem().equals(Items.golden_hoe) || heldItem.getItem().equals(Items.stone_hoe)
                    || heldItem.getItem().equals(Items.wooden_hoe)) {
                if (System.currentTimeMillis() - lastStemMessage > 20000) {
                    lastStemMessage = System.currentTimeMillis();
                    main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.AVOID_BREAKING_BOTTOM_SUGAR_CANE).getChatFormatting() + Message.MESSAGE_CANCELLED_CANE_BREAK.getMessage());
                }
                cir.setReturnValue(false);
            }
        }
    }

    @Redirect(method = "isPlayerRightClickingOnEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/NetHandlerPlayClient;addToSendQueue(Lnet/minecraft/network/Packet;)V",
    ordinal = 0))
    private void onPlayerRightClickEntity(NetHandlerPlayClient netHandlerPlayClient, Packet p_147297_1_) {
        checkIfShouldSendPacket(netHandlerPlayClient, p_147297_1_);
    }

    @Redirect(method = "interactWithEntitySendPacket", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/NetHandlerPlayClient;addToSendQueue(Lnet/minecraft/network/Packet;)V",
            ordinal = 0))
    private void interactWithEntitySendPacket(NetHandlerPlayClient netHandlerPlayClient, Packet p_147297_1_) {
        checkIfShouldSendPacket(netHandlerPlayClient, p_147297_1_);
    }

    private void checkIfShouldSendPacket(NetHandlerPlayClient netHandlerPlayClient, Packet p_147297_1_) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isEnabled(Feature.DONT_OPEN_PROFILES_WITH_BOW)) {
            Minecraft mc = Minecraft.getMinecraft();
            Entity entityIn = mc.objectMouseOver.entityHit;
            if (entityIn instanceof EntityOtherPlayerMP && !main.getUtils().isNPC(entityIn)) {
                ItemStack item = mc.thePlayer.inventory.getCurrentItem();
                ItemStack itemInUse = mc.thePlayer.getItemInUse();
                if ((item != null && item.getItem() != null && item.getItem().equals(Items.bow)) ||
                        (itemInUse != null && itemInUse.getItem() != null && itemInUse.getItem().equals(Items.bow))) {
                    if (System.currentTimeMillis()- lastProfileMessage > 20000) {
                        lastProfileMessage = System.currentTimeMillis();
                        main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.DONT_OPEN_PROFILES_WITH_BOW).getChatFormatting()+
                                Message.MESSAGE_STOPPED_OPENING_PROFILE.getMessage());
                    }
                    return;
                }
            }
        }
        netHandlerPlayClient.addToSendQueue(p_147297_1_);
    }

    /**
     * Cancels clicking a locked inventory slot, even from other mods
     */
    @Inject(method = "windowClick", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void onWindowClick(int windowId, int slotNum, int clickButton, int clickModifier, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (player != null && player.openContainer != null) {
            slotNum += main.getInventoryUtils().getSlotDifference(player.openContainer);
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getUtils().isOnSkyblock()
                    && main.getConfigValues().getLockedSlots().contains(slotNum)
                    && (slotNum >= 9 || player.openContainer instanceof ContainerPlayer && slotNum >= 5)){
                main.getUtils().playSound("note.bass", 0.5);
                cir.setReturnValue(null);
                cir.cancel();
            }
        }
    }
}
