package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Backpack {

    private static final Pattern BACKPACK_ID_PATTERN = Pattern.compile("([A-Z]+)_BACKPACK");

    @Setter private int x;
    @Setter private int y;
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

    public static Backpack getFromItem(ItemStack stack) {
        if (stack == null) return null;
        SkyblockAddons main = SkyblockAddons.getInstance();
        String id = main.getInventoryUtils().getSkyBlockItemID(stack);
        if (id != null) {
            NBTTagCompound extraAttributes = stack.getTagCompound().getCompoundTag("ExtraAttributes");
            Matcher matcher = BACKPACK_ID_PATTERN.matcher(id);
            boolean matches = matcher.matches();
            if (matches || (main.getConfigValues().isEnabled(Feature.CAKE_BAG_PREVIEW) // If it's a backpack OR it's a cake
                    && "NEW_YEAR_CAKE_BAG".equals(id))) { //                              bag and they have the setting enabled.
                byte[] bytes = null;
                for (String key : extraAttributes.getKeySet()) {
                    if (key.endsWith("backpack_data") || key.equals("new_year_cake_bag_data")) {
                        bytes = extraAttributes.getByteArray(key);
                        break;
                    }
                }
                try {
                    int length = 0;
                    if (matches) {
                        String backpackType = matcher.group(1);
                        switch (backpackType) { // because sometimes the size of the tag is not updated (etc. when you upcraft it)
                            case "SMALL": length = 9; break;
                            case "MEDIUM": length = 18; break;
                            case "LARGE": length = 27; break;
                            case "GREATER": length = 36; break;
                        }
                    }
                    ItemStack[] items = new ItemStack[length];
                    if (bytes != null) {
                        NBTTagCompound nbtTagCompound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
                        NBTTagList list = nbtTagCompound.getTagList("i", Constants.NBT.TAG_COMPOUND);
                        if (list.tagCount() > length) {
                            length = list.tagCount();
                            items = new ItemStack[length];
                        }
                        for (int i = 0; i < length; i++) {
                            NBTTagCompound item = list.getCompoundTagAt(i);
                            // This fixes an issue in Hypixel where enchanted potatoes have the wrong id (potato block instead of item).
                            short itemID = item.getShort("id");
                            if (itemID == 142 && item.hasKey("tag")) {
                                nbtTagCompound = item.getCompoundTag("tag");
                                if (nbtTagCompound.hasKey("ExtraAttributes")) {
                                    id = nbtTagCompound.getCompoundTag("ExtraAttributes").getString("id");
                                    if (id.equals("ENCHANTED_POTATO")) {
                                        item.setShort("id", (short) 392);
                                    }
                                }
                            }
                            ItemStack itemStack = ItemStack.loadItemStackFromNBT(item);
                            items[i] = itemStack;
                        }
                    }
                    BackpackColor color = BackpackColor.WHITE;
                    if (extraAttributes.hasKey("backpack_color")) {
                        try {
                            color = BackpackColor.valueOf(extraAttributes.getString("backpack_color"));
                        } catch (IllegalArgumentException ignored) {}
                    }
                    return new Backpack(items, TextUtils.stripColor(stack.getDisplayName()), color);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
