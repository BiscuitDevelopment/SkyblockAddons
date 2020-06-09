package codes.biscuit.skyblockaddons.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.mutable.MutableFloat;

public class FloatPair {

    private MutableFloat x;
    private MutableFloat y;

    public FloatPair(float x, float y) {
        this.x = new MutableFloat(x);
        this.y = new MutableFloat(y);
    }

    public float getX() {
        return x.getValue();
    }

    public float getY() {
        return y.getValue();
    }

    public void setY(float y) {
        this.y.setValue(y);
    }

    public void setX(float x) {
        this.x.setValue(x);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) { return false; }
        if (other == this) { return true; }
        if (other.getClass() != getClass()) {
            return false;
        }
        FloatPair otherFloatPair = (FloatPair)other;
        return new EqualsBuilder().append(getX(), otherFloatPair.getX()).append(getY(), otherFloatPair.getY()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(83, 11).append(getX()).append(getY()).toHashCode();
    }

    @Override
    public String toString() {
        return getX()+"|"+getY();
    }

    public FloatPair cloneCoords() {
        return new FloatPair(getX(), getY());
    }
}
