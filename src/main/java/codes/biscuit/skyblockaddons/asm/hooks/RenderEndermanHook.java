package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import net.minecraft.util.ResourceLocation;

public class RenderEndermanHook {

    private static final ResourceLocation BLANK_ENDERMAN_TEXTURE = new ResourceLocation("skyblockaddons", "blankenderman.png");

    public static ResourceLocation getEndermanTexture(ResourceLocation endermanTexture) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getUtils().getLocation() == Location.DRAGONS_NEST && main.getConfigValues().isEnabled(Feature.CHANGE_ZEALOT_COLOR)) {
            return BLANK_ENDERMAN_TEXTURE;
        }
        return endermanTexture;
    }
}
