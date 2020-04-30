package codes.biscuit.skyblockaddons.utils.item;

import codes.biscuit.skyblockaddons.constants.game.Rarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.EnumSet;

/**
 * Utility methods for Skyblock Items
 */
public class ItemUtils {
    /**
     * Returns the rarity of a given Skyblock item
     *
     * @param item the Skyblock item to check
     * @return the rarity of the item if a valid rarity is found, {@code INVALID} if no rarity is found, {@code null} if item is {@code null}
     */
    public static Rarity getRarity(ItemStack item) {
        if (item == null || !item.hasTagCompound())  {
            return null;
        }

        NBTTagCompound display = item.getSubCompound("display", false);

        if (display == null || !display.hasKey("Lore")) {
            return null;
        }

        NBTTagList lore = display.getTagList("Lore", Constants.NBT.TAG_STRING);

        // Determine the item's rarity
        for (int i = 0; i < lore.tagCount(); i++) {
            String currentLine = lore.getStringTagAt(i);

            for (Rarity rarity : EnumSet.allOf(Rarity.class)) {
                if (currentLine.startsWith(rarity.getTag())) {
                    return rarity;
                }
            }
        }

        // If the item doesn't have a valid rarity, return null
        return null;
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock item
     *
     * @param item the Skyblock item to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock item
     */
    public static String getSkyBlockItemID(final ItemStack item) {
        if (item == null) {
            throw new NullPointerException("Item cannot be null.");
        }
        else if (!item.hasTagCompound()) {
            return null;
        }

        NBTTagCompound skyBlockData = item.getSubCompound("ExtraAttributes", false);

        if (skyBlockData != null) {
            String itemId = skyBlockData.getString("id");

            if (!itemId.equals("")) {
                return itemId;
            }
        }

        return null;
    }
}
