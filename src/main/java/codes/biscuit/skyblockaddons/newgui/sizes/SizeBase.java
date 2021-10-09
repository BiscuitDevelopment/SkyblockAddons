package codes.biscuit.skyblockaddons.newgui.sizes;

import codes.biscuit.skyblockaddons.SkyblockAddons;

public class SizeBase {

    private int lastPositionsUpdate, lastSizesUpdate;
    private boolean forceUpdate;
    protected float x, y, w, h;

    public float getX() {
        if (positionsNeedUpdate()) {
            updatePositions();
        }
        return x;
    }

    public float getY() {
        if (positionsNeedUpdate()) {
            updatePositions();
        }
        return y;
    }

    public float getW() {
        if (sizesNeedUpdate()) {
            updateSizes();
        }
        return w;
    }

    public float getH() {
        if (sizesNeedUpdate()) {
            updateSizes();
        }
        return h;
    }

    public void setForceUpdate() {
        forceUpdate = true;
    }

    private boolean positionsNeedUpdate() {
        if (forceUpdate) {
            return true;
        }

        return SkyblockAddons.getInstance().getNewScheduler().getTotalTicks() != lastPositionsUpdate;
    }

    private boolean sizesNeedUpdate() {
        if (forceUpdate) {
            return true;
        }

        return SkyblockAddons.getInstance().getNewScheduler().getTotalTicks() != lastSizesUpdate;
    }

    public void updatePositions() {
    }

    public void updateSizes() {
    }
}
