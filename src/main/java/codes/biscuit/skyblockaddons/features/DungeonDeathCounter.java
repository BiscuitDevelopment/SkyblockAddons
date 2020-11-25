package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.item.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class keeps track of the number of times players have died during a dungeon run.
 */
public class DungeonDeathCounter {
    /** This is the skull that is displayed as an icon beside the number of deaths. */
    public static final ItemStack SKULL_ITEM = ItemUtils.createSkullItemStack("Skull", null,
            "c659cdd4-e436-4977-a6a7-d5518ebecfbb",
            "1ae3855f952cd4a03c148a946e3f812a5955ad35cbcb52627ea4acd47d3081");

    private static final Pattern PLAYER_LIST_INFO_DEATHS_PATTERN = Pattern.compile("§r§a§l.+: §r§f\\((?<deaths>\\d)\\)§r");

    @Getter private int deaths;
    @Getter private int alternateDeaths;
    private int playerListInfoDeaths;

    /**
     * Creates a new instance of {@code DungeonDeathCounter} with the number of deaths set to zero
     */
    public DungeonDeathCounter() {
        deaths = 0;
        alternateDeaths = 0;
        playerListInfoDeaths = 0;
    }

    /**
     * Returns the most accurate death count available. If the player has enabled their "Player List Info" setting, the
     * death count from the tab menu is returned. If that setting isn't enabled, the highest count out of the main counter
     * and the alternative counter's counts is returned.
     *
     * @return the most accurate death count available
     */
    public int getCount() {
        if (SkyblockAddons.getInstance().getUtils().isPlayerListInfoEnabled()) {
            return playerListInfoDeaths;
        }
        else {
            return Math.max(deaths, alternateDeaths);
        }
    }

    /**
     * Adds one death to the counter
     */
    public void increment() {
        deaths++;
    }

    /**
     * Adds one death to the alternative counter.
     */
    public void incrementAlternate() {alternateDeaths++;}

    /**
     * This method updates the death counter with the count from the death counter in the Player List Info display.
     * If the death counter isn't being shown in the Player List Info display, nothing will be changed.
     */
    public void updateDeathsFromPlayerListInfo() {
        NetHandlerPlayClient netHandlerPlayClient = Minecraft.getMinecraft().getNetHandler();
        NetworkPlayerInfo deathDisplayPlayerInfo = netHandlerPlayClient.getPlayerInfo("!B-f");

        if (deathDisplayPlayerInfo != null) {
            String deathDisplayString = deathDisplayPlayerInfo.getDisplayName().getFormattedText();
            Matcher deathDisplayMatcher = PLAYER_LIST_INFO_DEATHS_PATTERN.matcher(deathDisplayString);

            if (deathDisplayMatcher.matches()) {
                playerListInfoDeaths = Integer.parseInt(deathDisplayMatcher.group("deaths"));
            }
        }
    }

    /**
     * Resets the number of deaths to zero for all the counters.
     */
    public void reset() {
        deaths = 0;
        alternateDeaths = 0;
        playerListInfoDeaths = 0;
    }
}