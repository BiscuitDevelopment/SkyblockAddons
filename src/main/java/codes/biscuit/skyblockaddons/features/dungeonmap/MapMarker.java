package codes.biscuit.skyblockaddons.features.dungeonmap;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.Logger;

@Setter @Getter
public class MapMarker {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger logger = SkyblockAddons.getLogger();

    /** The icon type of this map marker (https://minecraft.fandom.com/wiki/Map#Map_icons) */
    private byte iconType;
    private float x;
    private float z;
    private float rotation;
    private String playerName;
    private String mapMarkerName;
    private boolean wearingHat;

    public MapMarker(EntityPlayer player) {
        this.playerName = player.getName();
        this.wearingHat = player.isWearing(EnumPlayerModelParts.HAT);

        if (player == Minecraft.getMinecraft().thePlayer) {
            iconType = 1;
        } else {
            iconType = 3;
        }
        updateXZRot(player);
    }

    public MapMarker(byte iconType, float x, float z, float rotation) {
        this.iconType = iconType;
        this.x = x;
        this.z = z;
        this.rotation = rotation;
    }

    public void setMapMarkerName(String mapMarkerName) {
        this.mapMarkerName = mapMarkerName;
    }

    public EntityPlayer getPlayer() {
        for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (player.getName().equals(playerName)) {
                return player;
            }
        }
        return null;
    }

    public void updateXZRot(EntityPlayer player) {
        x = DungeonMapManager.toMapCoordinate(player.posX, DungeonMapManager.getMarkerOffsetX());
        z = DungeonMapManager.toMapCoordinate(player.posZ, DungeonMapManager.getMarkerOffsetZ());
        rotation = MathHelper.wrapAngleTo180_float(player.rotationYaw) / 360F * 16F;
    }

    @Override
    public String toString() {
        return "MapMarker{" +
                "iconType=" + iconType +
                ", x=" + x +
                ", z=" + z +
                ", rotation=" + rotation +
                ", playerName='" + playerName + '\'' +
                ", mapMarkerName='" + mapMarkerName + '\'' +
                ", wearingHat=" + wearingHat +
                '}';
    }
}
