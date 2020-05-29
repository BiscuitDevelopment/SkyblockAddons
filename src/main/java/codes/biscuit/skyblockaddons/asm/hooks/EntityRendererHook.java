package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.npc.NPCUtils;
import codes.biscuit.skyblockaddons.utils.npc.Tag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import org.lwjgl.input.Mouse;

import java.util.List;

public class EntityRendererHook {

    public static void removeEntities(List<Entity> list) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock()) {

            if (!GuiScreen.isCtrlKeyDown() && Mouse.isButtonDown(1) && main.getConfigValues().isEnabled(Feature.IGNORE_ITEM_FRAME_CLICKS)) {
                list.removeIf(listEntity -> listEntity instanceof EntityItemFrame &&
                        (((EntityItemFrame)listEntity).getDisplayedItem() != null || Minecraft.getMinecraft().thePlayer.getHeldItem() == null));
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_PLAYERS_NEAR_NPCS)) {
                list.removeIf(entity -> entity instanceof EntityOtherPlayerMP && NPCUtils.isNearAnyNPCWithTag(entity, Tag.IMPORTANT) && !NPCUtils.isNPC(entity));
            }
        }
    }

    public static void preventBlink(ReturnValue<Float> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isEnabled(Feature.AVOID_BLINKING_NIGHT_VISION)) {
            returnValue.cancel(1.0F);
        }
    }
}
