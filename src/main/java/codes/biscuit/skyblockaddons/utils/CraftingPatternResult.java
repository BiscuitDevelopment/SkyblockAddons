package codes.biscuit.skyblockaddons.utils;

import net.minecraft.item.ItemStack;

/**
 * Class containing results of pattern checks through {@link CraftingPattern#checkAgainstGrid(ItemStack[])}
 */
public class CraftingPatternResult {

    private final boolean filled;
    private final boolean satisfied;
    private final int freeSpace;

    CraftingPatternResult(boolean filled, boolean satisfied, int freeSpace) {
        this.filled = filled;
        this.satisfied = satisfied;
        this.freeSpace = freeSpace;
    }

    /**
     * A pattern is considered filled if at least every expected slot is filled. Other slots may be filled too.
     *
     * @return Whether the checked grid filled the pattern
     */
    public boolean isFilled() {
        return filled;
    }

    /**
     * A pattern is considered satisfied if every expected slot and no other slots are filled.
     *
     * @return Whether the checked grid filled the pattern
     */
    public boolean isSatisfied() {
        return satisfied;
    }

    /**
     * @return Available space for items left in the item stacks inside the pattern
     */
    public int getFreeSpace() {
        return freeSpace;
    }
}
