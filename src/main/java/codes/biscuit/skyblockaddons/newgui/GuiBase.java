package codes.biscuit.skyblockaddons.newgui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.MathUtils;
import lombok.experimental.Accessors;

import java.util.*;

@Accessors(chain = true)
public abstract class GuiBase {

    private Map<Integer, List<GuiElement<?>>> elements = new TreeMap<>(); // Layer -> Elements
    private float minX, maxX, minY, maxY;

    private void initInternal() {
        init();
        calculateBounds();
    }

    protected void init() {

    }

    protected void render() {
        this.renderElements();
    }

    protected void renderElements() {
        for (List<GuiElement<?>> layerElements : elements.values()) {
            for (GuiElement<?> element : layerElements) {
                element.render();
            }
        }
    }

    protected boolean onMouseClick(float x, float y, MouseButton mouseButton) {
        for (List<GuiElement<?>> layerElements : elements.values()) {
            for (GuiElement<?> element : layerElements) {
                // TODO Maybe allow an element to make their own conditions?
                if (!element.isInside(x, y)) {
                    continue;
                }

                if (element.onMouseClick(x, y, mouseButton)) {
                    return true;
                }
            }
        }

        return false;
    }

    public GuiBase openAsGUI() {
        initInternal();
        SkyblockAddons.getInstance().getGuiManager().openAsGUI(this);
        return this;
    }

    public GuiBase openAsOverlay() {
        initInternal();
        SkyblockAddons.getInstance().getGuiManager().openAsOverlay(this);
        return this;
    }


    public boolean isInside(int x, int y) {
        return isInside((float) x, (float) y);
    }

    public boolean isInside(float x, float y) {
        return MathUtils.isInside(x, y, minX, minY, maxX, maxY);
    }
    
    private void calculateBounds() {
        for (List<GuiElement<?>> elements : elements.values()) {
            for (GuiElement<?> element : elements) {
                minX = Math.min(minX, element.getX());
                maxX = Math.max(maxX, element.getX2());
                minY = Math.min(minY, element.getY());
                maxY = Math.max(maxY, element.getY2());
            }
        }
    }

    public GuiBase add(GuiElement<?> guiElement) {
        return this.add(0, guiElement);
    }

    public GuiBase add(GuiElement<?>... guiElements) {
        return this.add(0, guiElements);
    }

    public GuiBase add(int layer, GuiElement<?> guiElement) {
        if (!elements.containsKey(layer)) {
            elements.put(layer, new LinkedList<>());
        }
        elements.get(layer).add(guiElement);
        return this;
    }

    public GuiBase add(int layer, GuiElement<?>... guiElements) {
        if (!elements.containsKey(layer)) {
            elements.put(layer, new LinkedList<>());
        }
        Collections.addAll(elements.get(layer), guiElements);
        return this;
    }

    public void close() {
        SkyblockAddons.getInstance().getGuiManager().close(this);
    }

    public void setFocused(boolean focused) {
        SkyblockAddons.getInstance().getGuiManager().setFocused(this, focused);
    }

    public boolean isFocused() {
        return SkyblockAddons.getInstance().getGuiManager().isFocused(this);
    }
}
