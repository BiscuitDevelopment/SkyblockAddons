package codes.biscuit.skyblockaddons.features.craftingpatterns;

import codes.biscuit.skyblockaddons.features.ItemDiff;
import net.minecraft.item.ItemStack;

import java.util.Map;

/**
 * Class containing results of pattern checks through {@link CraftingPattern#checkAgainstGrid(ItemStack[])}
 */
public class CraftingPatternResult {

    private final boolean filled;
    private final boolean satisfied;
    private final int emptySpace;
    private final Map<String, ItemDiff> freeSpaceMap;

    CraftingPatternResult(boolean filled, boolean satisfied, int emptySpace, Map<String, ItemDiff> freeSpaceMap) {
        this.filled = filled;
        this.satisfied = satisfied;
        this.emptySpace = emptySpace;
        this.freeSpaceMap = freeSpaceMap;
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
     * Checks whether a given ItemStack can still fit inside without violating the pattern.
     *
     * @param itemStack ItemStack to check
     * @return Whether that ItemStack can safely fit the pattern
     */
    public boolean fitsItem(ItemStack itemStack) {
        ItemDiff itemDiff = freeSpaceMap.getOrDefault(itemStack.getDisplayName(), null);
        if(itemDiff != null) {
            return itemStack.stackSize <= itemDiff.getAmount();
        } else {
            return itemStack.stackSize <= emptySpace;
        }
    }
}
