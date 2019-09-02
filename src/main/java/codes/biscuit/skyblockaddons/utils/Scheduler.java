package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.*;

public class Scheduler {

    private SkyblockAddons main;
    private long totalTicks = 0;
    private Map<Long, Set<Command>> queue = new TreeMap<>();

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
            addNewCommand(commandType, ticks, data);
        } else {
            addNewCommand(commandType, ticks, data);
        }
    }

    private void addNewCommand(CommandType commandType, long ticks, Object... data) {
        Set<Command> commandSet = new HashSet<>();
        commandSet.add(new Command(commandType, data));
        queue.put(ticks, commandSet);
    }

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
            if (totalTicks % 12000 == 0) { // check magma boss every 15 minutes
                if (main.getPlayerListener().getMagmaAccuracy() != EnumUtils.MagmaTimerAccuracy.EXACTLY) {
                    main.getUtils().fetchEstimateFromServer();
                }
            }
        }
    }

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

        CommandType getCommandType() {
            return commandType;
        }

        MutableInt getCount() {
            return count;
        }

        Object[] getData(int count) {
            return countData.get(count);
        }
    }

    public enum CommandType {
        RESET_MAGMA_PREDICTION,
        SUBTRACT_MAGMA_COUNT,
        SUBTRACT_BLAZE_COUNT,
        DELETE_RECENT_CHUNK;

        public void execute(Command command, int count) {
            PlayerListener playerListener = SkyblockAddons.getInstance().getPlayerListener();
            if (this == SUBTRACT_MAGMA_COUNT) {
                playerListener.setRecentMagmaCubes(playerListener.getRecentMagmaCubes()-1);
            } else if (this == SUBTRACT_BLAZE_COUNT) {
                playerListener.setRecentBlazes(playerListener.getRecentBlazes()-1);
            } else if (this == RESET_MAGMA_PREDICTION) {
                if (playerListener.getMagmaAccuracy() == EnumUtils.MagmaTimerAccuracy.SPAWNED_PREDICTION) {
                    playerListener.setMagmaAccuracy(EnumUtils.MagmaTimerAccuracy.ABOUT);
                    playerListener.setMagmaTime(7200, true);
                }
            } else if (this == DELETE_RECENT_CHUNK) {
                Object[] commandData = command.getData(count);
                int x = (int)commandData[0];
                int z = (int)commandData[1];
                CoordsPair coordsPair = new CoordsPair(x,z);
                playerListener.getRecentlyLoadedChunks().remove(coordsPair);
            }
        }
    }
}
