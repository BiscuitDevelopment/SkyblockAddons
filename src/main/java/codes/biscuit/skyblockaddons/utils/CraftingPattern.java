package codes.biscuit.skyblockaddons.utils;

import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;

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
    public static final int CRAFTING_RESULT_INDEX = 24;

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
     * Test if a pattern of filled slots fills this pattern, meaning every expected slot
     * is filled, but unexpected slots may be filled aswell.
     *
     * @param filledSlots Filled slots grid, true meaning filled, false not filled
     * @return Whether the pattern is filled
     */
    public boolean fillsPattern(boolean[] filledSlots) {
        for(int i = 0; i < pattern.length; i++) {
            if((pattern[i] == 1 && !filledSlots[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Test if a pattern of filled slots satisfies this crafting patten fully, meaning every
     * expected slot is filled and no unexpected slot is filled.
     *
     * @param filledSlots Filled slots grid, true meaning filled, false not filled
     * @return Whether the pattern is satisfied
     */
    public boolean satisfiesPattern(boolean[] filledSlots) {
        for(int i = 0; i < pattern.length; i++) {
            if((pattern[i] == 1 && !filledSlots[i]) || (pattern[i] == 0 && filledSlots[i])) {
                return false;
            }
        }
        return true;
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
