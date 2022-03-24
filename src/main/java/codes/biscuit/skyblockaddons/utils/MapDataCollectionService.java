package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.features.dungeonmap.DungeonMapManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ReportedException;
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

/**
 * This service collects data on the dungeon map in the background while the player is in a dungeon run.
 * It collects general information about the map, the player's position on the map, and the player's in-game position
 * at five-second intervals. This data is used to develop the dungeon map feature.
 *
 * Call {@link #run()} to start collection and {@link #stopAndSaveData()} to stop.
 */
public class MapDataCollectionService implements Runnable {
    private static final Logger logger = SkyblockAddons.getLogger();
    protected static final SkyblockAddonsMessageFactory dataCollectionTaskMessageFactory;
    protected static final Field mapData;
    protected static final Field lastReportedPosX;
    protected static final Field lastReportedPosZ;

    private final ScheduledExecutorService executorService;
    private final DataType dataType;
    private final List<Object> collectedData;
    private final AtomicInteger lastCollectedDataHash;

    static {
        try {
            dataCollectionTaskMessageFactory = new SkyblockAddonsMessageFactory(DataCollectionTask.class.getSimpleName());
            mapData = DungeonMapManager.class.getDeclaredField("mapData");
            mapData.setAccessible(true);
            lastReportedPosX = DungeonMapManager.class.getDeclaredField("lastReportedPosX");
            lastReportedPosX.setAccessible(true);
            lastReportedPosZ = DungeonMapManager.class.getDeclaredField("lastReportedPosZ");
            lastReportedPosZ.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ReportedException(CrashReport.makeCrashReport(e,
                    "Field not found, you did something wrong."));
        }
    }

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
            Logger logger = LogManager.getLogger(dataCollectionTaskMessageFactory);
            ItemStack possibleMapItemStack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(8);

            if (possibleMapItemStack == null || possibleMapItemStack.getItem() != Items.filled_map ||
                    !possibleMapItemStack.hasDisplayName() || !possibleMapItemStack.getDisplayName().contains("Magic")) {
                logger.warn("Player doesn't have a dungeon map, skipping.");
                return;
            }

            net.minecraft.world.storage.MapData mapData;

            try {
                mapData = (net.minecraft.world.storage.MapData) MapDataCollectionService.mapData.get(null);
            } catch (IllegalAccessException e) {
                logger.error("Couldn't get map data.", e);
                throw new RuntimeException(e);
            }

            if (mapData == null) {
                logger.warn("Map data not found, skipping.");
                return;
            }

            switch (dataType) {
                case BOTH:
                    collectData(mapData, DataType.PLAYER);
                case MAP:
                    collectData(mapData, DataType.MAP);
                    break;
                case PLAYER:
                    collectData(mapData, DataType.PLAYER);
                    break;
            }
        }

        private void collectData(@NonNull net.minecraft.world.storage.MapData mapData, @NonNull DataType dataType) {
            int collectedDataHash;
            Object collectedDataEntry = null;

            switch (dataType) {
                case MAP:
                    collectedDataEntry = getMapData(mapData);
                    break;
                case PLAYER:
                    collectedDataEntry = getPlayerData(mapData);
                    break;
            }

            if (collectedDataEntry == null) {
                return;
            }

            collectedDataHash = collectedDataEntry.hashCode();

            if (collectedDataHash == lastCollectedDataHash.get()) {
                logger.warn("Map data same as last, skipping.");
                return;
            }

            collectedData.add(collectedDataEntry);
            lastCollectedDataHash.set(collectedDataHash);
            logger.info("Data collected");
        }

        private MapDataCollectionService.MapData getMapData(net.minecraft.world.storage.MapData mapData) {

            return new MapData(
                    mapData.xCenter, mapData.zCenter, mapData.dimension, mapData.scale,
                    DungeonMapManager.getMarkerOffsetX(), DungeonMapManager.getMarkerOffsetZ(), mapData.mapDecorations);
        }

        private PlayerData getPlayerData(net.minecraft.world.storage.MapData mapData) {
            PlayerData collectedData = null;

            if (mapData.mapDecorations.isEmpty()) {
                logger.warn("MapData has no markers.");
                return null;
            }

            for (Vec4b decorationValue : mapData.mapDecorations.values()) {
                if (decorationValue.func_176110_a() == 1) {
                    try {
                        EntityPlayerSP thePlayer = Minecraft.getMinecraft().thePlayer;
                        double playerLastReportedX = ((Field) lastReportedPosX.get(null)).getDouble(thePlayer);
                        double playerLastReportedZ = ((Field) lastReportedPosZ.get(null)).getDouble(thePlayer);

                        collectedData = new PlayerData(decorationValue.func_176112_b(), decorationValue.func_176113_c(),
                                playerLastReportedX, playerLastReportedZ);
                        break;
                    } catch (IllegalAccessException e) {
                        logger.warn("Failed to get player coordinates, skipping.");
                        return null;
                    }
                }
            }

            if (collectedData != null) {
                return collectedData;
            } else {
                logger.warn("Current player's marker not found in map, skipping.");
                return null;
            }
        }
    }

    private String getFormattedData() {
        StringBuilder resultStringBuilder = new StringBuilder();

        switch (dataType) {
            case BOTH:
                resultStringBuilder.append("Map X,Map Z,Actual X,Actual Z,Calculated X,Calculated Z," +
                        "Marker X Offset,Marker Z Offset").append("\n");
                break;
            case PLAYER:
                resultStringBuilder.append("Map X,Map Z,Actual X,Actual Z,Calculated X,Calculated Z").append("\n");
                break;
        }

        synchronized (collectedData) {
            ListIterator<Object> collectedDataIterator = collectedData.listIterator();

            while (collectedDataIterator.hasNext()) {
                Object entry = collectedDataIterator.next();

                switch (dataType) {
                    case BOTH:
                        if (entry instanceof MapData) {
                            collectedDataIterator.previous();
                            Object prevEntry = collectedDataIterator.hasPrevious() ?
                                    collectedDataIterator.previous() : null;

                            if (prevEntry instanceof PlayerData) {
                                resultStringBuilder.append(",");
                            } else {
                                resultStringBuilder.append(",,,,,,");
                            }

                            resultStringBuilder.append(((MapData) entry).sbaToCsv()).append("\n");

                            collectedDataIterator.next();
                            if (prevEntry != null) {
                                collectedDataIterator.next();
                            }
                        } else {
                            resultStringBuilder.append(((PlayerData) entry).toCsv());
                        }
                        break;
                    case MAP:
                        resultStringBuilder.append(entry.toString()).append("\n");
                        break;
                    case PLAYER:
                        resultStringBuilder.append(((PlayerData) entry).toCsv()).append("\n");
                        break;
                }
            }
        }

        return resultStringBuilder.toString();
    }

    private File saveData() {
        try {
            Field settingsFileField = ConfigValues.class.getDeclaredField("settingsConfigFile");
            settingsFileField.setAccessible(true);
            File settingsFile = ((File) settingsFileField.get(SkyblockAddons.getInstance().getConfigValues())).getParentFile()
                    .getParentFile();
            String dungeonFloor = SkyblockAddons.getInstance().getDungeonManager().getCurrentFloor();
            String logFileName = (dungeonFloor != null ? dungeonFloor + "-" : "") + DateFormat.getDateTimeInstance()
                    .format(new Date()).replaceAll("[ :]", "-");
            File logFile = new File(settingsFile, String.format("logs/SBA-Map-%s.%s",
                    logFileName, dataType == DataType.MAP ? "log" : "csv"));

            try (FileWriter fileWriter = new FileWriter(logFile)) {
                fileWriter.write(getFormattedData());
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
        BOTH,
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
        private final Map<String, Vec4b> mapDecorations;

        // SBA
        private final double markerOffsetX;
        private final double markerOffsetZ;

        public MapData(int xCenter, int zCenter, int dimension, byte scale, double markerOffsetX, double markerOffsetZ,
                       Map<String, Vec4b> mapDecorations) {
            createdDate = new Date(System.currentTimeMillis());
            this.xCenter = xCenter;
            this.zCenter = zCenter;
            this.dimension = dimension;
            this.scale = scale;
            this.markerOffsetX = markerOffsetX;
            this.markerOffsetZ = markerOffsetZ;
            this.mapDecorations = Collections.unmodifiableMap(mapDecorations);
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

        public String sbaToCsv() {
            return String.format("%.2f,%.2f", markerOffsetX, markerOffsetZ);
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
                    "markerOffsetX=" + markerOffsetX + "\n" +
                    "markerOffsetZ=" + markerOffsetZ;
        }
    }

    private static class PlayerData {
        private final Date createdDate;
        private final int mapX;
        private final int mapZ;
        private final double lastReportedX;
        private final double lastReportedZ;
        private final double calculatedX;
        private final double calculatedZ;

        public PlayerData(int mapX, int mapZ, double lastReportedX, double lastReportedZ) {
            createdDate = new Date(System.currentTimeMillis());
            this.mapX = mapX;
            this.mapZ = mapZ;
            this.lastReportedX = lastReportedX;
            this.lastReportedZ = lastReportedZ;
            this.calculatedX = DungeonMapManager.toMapCoordinate(lastReportedX, DungeonMapManager.getMarkerOffsetX());
            this.calculatedZ = DungeonMapManager.toMapCoordinate(lastReportedZ, DungeonMapManager.getMarkerOffsetZ());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            PlayerData that = (PlayerData) o;

            return new EqualsBuilder().append(mapX, that.mapX).append(mapZ, that.mapZ).append(lastReportedX, that.lastReportedX)
                    .append(lastReportedZ, that.lastReportedZ).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(mapX).append(mapZ).append(lastReportedX)
                    .append(lastReportedZ).toHashCode();
        }

        public String toCsv() {
            return String.format("%d,%d,%.2f,%.2f,%.2f,%.2f", mapX, mapZ, lastReportedX, lastReportedZ, calculatedX, calculatedZ);
        }

        @Override
        public String toString() {
            return "PlayerData{" +
                    "createdDate=" + DateFormat.getDateTimeInstance().format(createdDate) +
                    ", mapX=" + mapX +
                    ", mapZ=" + mapZ +
                    ", lastReportedX=" + lastReportedX +
                    ", lastReportedZ=" + lastReportedZ +
                    ", calculatedX=" + calculatedX +
                    ", calculatedZ=" + calculatedZ +
                    '}';
        }
    }
}
