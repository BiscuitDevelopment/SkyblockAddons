package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.gui.elements.CraftingPatternSelection;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class PlayerControllerMPHook {

    /**
     * clickModifier value in {@link #onWindowClick(int, int, EntityPlayer, ReturnValue)}  for shift-clicks
     */
    private static final int SHIFTCLICK_CLICK_TYPE = 1;

    /**
     * Cooldown between playing error sounds to avoid stacking up
     */
    private static final int CRAFTING_PATTERN_SOUND_COOLDOWN = 400;

    private static final Set<Location> DEEP_CAVERNS_LOCATIONS = EnumSet.of(Location.DEEP_CAVERNS, Location.GUNPOWDER_MINES,
            Location.LAPIS_QUARRY, Location.PIGMAN_DEN, Location.SLIMEHILL, Location.DIAMOND_RESERVE, Location.OBSIDIAN_SANCTUARY);

    private static final Set<Block> DEEP_CAVERNS_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.coal_ore, Blocks.iron_ore, Blocks.gold_ore, Blocks.redstone_ore, Blocks.emerald_ore,
            Blocks.diamond_ore, Blocks.diamond_block, Blocks.obsidian, Blocks.lapis_ore, Blocks.lit_redstone_ore));

    private static final Set<Block> NETHER_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.glowstone, Blocks.quartz_ore, Blocks.nether_wart));

    private static final Set<Location> PARK = EnumSet.of(
            Location.BIRCH_PARK,
            Location.SPRUCE_WOODS,
            Location.SAVANNA_WOODLAND,
            Location.DARK_THICKET,
            Location.JUNGLE_ISLAND
    );

    private static final Set<Block> LOGS = new HashSet<>(
            Arrays.asList(
                    Blocks.log,
                    Blocks.log2
            )
    );

    private static long lastCraftingSoundPlayed = 0;
    private static long lastStemMessage = -1;
    private static long lastUnmineableMessage = -1;

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

    /**
     * Cancels stem breaks if holding an item, to avoid accidental breaking.
     */
    public static void onPlayerDamageBlock(BlockPos loc, ReturnValue<Boolean> returnValue) {
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
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.AVOID_BREAKING_STEMS) + Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                }
                returnValue.cancel();
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_MINE_ORES_DEEP_CAVERNS) && DEEP_CAVERNS_LOCATIONS.contains(main.getUtils().getLocation())
                    && main.getUtils().isPickaxe(heldItem.getItem()) && !DEEP_CAVERNS_MINEABLE_BLOCKS.contains(block)) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS) && now - lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.ONLY_MINE_ORES_DEEP_CAVERNS) + Message.MESSAGE_CANCELLED_NON_ORES_BREAK.getMessage());
                }
                returnValue.cancel();
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_MINE_VALUABLES_NETHER) && Location.BLAZING_FORTRESS.equals(main.getUtils().getLocation()) &&
                    main.getUtils().isPickaxe(heldItem.getItem()) && !NETHER_MINEABLE_BLOCKS.contains(block)) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_MINING_NETHER) && now - lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.ONLY_MINE_VALUABLES_NETHER) + Message.MESSAGE_CANCELLED_NON_ORES_BREAK.getMessage());
                }
                returnValue.cancel();
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_BREAK_LOGS_PARK) && PARK.contains(main.getUtils().getLocation())
                    && main.getUtils().isAxe(heldItem.getItem()) && !LOGS.contains(block)) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_BREAKING_PARK) && now - lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.ONLY_BREAK_LOGS_PARK) + Message.MESSAGE_CANCELLED_NON_LOGS_BREAK.getMessage());
                }
                lastUnmineableMessage = now;
                returnValue.cancel();
            } else if (main.getConfigValues().isEnabled(Feature.JUNGLE_AXE_COOLDOWN)) {
                if ((block.equals(Blocks.log) || block.equals(Blocks.log2))
                        && p.getHeldItem() != null) {

                    final boolean holdingJungleAxeOnCooldown = InventoryUtils.JUNGLE_AXE_DISPLAYNAME.equals(p.getHeldItem().getDisplayName()) && CooldownManager.isOnCooldown(InventoryUtils.JUNGLE_AXE_DISPLAYNAME);
                    final boolean holdingTreecapitatorOnCooldown = InventoryUtils.TREECAPITATOR_DISPLAYNAME.equals(p.getHeldItem().getDisplayName()) && CooldownManager.isOnCooldown(InventoryUtils.TREECAPITATOR_DISPLAYNAME);

                    if (holdingJungleAxeOnCooldown || holdingTreecapitatorOnCooldown) {
                        returnValue.cancel();
                    }
                }
            }
        }
    }

    public static void onPlayerDestroyBlock(BlockPos loc, ReturnValue<Boolean> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItem = Minecraft.getMinecraft().thePlayer.getHeldItem();
        if (heldItem != null) {
            Block block = mc.theWorld.getBlockState(loc).getBlock();
            if (main.getUtils().isOnSkyblock()
                    && main.getConfigValues().isEnabled(Feature.SHOW_ITEM_COOLDOWNS)
                    && (block.equals(Blocks.log) || block.equals(Blocks.log2))) {
                if(InventoryUtils.JUNGLE_AXE_DISPLAYNAME.equals(heldItem.getDisplayName())
                        || InventoryUtils.TREECAPITATOR_DISPLAYNAME.equals(heldItem.getDisplayName())) {
                    CooldownManager.put(heldItem);
                }
            }
        }
    }

    /**
     * Cancels clicking a locked inventory slot, even from other mods
     */
    public static void onWindowClick(int slotNum, int clickModifier, EntityPlayer player, ReturnValue<ItemStack> returnValue) { // return null
        // Handle blocking the next click, sorry I did it this way
        if (Utils.blockNextClick) {
            Utils.blockNextClick = false;
            returnValue.cancel();
            return;
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        final int slotId = slotNum;
        ItemStack itemStack = player.inventory.getItemStack();

        if (main.getUtils().isOnSkyblock()) {
            // Prevent dropping rare items
            if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS)) {
                if (checkItemDrop(clickModifier, slotNum, itemStack)) {
                    returnValue.cancel();
                }
            }

            if (player.openContainer != null) {
                slotNum += main.getInventoryUtils().getSlotDifference(player.openContainer);

                // Prevent clicking on locked slots.
                if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS)
                        && main.getConfigValues().getLockedSlots().contains(slotNum)
                        && (slotNum >= 9 || player.openContainer instanceof ContainerPlayer && slotNum >= 5)) {
                    main.getUtils().playLoudSound("note.bass", 0.5);
                    returnValue.cancel();
                }

                // Crafting patterns
                final Container slots = player.openContainer;

                Slot slotIn;
                try {
                    slotIn = slots.getSlot(slotId);
                } catch (IndexOutOfBoundsException e) {
                    slotIn = null;
                }

                if (slotIn != null && EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.CRAFTING_TABLE
                        && main.getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS)) {

                    final CraftingPattern selectedPattern = CraftingPatternSelection.selectedPattern;
                    final ItemStack clickedItem = slotIn.getStack();
                    if (selectedPattern != CraftingPattern.FREE && clickedItem != null) {
                        final ItemStack[] craftingGrid = new ItemStack[9];
                        for (int i = 0; i < CraftingPattern.CRAFTING_GRID_SLOTS.size(); i++) {
                            int slotIndex = CraftingPattern.CRAFTING_GRID_SLOTS.get(i);
                            craftingGrid[i] = slots.getSlot(slotIndex).getStack();
                        }

                        final CraftingPatternResult result = selectedPattern.checkAgainstGrid(craftingGrid);

                        if (slotIn.inventory.equals(Minecraft.getMinecraft().thePlayer.inventory)) {
                            if (result.isFilled() && !result.fitsItem(clickedItem) && clickModifier == SHIFTCLICK_CLICK_TYPE) {
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
                                    && CraftingPatternSelection.blockCraftingIncomplete) {
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
            if (checkItemDrop(clickModifier, slotNum, itemStack)) {
                returnValue.cancel();
            }
        }
    }
}
