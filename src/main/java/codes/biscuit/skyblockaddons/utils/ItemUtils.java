package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.ItemRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.text.WordUtils;

import java.util.EnumSet;
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

                for (ItemRarity itemRarity : EnumSet.allOf(ItemRarity.class)) {
                    if (rarity.startsWith(itemRarity.TAG)) {
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

    //TODO: document this
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
     * @param item the item to check
     * @return {@code true} if this item is a material, {@code false} otherwise
     */
    public static boolean isMaterialForRecipe(ItemStack item) {
        final List<String> tooltip = item.getTooltip(null, false);
        for (String s : tooltip) {
            if ("§5§o§eRight-click to view recipes!".equals(s)) {
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
}
