package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import net.minecraft.util.ResourceLocation;

public class RenderEndermanHook {

    private static final ResourceLocation BLANK_ENDERMAN_TEXTURE = new ResourceLocation("skyblockaddons", "blankenderman.png");

    public static ResourceLocation getEndermanTexture(ResourceLocation endermanTexture) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Location location = main.getUtils().getLocation();
        if (main.getUtils().isOnSkyblock() && (location == Location.DRAGONS_NEST || location == Location.ZEALOT_BRUISER_HIDEOUT || location == Location.VOID_SLATE) && main.getConfigValues().isEnabled(Feature.CHANGE_ZEALOT_COLOR)) {
            return BLANK_ENDERMAN_TEXTURE;
        }
        return endermanTexture;
    }
}
