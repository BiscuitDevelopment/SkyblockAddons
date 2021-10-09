package codes.biscuit.skyblockaddons.utils.draw;

import codes.biscuit.skyblockaddons.core.chroma.MulticolorShaderManager;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.opengl.GL11;

public abstract class DrawState {

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final WorldRenderer worldRenderer = tessellator.getWorldRenderer();


    protected boolean canAddVertices;
    protected int drawType;
    protected VertexFormat format;
    protected boolean textured;
    protected boolean ignoreTexture;
    protected SkyblockColor color;


    public DrawState(SkyblockColor theColor, int theDrawType, VertexFormat theFormat, boolean isTextured, boolean shouldIgnoreTexture) {
        color = theColor;
        drawType = theDrawType;
        format = theFormat;
        textured = isTextured;
        ignoreTexture = shouldIgnoreTexture;
        canAddVertices = true;
    }

    public DrawState(SkyblockColor theColor, boolean isTextured, boolean shouldIgnoreTexture) {
        color = theColor;
        ignoreTexture = shouldIgnoreTexture;
        textured = isTextured;
        canAddVertices = false;
    }

    public void beginWorld() {
        if (canAddVertices) {
            worldRenderer.begin(drawType, format);
        }
    }

    public void draw() {
        if (canAddVertices) {
            tessellator.draw();
        }
    }


    protected void newColor(boolean is3D) {
        if (color.drawMulticolorUsingShader()) {
            MulticolorShaderManager.begin(textured, ignoreTexture, is3D);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        }
        if (textured && ignoreTexture) {
            DrawUtils.enableOutlineMode();
            // Textured shader needs white color to work properly
            if (color.drawMulticolorUsingShader()) {
                DrawUtils.outlineColor(0xFFFFFFFF);
            }
            // Only set one color for a new color environment
            else {
                DrawUtils.outlineColor(color.getColor());
            }
        }
    }

    protected void bindColor(int colorInt) {
        if (textured && ignoreTexture) {
            if (color.isPositionalMulticolor() && color.drawMulticolorManually()) {
                DrawUtils.outlineColor(colorInt);
            }
        } else {
            GlStateManager.color(ColorUtils.getRed(colorInt) / 255F, ColorUtils.getGreen(colorInt) / 255F, ColorUtils.getBlue(colorInt) / 255F, ColorUtils.getAlpha(colorInt) / 255F);
        }
    }

    protected void endColor() {
        if (color.drawMulticolorUsingShader()) {
            MulticolorShaderManager.end();
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
        if (textured && ignoreTexture) {
            DrawUtils.disableOutlineMode();
        }
    }

    public void reColor(SkyblockColor newColor) {
        color = newColor;
    }
}
