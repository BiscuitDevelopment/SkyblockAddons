package codes.biscuit.skyblockaddons.features.backpacks;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;

@Getter
public class ContainerPreview {

    @Setter private int x;
    @Setter private int y;
    private int numRows;
    private int numCols;
    private ItemStack[] items;
    private String name;

    private BackpackColor backpackColor;

    public ContainerPreview(ItemStack[] items, String name, BackpackColor backpackColor, int rows, int cols) {
        this.items = items;
        this.name = name;
        this.backpackColor = backpackColor;
        this.numRows = Math.min(rows, 6);
        this.numCols = Math.min(cols, 9);
    }

    public ContainerPreview(ItemStack[] items, String backpackName, BackpackColor backpackColor, int rows, int cols, int x, int y) {
        this(items,backpackName,backpackColor, rows, cols);
        this.x = x;
        this.y = y;
    }
}
