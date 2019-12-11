package codes.biscuit.skyblockaddons.utils.discord;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSelect;
import codes.biscuit.skyblockaddons.utils.Attribute;
import codes.biscuit.skyblockaddons.utils.Message;
import codes.biscuit.skyblockaddons.utils.TextUtils;

import java.util.function.Supplier;

public enum DiscordStatus implements ButtonSelect.SelectItem {

    NONE(Message.DISCORD_STATUS_NONE_TITLE, Message.DISCORD_STATUS_NONE_DESCRIPTION, () -> ""),
    LOCATION(Message.DISCORD_STATUS_LOCATION_TITLE, Message.DISCORD_STATUS_LOCATION_DESCRIPTION, () -> SkyblockAddons.getInstance().getUtils().getLocation().getScoreboardName()),
    PURSE(Message.DISCORD_STATUS_PURSE_TITLE, Message.DISCORD_STATUS_PURSE_DESCRIPTION, () -> String.format("%s Coins", TextUtils.formatDouble(SkyblockAddons.getInstance().getUtils().getPurse()))),
    STATS(Message.DISCORD_STATUS_STATS_TITLE, Message.DISCORD_STATUS_STATS_DESCRIPTION, () -> {
        int health = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.HEALTH).getValue();
        int defense = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.DEFENCE).getValue();
        int mana = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.MANA).getValue();
        return String.format("%d❤ %d❈ %d✎", health, defense, mana);
    }),
    ZEALOTS(Message.DISCORD_STATUS_ZEALOTS_TITLE, Message.DISCORD_STATUS_ZEALOTS_DESCRIPTION, () -> String.format("%d Zealots killed", SkyblockAddons.getInstance().getPersistentValues().getKills())),
    ITEM(Message.DISCORD_STATUS_ITEM_TITLE, Message.DISCORD_STATUS_ITEM_DESCRIPTION, () -> "Item"),
    TIME(Message.DISCORD_STATUS_TIME_TITLE, Message.DISCORD_STATUS_TIME_DESCRIPTION, () -> SkyblockAddons.getInstance().getUtils().getCurrentDate().toString()),
    PROFILE(Message.DISCORD_STATUS_PROFILE_TITLE, Message.DISCORD_STATUS_PROFILE_DESCRIPTION, () -> {
        String profile = SkyblockAddons.getInstance().getUtils().getProfileName();
        return String.format("Profile: %s", profile == null ? "None" : profile);
    }),

    // Generic status texts
    IN_GAME(Message.DISCORD_STATUS_IN_GAME_TITLE, Message.DISCORD_STATUS_IN_GAME_DESCRIPTION, () -> "In Game"),
    MINING(Message.DISCORD_STATUS_MINING_TITLE, Message.DISCORD_STATUS_MINING_DESCRIPTION, () -> "Mining"),
    GRINDING(Message.DISCORD_STATUS_GRINDING_TITLE, Message.DISCORD_STATUS_GRINDING_DESCRIPTION, () -> "Grinding");

    private final Message title;
    private final Message description;
    private final Supplier<String> displayMessageSupplier;

    DiscordStatus(Message title, Message description, Supplier<String> displayMessageSupplier) {
        this.title = title;
        this.description = description;
        this.displayMessageSupplier = displayMessageSupplier;
    }

    public String getDisplayString() {
        return displayMessageSupplier.get();
    }

    @Override
    public String getName() {
        return title.getMessage();
    }

    @Override
    public String getDescription() {
        return description.getMessage();
    }
}
