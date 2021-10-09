package codes.biscuit.skyblockaddons.utils.skyblockdata;

import lombok.Getter;

import java.util.List;

public class ContainerData {

    private enum ContainerType {
        BACKPACK,
        NEW_YEARS_CAKE,
        PERSONAL_COMPACTOR,
        BUILDERS_WAND;
    }

    /**
     * The container type (see {@link ContainerType}).
     */
    private ContainerType type;

    /**
     * The size of the container
     */
    private int size;

    /**
     * The data tag where a compressed array of item stacks are stored.
     */
    private String compressedItemStacksTag;

    /**
     * Data tags where individual item stacks are stored.
     */
    private List<String> itemStackDataTags;

    /**
     * The ExtraAttributes NBT tag for retrieving backpack color
     */
    @Getter private String colorTag;

    /**
     * The container (item array) dimensions
     */
    private int[] dimensions = {6, 9};


    /* Functions that check the container type */

    public boolean isBackpack() {
        return type == ContainerType.BACKPACK;
    }

    public boolean isCakeBag() {
        return type == ContainerType.NEW_YEARS_CAKE;
    }

    public boolean isPersonalCompactor() {
        return type == ContainerType.PERSONAL_COMPACTOR;
    }

    public boolean isBuildersWand() {
        return type == ContainerType.BUILDERS_WAND;
    }

    /* Functions that check the size of the container */

    /**
     * @return the item capacity of the container, or a maximum of 54
     */
    public int getSize() {
        return Math.min(size, 54);
    }

    /**
     * @return the number of rows in the container, or a maximum of 6
     */
    public int getNumRows() {
        return dimensions.length == 2 ? Math.min(dimensions[0], 6) : 6;
    }

    /**
     * @return the number of columns in the container, or a maximum of 9
     */
    public int getNumCols() {
        return dimensions.length == 2 ? Math.min(dimensions[1], 9) : 9;
    }

    public String getCompressedDataTag() {
        return compressedItemStacksTag;
    }

    public List<String> getItemStackDataTags() {
        return itemStackDataTags;
    }




}
