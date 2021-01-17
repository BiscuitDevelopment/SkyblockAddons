package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreview;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackManager;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPattern;
import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPatternResult;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

public class PlayerControllerMPHook {

    /**
     * clickModifier value in {@link #onWindowClick(int, int, int, EntityPlayer, ReturnValue)}  for shift-clicks
     */
    private static final int SHIFTCLICK_CLICK_TYPE = 1;

    /**
     * Cooldown between playing error sounds to avoid stacking up
     */
    private static final int CRAFTING_PATTERN_SOUND_COOLDOWN = 400;

    private static long lastCraftingSoundPlayed = 0;

    /**
     * Checks if an item is being dropped and if an item is being dropped, whether it is allowed to be dropped.
     * This check works only for mouse clicks, not presses of the "Drop Item" key.
     *
     * @param clickModifier the click modifier
     * @param slotNum the number of the slot that was clicked on
     * @param heldStack the item stack the player is holding with their mouse
     * @return {@code true} if the action should be cancelled, {@code false} otherwise
     */
    public static boolean checkItemDrop(int clickModifier, int slotNum, ItemStack heldStack) {
        // Is this a left or right click?
        if ((clickModifier == 0 || clickModifier == 1)) {
            // Is the player clicking outside their inventory?
            if (slotNum == -999) {
                // Is the player holding an item stack with their mouse?
                if (heldStack != null) {
                    return !SkyblockAddons.getInstance().getUtils().getItemDropChecker().canDropItem(heldStack);
                }
            }
        }

        // The player is not dropping an item. Don't cancel this action.
        return false;
    }

    public static void onPlayerDestroyBlock(BlockPos loc, ReturnValue<Boolean> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItem = Minecraft.getMinecraft().thePlayer.getHeldItem();
        if (heldItem != null) {
            Block block = mc.theWorld.getBlockState(loc).getBlock();
            if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS) && (block.equals(Blocks.log) || block.equals(Blocks.log2))) {
                if (heldItem.getDisplayName().contains(InventoryUtils.JUNGLE_AXE_DISPLAYNAME) || heldItem.getDisplayName().contains(InventoryUtils.TREECAPITATOR_DISPLAYNAME)) {
                    CooldownManager.put(heldItem);
                }
            }
        }
    }

    /**
     * Cancels clicking a locked inventory slot, even from other mods
     */
    public static void onWindowClick(int slotNum, int mouseButtonClicked, int mode, EntityPlayer player, ReturnValue<ItemStack> returnValue) { // return null
        // Handle blocking the next click, sorry I did it this way
        if (Utils.blockNextClick) {
            Utils.blockNextClick = false;
            returnValue.cancel();
            return;
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        int slotId = slotNum;
        ItemStack itemStack = player.inventory.getItemStack();

        if (main.getUtils().isOnSkyblock()) {
            // Prevent dropping rare items
            if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.getUtils().isInDungeon()) {
                if (checkItemDrop(mode, slotNum, itemStack)) {
                    returnValue.cancel();
                }
            }

            if (player.openContainer != null) {
                slotNum += main.getInventoryUtils().getSlotDifference(player.openContainer);

                final Container slots = player.openContainer;

                Slot slotIn;
                try {
                    slotIn = slots.getSlot(slotId);
                } catch (IndexOutOfBoundsException e) {
                    slotIn = null;
                }

                if (mouseButtonClicked == 1 && slotIn != null && slotIn.getHasStack() && slotIn.getStack().getItem() == Items.skull) {
                    ContainerPreview containerPreview = BackpackManager.getFromItem(slotIn.getStack());
                    if (containerPreview != null && containerPreview.getBackpackColor() != null) {
                        BackpackManager.setOpenedBackpackColor(containerPreview.getBackpackColor());
                    }
                }

                // Prevent clicking on locked slots.
                if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS)
                        && main.getConfigValues().getLockedSlots().contains(slotNum)
                        && (slotNum >= 9 || player.openContainer instanceof ContainerPlayer && slotNum >= 5)) {
                    if (mouseButtonClicked == 1 && mode == 0 && slotIn != null && slotIn.getHasStack() && slotIn.getStack().getItem() == Items.skull) {

                        String itemID = ItemUtils.getSkyBlockItemID(slotIn.getStack());
                        if (itemID == null) itemID = "";

                        if (BackpackManager.isBackpack(slotIn.getStack()) || itemID.contains("SACK")) {
                            return;
                        }
                    }

                    main.getUtils().playLoudSound("note.bass", 0.5);
                    returnValue.cancel();
                }

                // Crafting patterns
                if (slotIn != null && main.getInventoryUtils().getInventoryType() == InventoryType.CRAFTING_TABLE
                        && main.getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS)) {

                    final CraftingPattern selectedPattern = main.getPersistentValuesManager().getPersistentValues().getSelectedCraftingPattern();
                    final ItemStack clickedItem = slotIn.getStack();
                    if (selectedPattern != CraftingPattern.FREE && clickedItem != null) {
                        final ItemStack[] craftingGrid = new ItemStack[9];
                        for (int i = 0; i < CraftingPattern.CRAFTING_GRID_SLOTS.size(); i++) {
                            int slotIndex = CraftingPattern.CRAFTING_GRID_SLOTS.get(i);
                            craftingGrid[i] = slots.getSlot(slotIndex).getStack();
                        }

                        final CraftingPatternResult result = selectedPattern.checkAgainstGrid(craftingGrid);

                        if (slotIn.inventory.equals(Minecraft.getMinecraft().thePlayer.inventory)) {
                            if (result.isFilled() && !result.fitsItem(clickedItem) && mode == SHIFTCLICK_CLICK_TYPE) {
                                // cancel shift-clicking items from the inventory if the pattern is already filled
                                if (System.currentTimeMillis() > lastCraftingSoundPlayed + CRAFTING_PATTERN_SOUND_COOLDOWN) {
                                    main.getUtils().playSound("note.bass", 0.5);
                                    lastCraftingSoundPlayed = System.currentTimeMillis();
                                }
                                returnValue.cancel();
                            }
                        } else {
                            if (slotIn.getSlotIndex() == CraftingPattern.CRAFTING_RESULT_INDEX
                                    && !result.isSatisfied()
                                    && main.getPersistentValuesManager().getPersistentValues().isBlockCraftingIncompletePatterns()) {
                                // cancel clicking the result if the pattern isn't satisfied
                                if (System.currentTimeMillis() > lastCraftingSoundPlayed + CRAFTING_PATTERN_SOUND_COOLDOWN) {
                                    main.getUtils().playSound("note.bass", 0.5);
                                    lastCraftingSoundPlayed = System.currentTimeMillis();
                                }
                                returnValue.cancel();
                            }
                        }
                    }
                }
            }
        }
        else {
            if (checkItemDrop(mode, slotNum, itemStack)) {
                returnValue.cancel();
            }
        }
    }
}
