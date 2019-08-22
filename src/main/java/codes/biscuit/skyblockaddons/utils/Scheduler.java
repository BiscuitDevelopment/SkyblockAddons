package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
     * @param delayTicks The delay in ticks (20 ticks = 1second)
     */
    public void schedule(CommandType commandType, int delayTicks) {
        long ticks = totalTicks+delayTicks;
        Set<Command> commands = queue.get(ticks);
        if (commands != null) {
            for (Command command : commands) {
                if (command.getCommandType() == commandType) {
                    command.addCount();
                    return;
                }
            }
            addNewCommand(commandType, ticks);
        } else {
            addNewCommand(commandType, ticks);
        }
    }

    private void addNewCommand(CommandType commandType, long ticks) {
        Set<Command> commandSet = new HashSet<>();
        commandSet.add(new Command(commandType));
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
                        command.getCommandType().execute();
                    }
                }
                queue.remove(totalTicks);
            }
            if (totalTicks % 18000 == 0) { // check magma boss every 15 minutes
                if (main.getPlayerListener().getMagmaAccuracy() == EnumUtils.MagmaTimerAccuracy.ABOUT) {
                    main.getUtils().fetchEstimateFromServer();
                }
            }
        }
    }

    private class Command {
        private CommandType commandType;
        private MutableInt count = new MutableInt(1);

        private Command(CommandType commandType) {
            this.commandType = commandType;
        }

        private void addCount() {
            count.increment();
        }

        CommandType getCommandType() {
            return commandType;
        }

        MutableInt getCount() {
            return count;
        }
    }

    public enum CommandType {
        RESET_MAGMA_PREDICTION,
        SUBTRACT_MAGMA_COUNT,
        SUBTRACT_BLAZE_COUNT;

        public void execute() {
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
            }
        }
    }
}
