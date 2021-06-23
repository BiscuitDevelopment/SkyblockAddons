package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import codes.biscuit.skyblockaddons.events.SkyblockBlockBreakEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;

import java.util.Iterator;
import java.util.Map;

public class WorldClientHook {

    public static void onEntityRemoved(Entity entityIn) {
        NPCUtils.getNpcLocations().remove(entityIn.getUniqueID());
    }

    public static void blockUpdated(BlockPos pos, IBlockState state) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            int BEDROCK_STATE = Block.getStateId(Blocks.bedrock.getDefaultState());
            int AIR_STATE = Block.getStateId(Blocks.air.getDefaultState());
            int stateBefore = Block.getStateId(mc.theWorld.getBlockState(pos));
            Iterator<Map.Entry<BlockPos, Long>> itr = MinecraftHook.recentlyClickedBlocks.entrySet().iterator();
            long currTime = System.currentTimeMillis();
            while (itr.hasNext()) {
                Map.Entry<BlockPos, Long> entry = itr.next();
                if (currTime - entry.getValue() < 300) {
                    break;
                }
                itr.remove();
            }
            // Fire event if the client is breaking a block that is not being broken by another player, and the block is changing
            // One infrequent bug is if client mining stone and it turns into ore randomly. This will trigger this method currently
            if (/*mc.playerController.getIsHittingBlock() && */MinecraftHook.recentlyClickedBlocks.containsKey(pos) &&
                    stateBefore != Block.getStateId(state) && stateBefore != BEDROCK_STATE && stateBefore != AIR_STATE) {
                // Get the player's ID (0 on public islands and the player's entity ID on private islands)
                Location location = SkyblockAddons.getInstance().getUtils().getLocation();
                // Blocks broken on guest islands don't count
                if (location == Location.GUEST_ISLAND || location == Location.ISLAND) {
                    return;
                }
                int playerID = /*location == Location.ISLAND || location == Location.GUEST_ISLAND ? mc.thePlayer.getEntityId() :*/ 0;
                // Don't fire if anyone else is mining the same block...
                // This will undercount your blocks if you broke the block before the other person
                // But the alternative is to overcount your blocks if someone else breaks the block before you...not much better
                // Also could mathematically determine a probability based on pos, yaw, pitch of other entities...worth it? ehh...
                boolean noOneElseMining = true;
                for (Map.Entry<Integer, DestroyBlockProgress> block : mc.renderGlobal.damagedBlocks.entrySet()) {
                    if (block.getKey() != playerID && block.getValue().getPosition().equals(pos)) {
                        noOneElseMining = false;
                    }
                }
                if (noOneElseMining) {

                    long mineTime = Math.max(System.currentTimeMillis() - MinecraftHook.startMineTime, 0);
                    MinecraftForge.EVENT_BUS.post(new SkyblockBlockBreakEvent(pos, mineTime));
                }
            }
        }
    }
}
