package codes.biscuit.skyblockaddons.utils.skyblockdata;

import lombok.Getter;

import java.util.List;

@Getter
public class ContainerItem {

    private int containerSize;
    private String colorTag;
    private boolean standardChest;
    private String compressedTag;
    private List<String> itemTags;


    public boolean isCompressed() {
        return compressedTag != null;
    }


}
