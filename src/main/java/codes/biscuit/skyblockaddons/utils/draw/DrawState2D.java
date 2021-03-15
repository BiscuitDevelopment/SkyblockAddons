package codes.biscuit.skyblockaddons.utils.draw;

import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;

public class DrawState2D extends DrawState {

    private static final WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();


    public DrawState2D(SkyblockColor theColor, int theDrawType, VertexFormat theFormat, boolean isTextured, boolean shouldIgnoreTexture) {
        super(theColor, theDrawType, theFormat, isTextured, shouldIgnoreTexture);
    }

    public DrawState2D(SkyblockColor theColor, boolean isTextured, boolean shouldIgnoreTexture) {
        super(theColor, isTextured, shouldIgnoreTexture);
    }

    public DrawState2D newColorEnv() {
        super.newColor(false);
        return this;
    }

    public DrawState2D endColorEnv() {
        super.endColor();
        return this;
    }

    public DrawState2D setColor(SkyblockColor color) {
        super.reColor(color);
        return this;
    }

    public DrawState2D bindActualColor() {
        super.bindColor(color.getColor());
        return this;
    }

    public DrawState2D bindAnimatedColor(float x, float y) {
        super.bindColor(color.getColorAtPosition(x, y));
        return this;
    }

    public DrawState2D addColoredVertex(float x, float y) {
        // Add a new position in the world with the correct color
        if (canAddVertices) {
            if (color.drawMulticolorManually()) {
                int colorInt = color.getColorAtPosition(x, y);
                worldRenderer.pos(x, y, 0).color(ColorUtils.getRed(colorInt), ColorUtils.getGreen(colorInt), ColorUtils.getBlue(colorInt), ColorUtils.getAlpha(colorInt)).endVertex();
            } else {
                worldRenderer.pos(x, y, 0).endVertex();
            }
        }
        // If we aren't actually adding world render positions (we are just setting the color)
        else {
            bindAnimatedColor(x, y);
        }
        return this;
    }
}
