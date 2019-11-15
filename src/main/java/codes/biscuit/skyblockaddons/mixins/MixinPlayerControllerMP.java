package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.elements.CraftingPatternSelection;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
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

    private static final Set<Block> DEEP_CAVERNS_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.coal_ore, Blocks.iron_ore, Blocks.gold_ore, Blocks.redstone_ore, Blocks.emerald_ore,
            Blocks.diamond_ore, Blocks.diamond_block, Blocks.obsidian, Blocks.lapis_ore, Blocks.lit_redstone_ore));

    private static final Set<Block> NETHER_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.glowstone, Blocks.quartz_ore, Blocks.nether_wart));

    private long lastStemMessage = -1;
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
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_BREAKING_STEMS) && now - lastStemMessage > 20000) {
                    lastStemMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.AVOID_BREAKING_STEMS).getChatFormatting()+Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                }
                cir.setReturnValue(false);
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_MINE_ORES_DEEP_CAVERNS) && DEEP_CAVERNS_LOCATIONS.contains(main.getUtils().getLocation())
                    && main.getUtils().isPickaxe(heldItem.getItem()) && !DEEP_CAVERNS_MINEABLE_BLOCKS.contains(block)) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS) && now - lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.ONLY_MINE_ORES_DEEP_CAVERNS).getChatFormatting() + Message.MESSAGE_CANCELLED_NON_ORES_BREAK.getMessage());
                }
                cir.setReturnValue(false);
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_MINE_VALUABLES_NETHER) && EnumUtils.Location.BLAZING_FORTRESS.equals(main.getUtils().getLocation()) &&
                    main.getUtils().isPickaxe(heldItem.getItem()) && !NETHER_MINEABLE_BLOCKS.contains(block)) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_MINING_NETHER) && now - lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.ONLY_MINE_VALUABLES_NETHER).getChatFormatting() + Message.MESSAGE_CANCELLED_NON_ORES_BREAK.getMessage());
                }
                cir.setReturnValue(false);
            } else if (main.getConfigValues().isEnabled(Feature.JUNGLE_AXE_COOLDOWN)) {
                if ((block.equals(Blocks.log)|| block.equals(Blocks.log2))
                        && CooldownManager.isOnCooldown(InventoryUtils.JUNGLE_AXE_DISPLAYNAME)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "onPlayerDestroyBlock", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onPlayerDestroyBlock(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)) {
            CooldownManager.put(Minecraft.getMinecraft().thePlayer.getHeldItem());
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
