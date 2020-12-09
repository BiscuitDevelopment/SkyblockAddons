package codes.biscuit.skyblockaddons.core;

import lombok.Getter;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Locale;

public enum EssenceType {

    WITHER,
    SPIDER,
    UNDEAD,
    DRAGON,
    GOLD,
    DIAMOND,
    ICE;

    private String niceName;
    @Getter private ResourceLocation resourceLocation;

    EssenceType() {
        niceName = WordUtils.capitalizeFully(this.name());
        resourceLocation = new ResourceLocation("skyblockaddons", "essences/" + this.name().toLowerCase(Locale.US) + ".png");
    }

    public static EssenceType fromName(String name) {
        for (EssenceType essenceType : EssenceType.values()) {
            if (essenceType.niceName.equals(name)) {
                return essenceType;
            }
        }

        return null;
    }
}
