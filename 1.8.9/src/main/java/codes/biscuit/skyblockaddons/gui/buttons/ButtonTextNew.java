package codes.biscuit.skyblockaddons.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ButtonTextNew extends GuiButton {

    private boolean centered;
    private int color;

    public ButtonTextNew(int x, int y, String text, boolean centered, int color) {
        super(0, x, y, text);

        this.centered = centered;
        this.color = color;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {

        int x = xPosition;
        int y = yPosition;

        if (centered) {
            x -= mc.fontRendererObj.getStringWidth(displayString)/2;
        }

        mc.fontRendererObj.drawString(displayString, x, y, color);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return false;
    }
}
