package codes.biscuit.skyblockaddons.features.backpacks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains utility methods for backpacks and stores the color of the backpack the player has open.
 */
public class BackpackManager {

    private static final Pattern BACKPACK_ID_PATTERN = Pattern.compile("([A-Z]+)_BACKPACK");

    private static BackpackColor openedBackpackColor = null;

    /**
     * Creates and returns a {@code Backpack} object representing the given {@code ItemStack} if it is a backpack
     *
     * @param stack the {@code ItemStack} to create a {@code Backpack} instance from
     * @return a {@code Backpack} object representing {@code stack} if it is a backpack, or {@code null} otherwise
     */
    public static Backpack getFromItem(ItemStack stack) {
        if (stack == null) return null;

        SkyblockAddons main = SkyblockAddons.getInstance();
        String id = ItemUtils.getSkyBlockItemID(stack);

        if (id != null) {
            NBTTagCompound extraAttributes = stack.getSubCompound("ExtraAttributes", false);
            Matcher matcher = BACKPACK_ID_PATTERN.matcher(id);
            boolean matches = matcher.matches();
            boolean isCakeBag = main.getConfigValues().isEnabled(Feature.CAKE_BAG_PREVIEW) &&
                    "NEW_YEAR_CAKE_BAG".equals(id) && EnumUtils.InventoryType.getCurrentInventoryType() != EnumUtils.InventoryType.BAKER;

            // If it's a backpack OR it's a cake bag and they have the setting enabled.
            if (matches || isCakeBag) {
                byte[] bytes = null;
                for (String key : extraAttributes.getKeySet()) {
                    if (key.endsWith("backpack_data") || key.equals("new_year_cake_bag_data")) {
                        bytes = extraAttributes.getByteArray(key);
                        break;
                    }
                }
                try {
                    int length = 0;
                    if (matches) {
                        String backpackType = matcher.group(1);
                        switch (backpackType) { // because sometimes the size of the tag is not updated (eg. when you upgrade it)
                            case "SMALL": length = 9; break;
                            case "MEDIUM": length = 18; break;
                            case "LARGE": length = 27; break;
                            case "GREATER": length = 36; break;
                            case "JUMBO": length = 54; break;
                        }
                    }
                    ItemStack[] items = new ItemStack[length];
                    if (bytes != null) {
                        NBTTagCompound nbtTagCompound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
                        NBTTagList list = nbtTagCompound.getTagList("i", Constants.NBT.TAG_COMPOUND);

                        if (list.tagCount() > length) {
                            length = list.tagCount();
                            items = new ItemStack[length];
                        }

                        for (int i = 0; i < length; i++) {
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
                    }
                    BackpackColor color = BackpackColor.WHITE;
                    if (extraAttributes.hasKey("backpack_color")) {
                        try {
                            color = BackpackColor.valueOf(extraAttributes.getString("backpack_color"));
                        } catch (IllegalArgumentException ignored) {}
                    }
                    return new Backpack(items, TextUtils.stripColor(stack.getDisplayName()), color);
                } catch (IOException ex) {
                    SkyblockAddons.getInstance().getLogger().error("There was an error parsing backpack data.");
                    SkyblockAddons.getInstance().getLogger().catching(ex);
                }
            }
        }
        return null;
    }

    /**
     * Checks if the given {@code ItemStack} is a backpack
     *
     * @param stack the {@code ItemStack} to check
     * @return {@code true} if {@code stack} is a backpack, {@code false} otherwise
     */
    public static boolean isBackpack(ItemStack stack) {
        if (stack == null) return false;
        String id = ItemUtils.getSkyBlockItemID(stack);
        if (id == null) return false;
        return BACKPACK_ID_PATTERN.matcher(id).matches();
    }

    /**
     * Checks if the given {@code ItemStack} is a backpack that represents its crafting recipe in the recipes menu or the
     * item the player is currently crafting in the crafting menu
     *
     * @param stack the {@code ItemStack} to check
     * @return {@code true} if {@code stack} is a backpack and represents its crafting recipe or the item the player is crafting, or {@code false} otherwise
     */
    public static boolean isBackpackCraftingMenuItem(ItemStack stack) {
        if (!isBackpack(stack)) {
            return false;
        }

        NBTTagCompound extraAttributesTag = stack.getSubCompound("ExtraAttributes", false);

        // If the backpack is being used to represent a crafting recipe, it won't have this key.
        return extraAttributesTag != null && !extraAttributesTag.hasKey("uuid");
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
