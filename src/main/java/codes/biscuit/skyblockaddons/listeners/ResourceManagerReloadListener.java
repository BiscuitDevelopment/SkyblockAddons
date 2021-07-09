package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class ResourceManagerReloadListener implements IResourceManagerReloadListener {
    private static final ResourceLocation containerPreviewLocation = new ResourceLocation("skyblockaddons", "containerPreview.png");

    private static final Pattern furfSkyRebornName = Pattern.compile("FurfSky Reborn.*");

    /**
     * This method is called after Minecraft's resource manager reloads.
     *
     * @param resourceManager the resource manager that reloaded
     */
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        boolean usingFSR = false;
        try {
            IResource inputStream = resourceManager.getResource(containerPreviewLocation);
            if (furfSkyRebornName.matcher(TextUtils.stripColor(inputStream.getResourcePackName())).matches()) {
                usingFSR = true;
            }
        } catch (IOException e) {
            SkyblockAddons.getLogger().error("Failed to get containerPreview texture", e);
        }
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main != null) { // Minecraft reloads textures before and after mods are loaded. So only set the variable if sba was initialized.
            main.getUtils().setUsingFSRcontainerPreviewTexture(usingFSR);
        }
    }
}
