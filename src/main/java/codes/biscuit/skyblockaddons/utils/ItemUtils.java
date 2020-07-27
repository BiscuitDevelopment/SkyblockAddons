package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.ItemRarity;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.EnumSet;
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

                for (ItemRarity itemRarity : EnumSet.allOf(ItemRarity.class)) {
                    if (rarity.startsWith(itemRarity.getTag())) {
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

    public static NBTTagCompound getExtraAttributes(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return null;
        }

        return item.getSubCompound("ExtraAttributes", false);
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
     * Returns the Base Stat Boost Percentage from a given Skyblock Extra Attributes NBT Compound
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
     * Returns a {@link ItemStack[]} of Items from the ExtraAttributes Skyblock data
     *
     * @param extraAttributes the Skyblock Data to check
     * @return A {@link ItemStack[]} or {@code null} if it isn't a Personal Compactor
     */
    public static ItemStack[] getPersonalCompactorContents(NBTTagCompound extraAttributes) {
        if (extraAttributes != null) {
            String itemId = extraAttributes.getString("id");

            if (!itemId.startsWith("PERSONAL_COMPACTOR")) {
                return null;
            }

            ItemStack[] items;
            if (itemId.endsWith("4000"))
                items = new ItemStack[1];
            else if (itemId.endsWith("5000"))
                items = new ItemStack[3];
            else if (itemId.endsWith("6000"))
                items = new ItemStack[7];
            else
                items = new ItemStack[0];

            for (int i = 0; i < 7; i++) {
                if (!extraAttributes.hasKey("personal_compact_" + i))
                    continue;

                String itemname = extraAttributes.getString("personal_compact_" + i);
                itemname = itemname.replaceFirst("ENCHANTED_", "");
                itemname = itemname.replaceFirst("RAW_", "");
                itemname = itemname.toLowerCase();
                if (itemname.contains("log")) {
                    ItemStack is;
                    switch (itemname) {
                        case "oak_log":
                            is = new ItemStack(Blocks.log);
                            break;
                        case "birch_log":
                            is = new ItemStack(Blocks.log, 1, 2);
                            break;
                        case "spruce_log":
                            is = new ItemStack(Blocks.log, 1, 1);
                            break;
                        case "jungle_log":
                            is = new ItemStack(Blocks.log, 1, 3);
                            break;
                        case "acacia_log":
                            is = new ItemStack(Blocks.log2);
                            break;
                        case "dark_oak_log":
                            is = new ItemStack(Blocks.log2, 1, 1);
                            break;
                        default:
                            continue;
                    }

                    is.addEnchantment(Enchantment.protection, 1);
                    items[i] = is;
                    continue;
                }
                Item it = null;
                if (Item.itemRegistry.getObject(new ResourceLocation(itemname)) != null) {
                    it = (Item.itemRegistry.getObject(new ResourceLocation(itemname)));
                } else if (Block.blockRegistry.getObject(new ResourceLocation(itemname)) != null) {
                    it = (Item.getItemFromBlock(Block.blockRegistry.getObject(new ResourceLocation(itemname))));
                }
                if (it != null) {
                    ItemStack is = new ItemStack(it);
                    is.addEnchantment(Enchantment.protection, 1);
                    items[i] = is;
                }
            }

            return items;
        }

        return null;
    }
}
