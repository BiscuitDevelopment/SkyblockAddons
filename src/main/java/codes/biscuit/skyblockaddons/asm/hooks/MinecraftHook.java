package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

public class MinecraftHook {

    private static final ResourceLocation currentLocation = new ResourceLocation("skyblockaddons", "bars.png");

    private static long lastProfileMessage = -1;

    public static void onRefreshResources(IReloadableResourceManager iReloadableResourceManager) {
        boolean usingOldTexture = false;
        boolean usingDefaultTexture = true;
        try {
            IResource currentResource = iReloadableResourceManager.getResource(currentLocation);
            String currentHash = DigestUtils.md5Hex(currentResource.getInputStream());

            InputStream oldStream = SkyblockAddons.class.getClassLoader().getResourceAsStream("assets/skyblockaddons/imperialoldbars.png");
            if (oldStream != null) {
                String oldHash = DigestUtils.md5Hex(oldStream);
                usingOldTexture = currentHash.equals(oldHash);
            }

            InputStream barsStream = SkyblockAddons.class.getClassLoader().getResourceAsStream("assets/skyblockaddons/bars.png");
            if (barsStream != null) {
                String barsHash = DigestUtils.md5Hex(barsStream);
                usingDefaultTexture = currentHash.equals(barsHash);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main != null) { // Minecraft reloads textures before and after mods are loaded. So only set the variable if sba was initialized.
            main.getUtils().setUsingOldSkyBlockTexture(usingOldTexture);
            main.getUtils().setUsingDefaultBarTextures(usingDefaultTexture);
        }
    }

    public static void rightClickMouse(ReturnValue returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity entityIn = mc.objectMouseOver.entityHit;
                if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && entityIn instanceof EntityItemFrame && ((EntityItemFrame)entityIn).getDisplayedItem() == null) {
                    int slot = mc.thePlayer.inventory.currentItem + 36;
                    if (main.getConfigValues().getLockedSlots().contains(slot) && slot >= 9) {
                        main.getUtils().playLoudSound("note.bass", 0.5);
                        main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_SLOT_LOCKED.getMessage());
                        returnValue.cancel();
                    }
                }
            }
        }
    }
}
