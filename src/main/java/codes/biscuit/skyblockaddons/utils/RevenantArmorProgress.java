package codes.biscuit.skyblockaddons.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class RevenantArmorProgress {

    private final ItemStack itemStack;
    private String progressText;
    private boolean isDummy;

    RevenantArmorProgress(ItemStack itemStack, String progress) {
        this.itemStack = itemStack;
        this.progressText = progress;
    }

    public RevenantArmorProgress(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.isDummy = true;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getProgressText() {
        if (this.isDummy) {
            return EnumChatFormatting.GREEN + "+90\u2748" + EnumChatFormatting.DARK_GRAY + "(" + EnumChatFormatting.GREEN + "500" + EnumChatFormatting.GRAY + "/" + EnumChatFormatting.RED + "1,000" + EnumChatFormatting.DARK_GRAY + ")";
        }
        return progressText;
    }

    void setProgressText(String progressText) {
        this.progressText = progressText;
    }

    @Override
    public String toString() {
        return "RevenantArmorProgress[itemStack=" + this.itemStack + ", progressText=" + this.progressText + "]";
    }
}
