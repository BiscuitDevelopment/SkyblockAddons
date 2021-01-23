package codes.biscuit.skyblockaddons.newgui;

import codes.biscuit.skyblockaddons.newgui.chronomatron.ChronomatronGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.util.LinkedList;
import java.util.ListIterator;

public class GuiManager {

    private LinkedList<GuiBase> openGuis = new LinkedList<>();
    private GuiBase focused;

    public void render() {
        openGuis.clear();
        new ChronomatronGui().openAsOverlay();

        GlStateManager.pushMatrix();
        float minecraftScale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        GlStateManager.scale(1 / minecraftScale, 1 / minecraftScale, 1);
        for (GuiBase openGui : openGuis) {
            openGui.render();
        }
        GlStateManager.popMatrix();
    }

    public void onMouseClick(float x, float y, int keyCode) {
        MouseButton mouseButton = MouseButton.fromKeyCode(keyCode);

        ListIterator<GuiBase> openGuisIterator = openGuis.listIterator();
        while (openGuisIterator.hasPrevious()) {
            GuiBase openGui = openGuisIterator.previous();

            if (!openGui.isInside(x, y)) {
                continue;
            }

            if (!openGui.isFocused()) {
                setFocused(openGui, true);
                return;
            }

            if (openGui.onMouseClick(x, y, mouseButton)) {
                return;
            }
        }
    }

    public void onKeyPress(int keyCode, char key) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            openGuis.removeLast();
        }
    }

    public void openAsGUI(GuiBase gui) {
        Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsMinecraftGui(gui));
    }

    public void openAsOverlay(GuiBase gui) {
        openGuis.add(gui);
        gui.setFocused(true);
    }

    public void setFocused(GuiBase gui, boolean focused) {
        // Set focus and move gui to the front
        if (focused) {
            this.focused = gui;

            if (openGuis.get(openGuis.size()-1) != gui) {
                openGuis.remove(gui);
                openGuis.add(gui);
            }

        // Otherwise remove focus
        } else if (this.focused == gui) {
            this.focused = null;
        }
    }

    public boolean isOpen(GuiBase gui) {
        return isOpen(gui.getClass());
    }

    public boolean isOpen(Class<? extends GuiBase> gui) {
        for (GuiBase openGui : openGuis) {
            if (openGui.getClass().equals(gui)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFocused(GuiBase gui) {
        return gui == focused;
    }

    public void close(GuiBase gui) {
        if (gui.isFocused()) {
            focused = null;
        }
        openGuis.remove(gui);
    }
}
