package codes.biscuit.skyblockaddons.utils.skyblockdata;


import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.gson.GsonInitializable;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

import java.lang.reflect.Field;

/**
 * For storing any skyblock item. Very much a work in progress, with other things (like pets perhaps) on the way
 * Another potential addition is prevent placing enchanted items (blacklist + whitelist), item cooldown amounts, etc.
 */
public class CompactorItem implements GsonInitializable {

    private String itemId;
    private String displayName;
    private boolean enchanted;
    private String skullId;
    private String texture;

    @Getter private transient ItemStack itemStack;

    /** Set by reflection, so ignore null error with itemid */
    public CompactorItem() {
    }

    /**
     * Generic constructor
     */
    public CompactorItem(String theItemId, String theDisplayName, boolean isEnchanted, String theSkullId, String theTexture) {
        itemId = theItemId;
        displayName = theDisplayName;
        enchanted = isEnchanted;
        skullId = theSkullId;
        texture = theTexture;
        makeItemStack();
    }

    /**
     * Made for a regular item instead of a skull
     */
    public CompactorItem(String theItemId, String theDisplayName, boolean isEnchanted) {
        this(theItemId, theDisplayName, isEnchanted, null, null);
    }

    @Override
    public void gsonInit() {
        makeItemStack();
    }

    @SneakyThrows
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Field f : CompactorItem.class.getDeclaredFields()) {
            builder.append(f.getName()).append(": ").append(f.get(this)).append(", ");
        }
        builder.append("}");
        return builder.toString();
    }


    private void makeItemStack() {
        try {
            if (itemId != null) {
                if (itemId.equals("skull")) {
                    itemStack = ItemUtils.createSkullItemStack(displayName, "", skullId, texture);
                } else {
                    String[] minecraftIdArray = itemId.split(":", 2);
                    int meta = minecraftIdArray.length == 2 ? Integer.parseInt(minecraftIdArray[1]) : 0;
                    Item item = Item.getByNameOrId(minecraftIdArray[0]);

                    if (item != null) {
                        itemStack = minecraftIdArray.length == 1 ? new ItemStack(item) : new ItemStack(item, 1, meta);
                        if (enchanted) {
                            itemStack.setTagInfo("ench", new NBTTagList());
                        }
                    }
                }
                if (itemStack != null) {
                    itemStack.setStackDisplayName(displayName);
                }
            }
        } catch (Exception ex) {
            itemStack = ItemUtils.createItemStack(Item.getItemFromBlock(Blocks.stone), displayName != null ? displayName : "", itemId != null ? itemId : "", false);
            SkyblockAddons.getLogger().error("An error occurred while making an itemname with ID " + itemId + " and name " + displayName + ".", ex);
        }
    }
}
