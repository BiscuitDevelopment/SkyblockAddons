package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.utils.ColorUtils;

public class ModelEndermanHook {

    public static void setEndermanColor() {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Location location = main.getUtils().getLocation();
        if (main.getUtils().isOnSkyblock() && (location == Location.DRAGONS_NEST || location == Location.ZEALOT_BRUISER_HIDEOUT || location == Location.VOID_SLATE) && main.getConfigValues().isEnabled(Feature.CHANGE_ZEALOT_COLOR)) {
            int color = main.getConfigValues().getColor(Feature.CHANGE_ZEALOT_COLOR);
            ColorUtils.bindColor(color);
        }
    }
}
