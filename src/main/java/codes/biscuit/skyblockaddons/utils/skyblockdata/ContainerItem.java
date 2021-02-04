package codes.biscuit.skyblockaddons.utils.skyblockdata;

import lombok.Getter;

import java.util.List;

public class ContainerItem {

    private enum ContainerType {
        BACKPACK(0),
        NEW_YEARS_CAKE(1),
        PERSONAL_COMPACTOR(2);

        @Getter int type;
        ContainerType(int theType) {
            type = theType;
        }
    }



    /**
     * The container type (see {@link ContainerType}).
     */
    private int type;
    /**
     * The size of the container
     */
    private int size;
    /**
     * The important ExtraAttributes NBT tags for retrieving data
     */
    private List<String> dataTags;
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
        return type == ContainerType.BACKPACK.getType();
    }

    public boolean isCakeBag() {
        return type == ContainerType.NEW_YEARS_CAKE.getType();
    }

    public boolean isPersonalCompactor() {
        return type == ContainerType.PERSONAL_COMPACTOR.getType();
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

    /* Functions that check the important ExtraAttributes NBT tags */


    public boolean hasDataTags() {
        return hasDataTags(0);
    }



    private boolean hasDataTags(int num) {
        return dataTags != null && dataTags.size() >= num;
    }

    public String getCompressedDataTag() {
        if ((isCakeBag() || isBackpack()) && hasDataTags(0)) {
            return dataTags.get(0);
        }
        return null;
    }

    public List<String> getDataTags() {
        return isPersonalCompactor() ? dataTags : null;
    }




}
