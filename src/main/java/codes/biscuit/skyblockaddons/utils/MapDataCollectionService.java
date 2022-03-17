package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.features.dungeonmap.DungeonMapManager;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec4b;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MapDataCollectionService implements Runnable {
    private static final Logger logger = SkyblockAddons.getLogger();

    private final ScheduledExecutorService executorService;
    private final DataType dataType;
    private final List<Object> collectedData;
    private final AtomicInteger lastCollectedDataHash;

    public MapDataCollectionService(@NonNull DataType dataType) {
        executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat(
                "SBA Map Data Collection Service").build());
        this.dataType = dataType;
        collectedData = Collections.synchronizedList(new ArrayList<>());
        lastCollectedDataHash = new AtomicInteger();
    }

    @Override
    public void run() {
        if (!executorService.isShutdown()) {
            executorService.scheduleAtFixedRate(new DataCollectionTask(), 100, 5000, TimeUnit.MILLISECONDS);
            logger.info("Service Started");
        } else {
            throw new IllegalStateException("Executor is shutdown.");
        }
    }

    public File stopAndSaveData() {
        logger.info("Service Stopping");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        logger.info("Service Stopped");

        if (collectedData.size() > 0) {
            return saveData();
        }

        return null;
    }

    public boolean isRunning() {
        return !executorService.isShutdown();
    }

    private class DataCollectionTask implements Runnable {

        @Override
        public void run() {
            Logger logger = LogManager.getLogger();
            ItemStack possibleMapItemStack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(8);

            if (possibleMapItemStack == null || possibleMapItemStack.getItem() != Items.filled_map ||
                    !possibleMapItemStack.hasDisplayName() || !possibleMapItemStack.getDisplayName().contains("Magic")) {
                logger.warn("Player doesn't have a dungeon map, skipping.");
                return;
            }

            net.minecraft.world.storage.MapData mapData = DungeonMapManager.getMapData();

            if (mapData == null) {
                logger.warn("Map data not found, skipping.");
                return;
            }

            int collectedDataHash;
            Object collectedDataEntry = null;

            if (dataType == DataType.MAP) {
                collectedDataEntry = new MapData(mapData.xCenter, mapData.zCenter, mapData.dimension, mapData.scale,
                        DungeonMapManager.getMapStartX(), DungeonMapManager.getMapStartZ());

                for (Map.Entry<String, Vec4b> decorationEntry : mapData.mapDecorations.entrySet()) {
                    ((MapData) collectedDataEntry).addDecoration(decorationEntry.getKey(), new Vec4b(decorationEntry.getValue()));
                }
            } else {
                for (Vec4b decorationValue : mapData.mapDecorations.values()) {
                    if (decorationValue.func_176110_a() == 1) {
                        BlockPos playerPos = Minecraft.getMinecraft().thePlayer.getPosition();
                        collectedDataEntry = new PlayerData(decorationValue.func_176112_b(), decorationValue.func_176113_c(),
                                playerPos.getX(), playerPos.getZ());
                        break;
                    }
                }

                if (collectedDataEntry == null) {
                    logger.warn("Current player's marker not found in map, skipping.");
                    return;
                }
            }

            collectedDataHash = collectedDataEntry.hashCode();

            if (collectedDataHash == lastCollectedDataHash.get()) {
                logger.warn("Map data same as last, skipping.");
                return;
            }

            Collections.synchronizedList(collectedData).add(collectedDataEntry);
            lastCollectedDataHash.set(collectedDataHash);
            logger.info("Data collected");
        }
    }

    private File saveData() {
        try {
            Field settingsFileField = ConfigValues.class.getDeclaredField("settingsConfigFile");
            settingsFileField.setAccessible(true);
            File settingsFile = ((File) settingsFileField.get(SkyblockAddons.getInstance().getConfigValues())).getParentFile()
                    .getParentFile();
            String dungeonFloor = SkyblockAddons.getInstance().getDungeonManager().getCurrentFloor();
            String logFileName = dungeonFloor != null ? dungeonFloor : "" + DateFormat.getDateTimeInstance()
                    .format(new Date()).replaceAll("[ :]", "-");
            File logFile = new File(settingsFile, String.format("logs/SBA-Map-%s.%s",
                    logFileName, dataType == DataType.MAP ? "log" : "csv"));

            try (FileWriter fileWriter = new FileWriter(logFile)) {
                StringBuilder resultStringBuilder = new StringBuilder();

                if (dataType == DataType.MAP) {
                    for (Object entry : Collections.synchronizedList(collectedData)) {
                        resultStringBuilder.append(entry.toString()).append("\n");
                    }
                } else {
                    resultStringBuilder.append("Map X,Map Z,Actual X,Actual Z").append("\n");
                    for (Object entry : Collections.synchronizedList(collectedData)) {
                        PlayerData playerDataEntry = (PlayerData) entry;
                        resultStringBuilder.append(playerDataEntry.toCsv()).append("\n");
                    }
                }

                fileWriter.write(resultStringBuilder.toString());
                logger.info(String.format("Saved map data to file \"%s\".", logFile.getPath()));
                return logFile;
            } catch (IOException | NullPointerException e) {
                logger.error(String.format("Couldn't write to file %s: %s", logFile.getPath(), e));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Failed to save data to file:");
            logger.error(e.getMessage());
        }

        return null;
    }

    public enum DataType {
        MAP,
        PLAYER
    }

    private static class MapData {
        private final Date createdDate;

        // Vanilla
        private final int xCenter;
        private final int zCenter;
        private final int dimension; //FML byte -> int
        private final byte scale;
        private final Map<String, Vec4b> mapDecorations = Maps.newLinkedHashMap();

        // SBA
        private final float mapStartX;
        private final float mapStartZ;

        public MapData(int xCenter, int zCenter, int dimension, byte scale, float mapStartX, float mapStartZ) {
            this.xCenter = xCenter;
            this.zCenter = zCenter;
            this.dimension = dimension;
            this.scale = scale;
            this.mapStartX = mapStartX;
            this.mapStartZ = mapStartZ;
            createdDate = new Date(System.currentTimeMillis());
        }

        public void addDecoration(String key, Vec4b value) {
            mapDecorations.put(key, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            MapData mapData = (MapData) o;

            return new EqualsBuilder().append(xCenter, mapData.xCenter).append(zCenter, mapData.zCenter)
                    .append(dimension, mapData.dimension).append(scale, mapData.scale)
                    .append(mapDecorations, mapData.mapDecorations).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(xCenter).append(zCenter)
                    .append(dimension).append(scale).append(mapDecorations).toHashCode();
        }

        @Override
        public String toString() {
            StringBuilder decorationStringBuilder = new StringBuilder("Decorations:" + "\n");

            for (Map.Entry<String, Vec4b> decorationEntry : mapDecorations.entrySet()) {
                decorationStringBuilder.append("{")
                        .append("\n  name=").append(decorationEntry.getKey())
                        .append("\n  iconType=").append(decorationEntry.getValue().func_176110_a())
                        .append("\n  x=").append(decorationEntry.getValue().func_176112_b())
                        .append("\n  z=").append(decorationEntry.getValue().func_176113_c())
                        .append("\n  iconDirection=").append(decorationEntry.getValue().func_176111_d())
                        .append("\n}\n");
            }

            return DateFormat.getDateTimeInstance().format(createdDate) + "\n" +
                    "Hypixel Map:" + "\n" +
                    "xCenter=" + xCenter + "\n" +
                    "zCenter=" + zCenter + "\n" +
                    "dimension=" + dimension + "\n" +
                    "scale=" + scale + "\n" +
                    decorationStringBuilder +
                    "SBA Map:" + "\n" +
                    "mapStartX=" + mapStartX + "\n" +
                    "mapStartZ=" + mapStartZ;
        }
    }

    private static class PlayerData {
        private final Date createdDate;
        private final int mapX;
        private final int mapZ;
        private final int actualX;
        private final int actualZ;

        public PlayerData(int mapX, int mapZ, int actualX, int actualZ) {
            createdDate = new Date(System.currentTimeMillis());
            this.mapX = mapX;
            this.mapZ = mapZ;
            this.actualX = actualX;
            this.actualZ = actualZ;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            PlayerData that = (PlayerData) o;

            return new EqualsBuilder().append(mapX, that.mapX).append(mapZ, that.mapZ).append(actualX, that.actualX)
                    .append(actualZ, that.actualZ).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(mapX).append(mapZ).append(actualX)
                    .append(actualZ).toHashCode();
        }

        public String toCsv() {
            return String.format("%d,%d,%d,%d", mapX, mapZ, actualX, actualZ);
        }

        @Override
        public String toString() {
            return "PlayerData{" +
                    "createdDate=" + DateFormat.getDateTimeInstance().format(createdDate) +
                    ", mapX=" + mapX +
                    ", mapZ=" + mapZ +
                    ", actualX=" + actualX +
                    ", actualZ=" + actualZ +
                    '}';
        }
    }
}
