package codes.biscuit.skyblockaddons.newgui.sizes.parent;

import codes.biscuit.skyblockaddons.newgui.GuiElement;
import codes.biscuit.skyblockaddons.newgui.sizes.SizeBase;

public class ParentPercentageSize extends SizeBase {

    private GuiElement<?> guiElement;
    private float xPercentage;
    private float yPercentage;

    public ParentPercentageSize(GuiElement<?> guiElement, float xPercentage, float yPercentage) {
        this.guiElement = guiElement;
        this.xPercentage = xPercentage;
        this.yPercentage = yPercentage;
    }

    public ParentPercentageSize(GuiElement<?> guiElement, float percentage) {
        this(guiElement, percentage, percentage);
    }

    @Override
    public void updateSizes() {
        h = guiElement.getParent().getH() * xPercentage;
        w = guiElement.getParent().getW() * yPercentage;
    }

    @Override
    public void updatePositions() {
        y = guiElement.getParent().getY() * xPercentage;
        x = guiElement.getParent().getX() * yPercentage;
    }
}
