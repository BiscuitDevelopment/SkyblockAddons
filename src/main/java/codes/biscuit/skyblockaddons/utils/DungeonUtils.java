package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.core.DungeonClass;
import codes.biscuit.skyblockaddons.core.DungeonMilestone;
import codes.biscuit.skyblockaddons.core.DungeonPlayer;
import codes.biscuit.skyblockaddons.core.EssenceType;
import codes.biscuit.skyblockaddons.features.DungeonDeathCounter;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains a set of utility methods for Skyblock Dungeons.
 */
public class DungeonUtils {

    private static final Pattern PATTERN_MILESTONE = Pattern.compile("^.+?(Healer|Tank|Mage|Archer|Berserk) Milestone .+?([❶-❿]).+?§r§.(\\d+)§.§7 .+?");
    private static final Pattern PATTERN_COLLECTED_ESSENCES = Pattern.compile("§.+?(\\d+) (Wither|Spider|Undead|Dragon|Gold|Diamond|Ice) Essence");
    private static final Pattern PATTERN_BONUS_ESSENCE = Pattern.compile("^§.+?[^You] .+?found a .+?(Wither|Spider|Undead|Dragon|Gold|Diamond|Ice) Essence.+?");
    private static final Pattern PATTERN_SALVAGE_ESSENCES = Pattern.compile("§.§.\\+([0-9])+? §.§.(Wither|Spider|Undead|Dragon|Gold|Diamond|Ice) Essence!+");
    private static final Pattern PATTERN_SECRETS = Pattern.compile("§7([0-9]+)/([0-9]+) Secrets");
    private static final Pattern PATTERN_PLAYER_LINE = Pattern.compile("^§.\\[(?<classLetter>.)] (?<name>[\\w§]+) (?:§.)*?§(?<healthColor>.)(?<health>[\\w]+)(?:§c❤)?");

    /** The last dungeon server the player played on */
    @Getter @Setter private String lastServerId;

    /** The latest milestone the player received during a dungeon game */
    @Getter @Setter private DungeonMilestone dungeonMilestone;

    /** The latest essences the player collected during a dungeon game */
    @Getter private final Map<EssenceType, Integer> collectedEssences = new EnumMap<>(EssenceType.class);

    /** The current teammates of the dungeon game */
    @Getter private final Map<String, DungeonPlayer> players = new HashMap<>();

    /** The current number of secrets found in the room */
    @Getter @Setter private int secrets = -1;

    /** The maximum number of secrets found in the room */
    @Getter @Setter private int maxSecrets;

    /** The counter for the number of player deaths during a dungeon game */
    @Getter private final DungeonDeathCounter deathCounter = new DungeonDeathCounter();

    private EssenceType lastEssenceType;
    private int lastEssenceAmount;
    private int lastEssenceRepeat;

    /**
     * Clear the dungeon game data. Called by {@link codes.biscuit.skyblockaddons.utils.Utils} each new game
     */
    public void reset() {
        dungeonMilestone = null;
        collectedEssences.clear();
        players.clear();
        deathCounter.reset();
    }

    /**
     * Returns the {@code DungeonPlayer} object for the player with the given username.
     *
     * @param userName the player's username
     * @return the {@code DungeonPlayer} object for the player with the given username
     */
    public DungeonPlayer getDungeonPlayer(String userName) {
        return players.get(userName);
    }

    /**
     * This method parses the class milestone attained from the chat message the player receives when they attain a milestone.
     *
     * @param message the chat message received
     * @return a {@code DungeonMilestone} object representing the milestone if one is found, or {@code null} if no milestone is found
     */
    public DungeonMilestone parseMilestone(String message) {
        Matcher matcher = PATTERN_MILESTONE.matcher(message);
        if (!matcher.lookingAt()) {
            return null;
        }

        DungeonClass dungeonClass = DungeonClass.fromDisplayName(matcher.group(1));
        return new DungeonMilestone(dungeonClass, matcher.group(2), matcher.group(3));

    }

    /**
     * This method parses the type and amount of essence the player collected from the action bar message that shows up
     * when an essence is collected. It then records the result in {@code collectedEssences}.
     *
     * @param message the action bar message to parse essence information from
     */
    public void parseCollectedEssence(String message) {
        Matcher matcher = PATTERN_COLLECTED_ESSENCES.matcher(message);

        while (matcher.find()) {

            int amount = Integer.parseInt(matcher.group(1));
            EssenceType essenceType = EssenceType.fromName(matcher.group(2));

            // Fix: Add x3 of the original collected
            // This happens because the action bar receives the collected essence 3 times
            if (lastEssenceType != null && lastEssenceAmount == amount && lastEssenceType == essenceType) {
                lastEssenceRepeat++;

                if (lastEssenceRepeat == 3) {
                    lastEssenceType = null; // Trigger a reset of the original collected essence in the third spam
                }
                continue; // Prevent the spam collected essence to be accounted
            }
            lastEssenceType = essenceType;
            lastEssenceAmount = amount;
            lastEssenceRepeat = 1;

            if (essenceType != null) {
                collectedEssences.put(essenceType, collectedEssences.getOrDefault(essenceType, 0) + amount);
            }
        }
    }

    /**
     * This method parses the type and amount of essence gained when a dungeon teammate finds a bonus essence.
     * This information is parsed from the given chat message. It then records the result in {@code collectedEssences}.
     *
     * @param message the chat message to parse essence information from
     */
    public void parseBonusEssence(String message) {
        Matcher matcher = PATTERN_BONUS_ESSENCE.matcher(message);

        if (matcher.matches()) {
            EssenceType essenceType = EssenceType.fromName(matcher.group(1));

            collectedEssences.put(essenceType, collectedEssences.getOrDefault(essenceType, 0) + 1);
        }
    }

    /**
     * This method parses the current and the maximum number of secrets found in the room.
     *
     * @param message the action bar message to parse secrets information from
     * @return A message without the secrets information
     */
    public String parseSecrets(String message) {
        Matcher matcher = PATTERN_SECRETS.matcher(message);
        if (!matcher.find()) {
            secrets = -1;
            return message;
        }

        secrets = Integer.parseInt(matcher.group(1));
        maxSecrets = Integer.parseInt(matcher.group(2));
        return matcher.replaceAll("");
    }

    /**
     * This method parses the type and amount of essences obtained from the salvaged item by the player.
     * This information is parsed from the given chat message. It then records the result in {@code collectedEssences}
     *
     * @param message the chat message to parse the obtained essences from
     */
    public void parseSalvagedEssences(String message) {
        Matcher matcher = PATTERN_SALVAGE_ESSENCES.matcher(message);

        while (matcher.find()) {
            EssenceType essenceType = EssenceType.fromName(matcher.group(2));
            int amount = Integer.parseInt(matcher.group(1));

            collectedEssences.put(essenceType, collectedEssences.getOrDefault(essenceType, 0) + amount);
        }
    }

    /**
     * This method parses dungeon player stats displayed on the scoreboard sidebar and stores them as {@code DungeonPlayer}
     * objects. It first determines if the given line represents a dungeon player's stats. If so, it then parses all the
     * stats from the line. Finally, it creates a new {@code DungeonPlayer} object containing the parsed stats or updates
     * an existing {@code DungeonPlayer} object with the parsed stats (if one already exists for the player whose stats
     * are shown in the line).
     */
    public void updatePlayer(String scoreboardLine) {
        Matcher matcher = PATTERN_PLAYER_LINE.matcher(scoreboardLine);

        if (matcher.matches()) {
            String name = TextUtils.stripColor(matcher.group("name"));
            DungeonClass dungeonClass = DungeonClass.fromFirstLetter(matcher.group("classLetter").charAt(0));
            ColorCode healthColor = ColorCode.getByChar(matcher.group("healthColor").charAt(0));
            String healthText = matcher.group("health");
            int health;

            if (healthText.equals("DEAD")) {
                health = 0;
            } else {
                health = Integer.parseInt(healthText);
            }

            for (DungeonPlayer player: players.values()) {
                if (player.getName().startsWith(name)) {
                    player.setHealthColor(healthColor);

                    if (player.getHealth() > 0 && health == 0) {
                        deathCounter.incrementAlternate();
                    }

                    player.setHealth(health);
                    return;
                }
            }

            for (NetworkPlayerInfo networkPlayerInfo: Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
                String profileName = networkPlayerInfo.getGameProfile().getName();

                if (profileName.startsWith(name)) {
                    players.put(profileName, new DungeonPlayer(profileName, dungeonClass, healthColor, health));
                }
            }
        }
    }
}
