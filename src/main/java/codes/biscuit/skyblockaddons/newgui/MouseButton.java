package codes.biscuit.skyblockaddons.newgui;

public enum MouseButton {

    LEFT,
    RIGHT,
    MIDDLE;

    public static MouseButton fromKeyCode(int keyCode) {
        if (keyCode == 1) {
            return RIGHT;
        }

        return null;
    }
}
