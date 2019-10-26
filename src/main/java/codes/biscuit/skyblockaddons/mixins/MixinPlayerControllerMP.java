package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.CooldownEntry;
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

import java.util.Arrays;
import java.util.List;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    private long lastStemMessage = -1;
    private long lastUnmineableMessage = -1;
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
        List<EnumUtils.Location> deepCavernsLocations = Arrays.asList(
                EnumUtils.Location.DEEP_CAVERNS,
                EnumUtils.Location.GUNPOWDER_MINES,
                EnumUtils.Location.LAPIS_QUARRY,
                EnumUtils.Location.PIGMAN_DEN,
                EnumUtils.Location.SLIMEHILL,
                EnumUtils.Location.DIAMOND_RESERVE,
                EnumUtils.Location.OBSIDIAN_SANCTUARY);
        List<Block> mineableBlocks = Arrays.asList(
                Blocks.coal_ore,
                Blocks.iron_ore,
                Blocks.gold_ore,
                Blocks.redstone_ore,
                Blocks.emerald_ore,
                Blocks.diamond_ore,
                Blocks.diamond_block,
                Blocks.obsidian
        );

        if (heldItem != null) {
            if (main.getConfigValues().isEnabled(Feature.AVOID_BREAKING_STEMS) && (block.equals(Blocks.melon_stem) || block.equals(Blocks.pumpkin_stem))) {
                if (System.currentTimeMillis()- lastStemMessage > 20000) {
                    lastStemMessage = System.currentTimeMillis();
                    main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.AVOID_BREAKING_STEMS).getChatFormatting()+Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                }
                cir.setReturnValue(false);
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_MINE_ORES_DEEP_CAVERNS) && deepCavernsLocations.contains(main.getUtils().getLocation()) && heldItem.getUnlocalizedName().contains("pickaxe") && !mineableBlocks.contains(block)) {
                if (System.currentTimeMillis()- lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = System.currentTimeMillis();
                    main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.ONLY_MINE_ORES_DEEP_CAVERNS).getChatFormatting() + Message.MESSAGE_CANCELLED_NON_ORES_BREAK.getMessage());
                }

                cir.setReturnValue(false);
            } else if (main.getConfigValues().isEnabled(Feature.JUNGLE_AXE_COOLDOWN)) {
                CooldownEntry cooldown = main.getUtils().getItemCooldown("§aJungle Axe");
                if (cooldown != null && (block.equals(Blocks.log) || block.equals(Blocks.log2)) && cooldown.getLastUse() + cooldown.getCooldownMillis() > System.currentTimeMillis()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "onPlayerDestroyBlock", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onPlayerDestroyBlock(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
            SkyblockAddons.getInstance().getUtils().logEntry(Minecraft.getMinecraft().thePlayer.getHeldItem());
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
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.DONT_OPEN_PROFILES_WITH_BOW)) {
            Minecraft mc = Minecraft.getMinecraft();
            Entity entityIn = mc.objectMouseOver.entityHit;
            if (entityIn instanceof EntityOtherPlayerMP && main.getUtils().isNotNPC(entityIn)) {
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
    private void onWindowClick(int windowId, int slotNum, int clickButton, int clickModifier, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
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
