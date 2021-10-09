package codes.biscuit.skyblockaddons.newgui;

import net.minecraft.client.gui.GuiScreen;

public class SkyblockAddonsMinecraftGui extends GuiScreen {

    private GuiBase gui;

    public SkyblockAddonsMinecraftGui(GuiBase gui) {
        this.gui = gui;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        gui.render();
    }

    @Override
    public void onGuiClosed() {
        gui.close();
    }
}
