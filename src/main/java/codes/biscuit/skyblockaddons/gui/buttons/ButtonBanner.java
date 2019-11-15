package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ButtonBanner extends GuiButton {

    private SkyblockAddons main;

    private static final ResourceLocation BANNER = new ResourceLocation("skyblockaddons", "featuredbanner.png");
    private static BufferedImage bannerImage = null;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    private static final int WIDTH = 130;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonBanner(double x, double y, SkyblockAddons main) {
        super(0, (int)x, (int)y, "");
        this.main = main;

        try {
            bannerImage = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(BANNER).getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        xPosition -= WIDTH/2;

        this.width = bannerImage.getWidth();
        this.height = bannerImage.getHeight();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        float alphaMultiplier = 1F;
        if (main.getUtils().isFadingIn()) {
            long timeSinceOpen = System.currentTimeMillis() - timeOpened;
            int fadeMilis = 500;
            if (timeSinceOpen <= fadeMilis) {
                alphaMultiplier = (float) timeSinceOpen / fadeMilis;
            }
        }

        float scale = (float)WIDTH/bannerImage.getWidth(); // max width


//        System.out.println(mouseX);
//        System.out.println(xPosition);

        hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition+
                WIDTH && mouseY < yPosition + bannerImage.getHeight()*scale;
        GlStateManager.enableBlend();

        if (hovered) {
            GlStateManager.color(1,1,1,alphaMultiplier*1);
        } else {
            GlStateManager.color(1,1,1,alphaMultiplier*0.8F);
        }

        mc.getTextureManager().bindTexture(BANNER);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        drawModalRectWithCustomSizedTexture(Math.round(xPosition/scale),
                Math.round(yPosition/scale), 0, 0, width, height, width, height);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return hovered;
    }
}
