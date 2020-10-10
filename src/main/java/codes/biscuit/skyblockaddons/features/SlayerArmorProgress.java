package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.utils.ColorCode;
import lombok.Getter;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

@Getter
public class SlayerArmorProgress {

    /** The itemstack that this progress is representing. */
    private final ItemStack itemStack;

    /** The current slayer progress % of the item. */
    @Getter private String percent;

    /** The current slayer defence reward of the item. */
    @Getter private String defence;

    public SlayerArmorProgress(ItemStack itemStack) {
        this.itemStack = new ItemStack(itemStack.getItem()); // Cloned because we change the helmet color later.
        this.percent = "55";
        this.defence = "§a40❈";

        setHelmetColor();
    }

    public SlayerArmorProgress(ItemStack itemStack, String percent, String defence) {
        this.itemStack = itemStack;
        this.percent = percent;
        this.defence = defence;
    }

    private void setHelmetColor() {
        if (itemStack.getItem().equals(Items.leather_helmet)) {
            ((ItemArmor)itemStack.getItem()).setColor(itemStack, ColorCode.BLACK.getColor());
        }
    }

    public void setDefence(String defence) {
        this.defence = defence;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }
}
