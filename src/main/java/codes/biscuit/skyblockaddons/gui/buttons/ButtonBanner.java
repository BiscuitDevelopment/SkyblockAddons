package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ButtonBanner extends GuiButton {

    private SkyblockAddons main = SkyblockAddons.getInstance();

    private static ResourceLocation banner;
    private static BufferedImage bannerImage;

    private static boolean grabbedBanner;

    // Used to calculate the transparency when fading in.
    private long timeOpened = System.currentTimeMillis();

    private static final int WIDTH = 130;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonBanner(double x, double y) {
        super(0, (int)x, (int)y, "");

        if (!grabbedBanner) {
            grabbedBanner = true;
            bannerImage = null;
            banner = null;

            SkyblockAddons.newThread(() -> {
                try {
                    URL url = new URL(this.main.getOnlineData().getBannerImageURL());
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setReadTimeout(5000);
                    connection.addRequestProperty("User-Agent", Utils.USER_AGENT);

                    bannerImage = TextureUtil.readBufferedImage(connection.getInputStream());

                    connection.disconnect();

                    this.width = bannerImage.getWidth();
                    this.height = bannerImage.getHeight();
                } catch (IOException ex) {
                    SkyblockAddons.getLogger().info("Couldn't grab main menu banner image from URL, falling back to local banner.");
                }
            }).start();
        }

        xPosition -= WIDTH/2;

        if (bannerImage != null) {
            this.width = bannerImage.getWidth();
            this.height = bannerImage.getHeight();
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (bannerImage != null && banner == null) { // This means it was just loaded from the URL above.
            banner = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("banner", new DynamicTexture(bannerImage));
        }

        if (banner != null) { // Could have not been loaded yet.
            float alphaMultiplier = 1F;
            if (main.getUtils().isFadingIn()) {
                long timeSinceOpen = System.currentTimeMillis() - timeOpened;
                int fadeMilis = 500;
                if (timeSinceOpen <= fadeMilis) {
                    alphaMultiplier = (float) timeSinceOpen / fadeMilis;
                }
            }

            float scale = (float) WIDTH / bannerImage.getWidth(); // max width

            hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition +
                    WIDTH && mouseY < yPosition + bannerImage.getHeight() * scale;
            GlStateManager.enableBlend();

            if (hovered) {
                GlStateManager.color(1, 1, 1, alphaMultiplier * 1);
            } else {
                GlStateManager.color(1, 1, 1, alphaMultiplier * 0.8F);
            }

            mc.getTextureManager().bindTexture(banner);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            drawModalRectWithCustomSizedTexture(Math.round(xPosition / scale),
                    Math.round(yPosition / scale), 0, 0, width, height, width, height);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return hovered;
    }
}
