package codes.biscuit.skyblockaddons.newgui.themes;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultTheme extends Theme {

    private String name;

    @Override
    public String getName() {
        return name;
    }
}
