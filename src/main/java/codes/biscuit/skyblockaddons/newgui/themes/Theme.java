package codes.biscuit.skyblockaddons.newgui.themes;

import codes.biscuit.skyblockaddons.newgui.themes.elements.ContainerTheme;
import lombok.Getter;

@Getter
public abstract class Theme {

    protected ContainerTheme containerOne;

    public abstract String getName();
}
