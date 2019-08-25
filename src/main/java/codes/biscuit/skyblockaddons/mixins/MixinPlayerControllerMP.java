package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    private long lastMessage = -1;

    /**
     * Cancels stem breaks if holding an item, to avoid accidental breaking.
     */
    @Inject(method = "clickBlock", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onPlayerDamageBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isEnabled(Feature.AVOID_BREAKING_STEMS)) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP p = mc.thePlayer;
            ItemStack heldItem = p.getHeldItem();
            Block block = mc.theWorld.getBlockState(loc).getBlock();
            if (heldItem != null && (block.equals(Blocks.melon_stem) || block.equals(Blocks.pumpkin_stem))) {
                cir.setReturnValue(false);
                if (System.currentTimeMillis()-lastMessage > 15000) {
                    lastMessage = System.currentTimeMillis();
                    main.getUtils().sendMessage(EnumChatFormatting.RED+Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                }
            }
        }
    }
}
