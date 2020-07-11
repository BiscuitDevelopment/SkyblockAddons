package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.client.Minecraft;

public class ButtonToggleTitle extends ButtonToggle {

    private SkyblockAddons main;

    public ButtonToggleTitle(double x, double y, String buttonText, SkyblockAddons main, Feature feature) {
        super(x, y, main, feature);
        displayString = buttonText;
        this.main = main;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        int fontColor = main.getUtils().getDefaultBlue(255);
        drawCenteredString(mc.fontRendererObj, displayString, xPosition+width/2, yPosition-10, fontColor);
    }
}
