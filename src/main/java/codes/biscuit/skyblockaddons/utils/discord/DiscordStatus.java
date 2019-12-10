package codes.biscuit.skyblockaddons.utils.discord;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSelect;

import java.util.function.Supplier;

public enum DiscordStatus implements ButtonSelect.SelectItem {

    NONE(() -> ""),
    LOCATION(() -> SkyblockAddons.getInstance().getUtils().getLocation().getScoreboardName()),
    PURSE(() -> String.format("%s Coins", Double.toString(SkyblockAddons.getInstance().getUtils().getPurse()))),
    STATS(() -> "Stats"),
    ZEALOTS(() -> String.format("%d Zealots killed", SkyblockAddons.getInstance().getPersistentValues().getKills())),
    ITEM(() -> "Item"),
    TIME(() -> SkyblockAddons.getInstance().getUtils().getCurrentDate().toString()),
    PROFILE(() -> {
        String profile = SkyblockAddons.getInstance().getUtils().getProfileName();
        return profile == null ? "None" : profile;
    }),

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

    @Override
    public String getName() {
        return getDisplayMessage();
    }

    @Override
    public String getDescription() {
        return null;
    }
}
