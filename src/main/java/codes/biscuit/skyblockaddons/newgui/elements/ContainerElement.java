package codes.biscuit.skyblockaddons.newgui.elements;

import codes.biscuit.skyblockaddons.newgui.GuiElement;
import codes.biscuit.skyblockaddons.newgui.themes.DarkTheme;
import codes.biscuit.skyblockaddons.newgui.themes.Theme;
import codes.biscuit.skyblockaddons.newgui.themes.ThemeManager;
import codes.biscuit.skyblockaddons.utils.DrawUtils;

public class ContainerElement extends GuiElement<ContainerElement> {

    @Override
    public void render() {
        ThemeManager.getInstance().setCurrentTheme(new DarkTheme()); // TODO Remove!
        Theme theme = ThemeManager.getInstance().getCurrentTheme();

        DrawUtils.drawRect(this.getX(), this.getY(), this.getW(), this.getH(), theme.getContainerOne().getColor(), theme.getContainerOne().getRounding());
    }

}
