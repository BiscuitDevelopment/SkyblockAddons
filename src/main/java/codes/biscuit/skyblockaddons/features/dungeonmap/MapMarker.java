package codes.biscuit.skyblockaddons.features.dungeonmap;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;

@Setter @Getter
public class MapMarker {

    private SkyblockAddons main = SkyblockAddons.getInstance();

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

    public float getX() {
        EntityPlayer player = getPlayer();
        if (player != null) {
            float playerMarkerX = ((float) player.posX - DungeonMapManager.getMapStartX()) / 1.5F;
            return x = (playerMarkerX - 64.0F) * 2.0F;
        } else {
            return x;
        }
    }

    public float getZ() {
        EntityPlayer player = getPlayer();
        if (player != null) {
            float playerMarkerZ = ((float) player.posZ - DungeonMapManager.getMapStartZ()) / 1.5F;
            return z = (playerMarkerZ - 64.0F) * 2.0F;
        } else {
            return z;
        }
    }

    public float getRotation() {
        EntityPlayer player = getPlayer();
        if (player != null) {
            return rotation = MathHelper.wrapAngleTo180_float(player.rotationYaw) / 360F * 16F;
        } else {
            return rotation;
        }
    }
}
