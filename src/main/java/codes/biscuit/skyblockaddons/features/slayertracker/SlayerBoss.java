package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.Translations;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;
import java.util.Locale;

public enum SlayerBoss {

    REVENANT("Zombie", SlayerDrop.REVENANT_FLESH, SlayerDrop.FOUL_FLESH, SlayerDrop.PESTILENCE_RUNE, SlayerDrop.UNDEAD_CATALYST, SlayerDrop.SMITE_SIX,
            SlayerDrop.BEHEADED_HORROR, SlayerDrop.REVENANT_CATALYST, SlayerDrop.SNAKE_RUNE, SlayerDrop.SCYTHE_BLADE),

    TARANTULA("Spider", SlayerDrop.TARANTULA_WEB, SlayerDrop.TOXIC_ARROW_POISON, SlayerDrop.SPIDER_CATALYST, SlayerDrop.BANE_OF_ARTHROPODS_SIX,
            SlayerDrop.BITE_RUNE, SlayerDrop.FLY_SWATTER, SlayerDrop.TARANTULA_TALISMAN, SlayerDrop.DIGESTED_MOSQUITO),

    SVEN("Wolf", SlayerDrop.WOLF_TOOTH, SlayerDrop.HAMSTER_WHEEL, SlayerDrop.SPIRIT_RUNE, SlayerDrop.CRITICAL_SIX, SlayerDrop.GRIZZLY_BAIT,
            SlayerDrop.RED_CLAW_EGG, SlayerDrop.OVERFLUX_CAPACITOR, SlayerDrop.COUTURE_RUNE);

    @Getter private List<SlayerDrop> drops;
    @Getter private String mobType;

    SlayerBoss(String mobType, SlayerDrop... drops) {
        this.mobType = mobType;
        this.drops = Lists.newArrayList(drops);
    }

    public static SlayerBoss getFromMobType(String mobType) {
        for (SlayerBoss slayerBoss : SlayerBoss.values()) {
            if (slayerBoss.mobType.equalsIgnoreCase(mobType)) {
                return slayerBoss;
            }
        }

        return null;
    }

    public String getDisplayName() {
        return Translations.getMessage("slayerTracker." + this.name().toLowerCase(Locale.US));
    }
}
