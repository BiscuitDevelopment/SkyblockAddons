package codes.biscuit.skyblockaddons.newgui.themes;

import codes.biscuit.skyblockaddons.newgui.themes.elements.ContainerTheme;
import codes.biscuit.skyblockaddons.utils.SkyblockColor;

public class DarkTheme extends DefaultTheme {

    public DarkTheme() {
        super("Dark");

        containerOne = new ContainerTheme(10, new SkyblockColor(0, 0, 0, 0.75F));
    }
}
