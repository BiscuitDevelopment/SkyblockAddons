package codes.biscuit.skyblockaddons.utils.skyblockdata;

import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;

@Getter
public class Rune {

    private String type;
    private int level;

    public Rune(NBTTagCompound runeData) {

        // There should only be 1 rune type
        for (String runeType : runeData.getKeySet()) {
            type = runeType;
            level = runeData.getInteger(runeType);
        }
    }

}
