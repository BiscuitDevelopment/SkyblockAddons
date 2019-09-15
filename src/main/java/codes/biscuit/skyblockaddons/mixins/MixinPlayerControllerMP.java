package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
                    main.getUtils().sendMessage(EnumChatFormatting.RED+Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                }
                cir.setReturnValue(false);
                return;
            }
        }
        if (heldItem == null || heldItem.getItem().equals(Items.diamond_hoe) || heldItem.getItem().equals(Items.iron_hoe) || heldItem.getItem().equals(Items.golden_hoe)
                || heldItem.getItem().equals(Items.wooden_hoe) || heldItem.getItem().equals(Items.wooden_hoe)) {
            if (main.getConfigValues().isEnabled(Feature.AVOID_BREAKING_BOTTOM_SUGAR_CANE) && (block.equals(Blocks.reeds) && mc.theWorld.getBlockState(loc.down()).getBlock() != Blocks.reeds)) {
                if (System.currentTimeMillis()- lastStemMessage > 20000) {
                    lastStemMessage = System.currentTimeMillis();
                    main.getUtils().sendMessage(EnumChatFormatting.RED+Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                }
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "isPlayerRightClickingOnEntity", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onPlayerRightClickEntity(EntityPlayer player, Entity entityIn, MovingObjectPosition movingObject, CallbackInfoReturnable<Boolean> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isEnabled(Feature.DONT_OPEN_PROFILES_WITH_BOW)) {
            if (entityIn instanceof EntityOtherPlayerMP) {
                ItemStack item = player.inventory.getCurrentItem();
                if (item != null && item.getItem() != null && item.getItem().equals(Items.bow)) {
                    if (System.currentTimeMillis()- lastProfileMessage > 20000) {
                        lastProfileMessage = System.currentTimeMillis();
                        main.getUtils().sendMessage(EnumChatFormatting.RED+Message.MESSAGE_STOPPED_OPENING_PROFILE.getMessage());
                    }
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
