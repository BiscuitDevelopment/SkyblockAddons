package codes.biscuit.skyblockaddons.features.discordrpc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Attribute;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.core.SkyblockDate;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSelect;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.util.function.Supplier;

/**
 * Statuses that are shown on the Discord RPC feature
 *
 * This file has LF line endings because ForgeGradle is weird and will throw a NullPointerException if it's CRLF.
 */
public enum DiscordStatus implements ButtonSelect.SelectItem {

    NONE(Message.DISCORD_STATUS_NONE_TITLE, Message.DISCORD_STATUS_NONE_DESCRIPTION, () -> null),
    LOCATION(Message.DISCORD_STATUS_LOCATION_TITLE, Message.DISCORD_STATUS_LOCATION_DESCRIPTION,
            () -> {
                Location location = SkyblockAddons.getInstance().getUtils().getLocation();

                // Don't display "Your Island."
                if (location == Location.ISLAND) {
                    return "Private Island";
                } else {
                    return location.getScoreboardName();
                }
            }),

    PURSE(Message.DISCORD_STATUS_PURSE_TITLE, Message.DISCORD_STATUS_PURSE_DESCRIPTION,
            () -> {
                double coins = SkyblockAddons.getInstance().getUtils().getPurse();
                String coinString = " Coin";

                if (coins == 1) {
                    return TextUtils.formatDouble(coins) + coinString;
                } else {
                    return TextUtils.formatDouble(coins) + coinString + 's';
                }
            }),

    BITS(Message.DISCORD_STATUS_BITS_TITLE, Message.DISCORD_STATUS_BITS_DESCRIPTION,
            ()-> {
                double bits = SkyblockAddons.getInstance().getUtils().getBits();
                String bitString = " Bit";

                if (bits == 1) {
                    return TextUtils.formatDouble(bits) + bitString;
                } else {
                    return TextUtils.formatDouble(bits) + bitString + 's';
                }
            }),

    STATS(Message.DISCORD_STATUS_STATS_TITLE, Message.DISCORD_STATUS_STATS_DESCRIPTION,
            () -> {
                float health = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.HEALTH).getValue();
                float defense = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.DEFENCE).getValue();
                float mana = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.MANA).getValue();
//                return String.format("%d\u2764 %d\u2748 %d\u270E", health, defense, mana);
                return String.format("%f H - %f D - %f M", health, defense, mana);
            }),

    ZEALOTS(Message.DISCORD_STATUS_ZEALOTS_TITLE, Message.DISCORD_STATUS_ZEALOTS_DESCRIPTION,
            () -> String.format("%d Zealots killed", SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getKills())),

    ITEM(Message.DISCORD_STATUS_ITEM_TITLE, Message.DISCORD_STATUS_ITEM_DESCRIPTION,
            () -> {
                final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                if(player != null && player.getHeldItem() != null) {
                    return String.format("Holding %s", TextUtils.stripColor(player.getHeldItem().getDisplayName()));
                }
                return "No item in hand";
            }),

    TIME(Message.DISCORD_STATUS_TIME_TITLE, Message.DISCORD_STATUS_TIME_DESCRIPTION,
            () -> {
                final SkyblockDate date = SkyblockAddons.getInstance().getUtils().getCurrentDate();
                return date != null ? date.toString() : "";
            }),

    PROFILE(Message.DISCORD_STATUS_PROFILE_TITLE, Message.DISCORD_STATUS_PROFILE_DESCRIPTION,
            () -> {
                String profile = SkyblockAddons.getInstance().getUtils().getProfileName();
                return String.format("Profile: %s", profile == null ? "None" : profile);
            }),

    CUSTOM(Message.DISCORD_STATUS_CUSTOM, Message.DISCORD_STATUS_CUSTOM_DESCRIPTION,
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();

                String text = main.getConfigValues().getCustomStatus(main.getDiscordRPCManager().getCurrentEntry());
                return text.substring(0, Math.min(text.length(), 100));
            }),

    AUTO_STATUS(Message.DISCORD_STATUS_AUTO, Message.DISCORD_STATUS_AUTO_DESCRIPTION, () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                Location location = main.getUtils().getLocation();

                if (location == Location.THE_END || location == Location.DRAGONS_NEST) {
                    return DiscordStatus.ZEALOTS.displayMessageSupplier.get();
                }
                EnumUtils.SlayerQuest slayerQuest = main.getUtils().getSlayerQuest();
                if (slayerQuest != null) {
                    if (slayerQuest == EnumUtils.SlayerQuest.REVENANT_HORROR) return DiscordStatus.valueOf("REVENANT").displayMessageSupplier.get();
                    if (slayerQuest == EnumUtils.SlayerQuest.SVEN_PACKMASTER) return DiscordStatus.valueOf("SVEN").displayMessageSupplier.get();
                    if (slayerQuest == EnumUtils.SlayerQuest.TARANTULA_BROODFATHER) return DiscordStatus.valueOf("TARANTULA").displayMessageSupplier.get();
                }

                if ("AUTO_STATUS".equals(main.getConfigValues().getDiscordAutoDefault().name())) { // Avoid self reference.
                    main.getConfigValues().setDiscordAutoDefault(DiscordStatus.NONE);
                }
                return main.getConfigValues().getDiscordAutoDefault().displayMessageSupplier.get();
            }),

    REVENANT(Message.DISCORD_STATUS_REVENANT, Message.DISCORD_STATUS_REVENANT_DESCRIPTION,
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                boolean bossAlive = main.getUtils().isSlayerBossAlive();

                if (bossAlive) {
                    return "Slaying a Revenant Horror "+main.getUtils().getSlayerQuestLevel()+" boss.";
                } else {
                    return "Doing a Revenant Horror "+main.getUtils().getSlayerQuestLevel()+" quest.";
                }
            }),

    SVEN(Message.DISCORD_STATUS_SVEN, Message.DISCORD_STATUS_SVEN_DESCRIPTION,
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                boolean bossAlive = main.getUtils().isSlayerBossAlive();

                if (bossAlive) {
                    return "Slaying a Sven Packmaster "+main.getUtils().getSlayerQuestLevel()+" boss.";
                } else {
                    return "Doing a Sven Packmaster "+main.getUtils().getSlayerQuestLevel()+" quest.";
                }
            }),

    TARANTULA(Message.DISCORD_STATUS_TARANTULA, Message.DISCORD_STATUS_TARANTULA_DESCRIPTION,
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                boolean bossAlive = main.getUtils().isSlayerBossAlive();

                if (bossAlive) {
                    return "Slaying a Tarantula Broodfather  "+main.getUtils().getSlayerQuestLevel()+" boss.";
                } else {
                    return "Doing a Tarantula Broodfather "+main.getUtils().getSlayerQuestLevel()+" quest.";
                }
            }),
    ;

    private final Message title;
    private final Message description;
    private final Supplier<String> displayMessageSupplier;

    DiscordStatus(Message title, Message description, Supplier<String> displayMessageSupplier) {
        this.title = title;
        this.description = description;
        this.displayMessageSupplier = displayMessageSupplier;
    }

    public String getDisplayString(EnumUtils.DiscordStatusEntry currentEntry) {
        SkyblockAddons.getInstance().getDiscordRPCManager().setCurrentEntry(currentEntry);
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