package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityEnderChestRenderer;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TileEntityEnderChestRendererHook {

    private static final ResourceLocation BLANK_ENDERCHEST = new ResourceLocation("skyblockaddons", "enderchest.png");

    private static Method bindTexture = null;

    public static void bindTexture(TileEntityEnderChestRenderer tileEntityEnderChestRenderer, ResourceLocation enderChestTexture) {
        try {
            SkyblockAddons main = SkyblockAddons.getInstance();
            if (bindTexture == null) {
                bindTexture = tileEntityEnderChestRenderer.getClass().getSuperclass().getDeclaredMethod(main.getUtils().isDevEnviroment() ? "bindTexture" : "func_147499_a", ResourceLocation.class);
                bindTexture.setAccessible(true);
            }

            if (main.getUtils().isOnSkyblock() && Minecraft.getMinecraft().currentScreen == null && main.getConfigValues().isEnabled(Feature.MAKE_ENDERCHESTS_GREEN_IN_END) &&
                    (main.getUtils().getLocation() == EnumUtils.Location.THE_END || main.getUtils().getLocation() == EnumUtils.Location.DRAGONS_NEST)) {
                bindTexture.invoke(tileEntityEnderChestRenderer, BLANK_ENDERCHEST);
            } else {
                bindTexture.invoke(tileEntityEnderChestRenderer, enderChestTexture);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void setEnderchestColor() {
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
