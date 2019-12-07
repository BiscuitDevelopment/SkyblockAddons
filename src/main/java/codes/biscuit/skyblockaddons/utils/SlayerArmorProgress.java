package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class SlayerArmorProgress {

    private final ItemStack itemStack;
    private String progressText = null;

    public SlayerArmorProgress(ItemStack itemStack) {
        this.itemStack = new ItemStack(itemStack.getItem());
        setHelmetColor();
    }

    SlayerArmorProgress(ItemStack itemStack, String progress) {
        this.itemStack = itemStack;
        this.progressText = progress;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getProgressText() {
        if (progressText == null) {
            ConfigColor color = SkyblockAddons.getInstance().getConfigValues().getColor(Feature.SLAYER_INDICATOR);
            return color + "55% (§a40❈" + color + ")";
        }
        return progressText;
    }

    void setProgressText(String progressText) {
        this.progressText = progressText;
    }

    private void setHelmetColor() {
        if (itemStack.getItem().equals(Items.leather_helmet)) {
            ((ItemArmor)itemStack.getItem()).setColor(itemStack, ConfigColor.BLACK.getColor());
        }
    }
}
