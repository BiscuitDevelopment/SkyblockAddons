package codes.biscuit.skyblockaddons.features.healingcircle;

import lombok.Getter;
import lombok.Setter;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class HealingCircle {

    private List<HealingCircleParticle> healingCircleParticles = new ArrayList<>();
    private double totalX;
    private double totalZ;
    private int totalParticles;
    private long creation = System.currentTimeMillis();
    private long lastParticle;
    private double particlesPerSecond;
    private long oldestParticle = -1;

    public HealingCircle(HealingCircleParticle healingCircleParticle) {
        addPoint(healingCircleParticle);
    }

    public void addPoint(HealingCircleParticle healingCircleParticle) {
        lastParticle = System.currentTimeMillis();
        if (lastParticle - healingCircleParticle.getCreation() < 1000) {
            particlesPerSecond++;
        }

        totalParticles++;
        totalX += healingCircleParticle.getPoint().getX();
        totalZ += healingCircleParticle.getPoint().getY();
        healingCircleParticles.add(healingCircleParticle);

        if (oldestParticle == -1) {
            oldestParticle = healingCircleParticle.getCreation();
        } else {
            oldestParticle = Math.min(oldestParticle, healingCircleParticle.getCreation());
        }
    }

    public double getAverageX() {
        return totalX / (double) totalParticles;
    }

    public double getAverageZ() {
        return totalZ / (double) totalParticles;
    }

    public double getParticlesPerSecond() {
        return particlesPerSecond;
    }

    public Point2D.Double getCircleCenter() {
        if (healingCircleParticles.size() < 3) {
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        Point2D.Double middlePoint = healingCircleParticles.get(0).getPoint();

        Point2D.Double firstPoint = null;
        for (HealingCircleParticle healingCircleParticle : healingCircleParticles) {
            Point2D.Double point = healingCircleParticle.getPoint();
            if (point != middlePoint && point.distance(middlePoint) > 2) {
                firstPoint = point;
                break;
            }
        }
        if (firstPoint == null) {
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        Point2D.Double secondPoint = null;
        for (HealingCircleParticle healingCircleParticle : healingCircleParticles) {
            Point2D.Double point = healingCircleParticle.getPoint();
            if (point != middlePoint && point != firstPoint) {
                double distanceToMiddle = point.distance(middlePoint);
                if (distanceToMiddle > 2 && point.distance(firstPoint) > distanceToMiddle) {
                    secondPoint = point;
                    break;
                }
            }
        }
        if (secondPoint == null) {
            return new Point2D.Double(Double.NaN, Double.NaN);
        }

        Point2D.Double firstChordMidpoint = new Point2D.Double((middlePoint.x + firstPoint.x) / 2D, (middlePoint.y + firstPoint.y) / 2D);
        Point2D.Double secondChordMidpoint = new Point2D.Double((middlePoint.x + secondPoint.x) / 2D, (middlePoint.y + secondPoint.y) / 2D);

        Point2D.Double firstChordFirst = rotatePoint(middlePoint, firstChordMidpoint, 90);
        Point2D.Double firstChordSecond = rotatePoint(firstPoint, firstChordMidpoint, 90);

        Point2D.Double secondChordFirst = rotatePoint(middlePoint, secondChordMidpoint, 90);
        Point2D.Double secondChordSecond = rotatePoint(secondPoint, secondChordMidpoint, 90);

        return lineLineIntersection(firstChordFirst, firstChordSecond, secondChordFirst, secondChordSecond);
    }

    private static Point2D.Double rotatePoint(Point2D.Double point, Point2D.Double center, double degrees) {
        double radians = Math.toRadians(degrees);

        double newX = center.getX() + (point.getX() - center.getX()) * Math.cos(radians) - (point.getY() - center.getY()) * Math.sin(radians);
        double newY = center.getY() + (point.getX() - center.getX()) * Math.sin(radians) + (point.getY() - center.getY()) * Math.cos(radians);

        return new Point2D.Double(newX, newY);
    }

    private static Point2D.Double lineLineIntersection(Point2D.Double a, Point2D.Double b, Point2D.Double c, Point2D.Double d) {
        // Line AB represented as a1x + b1y = c1
        double a1 = b.y - a.y;
        double b1 = a.x - b.x;
        double c1 = a1 * (a.x) + b1 * (a.y);

        // Line CD represented as a2x + b2y = c2
        double a2 = d.y - c.y;
        double b2 = c.x - d.x;
        double c2 = a2 * (c.x) + b2 * (c.y);

        double determinant = a1 * b2 - a2 * b1;

        if (determinant == 0) {
            // The lines are parallel.
            return new Point2D.Double(Double.NaN, Double.NaN);
        } else {
            double x = (b2 * c1 - b1 * c2) / determinant;
            double y = (a1 * c2 - a2 * c1) / determinant;
            return new Point2D.Double(x, y);
        }
    }
}
