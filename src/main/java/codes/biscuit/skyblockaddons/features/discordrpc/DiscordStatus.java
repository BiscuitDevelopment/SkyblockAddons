package codes.biscuit.skyblockaddons.features.discordrpc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.*;
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

    NONE("discordStatus.titleNone", "discordStatus.descriptionNone", () -> null),
    LOCATION("discordStatus.titleLocation", "discordStatus.descriptionLocation",
            () -> {
                Location location = SkyblockAddons.getInstance().getUtils().getLocation();

                // Don't display "Your Island."
                if (location == Location.ISLAND) {
                    return "Private Island";
                } else {
                    return location.getScoreboardName();
                }
            }),

    PURSE("discordStatus.titlePurse", "discordStatus.descriptionPurse",
            () -> {
                double coins = SkyblockAddons.getInstance().getUtils().getPurse();
                String coinString = " Coin";

                if (coins == 1) {
                    return TextUtils.formatDouble(coins) + coinString;
                } else {
                    return TextUtils.formatDouble(coins) + coinString + 's';
                }
            }),

    BITS("discordStatus.titleBits", "discordStatus.descriptionBits",
            ()-> {
                double bits = SkyblockAddons.getInstance().getUtils().getBits();
                String bitString = " Bit";

                if (bits == 1) {
                    return TextUtils.formatDouble(bits) + bitString;
                } else {
                    return TextUtils.formatDouble(bits) + bitString + 's';
                }
            }),

    STATS("discordStatus.titleStats", "discordStatus.descriptionStats",
            () -> {
                float health = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.HEALTH).getValue();
                float defense = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.DEFENCE).getValue();
                float mana = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.MANA).getValue();
//                return String.format("%d\u2764 %d\u2748 %d\u270E", health, defense, mana);
                return String.format("%.2f H - %.2f D - %.2f M", health, defense, mana);
            }),

    ZEALOTS("discordStatus.titleZealots", "discordStatus.descriptionZealots",
            () -> String.format("%d Zealots killed", SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getKills())),

    ITEM("discordStatus.titleItem", "discordStatus.descriptionItem",
            () -> {
                final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                if(player != null && player.getHeldItem() != null) {
                    return String.format("Holding %s", TextUtils.stripColor(player.getHeldItem().getDisplayName()));
                }
                return "No item in hand";
            }),

    TIME("discordStatus.titleTime", "discordStatus.descriptionTime",
            () -> {
                final SkyblockDate date = SkyblockAddons.getInstance().getUtils().getCurrentDate();
                return date != null ? date.toString() : "";
            }),

    PROFILE("discordStatus.titleProfile", "discordStatus.descriptionProfile",
            () -> {
                String profile = SkyblockAddons.getInstance().getUtils().getProfileName();
                return String.format("Profile: %s", profile == null ? "None" : profile);
            }),

    CUSTOM("discordStatus.titleCustom", "discordStatus.descriptionCustom",
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();

                String text = main.getConfigValues().getCustomStatus(main.getDiscordRPCManager().getCurrentEntry());
                return text.substring(0, Math.min(text.length(), 100));
            }),

    AUTO_STATUS("discordStatus.titleAuto", "discordStatus.descriptionAuto", () -> {
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

    REVENANT("discordStatus.titleRevenants", "discordStatus.descriptionRevenants",
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                boolean bossAlive = main.getUtils().isSlayerBossAlive();

                if (bossAlive) {
                    return "Slaying a Revenant Horror "+main.getUtils().getSlayerQuestLevel()+" boss.";
                } else {
                    return "Doing a Revenant Horror "+main.getUtils().getSlayerQuestLevel()+" quest.";
                }
            }),

    SVEN("discordStatus.titleSvens", "discordStatus.descriptionSvens",
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                boolean bossAlive = main.getUtils().isSlayerBossAlive();

                if (bossAlive) {
                    return "Slaying a Sven Packmaster "+main.getUtils().getSlayerQuestLevel()+" boss.";
                } else {
                    return "Doing a Sven Packmaster "+main.getUtils().getSlayerQuestLevel()+" quest.";
                }
            }),

    TARANTULA("discordStatus.titleTarantula", "discordStatus.descriptionTarantula",
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                boolean bossAlive = main.getUtils().isSlayerBossAlive();

                if (bossAlive) {
                    return "Slaying a Tarantula Broodfather  "+main.getUtils().getSlayerQuestLevel()+" boss.";
                } else {
                    return "Doing a Tarantula Broodfather "+main.getUtils().getSlayerQuestLevel()+" quest.";
                }
            }),

    VOIDGLOOM("discordStatus.titleVoidgloom", "discordStatus.descriptionVoidgloom",
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                boolean bossAlive = main.getUtils().isSlayerBossAlive();

                if (bossAlive) {
                    return "Slaying a Voidgloom Seraph  "+main.getUtils().getSlayerQuestLevel()+" boss.";
                } else {
                    return "Doing a Voidgloom Seraph "+main.getUtils().getSlayerQuestLevel()+" quest.";
                }
            }),

    INFERNO("discordStatus.titleInferno", "discordStatus.descriptionInferno",
            () -> {
                SkyblockAddons main = SkyblockAddons.getInstance();
                boolean bossAlive = main.getUtils().isSlayerBossAlive();

                if (bossAlive) {
                    return "Slaying a Inferno Demonlord  "+main.getUtils().getSlayerQuestLevel()+" boss.";
                } else {
                    return "Doing a Inferno Demonlord "+main.getUtils().getSlayerQuestLevel()+" quest.";
                }
            }),
    ;

    private final String title;
    private final String description;
    private final Supplier<String> displayMessageSupplier;

    DiscordStatus(String titleTranslationKey, String descriptionTranslationKey, Supplier<String> displayMessageSupplier) {
        this.title = Translations.getMessage(titleTranslationKey);
        this.description = Translations.getMessage(descriptionTranslationKey);
        this.displayMessageSupplier = displayMessageSupplier;
    }

    public String getDisplayString(EnumUtils.DiscordStatusEntry currentEntry) {
        SkyblockAddons.getInstance().getDiscordRPCManager().setCurrentEntry(currentEntry);
        return displayMessageSupplier.get();
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }
}