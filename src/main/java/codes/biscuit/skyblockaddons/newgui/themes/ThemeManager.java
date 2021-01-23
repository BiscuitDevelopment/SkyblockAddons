package codes.biscuit.skyblockaddons.newgui.themes;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ThemeManager {

    private static final ThemeManager INSTANCE = new ThemeManager();

    @Setter private Theme currentTheme = new DarkTheme();

    public static ThemeManager getInstance() {
        return INSTANCE;
    }
}
