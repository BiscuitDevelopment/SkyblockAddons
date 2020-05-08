package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class EndstoneProtectorManager {

    @Getter private static boolean canDetectSkull = false;
    @Getter private static Stage minibossStage = null;
    @Getter private static int zealotCount = -1;

    public static void tick() {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (mc.theWorld != null && (main.getUtils().getLocation() == Location.THE_END || main.getUtils().getLocation() == Location.DRAGONS_NEST) &&
                main.getConfigValues().isEnabled(Feature.ENDSTONE_PROTECTOR_DISPLAY)) {
            WorldClient worldClient = mc.theWorld;

            Chunk chunk = worldClient.getChunkFromBlockCoords(new BlockPos(-689, 5, -273));
            if (chunk == null || !chunk.isLoaded()) {
                canDetectSkull = false;
                return;
            }

            Stage stage = null;

            for (Entity entity : worldClient.loadedEntityList) {
                if (entity instanceof EntityIronGolem) {
                    stage = Stage.GOLEM_ALIVE;
                    break;
                }
            }

            if (stage == null) {
                stage = Stage.detectStage(worldClient);
            }

            canDetectSkull = true;

            if (minibossStage != stage) {
                minibossStage = stage;
                zealotCount = minibossStage.getZealotsRemaining();
            }
        } else {
            canDetectSkull = false;
        }
    }

    public static void onKill() {
        zealotCount--;

        if (zealotCount < minibossStage.getZealotsRemaining()-1000) zealotCount = minibossStage.getZealotsRemaining()-1000;

        if (zealotCount < 0) zealotCount = 0;
    }

    public static void reset() {
        minibossStage = null;
        zealotCount = -1;
        canDetectSkull = false;
    }

    public enum Stage {
        NO_HEAD(-1, 5000),
        STAGE_1(0, 4000),
        STAGE_2(1, 3000),
        STAGE_3(2, 2000),
        STAGE_4(3, 1000),
        STAGE_5(4, 0),
        GOLEM_ALIVE(-1, 0);

        private BlockPos blockPos = null;
        @Getter private int zealotsRemaining;

        Stage(int blocksUp, int zealotsRemaining) {
            if (blocksUp != -1) {
                this.blockPos = new BlockPos(-689, 5 + blocksUp, -273);
            }
            this.zealotsRemaining = zealotsRemaining;
        }

        public static Stage detectStage(WorldClient worldClient) {
            for (Stage stage : values()) {
                if (stage.blockPos != null) {
                    if (Blocks.skull.equals(worldClient.getBlockState(stage.blockPos).getBlock())) {
                        return stage;
                    }
                }
            }

            return Stage.NO_HEAD;
        }
    }
}
