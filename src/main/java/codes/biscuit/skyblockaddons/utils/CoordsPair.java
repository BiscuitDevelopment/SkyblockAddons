package codes.biscuit.skyblockaddons.utils;

import org.apache.commons.lang3.mutable.MutableInt;

public class CoordsPair {

    private MutableInt x;
    private MutableInt y;

    CoordsPair(int x, int y) {
        this.x = new MutableInt(x);
        this.y = new MutableInt(y);
    }

    public int getX() {
        return x.getValue();
    }

    public int getY() {
        return y.getValue();
    }

    public void setY(int y) {
        this.y.setValue(y);
    }

    public void setX(int x) {
        this.x.setValue(x);
    }
}
