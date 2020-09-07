package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.features.dungeonmap.MapMarker;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter @Setter
public class DungeonPlayer {
    public static Pattern DUNGEON_PLAYER_LINE = Pattern.compile("^§.\\[(?<class>.)] (?<name>[\\w§]+) (?:§.)*?§(?<healthColor>.)(?<health>[\\w]+)(?:§c❤)?");

    private String name;
    private DungeonClass dungeonClass;
    private int health;
    private ColorCode healthColor;
    private MapMarker mapMarker;

    public DungeonPlayer(String name) {
        this.name = name;
    }

    public static DungeonPlayer fromScoreboardLine(String line) {
        try {
            Matcher matcher = DUNGEON_PLAYER_LINE.matcher(line);
            if (matcher.find()) {
                String name = TextUtils.stripColor(matcher.group("name"));
                DungeonClass dungeonClass = DungeonClass.fromFirstLetter(matcher.group("class"));
                String healthText = matcher.group("health");
                int health;
                if (healthText.equals("DEAD")) {
                    health = 0;
                } else {
                    health = Integer.parseInt(matcher.group("health"));
                }
                ColorCode healthColor = ColorCode.getByChar(matcher.group("healthColor").toCharArray()[0]);

                boolean foundName = false;
                String similarName = null;
                for (NetworkPlayerInfo networkPlayerInfo : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
                    String profileName = networkPlayerInfo.getGameProfile().getName();
                    if (name.equals(profileName)) {
                        foundName = true;
                        break;

                    } else if (profileName.startsWith(name)) {
                        similarName = profileName;
                    }
                }

                if (!foundName) {
                    if (similarName == null) {
                        return null;
                    } else {
                        name = similarName;
                    }
                }

                DungeonPlayer dungeonPlayer = new DungeonPlayer(name);
                dungeonPlayer.dungeonClass = dungeonClass;
                dungeonPlayer.health = health;
                dungeonPlayer.healthColor = healthColor;

                return dungeonPlayer;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean isLow() {
        return healthColor == ColorCode.YELLOW;
    }

    public boolean isCritical() {
        return healthColor == ColorCode.RED;
    }

    public boolean isGhost() {
        return this.health == 0;
    }

    public void updateStatsFromOther(DungeonPlayer other) {
        this.dungeonClass = other.dungeonClass;
        this.health = other.health;
        this.healthColor = other.healthColor;
    }
}
