package codes.biscuit.skyblockaddons.utils;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/*
Armorstands have a lot of associated data that can be difficult to parse
This class is intended to centralize/standardize getting armorstand data
 */
public class ArmorStandUtils {


    /*
    Gets the skull ID from the head component of an armorstand
    Returns null on failure
     */
    public static String tryToGetSkullIdFromArmorstand(EntityArmorStand e) {
        NBTBase nbt = new NBTTagCompound();
        String s = "";
        e.writeEntityToNBT((NBTTagCompound)nbt);
        if (((NBTTagCompound)nbt).hasKey("Equipment")) {
            nbt = ((NBTTagCompound)nbt).getTag("Equipment");
            if (nbt.getId() == Constants.NBT.TAG_LIST && ((NBTTagList)nbt).tagCount() == 5) {
                nbt = ((NBTTagList)nbt).get(4);
                if (nbt.getId() == Constants.NBT.TAG_COMPOUND && ((NBTTagCompound)nbt).hasKey("tag")) {
                    nbt = ((NBTTagCompound)nbt).getTag("tag");
                    if (nbt.getId() == Constants.NBT.TAG_COMPOUND && ((NBTTagCompound)nbt).hasKey("SkullOwner")) {
                        nbt = ((NBTTagCompound)nbt).getTag("SkullOwner");
                        if (nbt.getId() == Constants.NBT.TAG_COMPOUND && ((NBTTagCompound)nbt).hasKey("Id")) {
                            s = ((NBTTagCompound)nbt).getString("Id");
                        }
                    }
                }
            }
        }
        return s.length() == 0 ? null : s;
    }
}
