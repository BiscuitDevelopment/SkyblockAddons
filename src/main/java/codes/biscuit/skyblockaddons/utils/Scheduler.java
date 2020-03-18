package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

public class Scheduler {

    private SkyblockAddons main;
    private long totalTicks = 0;
    private Map<Long, Set<Command>> queue = new HashMap<>();

    public Scheduler(SkyblockAddons main) {
        this.main = main;
    }

    /**
     * This class is a little something I came up with in order to schedule things
     * by client ticks reliably.
     *
     * @param commandType What you want to schedule
     * @param delaySeconds The delay in ticks (20 ticks = 1second)
     */
    public void schedule(CommandType commandType, int delaySeconds, Object... data) {
        long ticks = totalTicks+(delaySeconds*20);
        Set<Command> commands = queue.get(ticks);
        if (commands != null) {
            for (Command command : commands) {
                if (command.getCommandType() == commandType) {
                    command.addCount(data);
                    return;
                }
            }
            commands.add(new Command(commandType, data));
        } else {
            Set<Command> commandSet = new HashSet<>();
            commandSet.add(new Command(commandType, data));
            queue.put(ticks, commandSet);
        }
    }

    private boolean delayingMagmaCall = false; // this addition should decrease the amount of calls by a lot

    @SubscribeEvent()
    public void ticker(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            totalTicks++;
            Set<Command> commands = queue.get(totalTicks);
            if (commands != null) {
                for (Command command : commands) {
                    for (int times = 0; times < command.getCount().getValue(); times++) {
                        command.getCommandType().execute(command, times+1);
                    }
                }
                queue.remove(totalTicks);
            }
            if (totalTicks % 12000 == 0 || delayingMagmaCall) { // check magma boss every 15 minutes
                if (main.getPlayerListener().getMagmaAccuracy() != EnumUtils.MagmaTimerAccuracy.EXACTLY) {
                    if (main.getUtils().isOnSkyblock()) {
                        delayingMagmaCall = false;
                        main.getUtils().fetchEstimateFromServer();
                    } else if (!delayingMagmaCall) {
                        delayingMagmaCall = true;
                    }
                }
            }
            ChromaManager.increment(); // Run every tick
        }
    }

    @Getter
    private class Command {
        private CommandType commandType;
        private MutableInt count = new MutableInt(1);
        private Map<Integer, Object[]> countData = new HashMap<>();

        private Command(CommandType commandType, Object... data) {
            this.commandType = commandType;
            if (data.length > 0) {
                countData.put(1, data);
            }
        }

        private void addCount(Object... data) {
            count.increment();
            if (data.length > 0) {
                countData.put(count.getValue(), data);
            }
        }

        Object[] getData(int count) {
            return countData.get(count);
        }
    }

    public enum CommandType {
        RESET_MAGMA_PREDICTION,
        SUBTRACT_MAGMA_COUNT,
        SUBTRACT_BLAZE_COUNT,
        RESET_TITLE_FEATURE,
        RESET_SUBTITLE_FEATURE,
        RESET_UPDATE_MESSAGE,
        SET_LAST_SECOND_HEALTH,
        DELETE_RECENT_CHUNK;

        public void execute(Command command, int count) {
            SkyblockAddons main = SkyblockAddons.getInstance();
            PlayerListener playerListener = main.getPlayerListener();
            Object[] commandData = command.getData(count);
            if (this == SUBTRACT_MAGMA_COUNT) {
                playerListener.setRecentMagmaCubes(playerListener.getRecentMagmaCubes()-1);
            } else if (this == SUBTRACT_BLAZE_COUNT) {
                playerListener.setRecentBlazes(playerListener.getRecentBlazes()-1);
            } else if (this == RESET_MAGMA_PREDICTION) {
                if (playerListener.getMagmaAccuracy() == EnumUtils.MagmaTimerAccuracy.SPAWNED_PREDICTION) {
                    playerListener.setMagmaAccuracy(EnumUtils.MagmaTimerAccuracy.ABOUT);
                    playerListener.setMagmaTime(7200);
                }
            } else if (this == DELETE_RECENT_CHUNK) {
                int x = (int)commandData[0];
                int z = (int)commandData[1];
                CoordsPair coordsPair = new CoordsPair(x,z);
                playerListener.getRecentlyLoadedChunks().remove(coordsPair);
            } else if (this == RESET_TITLE_FEATURE) {
                main.getRenderListener().setTitleFeature(null);
            } else if (this == RESET_SUBTITLE_FEATURE) {
                main.getRenderListener().setSubtitleFeature(null);
            } else if (this == RESET_UPDATE_MESSAGE) {
                if (main.getRenderListener().getDownloadInfo().getMessageType() == commandData[0])
                main.getRenderListener().getDownloadInfo().setMessageType(null);
            } else if (this == SET_LAST_SECOND_HEALTH) {
                main.getPlayerListener().setLastSecondHealth((int)commandData[0]);
            }
        }
    }
}
