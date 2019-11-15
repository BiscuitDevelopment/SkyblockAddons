package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityEnderChestRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityEnderChestRenderer.class)
public abstract class MixinTileEntityEnderChestRenderer extends TileEntitySpecialRenderer<TileEntityEnderChest> {

    @Shadow @Final private static ResourceLocation ENDER_CHEST_TEXTURE;

    private final ResourceLocation BLANK_ENDERCHEST = new ResourceLocation("skyblockaddons", "enderchest.png");

    @Redirect(method = "renderTileEntityAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityEnderChestRenderer;bindTexture(Lnet/minecraft/util/ResourceLocation;)V",
            ordinal = 1))
    private void bindTexture(TileEntityEnderChestRenderer tileEntityEnderChestRenderer, ResourceLocation location) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && Minecraft.getMinecraft().currentScreen == null && main.getConfigValues().isEnabled(Feature.MAKE_ENDERCHESTS_GREEN_IN_END) &&
                (main.getUtils().getLocation() == EnumUtils.Location.THE_END || main.getUtils().getLocation() == EnumUtils.Location.DRAGONS_NEST)) {
            bindTexture(BLANK_ENDERCHEST);
        } else {
            bindTexture(ENDER_CHEST_TEXTURE);
        }
    }

    @Inject(method = "renderTileEntityAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelChest;renderAll()V", ordinal = 0))
    private void renderAll(TileEntityEnderChest te, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && Minecraft.getMinecraft().currentScreen == null && main.getConfigValues().isEnabled(Feature.MAKE_ENDERCHESTS_GREEN_IN_END) &&
                (main.getUtils().getLocation() == EnumUtils.Location.THE_END || main.getUtils().getLocation() == EnumUtils.Location.DRAGONS_NEST)) {
            ConfigColor color = main.getConfigValues().getColor(Feature.MAKE_ENDERCHESTS_GREEN_IN_END);
            if (color == ConfigColor.GREEN) {
                GlStateManager.color(0, 1, 0); // classic lime green
            } else {
                GlStateManager.color((float)color.getR()/255, (float)color.getG()/255, (float)color.getB()/255);
            }
        }
    }
}
