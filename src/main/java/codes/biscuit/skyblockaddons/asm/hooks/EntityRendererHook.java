package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;

import java.util.List;

public class EntityRendererHook {

    public static void removeEntities(List<Entity> list) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            if (!GuiScreen.isCtrlKeyDown() && main.getConfigValues().isEnabled(Feature.IGNORE_ITEM_FRAME_CLICKS)) {
                list.removeIf(listEntity -> listEntity instanceof EntityItemFrame);
            }
            if (main.getConfigValues().isEnabled(Feature.HIDE_AUCTION_HOUSE_PLAYERS)) {
                list.removeIf((entity -> entity instanceof EntityOtherPlayerMP && EnumUtils.SkyblockNPC.isNearNPC(entity)));
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
