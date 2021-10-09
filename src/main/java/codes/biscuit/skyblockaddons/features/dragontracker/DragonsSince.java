package codes.biscuit.skyblockaddons.features.dragontracker;

import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.core.Translations;
import com.google.common.base.CaseFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DragonsSince {

    SUPERIOR(ItemRarity.LEGENDARY),
    ASPECT_OF_THE_DRAGONS(ItemRarity.LEGENDARY),
    ENDER_DRAGON_PET(ItemRarity.LEGENDARY);

    @Getter private ItemRarity itemRarity;

    public String getDisplayName() {
        return Translations.getMessage("dragonTracker." +  CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name()));
    }
}
