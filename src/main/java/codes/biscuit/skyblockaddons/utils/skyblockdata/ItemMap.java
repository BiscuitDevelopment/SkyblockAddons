package codes.biscuit.skyblockaddons.utils.skyblockdata;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds a {@code HashMap} instance that stores the mappings of Skyblock Item IDs to their corresponding Minecraft items.
 */
public class ItemMap {
    // Updated when the map is deserialized by GSON, field is required to be a variable
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "FieldMayBeFinal"})
    private HashMap<String,SkyblockItem> items = new HashMap<>();
    private final SkyblockItem BISCUIT_HEAD = new SkyblockItem("skull", EnumChatFormatting.GOLD+"Unknown Item", false,
            "724c64a2-fc8b-4842-852b-6b4c2c6ef241", "e0180f4aeb6929f133c9ff10476ab496f74c46cf8b3be6809798a974929ccca3");
    private final SkyblockItem GLASS_FILLER = new SkyblockItem("stained_glass_pane:15", " ", false);

    /**
     * This method returns an {@code ItemStack} of the Minecraft item corresponding to the given Skyblock item ID. The
     * returned {@code ItemStack} will always have a quantity of one. This method works only for Skyblock item IDs stored in the item map.
     *
     * @param skyblockItemId the Skyblock item ID to get an {@code ItemStack} for
     * @return an {@code ItemStack} of the Minecraft item corresponding to {@code skyblockItemId} with a quantity of one
     * or {@link #BISCUIT_HEAD} if {@code skyblockItemId} isn't in the item map
     */
    public ItemStack getItemStack(String skyblockItemId) {
        return items.get(skyblockItemId) == null ? BISCUIT_HEAD.getItemStack() : items.get(skyblockItemId).getItemStack();
    }

    public ItemStack getGlassFiller() {
        return GLASS_FILLER.getItemStack();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, SkyblockItem> entry : items.entrySet()) {
            b.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return b.toString();
    }
}
