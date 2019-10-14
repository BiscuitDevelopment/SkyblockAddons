package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Blacklist;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
    @Inject(
            method = "clickBlock",
            at = @At(
                    value = "HEAD"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void onPlayerDamageBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        Block block = mc.world.getBlockState(loc).getBlock();
        Block blockFloor = mc.world.getBlockState(loc.down()).getBlock();

        if (main.getUtils().getLocation() == EnumUtils.Location.ISLAND) {
            if (main.getConfigValues().isEnabled(Feature.AVOID_BREAKING_STEMS)) {
                if (!GuiScreen.isCtrlKeyDown()) {
                    if (Blocks.MELON_STEM.equals(block) || Blocks.PUMPKIN_STEM.equals(block)) {
                        if (System.currentTimeMillis() - lastStemMessage > 20000) {
                            lastStemMessage = System.currentTimeMillis();
                            ChatFormatting formatting = main.getConfigValues().getColor(Feature.AVOID_BREAKING_STEMS).getChatFormatting();
                            main.getUtils().sendMessage(formatting + Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                        }

                        cir.setReturnValue(false);
                        return;
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.AVOID_BREAKING_BOTTOM_SUGAR_CANE)) {
                if (!GuiScreen.isCtrlKeyDown()) {
                    if ((Blocks.REEDS.equals(block) && !Blocks.REEDS.equals(blockFloor)) ||
                            (Blocks.CACTUS.equals(block) && !Blocks.CACTUS.equals(blockFloor))) {
                        if (System.currentTimeMillis() - lastStemMessage > 20000) {
                            lastStemMessage = System.currentTimeMillis();
                            ChatFormatting formatting = main.getConfigValues().getColor(Feature.AVOID_BREAKING_BOTTOM_SUGAR_CANE).getChatFormatting();
                            main.getUtils().sendMessage(formatting + Message.MESSAGE_CANCELLED_CANE_BREAK.getMessage());
                        }

                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }

    /**
     * This blocks interaction with Ember Rods on your island, to avoid blowing up chests.
     */
    @Inject(
            method = "processRightClick",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void processRightClick(EntityPlayer player, World worldIn, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItem = player.getHeldItemMainhand();

        if (player.equals(mc.player) && !ItemStack.EMPTY.equals(heldItem)) {
            for (Blacklist.BlacklistedItem blacklistedItem : Blacklist.DO_NOT_RIGHT_CLICK) {
                if (blacklistedItem.isEnabled()) {
                    if (!blacklistedItem.isOnlyEnchanted() || heldItem.isItemEnchanted()) {
                        if (!blacklistedItem.isOnlyOnIsland() || SkyblockAddons.getInstance().getUtils().getLocation() == EnumUtils.Location.ISLAND) {
                            if (blacklistedItem.getItem().equals(heldItem.getItem())) {
                                if (!blacklistedItem.canCtrlKeyBypass() || !GuiScreen.isCtrlKeyDown()) {
                                    cir.setReturnValue(EnumActionResult.FAIL);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This blocks placing items such as enchanted lava buckets and string.
     */
    @Inject(
            method = "processRightClickBlock",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItem = player.getHeldItemMainhand();
        // TODO: Check {pos} to see if it contains the clicked block I want to go through

        if (player.equals(mc.player) && !ItemStack.EMPTY.equals(heldItem)) {
            for (Blacklist.BlacklistedItem blacklistedItem : Blacklist.DO_NOT_PLACE) {
                if (blacklistedItem.isEnabled()) {
                    if (!blacklistedItem.isOnlyEnchanted() || heldItem.isItemEnchanted()) {
                        if (!blacklistedItem.isOnlyOnIsland() || SkyblockAddons.getInstance().getUtils().getLocation() == EnumUtils.Location.ISLAND) {
                            if (blacklistedItem.getItem().equals(heldItem.getItem())) {
                                if (!blacklistedItem.canCtrlKeyBypass() || !GuiScreen.isCtrlKeyDown()) {
                                    cir.setReturnValue(EnumActionResult.FAIL);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(
            method = "interactWithEntity",
            at = @At(
                    value = "HEAD"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void onInteractWithEntity(EntityPlayer player, Entity target, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isEnabled(Feature.DONT_OPEN_PROFILES_WITH_BOW)) {
            if (target instanceof EntityOtherPlayerMP && !main.getUtils().isNPC(target)) {
                ItemStack item = player.inventory.getCurrentItem();

                if (item != null && item.getItem() != null && item.getItem().equals(Items.BOW)) {
                    if (System.currentTimeMillis() - lastProfileMessage > 20000) {
                        lastProfileMessage = System.currentTimeMillis();
                        ChatFormatting formatting = main.getConfigValues().getColor(Feature.DONT_OPEN_PROFILES_WITH_BOW).getChatFormatting();
                        main.getUtils().sendMessage(formatting + Message.MESSAGE_STOPPED_OPENING_PROFILE.getMessage());
                    }

                    cir.setReturnValue(EnumActionResult.PASS);
                }
            }
        }
    }

    /**
     * Cancels clicking a locked inventory slot, even from other mods
     */
    @Inject(method = "windowClick", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void onWindowClick(int windowId, int slotId, int mouseButton, ClickType type, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (player != null && player.openContainer != null) {
            slotId += main.getInventoryUtils().getSlotDifference(player.openContainer);

            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getUtils().isOnSkyblock()
                    && main.getConfigValues().getLockedSlots().contains(slotId)
                    && (slotId >= 9 || player.openContainer instanceof ContainerPlayer && slotId >= 5)) {
                main.getUtils().playSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                cir.setReturnValue(null);
            }
        }
    }

}
