package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.item.ItemStack;

public class RevenantArmorProgress {

    private final ItemStack itemStack;
    private String progressText = null;

    public RevenantArmorProgress(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    RevenantArmorProgress(ItemStack itemStack, String progress) {
        this(itemStack);
        this.progressText = progress;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getProgressText() {
        if (progressText == null) {
            ConfigColor color = SkyblockAddons.getInstance().getConfigValues().getColor(Feature.REVENANT_INDICATOR);
            return color + "14,418/15,000 (§a240❈" + color + ")";
        }
        return progressText;
    }

    void setProgressText(String progressText) {
        this.progressText = progressText;
    }
}
