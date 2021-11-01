package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.core.Message;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

import java.util.*;

public class MinecraftHook {

    @Getter private static long lastLockedSlotItemChange = -1;

    public static BlockPos prevClickBlock = new BlockPos(-1, -1, -1);
    public static long startMineTime = Long.MAX_VALUE;

    public static LinkedHashMap<BlockPos, Long> recentlyClickedBlocks = new LinkedHashMap<>();

    public static void rightClickMouse(ReturnValue<?> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity entityIn = mc.objectMouseOver.entityHit;

                if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && entityIn instanceof EntityItemFrame && ((EntityItemFrame)entityIn).getDisplayedItem() == null) {
                    int slot = mc.thePlayer.inventory.currentItem + 36;
                    if (main.getConfigValues().getLockedSlots().contains(slot) && slot >= 9) {
                        main.getUtils().playLoudSound("note.bass", 0.5);
                        main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_SLOT_LOCKED.getMessage());
                        returnValue.cancel();
                    }
                }
            }
        }
    }

    public static void updatedCurrentItem() {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            int slot = mc.thePlayer.inventory.currentItem + 36;
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getConfigValues().getLockedSlots().contains(slot)
                    && (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)) {

                MinecraftHook.lastLockedSlotItemChange = System.currentTimeMillis();
            }

            ItemStack heldItemStack = mc.thePlayer.getHeldItem();
            if (heldItemStack != null && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.getUtils().isInDungeon()
                    && !main.getUtils().getItemDropChecker().canDropItem(heldItemStack, true, false)) {

                MinecraftHook.lastLockedSlotItemChange = System.currentTimeMillis();
            }
        }
    }

    public static void onClickMouse(ReturnValue<?> returnValue) {

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }
        BlockPos blockPos = mc.objectMouseOver.getBlockPos();
        if (mc.theWorld.getBlockState(blockPos).getBlock().getMaterial() == Material.air) {
            return;
        }


        if (!returnValue.isCancelled() && !prevClickBlock.equals(blockPos)) {
            startMineTime = System.currentTimeMillis();
        }
        prevClickBlock = blockPos;
        if (!returnValue.isCancelled()) {
            recentlyClickedBlocks.put(blockPos, System.currentTimeMillis());
        }
    }

    public static void onSendClickBlockToController(boolean leftClick, ReturnValue<?> returnValue) {
        // If we aren't trying to break anything, don't change vanilla behavior (was causing false positive chat messages)
        if (!leftClick) {
            return;
        }
        onClickMouse(returnValue);
        // Canceling this is tricky. Not only do we have to reset block removing, but also reset the position we are breaking
        // This is because we want playerController.onClick to be called when they go back to that block
        // It's also important to resetBlockRemoving before changing current block, since then we'd be sending the server inaccurate info that could trigger wdr
        // This mirrors PlayerControllerMP.clickBlock(), which sends an ABORT_DESTROY message, before calling onPlayerDestroyBlock, which changes "currentBlock"
        if (returnValue.isCancelled()) {
            Minecraft.getMinecraft().playerController.resetBlockRemoving();
            Minecraft.getMinecraft().playerController.currentBlock = new BlockPos(-1, -1, -1);
        }
    }

}
