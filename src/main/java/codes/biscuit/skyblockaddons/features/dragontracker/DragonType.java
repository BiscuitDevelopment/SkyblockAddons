package codes.biscuit.skyblockaddons.features.dragontracker;

import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Locale;

@AllArgsConstructor
public enum DragonType {

    PROTECTOR(ColorCode.DARK_BLUE),
    OLD(ColorCode.GRAY),
    WISE(ColorCode.BLUE),
    UNSTABLE(ColorCode.BLACK),
    YOUNG(ColorCode.WHITE),
    STRONG(ColorCode.RED),
    SUPERIOR(ColorCode.GOLD);

    @Getter private ColorCode color;

    public String getDisplayName() {
        return Translations.getMessage("dragonTracker." + this.name().toLowerCase(Locale.US));
    }

    public static DragonType fromName(String name) {
        for (DragonType dragonType : DragonType.values()) {
            if (dragonType.name().equals(name.toUpperCase(Locale.US))) {
                return dragonType;
            }
        }

        return null;
    }
}
