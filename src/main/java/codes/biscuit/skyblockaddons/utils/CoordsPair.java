package codes.biscuit.skyblockaddons.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.mutable.MutableInt;

public class CoordsPair {

    private MutableInt x;
    private MutableInt y;

    public CoordsPair(int x, int y) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
            return false;
        }
        CoordsPair chunkCoords = (CoordsPair)obj;
        return new EqualsBuilder().append(getX(), chunkCoords.getX()).append(getY(), chunkCoords.getY()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(83, 11).append(getX()).append(getY()).toHashCode();
    }

    @Override
    public String toString() {
        return getX()+"|"+getY();
    }
}
