package codes.biscuit.skyblockaddons.utils.discord;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSelect;
import codes.biscuit.skyblockaddons.utils.Attribute;
import codes.biscuit.skyblockaddons.utils.Message;
import codes.biscuit.skyblockaddons.utils.SkyblockDate;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import java.util.function.Supplier;

public enum DiscordStatus implements ButtonSelect.SelectItem {


    NONE(Message.DISCORD_STATUS_NONE_TITLE, Message.DISCORD_STATUS_NONE_DESCRIPTION, () -> null),
    LOCATION(Message.DISCORD_STATUS_LOCATION_TITLE, Message.DISCORD_STATUS_LOCATION_DESCRIPTION,
            () -> SkyblockAddons.getInstance().getUtils().getLocation().getScoreboardName()),

    PURSE(Message.DISCORD_STATUS_PURSE_TITLE, Message.DISCORD_STATUS_PURSE_DESCRIPTION,
            () -> String.format("%s Coins", TextUtils.formatDouble(SkyblockAddons.getInstance().getUtils().getPurse()))),

    STATS(Message.DISCORD_STATUS_STATS_TITLE, Message.DISCORD_STATUS_STATS_DESCRIPTION,
            () -> {
                int health = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.HEALTH).getValue();
                int defense = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.DEFENCE).getValue();
                int mana = SkyblockAddons.getInstance().getUtils().getAttributes().get(Attribute.MANA).getValue();
//                return String.format("%d\u2764 %d\u2748 %d\u270E", health, defense, mana);
                return String.format("%d H - %d D - %d M", health, defense, mana);
            }),

    ZEALOTS(Message.DISCORD_STATUS_ZEALOTS_TITLE, Message.DISCORD_STATUS_ZEALOTS_DESCRIPTION,
            () -> String.format("%d Zealots killed", SkyblockAddons.getInstance().getPersistentValues().getKills())),

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

    // Generic status texts
    GENERIC_IN_GAME(Message.DISCORD_STATUS_GENERIC_IN_GAME_TITLE, Message.DISCORD_STATUS_GENERIC_IN_GAME_DESCRIPTION, () -> "In Game"),
    GENERIC_GRINDING(Message.DISCORD_STATUS_GENERIC_GRINDING_TITLE, Message.DISCORD_STATUS_GENERIC_GRINDING_DESCRIPTION, () -> "Grinding"),
    GENERIC_MINING(Message.DISCORD_STATUS_GENERIC_MINING_TITLE, Message.DISCORD_STATUS_GENERIC_MINING_DESCRIPTION, () -> "Mining"),
    GENERIC_FARMING(Message.DISCORD_STATUS_GENERIC_FARMING_TITLE, Message.DISCORD_STATUS_GENERIC_FARMING_DESCRIPTION, () -> "Farming"),
    GENERIC_FISHING(Message.DISCORD_STATUS_GENERIC_FISHING_TITLE, Message.DISCORD_STATUS_GENERIC_FISHING_DESCRIPTION, () -> "Fishing"),
    GENERIC_BREWING(Message.DISCORD_STATUS_GENERIC_BREWING_TITLE, Message.DISCORD_STATUS_GENERIC_BREWING_DESCRIPTION, () -> "Brewing"),
    GENERIC_COMBAT(Message.DISCORD_STATUS_GENERIC_COMBAT_TITLE, Message.DISCORD_STATUS_GENERIC_COMBAT_DESCRIPTION, () -> "Killing Mobs"),
    GENERIC_ENCHANTING(Message.DISCORD_STATUS_GENERIC_ENCHANTING_TITLE, Message.DISCORD_STATUS_GENERIC_ENCHANTING_DESCRIPTION, () -> "Enchanting"),
    GENERIC_CHOPPING_WOOD(Message.DISCORD_STATUS_GENERIC_FORAGING_TITLE, Message.DISCORD_STATUS_GENERIC_FORAGING_DESCRIPTION, () -> "Chopping Wood"),
    GENERIC_CRAFTING_RUNES(Message.DISCORD_STATUS_GENERIC_RUNECRAFTING_TITLE, Message.DISCORD_STATUS_GENERIC_RUNECRAFTING_DESCRIPTION, () -> "Crafting Runes"),
    GENERIC_WOLF_SLAYER(Message.DISCORD_STATUS_GENERIC_WOLF_SLAYER_TITLE, Message.DISCORD_STATUS_GENERIC_WOLF_SLAYER_DESCRIPTION, () -> "Petting Svens"),
    GENERIC_SPIDER_SLAYER(Message.DISCORD_STATUS_GENERIC_SPIDER_SLAYER_TITLE, Message.DISCORD_STATUS_GENERIC_SPIDER_SLAYER_DESCRIPTION, () -> "Trampling on Tarantulas"),
    GENERIC_ZOMBIE_SLAYER(Message.DISCORD_STATUS_GENERIC_ZOMBIE_SLAYER_TITLE, Message.DISCORD_STATUS_GENERIC_ZOMBIE_SLAYER_DESCRIPTION, () -> "Resurrecting Revenants"),
    GENERIC_GRINDING_ZEALOTS(Message.DISCORD_STATUS_GENERIC_GRINDING_ZEALOTS_TITLE, Message.DISCORD_STATUS_GENERIC_GRINDING_ZEALOTS_DESCRIPTION, () -> "Grinding Zealots"),
    GENERIC_AFK(Message.DISCORD_STATUS_GENERIC_AFK_TITLE, Message.DISCORD_STATUS_GENERIC_AFK_DESCRIPTION, () -> "AFK"),
    GENERIC_STONKS(Message.DISCORD_STATUS_GENERIC_STONKS_TITLE, Message.DISCORD_STATUS_GENERIC_STONKS_DESCRIPTION, () -> "$$ Making STONKS $$");

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