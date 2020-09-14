package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import net.minecraft.entity.Entity;

public class WorldClientHook {

    public static void onEntityRemoved(Entity entityIn) {
        NPCUtils.getNpcLocations().remove(entityIn.getUniqueID());
    }
}
