package codes.biscuit.skyblockaddons.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemUtils {

    public static String getSkyBlockItemID(final ItemStack item) {
        if (item == null) return "";
        if (item.hasTagCompound()) {
            NBTTagCompound skyBlockData = item.getTagCompound().getCompoundTag("ExtraAttributes");
            return skyBlockData.getString("id");
        }
        return "";
    }
}
