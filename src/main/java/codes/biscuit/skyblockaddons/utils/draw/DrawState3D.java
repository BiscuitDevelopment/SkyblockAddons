package codes.biscuit.skyblockaddons.utils.draw;

import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;

public class DrawState3D extends DrawState {

    private static final WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

    public DrawState3D(SkyblockColor theColor, int theDrawType, VertexFormat theFormat, boolean isTextured, boolean shouldIgnoreTexture) {
        super(theColor, theDrawType, theFormat, isTextured, shouldIgnoreTexture);
    }

    public DrawState3D(SkyblockColor theColor, boolean isTextured, boolean shouldIgnoreTexture) {
        super(theColor, isTextured, shouldIgnoreTexture);
    }

    public DrawState3D newColorEnv() {
        super.newColor(true);
        return this;
    }

    public DrawState3D endColorEnv() {
        super.endColor();
        return this;
    }

    public DrawState3D setColor(SkyblockColor color) {
        super.reColor(color);
        return this;
    }

    public DrawState3D beginWorldRenderer() {
        super.beginWorld();
        return this;
    }

    public DrawState3D bindColor(float x, float y, float z) {
        super.bindColor(color.getColorAtPosition(x, y, z));
        return this;
    }

    public DrawState3D addColoredVertex(float x, float y, float z) {
        // Add a new position in the world with the correct color
        if (canAddVertices) {
            if (color.drawMulticolorManually()) {
                int colorInt = color.getColorAtPosition(x, y);
                worldRenderer.pos(x, y, z).color(ColorUtils.getRed(colorInt), ColorUtils.getGreen(colorInt), ColorUtils.getBlue(colorInt), ColorUtils.getAlpha(colorInt)).endVertex();
            } else {
                worldRenderer.pos(x, y, z).endVertex();
            }
        }
        // If we aren't actually adding world render positions (we are just setting the color)
        else {
            bindColor(x, y, z);
        }
        return this;
    }

}
