package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
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

        drawRect(xPosition, yPosition, xPosition+width, yPosition+height, ChatFormatting.RED.getRGB());
        MinecraftReflection.FontRenderer.drawString(displayString, xPosition+4, yPosition+2, ChatFormatting.WHITE.getRGB());
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
    }
}
