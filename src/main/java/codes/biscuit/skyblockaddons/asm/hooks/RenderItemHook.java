package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderItemHook {

    private static final ResourceLocation BLANK = new ResourceLocation("skyblockaddons","blank.png");

    public static void renderToxicArrowPoisonEffect(IBakedModel model, ItemStack stack) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.TURN_BOW_GREEN_WHEN_USING_TOXIC_ARROW_POISON)
                && main.getInventoryUtils().isUsingToxicArrowPoison() && Items.bow.equals(stack.getItem()) && main.getUtils().itemIsInHotbar(stack)) {
            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

            GlStateManager.depthMask(false);
            GlStateManager.depthFunc(514);
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(768, 1);
            textureManager.bindTexture(BLANK);
            GlStateManager.matrixMode(5890);

            GlStateManager.pushMatrix();

            Minecraft.getMinecraft().getRenderItem().renderModel(model, 0x201cba41);
            GlStateManager.popMatrix();

            GlStateManager.matrixMode(5888);
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableLighting();
            GlStateManager.depthFunc(515);
            GlStateManager.depthMask(true);
            textureManager.bindTexture(TextureMap.locationBlocksTexture);
        }
    }
}
