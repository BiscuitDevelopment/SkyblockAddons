package codes.biscuit.skyblockaddons.newgui.themes;

import codes.biscuit.skyblockaddons.newgui.themes.elements.ContainerTheme;
import lombok.AllArgsConstructor;

import java.io.File;

@AllArgsConstructor
public class CustomTheme extends Theme {

    private DefaultTheme baseTheme;

    private File file;

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public ContainerTheme getContainerOne() {
        return containerOne == null ? baseTheme.containerOne : containerOne;
    }
}
