package codes.biscuit.skyblockaddons.utils.skyblockdata;


import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.gson.GsonInitializable;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

import java.lang.reflect.Field;

/**
 * For storing any skyblock item. Very much a work in progress, with other things (like pets perhaps) on the way
 * Another potential addition is prevent placing enchanted items (blacklist + whitelist), item cooldown amounts, etc.
 */
public class SkyblockItem implements GsonInitializable {
    private String itemid;
    private String displayname;
    private boolean ench;
    private String skullid;
    private String texture;

    @Getter private transient ItemStack itemStack;

    /** Set by reflection, so ignore null error with itemid */
    public SkyblockItem() {
    }

    /**
     * Generic constructor that should process
     * @param theItemId
     * @param theDisplayName
     * @param isEnchanted
     * @param theSkullId
     * @param theTexture
     */
    public SkyblockItem(String theItemId, String theDisplayName, boolean isEnchanted, String theSkullId, String theTexture) {
        itemid = theItemId;
        displayname = theDisplayName;
        ench = isEnchanted;
        skullid = theSkullId;
        texture = theTexture;
        // Create the itemstack here
        makeItemStack();
    }

    /**
     * Made for a regular item instead of a skull
     * @param theItemId
     * @param theDisplayName
     * @param isEnchanted
     */
    public SkyblockItem(String theItemId, String theDisplayName, boolean isEnchanted) {
        this(theItemId, theDisplayName, isEnchanted, null, null);
    }

    @SneakyThrows
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Field f : SkyblockItem.class.getDeclaredFields()) {
            builder.append(f.getName()).append(": ").append(f.get(this)).append(", ");
        }
        builder.append("}");
        return builder.toString();
    }

    /*public String toString() {
        return itemStack == null ? "(null)" : itemStack.toString();
    }*/

    @Override
    public void gsonInit() {
        // TODO: Throw malformed json on error
        makeItemStack();
    }


    private void makeItemStack() {
        if (itemid != null) {
            if (itemid.equals("skull")) {
                itemStack = ItemUtils.createSkullItemStack(displayname, "", skullid, texture);
            }
            else {
                String[] minecraftIdArray = itemid.split(":", 2);
                int meta = minecraftIdArray.length == 2 ? Integer.parseInt(minecraftIdArray[1]) : 0;
                Item item = Item.getByNameOrId(minecraftIdArray[0]);

                if (item != null) {
                    itemStack = minecraftIdArray.length == 1 ? new ItemStack(item) : new ItemStack(item, 1, meta);
                    if (ench) {
                        itemStack.setTagInfo("ench", new NBTTagList());
                    }
                }
            }
            if (itemStack != null) {
                itemStack.setStackDisplayName(displayname);
            }
        }
    }
}
