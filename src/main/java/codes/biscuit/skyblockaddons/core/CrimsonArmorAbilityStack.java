package codes.biscuit.skyblockaddons.core;

import lombok.Getter;
import lombok.Setter;

public enum CrimsonArmorAbilityStack {

    CRIMSON("Crimson", "Dominus", "ᝐ"),
    TERROR("Terror", "Hydra Strike", "⁑"),
    AURORA("Aurora", "Arcane Vision", "Ѫ"),
    FERVOR("Fervor", "Fervor", "҉");

    @Getter private final String armorName;
    @Getter private final String abilityName;
    @Getter private final String symbol;

    @SuppressWarnings("NonFinalFieldInEnum") //lombok plugin moment
    @Setter @Getter private int currentValue = 0;

    CrimsonArmorAbilityStack(String armorName, String abilityName, String symbol) {
        this.armorName = armorName;
        this.abilityName = abilityName;
        this.symbol = symbol;
    }

}
