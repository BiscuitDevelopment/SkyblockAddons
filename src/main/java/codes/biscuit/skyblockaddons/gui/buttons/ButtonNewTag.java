package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.utils.ColorCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;

public class ButtonNewTag extends GuiButton {

    public ButtonNewTag(int x, int y) {
        super(0, x, y, "NEW");

        width = 25;
        height = 11;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {

        drawRect(xPosition, yPosition, xPosition+width, yPosition+height, ColorCode.RED.getColor());
        mc.fontRendererObj.drawString(displayString, xPosition+4, yPosition+2, ColorCode.WHITE.getColor());
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
    }
}
