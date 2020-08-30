package codes.biscuit.skyblockaddons.features.healingcircle;

import lombok.Getter;

import java.awt.geom.Point2D;

@Getter
public class HealingCircleParticle {

    private Point2D.Double point;
    private long creation = System.currentTimeMillis();

    public HealingCircleParticle(double x, double z) {
        point = new Point2D.Double(x, z);
    }
}
