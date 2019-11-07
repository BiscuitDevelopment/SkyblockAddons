package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.elements.CraftingPatternSelection;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    /**
     * clickModifier value in {@link #onWindowClick(int, int, int, int, EntityPlayer, CallbackInfoReturnable)}  for shift-clicks
     */
    private static final int SHIFTCLICK_CLICK_TYPE = 1;

    /**
     * Cooldown between playing error sounds to avoid stacking up
     */
    private static int CRAFTING_PATTERN_SOUND_COOLDOWN = 400;
    private long lastCraftingSoundPlayed = 0;

    private static final Set<EnumUtils.Location> DEEP_CAVERNS_LOCATIONS = EnumSet.of(EnumUtils.Location.DEEP_CAVERNS, EnumUtils.Location.GUNPOWDER_MINES,
            EnumUtils.Location.LAPIS_QUARRY, EnumUtils.Location.PIGMAN_DEN, EnumUtils.Location.SLIMEHILL, EnumUtils.Location.DIAMOND_RESERVE, EnumUtils.Location.OBSIDIAN_SANCTUARY);

    private static final Set<Block> MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.coal_ore, Blocks.iron_ore, Blocks.gold_ore, Blocks.redstone_ore, Blocks.emerald_ore,
            Blocks.diamond_ore, Blocks.diamond_block, Blocks.obsidian, Blocks.lapis_ore, Blocks.lit_redstone_ore));

    private long lastStemMessage = -1;
    private long lastProfileMessage = -1;
    private long lastUnmineableMessage = -1;

    /**
     * Cancels stem breaks if holding an item, to avoid accidental breaking.
     */
    @Inject(method = "clickBlock", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onPlayerDamageBlock(BlockPos loc, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = mc.thePlayer;
        ItemStack heldItem = p.getHeldItem();
        if (heldItem != null) {
            Block block = mc.theWorld.getBlockState(loc).getBlock();
            long now = System.currentTimeMillis();
            if (main.getConfigValues().isEnabled(Feature.AVOID_BREAKING_STEMS) && (block.equals(Blocks.melon_stem) || block.equals(Blocks.pumpkin_stem))) {
                if (now- lastStemMessage > 20000) {
                    lastStemMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.AVOID_BREAKING_STEMS).getChatFormatting()+Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                }
                cir.setReturnValue(false);
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_MINE_ORES_DEEP_CAVERNS) && DEEP_CAVERNS_LOCATIONS.contains(main.getUtils().getLocation())
                    && main.getUtils().isPickaxe(heldItem.getItem()) && !MINEABLE_BLOCKS.contains(block)) {
                if (now-lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.ONLY_MINE_ORES_DEEP_CAVERNS).getChatFormatting() + Message.MESSAGE_CANCELLED_NON_ORES_BREAK.getMessage());
                }
                cir.setReturnValue(false);
            } else if (main.getConfigValues().isEnabled(Feature.JUNGLE_AXE_COOLDOWN)) {
                CooldownEntry cooldown = main.getUtils().getItemCooldown("§aJungle Axe");
                if (cooldown != null && (block.equals(Blocks.log) || block.equals(Blocks.log2)) && cooldown.getLastUse() + cooldown.getCooldownMillis() > now) {
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

    private boolean isItemBow(ItemStack item) {
        return item != null && item.getItem() != null &&
                item.getItem().equals(Items.bow);
    }

    @Inject(method = "isPlayerRightClickingOnEntity", cancellable = true,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;syncCurrentPlayItem()V"))
    private void shouldPlayerRightClickEntity(EntityPlayer player, Entity entity, MovingObjectPosition ignored, CallbackInfoReturnable<Boolean> cb) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Utils utils = main.getUtils();

        if(!utils.isOnSkyblock())
            return;

        ConfigValues config = main.getConfigValues();
        boolean cancel = false;
        if(config.isEnabled(Feature.DONT_OPEN_PROFILES_WITH_BOW)) {
            if(entity instanceof EntityOtherPlayerMP && utils.isNotNPC(entity)) {
                ItemStack item = player.inventory.getCurrentItem();
                ItemStack itemInUse = player.getItemInUse();

                if(isItemBow(item) || isItemBow(itemInUse)) {
                    long currentTime = System.currentTimeMillis();
                    if((currentTime - lastProfileMessage) > 20000) {
                        lastProfileMessage = currentTime;

                        utils.sendMessage(config.getColor(Feature.DONT_OPEN_PROFILES_WITH_BOW).getChatFormatting() +
                                Message.MESSAGE_STOPPED_OPENING_PROFILE.getMessage());
                    }

                    cancel = true;
                }
            }
        }

        if(config.isEnabled(Feature.LOCK_SLOTS)) {
            if(entity instanceof EntityItemFrame && ((EntityItemFrame)entity).getDisplayedItem() == null) {
                int slot = player.inventory.currentItem + 36;
                if(config.getLockedSlots().contains(slot) && (slot >= 9 || player.openContainer instanceof ContainerPlayer && slot >= 5)) {
                    utils.playSound("note.bass", 0.5);
                    utils.sendMessage(config.getColor(Feature.DROP_CONFIRMATION).getChatFormatting() + Message.MESSAGE_SLOT_LOCKED.getMessage());

                    cancel = true;
                }
            }
        }

        if(cancel) {
            cb.setReturnValue(true);
            cb.cancel();
        }
    }

    /**
     * Cancels clicking a locked inventory slot, even from other mods
     */
    @Inject(method = "windowClick", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onWindowClick(int windowId, int slotNum, int clickButton, int clickModifier, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        // Handle blocking the next click, sorry I did it this way
        if (Utils.blockNextClick) {
            Utils.blockNextClick = false;
            cir.setReturnValue(null);
            cir.cancel();
            return;
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        final int slotId = slotNum;
        if (player != null && player.openContainer != null) {
            slotNum += main.getInventoryUtils().getSlotDifference(player.openContainer);
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getUtils().isOnSkyblock()
                    && main.getConfigValues().getLockedSlots().contains(slotNum)
                    && (slotNum >= 9 || player.openContainer instanceof ContainerPlayer && slotNum >= 5)){
                main.getUtils().playLoudSound("note.bass", 0.5);
                cir.setReturnValue(null);
                cir.cancel();
            }

            // Crafting patterns
            final Container slots = player.openContainer;

            Slot slotIn;
            try {
                slotIn = slots.getSlot(slotId);
            } catch (ArrayIndexOutOfBoundsException e) {
                slotIn = null;
            }

            if(slotIn != null && EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.CRAFTING_TABLE
                    && main.getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS)) {

                final CraftingPattern selectedPattern = CraftingPatternSelection.selectedPattern;
                final ItemStack clickedItem = slotIn.getStack();
                if(selectedPattern != CraftingPattern.FREE && clickedItem != null) {
                    final ItemStack[] craftingGrid = new ItemStack[9];
                    for (int i = 0; i < CraftingPattern.CRAFTING_GRID_SLOTS.size(); i++) {
                        int slotIndex = CraftingPattern.CRAFTING_GRID_SLOTS.get(i);
                        craftingGrid[i] = slots.getSlot(slotIndex).getStack();
                    }

                    final CraftingPatternResult result = selectedPattern.checkAgainstGrid(craftingGrid);

                    if(slotIn.inventory.equals(Minecraft.getMinecraft().thePlayer.inventory)) {
                        if(result.isFilled() && !result.fitsItem(clickedItem) && clickModifier == SHIFTCLICK_CLICK_TYPE) {
                            // cancel shift-clicking items from the inventory if the pattern is already filled
                            if(System.currentTimeMillis() > lastCraftingSoundPlayed+CRAFTING_PATTERN_SOUND_COOLDOWN) {
                                main.getUtils().playSound("note.bass", 0.5);
                                lastCraftingSoundPlayed = System.currentTimeMillis();
                            }
                            cir.setReturnValue(null);
                            cir.cancel();
                        }
                    } else {
                        if(slotIn.getSlotIndex() == CraftingPattern.CRAFTING_RESULT_INDEX
                                && !result.isSatisfied()
                                && CraftingPatternSelection.blockCraftingIncomplete) {
                            // cancel clicking the result if the pattern isn't satisfied
                            if(System.currentTimeMillis() > lastCraftingSoundPlayed+CRAFTING_PATTERN_SOUND_COOLDOWN) {
                                main.getUtils().playSound("note.bass", 0.5);
                                lastCraftingSoundPlayed = System.currentTimeMillis();
                            }
                            cir.setReturnValue(null);
                            cir.cancel();
                        }
                    }
                }
            }
        }
    }
}
