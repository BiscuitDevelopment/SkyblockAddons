package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.utils.ColorUtils;

public class ModelEndermanHook {

    public static void setEndermanColor() {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getUtils().getLocation() == Location.DRAGONS_NEST && main.getConfigValues().isEnabled(Feature.CHANGE_ZEALOT_COLOR)) {
            int color = main.getConfigValues().getColor(Feature.CHANGE_ZEALOT_COLOR);
            ColorUtils.bindColor(color);
        }
    }
}
