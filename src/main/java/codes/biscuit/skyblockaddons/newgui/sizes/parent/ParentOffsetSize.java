package codes.biscuit.skyblockaddons.newgui.sizes.parent;

import codes.biscuit.skyblockaddons.newgui.GuiElement;
import codes.biscuit.skyblockaddons.newgui.sizes.SizeBase;

public class ParentOffsetSize extends SizeBase {

    private GuiElement<?> guiElement;
    private float x;
    private float y;

    public ParentOffsetSize(GuiElement<?> guiElement, float x, float y) {
        this.guiElement = guiElement;
        this.x = x;
        this.y = y;
    }

    public ParentOffsetSize(GuiElement<?> guiElement, float offset) {
        this(guiElement, offset, offset);
    }

    @Override
    public void updateSizes() {
        h = guiElement.getParent().getH() + x;
        w = guiElement.getParent().getW() + y;
    }

    @Override
    public void updatePositions() {
        y = guiElement.getParent().getY() + x;
        x = guiElement.getParent().getX() + y;
    }
}
