package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackColor;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerItem;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ItemMap;
import codes.biscuit.skyblockaddons.utils.skyblockdata.PetInfo;
import codes.biscuit.skyblockaddons.utils.skyblockdata.Rune;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.text.WordUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for Skyblock Items
 */
public class ItemUtils {

    public static final int NBT_INTEGER = 3;
    public static final int NBT_STRING = 8;
    public static final int NBT_LIST = 9;
    public static final int NBT_COMPOUND = 10;

    public static ItemMap itemMap = new ItemMap();

    // Group 0 -> Recombobulator 3000 & Group 1 -> Color Codes
    private static final Pattern RARITY_PATTERN = Pattern.compile("(§[0-9a-f]§l§ka§r )?([§0-9a-fk-or]+)(?<rarity>[A-Z]+)");

    /**
     * Returns the rarity of a given Skyblock item
     *
     * @param item the Skyblock item to check
     * @return the rarity of the item if a valid rarity is found, {@code INVALID} if no rarity is found, {@code null} if item is {@code null}
     */
    public static ItemRarity getRarity(ItemStack item) {
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

            Matcher rarityMatcher = RARITY_PATTERN.matcher(currentLine);
            if (rarityMatcher.find()) {
                String rarity = rarityMatcher.group("rarity");

                for (ItemRarity itemRarity : ItemRarity.values()) {
                    if (rarity.startsWith(itemRarity.getLoreName())) {
                        return itemRarity;
                    }
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
    public static String getSkyBlockItemID(ItemStack item) {
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
     * Returns the {@code ExtraAttributes} compound tag from the item's NBT data.
     *
     * @param item the item to get the tag from
     * @return the item's {@code ExtraAttributes} compound tag or {@code null} if the item doesn't have one
     */
    public static NBTTagCompound getExtraAttributes(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return null;
        }

        return item.getSubCompound("ExtraAttributes", false);
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
     * Checks if the given {@code ItemStack}'s item is a mining tool (pickaxe or drill).
     *
     * @param itemStack the {@code ItemStack} to check
     * @return {@code true} if this item is a pickaxe or drill, {@code false} otherwise
     */
    // TODO: This is a hotfix until we come up with a way to jsonify
    public static boolean isMiningTool(ItemStack itemStack) {
        String id;
        return itemStack.getItem() instanceof ItemPickaxe ||
                (id = getSkyBlockItemID(itemStack)) != null && (id.startsWith("MITHRIL_DRILL_") || id.startsWith("TITANIUM_DRILL_"));
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock Extra Attributes NBT Compound
     *
     * @param extraAttributes the NBT to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock NBT
     */
    public static String getSkyBlockItemID(NBTTagCompound extraAttributes) {
        if (extraAttributes != null) {
            String itemId = extraAttributes.getString("id");

            if (!itemId.equals("")) {
                return itemId;
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
        NBTTagCompound extraAttributes = getExtraAttributes(stack);
        ContainerItem container = itemMap.getContainerItem(getSkyBlockItemID(extraAttributes));
        return container != null && container.isBackpack();
    }

    /**
     * Gets the color of the backpack in the given {@code ItemStack}
     * @param stack the {@code ItemStack} containing the backpack
     * @return The color of the backpack; or {@code WHITE} if there is no color; or {@code null} if it is not a container
     */
    public static BackpackColor getBackpackColor(ItemStack stack) {
        NBTTagCompound extraAttributes = getExtraAttributes(stack);
        ContainerItem container = itemMap.getContainerItem(getSkyBlockItemID(extraAttributes));
        if (container != null) {
            try {
                return BackpackColor.valueOf(extraAttributes.getString(container.getColorTag()));
            } catch (IllegalArgumentException ignored) {}
            return BackpackColor.WHITE;
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
     * Returns the contents of a personal compactor using the data from the compactor's {@code ItemStack}.
     * This method guesses the Minecraft item of the items in the compactor form their Skyblock ID.
     *
     * @param compactor the ItemStack to check
     * @return an {@link ItemStack[]} of all the items in the personal compactor or {@code null} if {@code compactor} isn't a personal compactor
     */
    public static ItemStack[] getPersonalCompactorContents(ItemStack compactor) {
        String skyblockID = ItemUtils.getSkyBlockItemID(compactor);

        if (skyblockID == null || !skyblockID.startsWith("PERSONAL_COMPACTOR")) {
            return null;
        }

        NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(compactor);

        if (extraAttributes != null) {
            int length = 18;
            switch (skyblockID) { // because sometimes the size of the tag is not updated (eg. when you upgrade it)
                case "PERSONAL_COMPACTOR_4000": length = 1; break;
                case "PERSONAL_COMPACTOR_5000": length = 3; break;
                case "PERSONAL_COMPACTOR_6000": length = 7; break;
                case "PERSONAL_COMPACTOR_7000": length = 12; break;
            }
            ItemStack[] items = new ItemStack[length];
            for (int i = 0; i < items.length; i++) {
                if (!extraAttributes.hasKey("personal_compact_" + i)) {
                    continue;
                }
                skyblockID = extraAttributes.getString("personal_compact_" + i);
                items[i] = itemMap.getPersonalCompactorItem(skyblockID);
            }

            return items;
        }

        return null;
    }

    /**
     * Returns a string list containing the nbt lore of an ItemStack, or
     * an empty list if this item doesn't have a lore. The returned lore
     * list is unmodifiable since it has been converted from an NBTTagList.
     *
     * @param itemStack the ItemStack to get the lore from
     * @return the lore of an ItemStack as a string list
     */
    public static List<String> getItemLore(ItemStack itemStack) {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("display", ItemUtils.NBT_COMPOUND)) {
            NBTTagCompound display = itemStack.getTagCompound().getCompoundTag("display");

            if (display.hasKey("Lore", ItemUtils.NBT_LIST)) {
                NBTTagList lore = display.getTagList("Lore", ItemUtils.NBT_STRING);

                List<String> loreAsList = new ArrayList<>();
                for (int lineNumber = 0; lineNumber < lore.tagCount(); lineNumber++) {
                    loreAsList.add(lore.getStringTagAt(lineNumber));
                }

                return Collections.unmodifiableList(loreAsList);
            }
        }

        return Collections.emptyList();
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
        enchantments.setString(enchantName, String.valueOf(enchantLevel));

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
}
