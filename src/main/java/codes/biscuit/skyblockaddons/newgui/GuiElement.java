package codes.biscuit.skyblockaddons.newgui;

import codes.biscuit.skyblockaddons.newgui.sizes.SizeBase;
import codes.biscuit.skyblockaddons.newgui.sizes.parent.ParentOffsetSize;
import codes.biscuit.skyblockaddons.newgui.sizes.screen.ScreenPercentageSize;
import codes.biscuit.skyblockaddons.newgui.sizes.staticc.StaticSize;
import codes.biscuit.skyblockaddons.utils.MathUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class GuiElement<T extends GuiElement> {

    @Getter private GuiElement<?> parent;
    private List<GuiElement<?>> elements = new LinkedList<>();
    private SizeBase position;
    private SizeBase size;

    @Setter private Runnable onClickCallback;

    // TODO Register into new events system?
    public void init() {
    }

    // TODO Use new events system?
    public void render() {
    }

    public boolean onMouseClick(float x, float y, MouseButton mouseButton) {
        if (onClickCallback != null) {
            onClickCallback.run();
        }

        return true;
    }

    // Static Sizes
    public T xy(float x, float y) {
        this.position = new StaticSize().xy(x, y);
        return (T) this;
    }

    public T wh(float w, float h) {
        this.size = new StaticSize().wh(w, h);
        return (T) this;
    }

    // Screen Sizes
    public T screenPercentPosition(float percent) {
        position = new ScreenPercentageSize(percent);
        return (T) this;
    }

    public T screenPercentSize(float percent) {
        size = new ScreenPercentageSize(percent);
        return (T) this;
    }

    // Relative to Parent Sizes - Offset
    public T relativeXY(float xOffset, float yOffset) {
        this.position = new ParentOffsetSize(this, xOffset, yOffset);
        return (T) this;
    }

    public T relativeWH(float wOffset, float hOffset) {
        this.size = new ParentOffsetSize(this, wOffset, hOffset);
        return (T) this;
    }

    // Relative to Parent Sizes - Percent
    public T relativeXYPercent(float xPercent, float yPercent) {
        this.position = new ParentOffsetSize(this, xPercent, yPercent);
        return (T) this;
    }

    public T relativeWHPercent(float wPercent, float hPercent) {
        this.size = new ParentOffsetSize(this, wPercent, hPercent);
        return (T) this;
    }

    public T fillToScreen() {
        return fillToScreenWithMargin(0);
    }

    public T fillToScreenWithMargin(float marginPercentage) {
        screenPercentPosition(marginPercentage);
        screenPercentSize(1 - (marginPercentage * 2));
        return (T) this;
    }

    public float getX() {
        return position.getX();
    }

    public float getY() {
        return position.getY();
    }

    public float getX2() {
        return this.getX() + this.getW();
    }

    public float getY2() {
        return this.getY() + this.getH();
    }

    public float getW() {
        return size.getW();
    }

    public float getH() {
        return size.getH();
    }

    public boolean isInside(float x, float y) {
        return MathUtils.isInside(x, y, getX(), getY(), getX2(), getY2());
    }

    public T add(GuiElement<?> guiElement) {
        elements.add(guiElement);
        guiElement.parent = this;
        return (T) this;
    }

    public T add(GuiElement<?>... guiElements) {
        for (GuiElement<?> guiElement : guiElements) {
            elements.add(guiElement);
            guiElement.parent = this;
        }
        return (T) this;
    }
}
