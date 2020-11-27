package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

@Getter
public class ButtonLocation extends ButtonFeature {

    // So we know the latest hovered feature (used for arrow key movement).
    @Getter private static Feature lastHoveredFeature = null;

    private SkyblockAddons main = SkyblockAddons.getInstance();

    private float boxXOne;
    private float boxXTwo;
    private float boxYOne;
    private float boxYTwo;

    private float scale;

    /**
     * Create a button that allows you to change the location of a GUI element.
     */
    public ButtonLocation(Feature feature) {
        super(-1, 0, 0, null, feature);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        float scale = main.getConfigValues().getGuiScale(feature);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);

        if (feature == Feature.DEFENCE_ICON) { // this one is just a little different
            scale *= 1.5;
            GlStateManager.scale(scale,scale,1);
            main.getRenderListener().drawIcon(scale, mc, this);
            scale /= 1.5;
            GlStateManager.scale(scale,scale,1);
        } else {
            feature.draw(scale, mc, this);
        }
        GlStateManager.popMatrix();

        if (hovered) {
            lastHoveredFeature = feature;
        }
    }

    /**
     * This just updates the hovered status and draws the box around each feature. To avoid repetitive code.
     */
    public void checkHoveredAndDrawBox(float boxXOne, float boxXTwo, float boxYOne, float boxYTwo, float scale) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        float minecraftScale = sr.getScaleFactor();
        float floatMouseX = Mouse.getX() / minecraftScale;
        float floatMouseY = (Minecraft.getMinecraft().displayHeight - Mouse.getY()) / minecraftScale;

        hovered = floatMouseX >= boxXOne * scale && floatMouseY >= boxYOne * scale && floatMouseX < boxXTwo * scale && floatMouseY < boxYTwo * scale;
        int boxAlpha = 70;
        if (hovered) {
            boxAlpha = 120;
        }
        int boxColor = ColorCode.GRAY.getColor(boxAlpha);
        main.getUtils().drawRect(boxXOne, boxYOne, boxXTwo, boxYTwo, boxColor);

        this.boxXOne = boxXOne;
        this.boxXTwo = boxXTwo;
        this.boxYOne = boxYOne;
        this.boxYTwo = boxYTwo;

        if (this.feature == Feature.DEFENCE_ICON) {
            this.boxXOne *= scale;
            this.boxXTwo *= scale;
            this.boxYOne *= scale;
            this.boxYTwo *= scale;
        }

        this.scale = scale;
    }

    /**
     * Because the box changes with the scale, have to override this.
     */
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.enabled && this.visible && hovered;
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {}
}
