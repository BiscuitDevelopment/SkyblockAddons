package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.utils.skyblockdata.PetInfo;
import codes.biscuit.skyblockaddons.utils.skyblockdata.Rune;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for Skyblock Items
 */
public class ItemUtils {

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
            throw new NullPointerException("Item cannot be null.");
        } else if (!item.hasTagCompound()) {
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
     * Returns the Base Stat Boost Percentage from a given Skyblock Extra Attributes NBT Compound
     *
     * @param extraAttributes the NBT to check
     * @return the BSPB or {@code -1} if it isn't a Dungeons Item or this isn't a valid Skyblock NBT
     */
    public static int getBaseStatBoostPercentage(NBTTagCompound extraAttributes) {
        if (extraAttributes == null || !extraAttributes.hasKey("baseStatBoostPercentage")) {
            return -1;
        }

        return extraAttributes.getInteger("baseStatBoostPercentage");
    }

    /**
     * Returns a boolean relating to if the rarity of a given Skyblock Extra Attributes NBT Compound has been upgraded
     *
     * @param extraAttributes the NBT to check
     * @return {@code true} if its rarity has been upgraded or {@code false} if it hasn't or this isn't a valid Skyblock NBT
     */
    public static int getRarityUpgrades(NBTTagCompound extraAttributes) {
        if (extraAttributes != null) {
            if (!extraAttributes.hasKey("rarity_upgrades")) {
                return 0;
            }

            return extraAttributes.getInteger("rarity_upgrades");
        }

        return 0;
    }

    /**
     * Returns the Dungeon Floor an item was obtained from, from a given Skyblock Extra Attributes NBT Compound
     * Entrance is floor 0
     * @param extraAttributes the NBT to check
     * @return the Floor or {@code -1} if it isn't a Dungeons Item or this isn't a valid Skyblock NBT
     */
    public static int getDungeonFloor(NBTTagCompound extraAttributes) {
        if (extraAttributes == null || !extraAttributes.hasKey("item_tier")) {
            return -1;
        }

        return extraAttributes.getInteger("item_tier");
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
     * Checks if the given item is a pickaxe.
     *
     * @param item the item to check
     * @return {@code true} if this item is a pickaxe, {@code false} otherwise
     */
    public static boolean isPickaxe(Item item) {
        return item instanceof ItemPickaxe;
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
     * Returns the Skyblock Reforge of a given Skyblock Extra Attributes NBT Compound
     *
     * @param extraAttributes the NBT to check
     * @return the Reforge (in lowercase) of this item or {@code null} if this isn't a valid Skyblock NBT or reforge
     */
    public static String getReforge(NBTTagCompound extraAttributes) {

        if (extraAttributes != null) {
            if (!extraAttributes.hasKey("modifier")) return null;
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
     * Returns the contents of a personal compactor using the data from an ItemStack
     *
     * @param compactor the ItemStack to check
     * @return an {@link ItemStack[]} or {@code null} if it isn't a personal compactor
     */
    public static ItemStack[] getPersonalCompactorContents(ItemStack compactor) {
        String skyblockID = ItemUtils.getSkyBlockItemID(compactor);

        if (skyblockID == null || !skyblockID.startsWith("PERSONAL_COMPACTOR")) {
            return null;
        }

        NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(compactor);

        if (extraAttributes != null) {
            ItemStack[] items = new ItemStack[9];

            for (int i = 0; i < items.length; i++) {
                if (!extraAttributes.hasKey("personal_compact_" + i)) {
                    continue;
                }
                String itemName = extraAttributes.getString("personal_compact_" + i);

                boolean enchanted = itemName.contains("ENCHANTED");

                itemName = itemName.replaceFirst("ENCHANTED_", "")
                        .replaceFirst("RAW_", "").toLowerCase();

                ItemStack itemStack = null;
                if (itemName.contains("log")) {
                    switch (itemName) {
                        case "oak_log":
                            itemStack = new ItemStack(Blocks.log);
                            break;
                        case "birch_log":
                            itemStack = new ItemStack(Blocks.log, 1, 2);
                            break;
                        case "spruce_log":
                            itemStack = new ItemStack(Blocks.log, 1, 1);
                            break;
                        case "jungle_log":
                            itemStack = new ItemStack(Blocks.log, 1, 3);
                            break;
                        case "acacia_log":
                            itemStack = new ItemStack(Blocks.log2);
                            break;
                        case "dark_oak_log":
                            itemStack = new ItemStack(Blocks.log2, 1, 1);
                            break;
                    }
                }

                if (itemStack == null) {
                    Item item = Item.getByNameOrId(itemName);
                    if (item == null) {
                        Block block = Block.getBlockFromName(itemName);
                        if (block != null) {
                            item = Item.getItemFromBlock(block);
                        }
                    }
                    if (item != null) {
                        itemStack = new ItemStack(item);
                    }
                }

                if (itemStack != null && enchanted) {
                    itemStack.addEnchantment(Enchantment.protection, 1);
                }

                items[i] = itemStack;
            }

            return items;
        }

        return null;
    }

    /**
     * Returns the number of Hot Potato Books from a given Skyblock Extra Attributes NBT Compound
     * MAX 15 because of Fuming Potato Books
     * @param extraAttributes the NBT to check
     * @return the number of Hot Potato Books or {@code -1} if it isn't a valid Skyblock NBT
     */
    public static int getHotPotatoBookCount(NBTTagCompound extraAttributes) {
        if (extraAttributes != null) {

            if (!extraAttributes.hasKey("hot_potato_count")) {
                return 0;
            }

            return extraAttributes.getInteger("hot_potato_count");
        }

        return -1;
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
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("display", 10)) { // 10 -> Compound
            NBTTagCompound display = itemStack.getTagCompound().getCompoundTag("display");

            if (display.hasKey("Lore", 9)) { // 9 -> List
                NBTTagList lore = display.getTagList("Lore", 8); // 8 -> String

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

        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("ExtraAttributes", 10)) {
            NBTTagCompound extraAttributesTag = itemStack.getSubCompound("ExtraAttributes", false);

            // If this item stack is a menu item, it won't have this key.
            return !extraAttributesTag.hasKey("uuid");
        } else {
            return false;
        }
    }

    /**
     * Checks if the given {@code ItemStack} is a Skyblock item.
     *
     * @param itemStack the {@code ItemStack} to check
     * @return {@code true} if {@code itemStack} is a Skyblock item, or {@code false} if it's not
     */
    public static boolean isSkyblockItem(ItemStack itemStack) {
        if (itemStack == null) {
            throw new NullPointerException("Item stack cannot be null!");
        }

        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("ExtraAttributes", 10)) {
            NBTTagCompound extraAttributesTag = itemStack.getSubCompound("ExtraAttributes", false);

            // If the Skyblock item ID tag is present, it's a Skyblock item.
            return extraAttributesTag.hasKey("id", 8);
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
     * Returns the number of expertise kills from a given Skyblock ExtraAttributes compound tag
     *
     * @param extraAttributes the ExtraAttributes compound tag to check
     * @return the number of expertise kills or {@code -1} if the ExtraAttributes tag is {@code null} or lacks the expertise kills key
     */
    public static int getExpertiseKills(NBTTagCompound extraAttributes) {
        if (extraAttributes != null && extraAttributes.hasKey("expertise_kills")) {
            return extraAttributes.getInteger("expertise_kills");
        }
        else {
            return -1;
        }
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
