package codes.biscuit.skyblockaddons.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Feature#CRAFTING_PATTERNS Crafting patterns} enum, constants and utility methods
 *
 * @author DidiSkywalker
 */
public enum CraftingPattern {

    FREE(0, null),
    THREE(1, new int[]{
        1, 1, 1,
        0, 0, 0,
        0, 0, 0
    }),
    FIVE(2, new int[]{
        1, 1, 1,
        1, 1, 0,
        0, 0, 0
    }),
    SIX(3, new int[]{
        1, 1, 1,
        1, 1, 1,
        0, 0, 0
    });

    public static final ResourceLocation ICONS = new ResourceLocation("skyblockaddons", "craftingpatterns.png");

    /**
     * Displayname of the SkyBlock crafting table
     */
    public static final String CRAFTING_TABLE_DISPLAYNAME = "Craft Item";

    /**
     * Slot index of the crafting result
     */
    public static final int CRAFTING_RESULT_INDEX = 23;

    public static final List<Integer> CRAFTING_GRID_SLOTS = Arrays.asList(
      10, 11, 12,
      19, 20, 21,
      28, 29, 30
    );

    public final int index;
    public final int[] pattern;

    CraftingPattern(final int index, final int[] pattern) {
        this.index = index;
        this.pattern = pattern;
    }

    /**
     * Check if a translated slot is within the pattern
     *
     * @param slot Slot index translated with {@link #slotToCraftingGridIndex(int)}
     * @return Whether that slot is within the pattern
     */
    public boolean isSlotInPattern(int slot) {
        return slot >= 0 && slot <= 8 && pattern[slot] == 1;
    }

    /**
     * Checks the items of a crafting grid against the pattern for these characteristics:
     * - filled: Every expected slot is filled, but other slots may be filled too
     * - satisfied: Only expected slots are filled
     * - free space: Amount of items that still fit into the stacks inside the patterns
     *
     * @param grid ItemStack array of length 9 containing the items of the crafting grid
     * @return {@link CraftingPatternResult} containing all above mentioned characteristics
     */
    public CraftingPatternResult checkAgainstGrid(ItemStack[] grid) {
        if(grid == null || grid.length < 9) {
            throw new IllegalArgumentException("grid cannot be null or smaller than 9.");
        }

        boolean filled = true;
        boolean satisfied = true;
        int emptySpace = 0;
        Map<String, ItemDiff> freeSpaceMap = new HashMap<>();

        for(int i = 0; i < pattern.length; i++) {
            ItemStack itemStack = grid[i];
            boolean hasStack = itemStack != null;

            if(isSlotInPattern(i) && !hasStack) {
                filled = false;
                satisfied = false;
            } else if(!isSlotInPattern(i) && hasStack) {
                satisfied = false;
            }

            if(isSlotInPattern(i)) {
                if(hasStack) {
                    if(!freeSpaceMap.containsKey(itemStack.getDisplayName())) {
                        freeSpaceMap.put(itemStack.getDisplayName(), new ItemDiff(itemStack.getDisplayName(), 0));
                    }

                    ItemDiff diff = freeSpaceMap.get(itemStack.getDisplayName());
                    diff.add(itemStack.getMaxStackSize() - itemStack.stackSize);
                } else {
                    // empty slot inside the pattern: add 64 free space
                    emptySpace += 64;
                }
            }
        }

        return new CraftingPatternResult(filled, satisfied, emptySpace, freeSpaceMap);
    }

    /**
     * Translates a slot index to the corresponding index in the crafting grid between 0 and 8 or
     * return -1 if the slot is not within the crafting grid.
     *
     * @param slotIndex Slot index to translate
     * @return index 0-8 or -1 if not in the crafting grid
     */
    public static int slotToCraftingGridIndex(int slotIndex) {
        return CRAFTING_GRID_SLOTS.indexOf(slotIndex);
    }

    /**
     * Translate a crafting grid index to the corresponding slot index in the full inventory
     *
     * @param index Crafting grid index 0-8
     * @return Slot index
     */
    public static int craftingGridIndexToSlot(int index) {
        if(index < 0 || index > 8) {
            throw new IndexOutOfBoundsException("Crafting Grid index must be between 0 and 8");
        }

        return CRAFTING_GRID_SLOTS.get(index);
    }

}
