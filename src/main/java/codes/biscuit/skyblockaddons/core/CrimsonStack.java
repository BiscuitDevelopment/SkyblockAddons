package codes.biscuit.skyblockaddons.core;

import lombok.Getter;
import lombok.Setter;

public enum CrimsonStack {

    CRIMSON("Crimson", "Dominus", "ᝐ"),
    TERROR("Terror", "Hydra Strike", "⁑"),
    AURORA("Aurora", "Arcane Vision", "Ѫ"),
    FERVOR("Fervor", "Fervor", "҉");

    @Getter private final String armorName;
    @Getter private final String stackName;
    @Getter private final String symbol;

    @SuppressWarnings("NonFinalFieldInEnum") //lombok plugin moment
    @Setter @Getter private int currentValue = 0;

    CrimsonStack(String armorName, String stackName, String symbol) {
        this.armorName = armorName;
        this.stackName = stackName;
        this.symbol = symbol;
    }

}
