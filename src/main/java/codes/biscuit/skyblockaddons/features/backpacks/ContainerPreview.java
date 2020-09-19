package codes.biscuit.skyblockaddons.features.backpacks;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;

@Getter
public class ContainerPreview {

    @Setter private int x;
    @Setter private int y;
    private ItemStack[] items;
    private String name;

    private BackpackColor backpackColor;

    public ContainerPreview(ItemStack[] items, String name, BackpackColor backpackColor) {
        this.items = items;
        this.name = name;
        this.backpackColor = backpackColor;
    }

    public ContainerPreview(ItemStack[] items, String backpackName, BackpackColor backpackColor, int x, int y) {
        this(items,backpackName,backpackColor);
        this.x = x;
        this.y = y;
    }
}
