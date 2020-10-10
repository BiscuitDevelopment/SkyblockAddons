package codes.biscuit.skyblockaddons.features.dragontracker;

import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import com.google.common.base.CaseFormat;
import lombok.Getter;

public enum DragonsSince {

    SUPERIOR(ItemRarity.LEGENDARY),
    ASPECT_OF_THE_DRAGONS(ItemRarity.LEGENDARY),
    ENDER_DRAGON_PET(ItemRarity.LEGENDARY);

    @Getter private ItemRarity itemRarity;

    DragonsSince(ItemRarity itemRarity) {
        this.itemRarity = itemRarity;
    }

    public String getDisplayName() {
        return Translations.getMessage("dragonTracker." +  CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                this.name()));
    }
}
