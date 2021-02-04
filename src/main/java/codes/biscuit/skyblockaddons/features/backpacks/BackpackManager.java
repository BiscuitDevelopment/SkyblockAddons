package codes.biscuit.skyblockaddons.features.backpacks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraftforge.common.util.Constants.NBT.TAG_BYTE_ARRAY;

/**
 * This class contains utility methods for backpacks and stores the color of the backpack the player has open.
 */
public class BackpackManager {

    private static final Pattern BACKPACK_ID_PATTERN = Pattern.compile("([A-Z]+)_BACKPACK");

    private static BackpackColor openedBackpackColor = null;

    /**
     * Creates and returns a {@code ContainerPreview} object representing the given {@code ItemStack} if it is a backpack
     *
     * @param stack the {@code ItemStack} to create a {@code Backpack} instance from
     * @return a {@code ContainerPreview} object representing {@code stack} if it is a backpack, or {@code null} otherwise
     */
    public static ContainerPreview getFromItem(ItemStack stack) {

        if (stack == null) {
            return null;
        }

        NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(stack);
        String id = ItemUtils.getSkyBlockItemID(extraAttributes);
        ContainerItem containerItem;
        if (id != null && (containerItem = ItemUtils.itemMap.getContainerItem(id)) != null) {

            int containerSize = containerItem.getSize();

            // Parse out a list of items in the container
            ItemStack[] items = null;
            if (containerItem.isBackpack() || containerItem.isCakeBag() || containerItem.isBuildersWand()) {
                String compressedDataTag = containerItem.getCompressedDataTag();
                if (compressedDataTag != null && extraAttributes.hasKey(compressedDataTag, TAG_BYTE_ARRAY)) {
                    byte[] bytes = extraAttributes.getByteArray(compressedDataTag);
                    items = decompressItems(bytes, containerSize);
                }
            }
            else if (containerItem.isPersonalCompactor()) {
                items = new ItemStack[containerSize];
                Iterator<String> itr = containerItem.getDataTags().iterator();
                for (int i = 0; i < containerSize && itr.hasNext(); i++) {
                    String key = itr.next();
                    if (!extraAttributes.hasKey(key)) {
                        continue;
                    }
                    items[i] = ItemUtils.itemMap.getPersonalCompactorItem(extraAttributes.getString(key));
                }
            }
            if (items == null) {
                SkyblockAddons.getLogger().error("There was an error parsing container data.");
                return null;
            }

            // Get the container color
            BackpackColor color = ItemUtils.getBackpackColor(stack);
            String name = containerItem.isPersonalCompactor() ? "" : TextUtils.stripColor(stack.getDisplayName());

            return new ContainerPreview(items, name, color, containerItem.getNumRows(), containerItem.getNumCols());
        }
        return null;
    }

    private static ItemStack[] decompressItems(byte[] bytes, int maxItems) {
        ItemStack[] items = null;
        try {
            NBTTagCompound decompressedData = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
            NBTTagList list = decompressedData.getTagList("i", Constants.NBT.TAG_COMPOUND);
            if (list.hasNoTags()) {
                throw new Exception("Decompressed container list has no item tags");
            }
            int size = Math.min(list.tagCount(), maxItems);
            items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                NBTTagCompound item = list.getCompoundTagAt(i);
                // This fixes an issue in Hypixel where enchanted potatoes have the wrong id (potato block instead of item).
                short itemID = item.getShort("id");
                if (itemID == 142) { // Potato Block -> Potato Item
                    item.setShort("id", (short) 392);
                } else if (itemID == 141) { // Carrot Block -> Carrot Item
                    item.setShort("id", (short) 391);
                }
                ItemStack itemStack = ItemStack.loadItemStackFromNBT(item);
                items[i] = itemStack;
            }
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("There was an error decompressing container data.");
            SkyblockAddons.getLogger().catching(ex);
        }
        return items;
    }

    /**
     * Returns the color of the backpack the player currently has open
     *
     * @return the color of the backpack the player currently has open
     */
    public static BackpackColor getOpenedBackpackColor() {
        return openedBackpackColor;
    }

    /**
     * <p>Sets {@code openedBackpackColor}</p>
     * <p>This variable is used when rendering the backpack inventory to change the background color to the backpack's color.</p>
     *
     * @param openedBackpackColor the color of the backpack that the player has open
     */
    public static void setOpenedBackpackColor(BackpackColor openedBackpackColor) {
        BackpackManager.openedBackpackColor = openedBackpackColor;
    }
}
