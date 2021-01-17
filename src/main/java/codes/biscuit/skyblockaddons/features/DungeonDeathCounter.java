package codes.biscuit.skyblockaddons.features;

import lombok.Getter;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;

/**
 * This class keeps track of the number of times players have died during a dungeon run.
 */
public class DungeonDeathCounter {
    /** This is the skull that is displayed as an icon beside the number of deaths. */
    public static final ItemStack SKULL_ITEM;

    static {
        try {
            SKULL_ITEM = new ItemStack(Items.skull, 1, 3);
            SKULL_ITEM.setTagCompound(JsonToNBT.getTagFromJson("{display:{Name:\"Skull\"},SkullOwner:{Id:\"c659cdd4-e436-4977-a6a7-d5518ebecfbb\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19\"}]}}}"));
        } catch (NBTException e) {
            throw new RuntimeException("Couldn't read death counter skull NBT", e);
        }
    }

    @Getter private int deaths;

    /**
     * Creates a new instance of {@code DungeonDeathCounter} with the number of deaths set to zero
     */
    public DungeonDeathCounter() {
        deaths = 0;
    }

    /**
     * Adds one death to the counter
     */
    public void increment() {
        deaths++;
    }

    /**
     * Resets the number of deaths to zero
     */
    public void reset() {
        deaths = 0;
    }
}