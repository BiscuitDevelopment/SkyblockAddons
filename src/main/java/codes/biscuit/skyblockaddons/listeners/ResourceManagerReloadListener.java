package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

public class ResourceManagerReloadListener implements IResourceManagerReloadListener {
    private static final ResourceLocation currentLocation = new ResourceLocation("skyblockaddons", "bars.png");

    /**
     * This method is called after Minecraft's resource manager reloads.
     *
     * @param resourceManager the resource manager that reloaded
     */
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        boolean usingOldPackTexture = false;
        boolean usingDefaultTexture = true;
        try(InputStream inputStream = resourceManager.getResource(currentLocation).getInputStream()) {
            String currentHash = DigestUtils.md5Hex(inputStream);

            // Hash for "assets/skyblockaddons/imperialoldbars.png"
            String oldHash = "ee7d133914d95f4cd840b62ebb862fb2";
            usingOldPackTexture = currentHash.equals(oldHash);

            // Hash for "assets/skyblockaddons/bars.png"
            String barsHash = "e593d648ff40aec9972423b65cf36a22";
            usingDefaultTexture = currentHash.equals(barsHash);
        } catch (IOException e) {
            SkyblockAddons.getLogger().error("Failed to get bar texture", e);
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main != null) { // Minecraft reloads textures before and after mods are loaded. So only set the variable if sba was initialized.
            main.getUtils().setUsingOldSkyBlockTexture(usingOldPackTexture);
            main.getUtils().setUsingDefaultBarTextures(usingDefaultTexture);
        }
    }
}
