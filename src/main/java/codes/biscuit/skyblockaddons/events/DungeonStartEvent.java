package codes.biscuit.skyblockaddons.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class DungeonStartEvent extends Event {
    public final String serverId;

    public DungeonStartEvent(String serverId) {
        this.serverId = serverId;
    }
}
