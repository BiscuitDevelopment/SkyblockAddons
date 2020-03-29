package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * This button is for when you are choosing one of the 16 color codes.
 */
public class ButtonColorBox extends GuiButton {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 20;

    private ChatFormatting color;

    public ButtonColorBox(int x, int y, ChatFormatting color) {
        super(0, x, y, null);

        this.width = 40;
        this.height = 20;

        this.color = color;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        hovered = mouseX > xPosition && mouseX < xPosition+width && mouseY > yPosition && mouseY < yPosition+height;

        if (hovered) {
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color.getRGB());
        } else {
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color.getColor(127).getRGB());
        }
    }

    public ChatFormatting getColor() {
        return color;
    }
}
