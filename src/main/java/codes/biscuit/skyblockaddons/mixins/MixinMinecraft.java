package codes.biscuit.skyblockaddons.mixins;

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

    @Shadow private IReloadableResourceManager mcResourceManager;

    @Inject(method = "refreshResources", at = @At("RETURN"))
    private void onRefreshResources(CallbackInfo ci) {
        boolean usingOldTexture = false;

        try {
            IResource currentResource = mcResourceManager.getResource(currentLocation);
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
        if (main != null) { // Minecraft reloads textures before and after mods are loaded. So only set the variable if sba was initialized
            main.getUtils().setUsingOldSkyBlockTexture(usingOldTexture);
        }
    }

    private long lastProfileMessage = -1;

    @Inject(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z", shift = At.Shift.AFTER, ordinal = 0),
            cancellable = true)
    private void rightClickMouse(CallbackInfo ci) {
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
                            ci.cancel();
                            return;
                        }
                    }
                }
                if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && entityIn instanceof EntityItemFrame && ((EntityItemFrame)entityIn).getDisplayedItem() == null) {
                    int slot = mc.thePlayer.inventory.currentItem + 36;
                    if (main.getConfigValues().getLockedSlots().contains(slot) && slot >= 9) {
                        main.getUtils().playLoudSound("note.bass", 0.5);
                        main.getUtils().sendMessage(main.getConfigValues().getColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_SLOT_LOCKED.getMessage());
                        ci.cancel();
                    }
                }
            }
        }
    }

    private boolean isItemBow(ItemStack item) {
        return item != null && item.getItem() != null && item.getItem().equals(Items.bow);
    }
}
