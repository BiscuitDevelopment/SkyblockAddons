package codes.biscuit.skyblockaddons.features.slayertracker;

import codes.biscuit.skyblockaddons.core.Translations;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;
import java.util.Locale;

import static codes.biscuit.skyblockaddons.features.slayertracker.SlayerDrop.*;
import static codes.biscuit.skyblockaddons.features.slayertracker.SlayerDrop.SUBZERO_INVERTER;

public enum SlayerBoss {

    REVENANT("Zombie", REVENANT_FLESH, FOUL_FLESH, PESTILENCE_RUNE, UNDEAD_CATALYST, SMITE_SIX, BEHEADED_HORROR,
            REVENANT_CATALYST, SNAKE_RUNE, SCYTHE_BLADE, SMITE_SEVEN, SHARD_OF_SHREDDED, WARDEN_HEART),

    TARANTULA("Spider", TARANTULA_WEB, TOXIC_ARROW_POISON, SPIDER_CATALYST, BANE_OF_ARTHROPODS_SIX, BITE_RUNE,
            FLY_SWATTER, TARANTULA_TALISMAN, DIGESTED_MOSQUITO),

    SVEN("Wolf", WOLF_TOOTH, HAMSTER_WHEEL, SPIRIT_RUNE, CRITICAL_SIX, FURBALL, RED_CLAW_EGG, COUTURE_RUNE, OVERFLUX_CAPACITOR,
            GRIZZLY_BAIT),

    VOIDGLOOM("Enderman", NULL_SPHERE, TWILIGHT_ARROW_POISON, ENDERSNAKE_RUNE, SUMMONING_EYE, MANA_STEAL_ONE,
            TRANSMISSION_TUNER, NULL_ATOM, HAZMAT_ENDERMAN, POCKET_ESPRESSO_MACHINE, SMARTY_PANTS_ONE, END_RUNE,
            HANDY_BLOOD_CHALICE, SINFUL_DICE, EXCEEDINGLY_RARE_ENDER_ARTIFACT_UPGRADER, VOID_CONQUEROR_ENDERMAN_SKIN,
            ETHERWARP_MERGER, JUDGEMENT_CORE, ENCHANT_RUNE, ENDER_SLAYER_SEVEN),

    INFERNO("Blaze", DERELICT_ASHE, LAVATEARS_RUNE, WISP_ICE_FLAVORED_WATER, BUNDLE_OF_MAGMA, MANA_DISINTEGRATOR,
            SCORCHED_BOOKS, KELVIN_INVERTER, BLAZE_ROD_DISTILLATE, GLOWSTONE_DISTILLATE, MAGMA_CREAM_DISTILLATE, NETHER_WART_DISTILLATE,
            GABAGOOL_DISTILLATE, SCORCHED_POWER_CRYSTAL, ARCHFIEND_DICE, FIRE_ASPECT, FIERY_BURST_RUNE, FLAWED_OPAL_GEMSTONE,
            DUPLEX, HIGH_CLASS_ARCHFIEND_DICE, WILSON_ENGINEERING_PLANS, SUBZERO_INVERTER);

    @Getter
    private final List<SlayerDrop> drops;
    @Getter
    private final String mobType;

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
