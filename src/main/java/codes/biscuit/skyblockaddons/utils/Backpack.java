package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Backpack {

    private int x;
    private int y;
    private ItemStack[] items;
    private String backpackName;
    private BackpackColor backpackColor;

    public Backpack(ItemStack[] items, String backpackName, BackpackColor backpackColor) {
        this.items = items;
        this.backpackName = backpackName;
        this.backpackColor = backpackColor;
    }

    public Backpack(ItemStack[] items, String backpackName, BackpackColor backpackColor, int x, int y) {
        this(items,backpackName,backpackColor);
        this.x = x;
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public String getBackpackName() {
        return backpackName;
    }

    public BackpackColor getBackpackColor() {
        return backpackColor;
    }

    public static Backpack getFromItem(ItemStack stack) {
        if (stack == null) return null;
        String id = ItemUtils.getSkyBlockItemID(stack);
        if (!id.isEmpty()) {
            NBTTagCompound extraAttributes = stack.getTagCompound().getCompoundTag("ExtraAttributes");
            if (id.contains("BACKPACK")) {
                byte[] bytes = null;
                for (String key : extraAttributes.getKeySet()) {
                    if (key.endsWith("backpack_data")) {
                        bytes = extraAttributes.getByteArray(key);
                        break;
                    }
                }
                if (bytes == null) return null;
                try {
                    NBTTagCompound nbtTagCompound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
                    NBTTagList list = nbtTagCompound.getTagList("i", Constants.NBT.TAG_COMPOUND);
                    int length = list.tagCount();
                    ItemStack[] items = new ItemStack[length];
                    for (int i = 0; i < length; i++) {
                        NBTTagCompound item = list.getCompoundTagAt(i);
                        // This fixes an issue in Hypixel where enchanted potatoes have the wrong id (potato block instead of item).
                        short itemID = item.getShort("id");
                        if (itemID == 142 && item.hasKey("tag")) {
                            nbtTagCompound = item.getCompoundTag("tag");
                            if (nbtTagCompound.hasKey("ExtraAttributes")) {
                                id = nbtTagCompound.getCompoundTag("ExtraAttributes").getString("id");
                                if (id.equals("ENCHANTED_POTATO")) {
                                    item.setShort("id", (short)392);
                                }
                            }
                        }
                        ItemStack itemStack = ItemStack.loadItemStackFromNBT(item);
                        items[i] = itemStack;
                    }
//                        main.getUtils().setBackpackToRender(new Backpack(x, y, items, main.getUtils().stripColor(stack.getDisplayName())));
                    BackpackColor color = BackpackColor.WHITE;
                    if (extraAttributes.hasKey("backpack_color")) {
                        try {
                            color = BackpackColor.valueOf(extraAttributes.getString("backpack_color"));
                        } catch (IllegalArgumentException ignored) {}
                    }
                    return new Backpack(items, SkyblockAddons.getInstance().getUtils().stripColor(stack.getDisplayName()), color);

//                        main.getUtils().setBackpackColor(color);
//                        main.getPlayerListener().onItemTooltip(new ItemTooltipEvent(stack,
//                                null, null, false));
//                        ci.cancel();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
