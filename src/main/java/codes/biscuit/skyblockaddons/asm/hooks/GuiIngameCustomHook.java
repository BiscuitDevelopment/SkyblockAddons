package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;

/**
 * Alternative hooks for the labymod custom gui, to disable specific bars.
 */
public class GuiIngameCustomHook {

    public static boolean shouldRenderArmor() {
        return shouldRender(Feature.HIDE_FOOD_ARMOR_BAR);
    }

    public static boolean shouldRenderHealth() {
        return shouldRender(Feature.HIDE_HEALTH_BAR);
    }

    public static boolean shouldRenderFood() {
        return shouldRender(Feature.HIDE_FOOD_ARMOR_BAR);
    }

    public static boolean shouldRenderMountHealth() {
        return shouldRender(Feature.HIDE_PET_HEALTH_BAR);
    }

    public static boolean shouldRender(Feature feature) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (!main.getUtils().isOnSkyblock()) {
            return true;
        }
        return !main.getConfigValues().isEnabled(feature);
    }
}
