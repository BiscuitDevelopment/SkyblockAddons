package codes.biscuit.skyblockaddons.utils;

public class CoordsPair {

    private float x;
    private float y;

    CoordsPair(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setX(float x) {
        this.x = x;
    }
}
