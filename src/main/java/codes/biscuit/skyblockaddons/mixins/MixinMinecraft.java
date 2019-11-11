package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.digest.DigestUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;

@Mixin(Minecraft.class)

public class MixinMinecraft {

    private final ResourceLocation currentLocation = new ResourceLocation("skyblockaddons", "bars.png");
    private final String oldPath = "assets/skyblockaddons/imperialoldbars.png";

    @Shadow private IReloadableResourceManager mcResourceManager;

    @Inject(method = "refreshResources", at = @At("RETURN"))
    private void onRefreshResources(CallbackInfo cb) {
        boolean usingOldTexture = false;

        try {
            IResource currentResource = mcResourceManager.getResource(currentLocation);
            InputStream oldStream = SkyblockAddons.class.getClassLoader().getResourceAsStream(oldPath);
            if (oldStream != null) {
                String currentHash = DigestUtils.md5Hex(currentResource.getInputStream());
                String oldHash = DigestUtils.md5Hex(oldStream);

                usingOldTexture = currentHash.equals(oldHash);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main != null) { // Minecraft reloads textures before and after mods are loaded. So only set the variable if sba was initialized
            main.getUtils().setUsingOldSkyBlockTexture(usingOldTexture);
        }
    }
}
