package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class ModelEndermanHook {

    public static void setEndermanColor() {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getUtils().getLocation() == Location.DRAGONS_NEST && main.getConfigValues().isEnabled(Feature.CHANGE_ZEALOT_COLOR)) {
            Color color = main.getConfigValues().getColorObject(Feature.CHANGE_ZEALOT_COLOR);
            GlStateManager.color((float)color.getRed()/255, (float)color.getGreen()/255, (float)color.getBlue()/255);
        }
    }
}
