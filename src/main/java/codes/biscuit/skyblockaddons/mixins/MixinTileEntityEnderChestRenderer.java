package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityEnderChestRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityEnderChestRenderer.class)
public abstract class MixinTileEntityEnderChestRenderer extends TileEntitySpecialRenderer<TileEntityEnderChest> {

    @Shadow
    @Final
    private static ResourceLocation ENDER_CHEST_TEXTURE;

    private final ResourceLocation GREEN_ENDERCHEST = new ResourceLocation("skyblockaddons", "greenenderchest.png");

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/tileentity/TileEntityEnderChestRenderer;bindTexture(Lnet/minecraft/util/ResourceLocation;)V",
                    ordinal = 1
            )
    )
    public void render(TileEntityEnderChest te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();

        // TODO: Does this even work?
        if (main.getUtils().isOnSkyblock() && mc.currentScreen == null && main.getConfigValues().isEnabled(Feature.MAKE_ENDERCHESTS_GREEN_IN_END) &&
                (main.getUtils().getLocation() == EnumUtils.Location.THE_END || main.getUtils().getLocation() == EnumUtils.Location.DRAGONS_NEST))
            bindTexture(GREEN_ENDERCHEST);
        else
            bindTexture(ENDER_CHEST_TEXTURE);
    }

}
