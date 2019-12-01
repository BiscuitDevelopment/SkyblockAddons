package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
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
        try {
            IResource currentResource = iReloadableResourceManager.getResource(currentLocation);
            InputStream oldStream = SkyblockAddons.class.getClassLoader().getResourceAsStream("assets/skyblockaddons/imperialoldbars.png");
            if (oldStream != null) {
                String currentHash = DigestUtils.md5Hex(currentResource.getInputStream());
                String oldHash = DigestUtils.md5Hex(oldStream);

                usingOldTexture = currentHash.equals(oldHash);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main != null) { // Minecraft reloads textures before and after mods are loaded. So only set the variable if sba was initialized.
            main.getUtils().setUsingOldSkyBlockTexture(usingOldTexture);
        }
    }

    public static void rightClickMouse(ReturnValue returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity entityIn = mc.objectMouseOver.entityHit;
                if (main.getConfigValues().isEnabled(Feature.DONT_OPEN_PROFILES_WITH_BOW)) {
                    if (entityIn instanceof EntityOtherPlayerMP && main.getUtils().isNotNPC(entityIn)) {
                        ItemStack item = mc.thePlayer.inventory.getCurrentItem();
                        ItemStack itemInUse = mc.thePlayer.getItemInUse();
                        if ((isItemBow(item) || isItemBow(itemInUse))) {
                            if (System.currentTimeMillis() - lastProfileMessage > 20000) {
                                lastProfileMessage = System.currentTimeMillis();
                                main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.DONT_OPEN_PROFILES_WITH_BOW).getChatFormatting() +
                                        Message.MESSAGE_STOPPED_OPENING_PROFILE.getMessage());
                            }
                            returnValue.cancel();
                            return;
                        }
                    }
                }
                if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && entityIn instanceof EntityItemFrame && ((EntityItemFrame)entityIn).getDisplayedItem() == null) {
                    int slot = mc.thePlayer.inventory.currentItem + 36;
                    if (main.getConfigValues().getLockedSlots().contains(slot) && slot >= 9) {
                        main.getUtils().playLoudSound("note.bass", 0.5);
                        main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_SLOT_LOCKED.getMessage());
                        returnValue.cancel();
                    }
                }
            }
        }
    }

    private static boolean isItemBow(ItemStack item) {
        return item != null && item.getItem() != null && item.getItem().equals(Items.bow);
    }
}
