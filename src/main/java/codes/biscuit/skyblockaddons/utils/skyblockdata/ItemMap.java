package codes.biscuit.skyblockaddons.utils.skyblockdata;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

/**
 * This class holds a {@code HashMap} instance that stores the mappings of Skyblock Item IDs to their corresponding Minecraft items.
 */
public class ItemMap {
    // Updated when the map is deserialized by GSON, field is required to be a variable
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "FieldMayBeFinal"})
    private HashMap<String,String> itemMap = new HashMap<>();

    /**
     * This method returns an {@code ItemStack} of the Minecraft item corresponding to the given Skyblock item ID. The
     * returned {@code ItemStack} will always have a quantity of one. This method works only for Skyblock item IDs stored in the item map.
     *
     * @param skyblockItemId the Skyblock item ID to get an {@code ItemStack} for
     * @return an {@code ItemStack} of the Minecraft item corresponding to {@code skyblockItemId} with a quantity of one
     * or {@code null} if {@code skyblockItemId} isn't in the item map
     */
    public ItemStack getItemStack(String skyblockItemId) {
        if (itemMap.containsKey(skyblockItemId)) {
            String minecraftIdString = itemMap.get(skyblockItemId);
            String[] minecraftIdArray = minecraftIdString.split(":", 2);
            int meta = minecraftIdArray.length == 2 ? Integer.parseInt(minecraftIdArray[1]) : 0;
            Item item = Item.getByNameOrId(minecraftIdArray[0]);

            if (item != null) {
                switch (minecraftIdArray.length) {
                    case 1:
                        return new ItemStack(item);
                    case 2:
                        return new ItemStack(item, 1, meta);
                }
            }
        }

        return null;
    }
}
