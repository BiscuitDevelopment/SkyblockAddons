package codes.biscuit.skyblockaddons.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class MathUtils {

    public static boolean isInside(int x, int y, int minX, int minY, int maxX, int maxY) {
        return x >= minX && x <= maxX && y > minY && y < maxY;
    }

    public static boolean isInside(float x, float y, float minX, float minY, float maxX, float maxY) {
        return x >= minX && x <= maxX && y > minY && y < maxY;
    }

    /**
     * Converts a regular value to a normalized slider value.
     * <p>
     * For example, if a slider's value can be from 0 to 5, and the value given is 2.5, that is half
     * of the slider's total value, so this will return 0.5.
     * <p>
     * This will also snap to the given step value. If the step value is 0.1 and the given value is
     * 0.05, that will round to 0.1 giving a slider its "snapping" effect.
     *
     * @param value The denormalized slider value (usually min -> max)
     * @param min   The min slider value
     * @param max   The max slider value
     * @param step  The step value
     * @return The normalized slider value (0 -> 1F)
     */
    public static float normalizeSliderValue(float value, float min, float max, float step) {
        return clamp((snapToStep(value, step) - min) / (max - min), 0.0F, 1.0F);
    }

    /**
     * Converts a normalized slider value to a regular value.
     * <p>
     * For example, if a slider's value can be from 0 to 5, and the value given is 0.5,
     * this number will by multiplied by the slider's range, returning 2.5.
     * <p>
     * This will also snap to the given step value. If the step value is 0.1 and the value that was
     * going to be returned is 0.05, that will round to 0.1 giving a slider its "snapping" effect.
     *
     * @param value The normalized slider value (usually 0 -> 1F)
     * @param min   The min slider value
     * @param max   The max slider value
     * @param step  The step value
     * @return The denormalized slider value (min -> max)
     */
    public static float denormalizeSliderValue(float value, float min, float max, float step) {
        return clamp(snapToStep(min + (max - min) * clamp(value, 0, 1), step), min, max);
    }

    /**
     * Snaps the given value to the step amount.
     * <p>
     * For example, if the step is 0.1 and the value is 0.15, that will round up to 0.2 giving a
     * slider its "snapping" effect.
     *
     * @param value The value to round
     * @param step  The step amount
     * @return The value rounded to the step amount
     */
    private static float snapToStep(float value, float step) {
        return step * (float) Math.round(value / step);
    }

    /**
     * Clamps a value between two bounds.
     * <p>
     * For example, if the given value is 2.5, and the max is 2, this will round down to 2.
     *
     * @param value The value to round
     * @param min   The bottom bound
     * @param max   The top bounds
     * @return The value rounded to the given bounds
     */
    @SuppressWarnings("ManualMinMaxCalculation")
    public static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    public static double interpolateX(Entity entity, float partialTicks) {
        return interpolate(entity.prevPosX, entity.posX, partialTicks);
    }

    public static double interpolateY(Entity entity, float partialTicks) {
        return interpolate(entity.prevPosY, entity.posY, partialTicks);
    }

    public static double interpolateZ(Entity entity, float partialTicks) {
        return interpolate(entity.prevPosZ, entity.posZ, partialTicks);
    }

    public static double interpolate(double first, double second, float partialTicks) {
        return first + (second - first) * (double) partialTicks;
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return distance((float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2);
    }

    public static double distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        float deltaX = x1 - x2;
        float deltaY = y1 - y2;
        float deltaZ = z1 - z2;
        return MathHelper.sqrt_float(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        return distance((float) x1, (float) y1, (float) x2, (float) y2);
    }

    public static double distance(float x1, float y1, float x2, float y2) {
        float deltaX = x1 - x2;
        float deltaY = y1 - y2;
        return MathHelper.sqrt_float(deltaX * deltaX + deltaY * deltaY);
    }
}
