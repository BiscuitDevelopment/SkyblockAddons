package codes.biscuit.skyblockaddons.events;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * As of the Dwarven Mines update, all block-breaking on public islands has been migrated to server-side control.
 * Whereas vanilla Minecraft uses {@link net.minecraft.client.multiplayer.PlayerControllerMP#curBlockDamageMP} to track
 * block-breaking progress, and then fires {@link net.minecraft.client.multiplayer.PlayerControllerMP#onPlayerDestroyBlock(BlockPos, EnumFacing)}
 * when {@link net.minecraft.client.multiplayer.PlayerControllerMP#curBlockDamageMP} > 1F, this no longer fires in public islands
 */
public class SkyblockBlockBreakEvent extends Event {

    public BlockPos blockPos;
    public long timeToBreak;

    public SkyblockBlockBreakEvent(BlockPos pos) {
        this(pos, 0);
    }

    public SkyblockBlockBreakEvent(BlockPos pos, long breakTime) {
        blockPos = pos;
        timeToBreak = breakTime;
    }


}
