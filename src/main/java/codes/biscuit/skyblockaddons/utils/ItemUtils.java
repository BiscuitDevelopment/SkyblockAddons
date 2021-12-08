package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.core.ItemType;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackColor;
import codes.biscuit.skyblockaddons.utils.skyblockdata.CompactorItem;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerData;
import codes.biscuit.skyblockaddons.utils.skyblockdata.PetInfo;
import codes.biscuit.skyblockaddons.utils.skyblockdata.Rune;
import lombok.Setter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.text.WordUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraftforge.common.util.Constants.NBT.*;

/**
 * Utility methods for Skyblock Items
 */
public class ItemUtils {

    public static final int NBT_INTEGER = 3;
    public static final int NBT_STRING = 8;
    public static final int NBT_LIST = 9;
    /**
     * This expression matches the line with a Skyblock item's rarity and item type that's at the end of its lore.
     */
    private static final Pattern ITEM_TYPE_AND_RARITY_PATTERN = Pattern.compile("§l(?<rarity>[A-Z]+) ?(?<type>[A-Z ]+)?(?:§[0-9a-f]§l§ka)?$");
    @SuppressWarnings({"FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"})
    @Setter private static Map<String, CompactorItem> compactorItems;
    @SuppressWarnings({"FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"})
    @Setter private static Map<String, ContainerData> containers;


    /**
     * Returns the rarity of a given Skyblock item. The rarity is read from the item's lore.
     * The item must not be {@code null}.
     *
     * @param item the Skyblock item to check, can't be {@code null}
     * @return the rarity of the item if a valid rarity is found, or {@code null} if item is {@code null} or no valid rarity is found
     */
    public static ItemRarity getRarity(ItemStack item) {
        if (item == null) {
            throw new NullPointerException("The item cannot be null!");
        }
        if (!item.hasTagCompound())  {
            return null;
        }

        return getRarity(getItemLore(item));
    }

    /**
     * Returns the item type of a given Skyblock item.
     * The item must not be {@code null}.
     *
     * @param item the Skyblock item to check, can't be {@code null}
     * @return the item type of the item or {@code null} if no item type was found
     */
    public static ItemType getItemType(ItemStack item) {
        if (item == null) {
            throw new NullPointerException("The item cannot be null!");
        }
        if (!item.hasTagCompound())  {
            return null;
        }

        return getType(getItemLore(item));
    }

    /**
     * Returns the itemstack that this personal compactor skyblock ID represents. Note that
     * a personal compactor skyblock ID is not the same as an item's regular skyblock id!
     *
     * @param personalCompactorSkyblockID The personal compactor skyblock ID (ex. ENCHANTED_ACACIA_LOG)
     * @return The itemstack that this personal compactor skyblock ID represents
     */
    public static ItemStack getPersonalCompactorItemStack(String personalCompactorSkyblockID) {
        CompactorItem compactorItem = compactorItems.get(personalCompactorSkyblockID);
        return compactorItem != null ? compactorItem.getItemStack() : ItemUtils.createSkullItemStack("§7Unknown (" + personalCompactorSkyblockID + ")", Collections.singletonList("§6also biscut was here hi!!"), personalCompactorSkyblockID,
                "724c64a2-fc8b-4842-852b-6b4c2c6ef241", "e0180f4aeb6929f133c9ff10476ab496f74c46cf8b3be6809798a974929ccca3");
    }

    /**
     * Returns data about the container that is passed in.
     *
     * @param skyblockID The skyblock ID of the container
     * @return A {@link ContainerData} object containing info about the container in general
     */
    public static ContainerData getContainerData(String skyblockID) {
        return containers.get(skyblockID);
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock item
     *
     * @param item the Skyblock item to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock item
     */
    public static String getSkyblockItemID(ItemStack item) {
        if (item == null) {
            return null;
        }

        NBTTagCompound extraAttributes = getExtraAttributes(item);
        if (extraAttributes == null) {
            return null;
        }

        if (!extraAttributes.hasKey("id", ItemUtils.NBT_STRING)) {
            return null;
        }

        return extraAttributes.getString("id");
    }

    /**
     * Returns the {@code ExtraAttributes} compound tag from the item's NBT data. The item must not be {@code null}.
     *
     * @param item the item to get the tag from
     * @return the item's {@code ExtraAttributes} compound tag or {@code null} if the item doesn't have one
     */
    public static NBTTagCompound getExtraAttributes(ItemStack item) {
        if (item == null) {
            throw new NullPointerException("The item cannot be null!");
        }
        if (!item.hasTagCompound()) {
            return null;
        }

        return item.getSubCompound("ExtraAttributes", false);
    }


    /**
     * Returns the {@code enchantments} compound tag from the item's NBT data.
     *
     * @param item the item to get the tag from
     * @return the item's {@code enchantments} compound tag or {@code null} if the item doesn't have one
     */
    public static NBTTagCompound getEnchantments(ItemStack item) {
        NBTTagCompound extraAttributes = getExtraAttributes(item);
        return extraAttributes == null ? null : extraAttributes.getCompoundTag("enchantments");
    }

    /**
     * @return The Skyblock reforge of a given itemstack
     */
    public static String getReforge(ItemStack item) {
        if (item.hasTagCompound()) {
            NBTTagCompound extraAttributes = item.getTagCompound();
            if (extraAttributes.hasKey("ExtraAttributes")) {
                extraAttributes = extraAttributes.getCompoundTag("ExtraAttributes");
                if (extraAttributes.hasKey("modifier")) {
                    String reforge = WordUtils.capitalizeFully(extraAttributes.getString("modifier"));

                    reforge = reforge.replace("_sword", ""); //fixes reforges like "Odd_sword"
                    reforge = reforge.replace("_bow", "");

                    return reforge;
                }
            }
        }
        return null;
    }

    /**
     * Checks if the given item is a material meant to be used in a crafting recipe. Dragon fragments are an example
     * since they are used to make dragon armor.
     *
     * @param itemStack the item to check
     * @return {@code true} if this item is a material, {@code false} otherwise
     */
    //TODO: Fix for Hypixel localization
    public static boolean isMaterialForRecipe(ItemStack itemStack) {
        List<String> lore = ItemUtils.getItemLore(itemStack);
        for (String loreLine : lore) {
            if ("Right-click to view recipes!".equals(TextUtils.stripColor(loreLine))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given item is a mining tool (pickaxe or drill).
     *
     * @param itemStack the item to check
     * @return {@code true} if this item is a pickaxe/drill, {@code false} otherwise
     */
    public static boolean isMiningTool(ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemPickaxe || isDrill(itemStack);
    }


    /**
     * Checks if the given {@code ItemStack} is a drill. It works by checking for the presence of the {@code drill_fuel} NBT tag,
     * which only drills have.
     *
     * @param itemStack the item to check
     * @return {@code true} if this item is a drill, {@code false} otherwise
     */
    public static boolean isDrill(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        NBTTagCompound extraAttributes = getExtraAttributes(itemStack);

        if (extraAttributes != null) {
            return extraAttributes.hasKey("drill_fuel", TAG_INT);
        } else {
            return false;
        }
    }


    /**
     * Returns the Skyblock Item ID of a given Skyblock Extra Attributes NBT Compound
     *
     * @param extraAttributes the NBT to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock NBT
     */
    public static String getSkyblockItemID(NBTTagCompound extraAttributes) {
        if (extraAttributes == null) {
            return null;
        }

        String itemId = extraAttributes.getString("id");
        if (itemId.equals("")) {
            return null;
        }

        return itemId;
    }

    /**
     * Checks if the given {@code ItemStack} is a backpack
     *
     * @param stack the {@code ItemStack} to check
     * @return {@code true} if {@code stack} is a backpack, {@code false} otherwise
     */
    public static boolean isBackpack(ItemStack stack) {
        NBTTagCompound extraAttributes = getExtraAttributes(stack);
        ContainerData containerData = containers.get(getSkyblockItemID(extraAttributes));
        return containerData != null && containerData.isBackpack();
    }

    /**
     * Checks if the given {@code ItemStack} is a builders wand
     * See {@link codes.biscuit.skyblockaddons.asm.hooks.PlayerControllerMPHook#onWindowClick(int, int, int, EntityPlayer, ReturnValue)} for a commented-out implementation (may come back in the future).
     *
     * @param stack the {@code ItemStack} to check
     * @return {@code true} if {@code stack} is a backpack, {@code false} otherwise
     */
    public static boolean isBuildersWand(ItemStack stack) {
        NBTTagCompound extraAttributes = getExtraAttributes(stack);
        ContainerData containerData = containers.get(getSkyblockItemID(extraAttributes));
        return containerData != null && containerData.isBuildersWand();
    }

    /**
     * Gets the color of the backpack in the given {@code ItemStack}
     *
     * @param stack the {@code ItemStack} containing the backpack
     * @return The color of the backpack; or {@code WHITE} if there is no color; or {@code null} if it is not a container
     */
    public static BackpackColor getBackpackColor(ItemStack stack) {
        NBTTagCompound extraAttributes = getExtraAttributes(stack);
        ContainerData containerData = containers.get(getSkyblockItemID(extraAttributes));
        if (extraAttributes != null) {
            if (containerData != null) {
                try {
                    return BackpackColor.valueOf(extraAttributes.getString(containerData.getColorTag()));
                } catch (IllegalArgumentException ignored) {
                }
                return BackpackColor.WHITE;
            } else if (extraAttributes.hasKey("backpack_color")) {
                try {
                    return BackpackColor.valueOf(extraAttributes.getString("backpack_color"));
                } catch (IllegalArgumentException ignored) {
                }
                return BackpackColor.WHITE;
            }
        }

        return null;
    }

    /**
     * Returns the Skyblock Reforge of a given Skyblock Extra Attributes NBT Compound
     *
     * @param extraAttributes the NBT to check
     * @return the Reforge (in lowercase) of this item or {@code null} if this isn't a valid Skyblock NBT or reforge
     */
    public static String getReforge(NBTTagCompound extraAttributes) {
        if (extraAttributes != null && extraAttributes.hasKey("modifier", NBT_STRING)) {
            return extraAttributes.getString("modifier");
        }

        return null;
    }

    /**
     * Returns a {@link Rune} from the ExtraAttributes Skyblock data
     * This can ge retrieved from a rune itself or an infused item
     *
     * @param extraAttributes the Skyblock Data to check
     * @return A {@link Rune} or {@code null} if it doesn't have it
     */
    public static Rune getRuneData(NBTTagCompound extraAttributes) {
        if (extraAttributes != null) {
            if (!extraAttributes.hasKey("runes")) {
                return null;
            }

            return new Rune(extraAttributes.getCompoundTag("runes"));
        }

        return null;
    }

    /**
     * Returns a {@link PetInfo} from the ExtraAttributes Skyblock data
     *
     * @param extraAttributes the Skyblock Data to check
     * @return A {@link PetInfo} or {@code null} if it isn't a pet
     */
    public static PetInfo getPetInfo(NBTTagCompound extraAttributes) {
        if (extraAttributes != null) {
            String itemId = extraAttributes.getString("id");

            if (!itemId.equals("PET") || !extraAttributes.hasKey("petInfo")) {
                return null;
            }

            return SkyblockAddons.getGson().fromJson(extraAttributes.getString("petInfo"), PetInfo.class);
        }

        return null;
    }

    /**
     * Returns a string list containing the NBT lore of an {@code ItemStack}, or
     * an empty list if this item doesn't have a lore tag.
     * The itemStack argument must not be {@code null}. The returned lore list is unmodifiable since it has been
     * converted from an {@code NBTTagList}.
     *
     * @param itemStack the ItemStack to get the lore from
     * @return the lore of an ItemStack as a string list
     */
    public static List<String> getItemLore(ItemStack itemStack) {
        if (itemStack != null) {
            if (itemStack.hasTagCompound()) {
                NBTTagCompound display = itemStack.getSubCompound("display", false);

                if (display != null && display.hasKey("Lore", ItemUtils.NBT_LIST)) {
                    NBTTagList lore = display.getTagList("Lore", ItemUtils.NBT_STRING);

                    List<String> loreAsList = new ArrayList<>();
                    for (int lineNumber = 0; lineNumber < lore.tagCount(); lineNumber++) {
                        loreAsList.add(lore.getStringTagAt(lineNumber));
                    }

                    return Collections.unmodifiableList(loreAsList);
                }
            }

            return Collections.emptyList();
        } else {
            throw new NullPointerException("Cannot get lore from null item!");
        }
    }

    /**
     * Sets the lore text of a given {@code ItemStack}.
     *
     * @param itemStack the {@code ItemStack} to set the lore for
     * @param lore the new lore
     */
    public static void setItemLore(ItemStack itemStack, List<String> lore) {
        NBTTagCompound display = itemStack.getSubCompound("display", true);

        NBTTagList loreTagList = new NBTTagList();
        for (String loreLine : lore) {
            loreTagList.appendTag(new NBTTagString(loreLine));
        }

        display.setTag("Lore", loreTagList);
    }

    /**
     * Check if the given {@code ItemStack} is an item shown in a menu as a preview or placeholder
     * (e.g. items in the recipe book).
     *
     * @param itemStack the {@code ItemStack} to check
     * @return {@code true} if {@code itemStack} is an item shown in a menu as a preview or placeholder, {@code false} otherwise
     */
    public static boolean isMenuItem(ItemStack itemStack) {
        if (itemStack == null) {
            throw new NullPointerException("Item stack cannot be null!");
        }

        NBTTagCompound extraAttributes = getExtraAttributes(itemStack);
        if (extraAttributes != null) {
            // If this item stack is a menu item, it won't have this key.
            return !extraAttributes.hasKey("uuid");
        } else {
            return false;
        }
    }

    /**
     * Creates a new {@code ItemStack} instance with the given item and a fake enchantment to enable the enchanted "glint"
     * effect if {@code enchanted} is true. This method should be used when you want to create a bare-bones {@code ItemStack}
     * to render as part of a GUI.
     *
     * @param item the {@code Item} the created {@code ItemStack} should be
     * @param enchanted the item has the enchanted "glint" effect enabled if {@code true}, disabled if {@code false}
     * @return a new {@code ItemStack} instance with the given item and a fake enchantment if applicable
     */
    public static ItemStack createItemStack(Item item, boolean enchanted) {
        return createItemStack(item, 0, null, null, enchanted);
    }

    public static ItemStack createItemStack(Item item, String name, String skyblockID, boolean enchanted) {
        return createItemStack(item, 0, name, skyblockID, enchanted);
    }

    public static ItemStack createItemStack(Item item, int meta, String name, String skyblockID, boolean enchanted) {
        ItemStack stack = new ItemStack(item, 1, meta);

        if (name != null) {
            stack.setStackDisplayName(name);
        }

        if (enchanted) {
            stack.addEnchantment(Enchantment.protection, 0);
        }

        if (skyblockID != null) {
            setItemStackSkyblockID(stack, skyblockID);
        }

        return stack;
    }

    public static ItemStack createEnchantedBook(String name, String skyblockID, String enchantName, int enchantLevel) {
        ItemStack stack = createItemStack(Items.enchanted_book, name, skyblockID, false);

        NBTTagCompound enchantments = new NBTTagCompound();
        enchantments.setInteger(enchantName, enchantLevel);

        NBTTagCompound extraAttributes = stack.getTagCompound().getCompoundTag("ExtraAttributes");
        extraAttributes.setTag("enchantments", enchantments);

        return stack;
    }

    public static ItemStack createSkullItemStack(String name, String skyblockID, String skullID, String textureURL) {
        ItemStack stack = new ItemStack(Items.skull, 1, 3);

        NBTTagCompound texture = new NBTTagCompound();
        texture.setString("Value", TextUtils.encodeSkinTextureURL(textureURL));

        NBTTagList textures = new NBTTagList();
        textures.appendTag(texture);

        NBTTagCompound properties = new NBTTagCompound();
        properties.setTag("textures", textures);

        NBTTagCompound skullOwner = new NBTTagCompound();
        skullOwner.setTag("Properties", properties);

        skullOwner.setString("Id", skullID);

        stack.setTagInfo("SkullOwner", skullOwner);

        if (name != null) {
            stack.setStackDisplayName(name);
        }

        if (skyblockID != null) {
            setItemStackSkyblockID(stack, skyblockID);
        }

        return stack;
    }

    public static ItemStack createSkullItemStack(String name, List<String> lore, String skyblockID, String skullID, String textureURL) {
        ItemStack stack = new ItemStack(Items.skull, 1, 3);

        NBTTagCompound texture = new NBTTagCompound();
        texture.setString("Value", TextUtils.encodeSkinTextureURL(textureURL));

        NBTTagList textures = new NBTTagList();
        textures.appendTag(texture);

        NBTTagCompound properties = new NBTTagCompound();
        properties.setTag("textures", textures);

        NBTTagCompound skullOwner = new NBTTagCompound();
        skullOwner.setTag("Properties", properties);

        skullOwner.setString("Id", skullID);

        stack.setTagInfo("SkullOwner", skullOwner);

        if (name != null) {
            stack.setStackDisplayName(name);
            ItemUtils.setItemLore(stack, lore);
        }

        if (skyblockID != null) {
            setItemStackSkyblockID(stack, skyblockID);
        }

        return stack;
    }

    public static void setItemStackSkyblockID(ItemStack itemStack, String skyblockID) {
        NBTTagCompound extraAttributes = new NBTTagCompound();
        extraAttributes.setString("id", skyblockID);
        itemStack.setTagInfo("ExtraAttributes", extraAttributes);
    }

    /**
     * Given a skull ItemStack, returns the skull owner ID, or null if it doesn't exist.
     */
    public static String getSkullOwnerID(ItemStack skull) {
        if (skull == null || !skull.hasTagCompound()) {
            return null;
        }

        NBTTagCompound nbt = skull.getTagCompound();
        if (nbt.hasKey("SkullOwner", 10)) {
            nbt = nbt.getCompoundTag("SkullOwner");
            if (nbt.hasKey("Id", 8)) {
                return nbt.getString("Id");
            }
        }
        return null;
    }

    public static NBTTagByteArray getCompressedNBT(ItemStack[] items) {
        if (items == null) {
            return null;
        }
        // Add each item's nbt to a tag list
        NBTTagList list = new NBTTagList();
        for (ItemStack item : items) {
            if (item == null) {
                list.appendTag((new ItemStack((Item) null)).serializeNBT());
            } else {
                list.appendTag(item.serializeNBT());
            }
        }
        // Append standard "i" tag for compression
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("i", list);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            CompressedStreamTools.writeCompressed(nbt, stream);
        } catch (IOException e) {
            return null;
        }
        return new NBTTagByteArray(stream.toByteArray());
    }

    /**
     * Returns the rarity of a Skyblock item given its lore. This method takes the item's lore as a string list as input.
     * This method is split up from the method that takes the {@code ItemStack} instance for easier unit testing.
     *
     * @param lore the {@code List<String>} containing the item's lore
     * @return the rarity of the item if a valid rarity is found, or {@code null} if item is {@code null} or no valid rarity is found
     */
    private static ItemRarity getRarity(List<String> lore) {
        // Start from the end since the rarity is usually the last line or one of the last.
        for (int i = lore.size() - 1; i >= 0 ; i--) {
            String currentLine = lore.get(i);

            Matcher rarityMatcher = ITEM_TYPE_AND_RARITY_PATTERN.matcher(currentLine);
            if (rarityMatcher.find()) {
                String rarity = rarityMatcher.group("rarity");

                for (ItemRarity itemRarity : ItemRarity.values()) {
                    // Use a "startsWith" check here because "VERY SPECIAL" has two words and only "VERY" is matched.
                    if (itemRarity.getLoreName().startsWith(rarity)) {
                        return itemRarity;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the item type of a Skyblock item given its lore. This method takes the item's lore as a string list as input.
     * This method is split up from the method that takes the {@code ItemStack} instance for easier unit testing.
     *
     * @param lore the {@code List<String>} containing the item's lore
     * @return the rarity of the item if a valid rarity is found, or {@code null} if item is {@code null} or no valid rarity is found
     */
    private static ItemType getType(List<String> lore) {
        // Start from the end since the rarity is usually the last line or one of the last.
        for (int i = lore.size() - 1; i >= 0; i--) {
            String currentLine = lore.get(i);

            Matcher itemTypeMatcher = ITEM_TYPE_AND_RARITY_PATTERN.matcher(currentLine);
            if (itemTypeMatcher.find()) {
                String type = itemTypeMatcher.group("type");

                if (type != null) {
                    for (ItemType itemType : ItemType.values()) {
                        if (itemType.getLoreName().startsWith(type)) {
                            return itemType;
                        }
                    }
                }
            }
        }

        return null;
    }
}
