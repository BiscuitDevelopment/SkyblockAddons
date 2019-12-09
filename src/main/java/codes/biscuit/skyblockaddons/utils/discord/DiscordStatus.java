package codes.biscuit.skyblockaddons.utils.discord;

import codes.biscuit.skyblockaddons.SkyblockAddons;

import java.util.function.Supplier;

public enum DiscordStatus {

    NONE(() -> ""),
    LOCATION(() -> SkyblockAddons.getInstance().getUtils().getLocation().getScoreboardName()),
    PURSE(() -> "Purse"),
    STATS(() -> "Stats"),
    ZEALOTS(() -> "Zealots"),
    ITEM(() -> "Item"),
    TIME(() -> SkyblockAddons.getInstance().getUtils().getCurrentDate().toString()),
    PROFILE(() -> "Profile"),

    // Generic status texts
    IN_GAME(() -> "In Game"),
    MINING(() -> "Mining"),
    GRINDING(() -> "Grinding");

    // Message title
    // Message description (both for GUI display)
    private final Supplier<String> displayMessageSupplier;

    DiscordStatus(Supplier<String> displayMessageSupplier) {
        this.displayMessageSupplier = displayMessageSupplier;
    }

    public String getDisplayMessage() {
        return displayMessageSupplier.get();
    }
}
