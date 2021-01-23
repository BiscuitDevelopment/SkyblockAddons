package codes.biscuit.skyblockaddons.newgui.sizes.staticc;

import codes.biscuit.skyblockaddons.newgui.sizes.SizeBase;

public class StaticSize extends SizeBase {

    public StaticSize xy(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public StaticSize wh(float w, float h) {
        this.w = w;
        this.h = h;
        return this;
    }
}
