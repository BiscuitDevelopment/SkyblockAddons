package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.chroma.MulticolorShaderManager;
import codes.biscuit.skyblockaddons.core.chroma.ManualChromaManager;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenShader;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * This button is for when you are choosing one of the 16 color codes.
 */
public class ButtonColorBox extends GuiButton {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 20;

    private final ColorCode color;

    public ButtonColorBox(int x, int y, ColorCode color) {
        super(0, x, y, null);

        this.width = 40;
        this.height = 20;

        this.color = color;
    }


    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        hovered = mouseX > xPosition && mouseX < xPosition+width && mouseY > yPosition && mouseY < yPosition+height;
        if (color == ColorCode.CHROMA && !MulticolorShaderManager.shouldUseChromaShaders()) {
            if (hovered) {
                drawChromaRect(xPosition, yPosition, xPosition + width, yPosition + height, 255);
            }
            else {
                drawChromaRect(xPosition, yPosition, xPosition + width, yPosition + height, 127);
            }
        }
        else {
            if (color == ColorCode.CHROMA && MulticolorShaderManager.shouldUseChromaShaders()) {
                ShaderManager.getInstance().enableShader(ChromaScreenShader.class);
            }
            if (hovered) {
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color.getColor());
            } else {
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color.getColor(127));
            }
            if (color == ColorCode.CHROMA && MulticolorShaderManager.shouldUseChromaShaders()) {
                ShaderManager.getInstance().disableShader();
            }
        }
    }

    public ColorCode getColor() {
        return color;
    }


    public static void drawChromaRect(int left, int top, int right, int bottom, int alpha)
    {
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        //GlStateManager.disableAlpha();

        //GlStateManager.color(1, 1, 1, 1);
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int colorLB = ManualChromaManager.getChromaColor(left, bottom, 1);
        int colorRB = ManualChromaManager.getChromaColor(right, bottom, 1);;
        int colorLT = ManualChromaManager.getChromaColor(left, top, 1);;
        int colorRT = ManualChromaManager.getChromaColor(right, top, 1);
        int colorMM = ManualChromaManager.getChromaColor((right+left)/2, (top+bottom)/2, 1);;
        // First triangle
        worldrenderer.pos(right, bottom, 0.0D).color(ColorUtils.getRed(colorRB), ColorUtils.getGreen(colorRB), ColorUtils.getBlue(colorRB), alpha).endVertex();
        worldrenderer.pos((right+left)/2, (top+bottom)/2, 0.0D).color(ColorUtils.getRed(colorMM), ColorUtils.getGreen(colorMM), ColorUtils.getBlue(colorMM), alpha).endVertex();
        worldrenderer.pos(left, top, 0.0D).color(ColorUtils.getRed(colorLT), ColorUtils.getGreen(colorLT), ColorUtils.getBlue(colorLT), alpha).endVertex();
        worldrenderer.pos(left, bottom, 0.0D).color(ColorUtils.getRed(colorLB), ColorUtils.getGreen(colorLB), ColorUtils.getBlue(colorLB), alpha).endVertex();
        // 2nd triangle
        worldrenderer.pos(right, bottom, 0.0D).color(ColorUtils.getRed(colorRB), ColorUtils.getGreen(colorRB), ColorUtils.getBlue(colorRB), alpha).endVertex();
        worldrenderer.pos(right, top, 0.0D).color(ColorUtils.getRed(colorRT), ColorUtils.getGreen(colorRT), ColorUtils.getBlue(colorRT), alpha).endVertex();
        worldrenderer.pos(left, top, 0.0D).color(ColorUtils.getRed(colorLT), ColorUtils.getGreen(colorLT), ColorUtils.getBlue(colorLT), alpha).endVertex();
        worldrenderer.pos((right+left)/2, (top+bottom)/2, 0.0D).color(ColorUtils.getRed(colorMM), ColorUtils.getGreen(colorMM), ColorUtils.getBlue(colorMM), alpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();//GlStateManager.enableAlpha();
    }
}
