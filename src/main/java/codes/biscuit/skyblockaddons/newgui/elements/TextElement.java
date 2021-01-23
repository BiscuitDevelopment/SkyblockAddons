package codes.biscuit.skyblockaddons.newgui.elements;

import codes.biscuit.skyblockaddons.newgui.GuiElement;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.GlStateManager;

@RequiredArgsConstructor
public class TextElement extends GuiElement<TextElement> {

    private final String text;

    private float scale = 1.0F;

    @Override
    public void render() {
        if (scale != 1.0F) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
        }

        DrawUtils.drawText(text, getX(), getY(), 0xFF_FF_FF_FF);

        if (scale != 1.0F) {
            GlStateManager.popMatrix();
        }
    }
}
