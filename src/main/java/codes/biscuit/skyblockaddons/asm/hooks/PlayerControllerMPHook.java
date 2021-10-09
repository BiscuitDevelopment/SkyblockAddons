package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.events.SkyblockBlockBreakEvent;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackColor;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackInventoryManager;
import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPattern;
import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPatternResult;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;

import java.util.Set;

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

    private static final Set<Integer> ORES = Sets.newHashSet(Block.getIdFromBlock(Blocks.coal_ore), Block.getIdFromBlock(Blocks.iron_ore),
            Block.getIdFromBlock(Blocks.gold_ore), Block.getIdFromBlock(Blocks.redstone_ore), Block.getIdFromBlock(Blocks.emerald_ore),
            Block.getIdFromBlock(Blocks.lapis_ore), Block.getIdFromBlock(Blocks.diamond_ore), Block.getIdFromBlock(Blocks.lit_redstone_ore),
            Utils.getBlockMetaId(Blocks.stone, BlockStone.EnumType.DIORITE_SMOOTH.getMetadata()),
            Utils.getBlockMetaId(Blocks.stained_hardened_clay, EnumDyeColor.CYAN.getMetadata()),
            Utils.getBlockMetaId(Blocks.prismarine, BlockPrismarine.EnumType.ROUGH.getMetadata()),
            Utils.getBlockMetaId(Blocks.prismarine, BlockPrismarine.EnumType.DARK.getMetadata()),
            Utils.getBlockMetaId(Blocks.prismarine, BlockPrismarine.EnumType.BRICKS.getMetadata()),
            Utils.getBlockMetaId(Blocks.wool, EnumDyeColor.LIGHT_BLUE.getMetadata()),
            Utils.getBlockMetaId(Blocks.wool, EnumDyeColor.GRAY.getMetadata()));

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

    public static void onPlayerDestroyBlock(BlockPos blockPos) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();

        if (main.getUtils().isOnSkyblock()) {
            IBlockState block = mc.theWorld.getBlockState(blockPos);
            // Use vanilla break mechanic to get breaking time
            double perTickIncrease = block.getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.thePlayer.worldObj, blockPos);
            int MILLISECONDS_PER_TICK = 1000 / 20;
            MinecraftForge.EVENT_BUS.post(new SkyblockBlockBreakEvent(blockPos, (long) (MILLISECONDS_PER_TICK / perTickIncrease)));
        }
    }

    public static void onResetBlockRemoving() {
        MinecraftHook.prevClickBlock = new BlockPos(-1, -1, -1);
    }

    /**
     * Cancels clicking a locked inventory slot, even from other mods
     */
    public static void onWindowClick(int slotNum, int mouseButtonClicked, int mode, EntityPlayer player, ReturnValue<ItemStack> returnValue) { // return null
        //if (Minecraft.getMinecraft().thePlayer.openContainer != null) {
        //    SkyblockAddons.getLogger().info("Handling windowclick--slotnum: " + slotNum + " should be locked: " + SkyblockAddons.getInstance().getConfigValues().getLockedSlots().contains(slotNum) + " mousebutton: " + mouseButtonClicked + " mode: " + mode + " container class: " + player.openContainer.getClass().toString());
        //}

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
                    BackpackColor color = ItemUtils.getBackpackColor(slotIn.getStack());
                    if (color != null) {
                        BackpackInventoryManager.setBackpackColor(color);
                    }
                }

                // Prevent clicking on locked slots.
                if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS)
                        && main.getConfigValues().getLockedSlots().contains(slotNum)
                        && (slotNum >= 9 || player.openContainer instanceof ContainerPlayer && slotNum >= 5)) {
                    if (mouseButtonClicked == 1 && mode == 0 && slotIn != null && slotIn.getHasStack() && slotIn.getStack().getItem() == Items.skull) {

                        String itemID = ItemUtils.getSkyblockItemID(slotIn.getStack());
                        if (itemID == null) itemID = "";

                        // Now that right clicking backpacks is removed, remove this check and block right clicking on backpacks if locked
                        if (/*ItemUtils.isBuildersWand(slotIn.getStack()) || ItemUtils.isBackpack(slotIn.getStack()) || */itemID.contains("SACK")) {
                            return;
                        }
                    }

                    main.getUtils().playLoudSound("note.bass", 0.5);
                    returnValue.cancel();
                }

                // Crafting patterns
                if (false && slotIn != null && main.getInventoryUtils().getInventoryType() == InventoryType.CRAFTING_TABLE
                    /*&& main.getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS)*/) {

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
