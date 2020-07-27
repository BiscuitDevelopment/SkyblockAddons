package codes.biscuit.skyblockaddons.asm.api.helper;

import lombok.Getter;

/**
 * An identifier for a class to transform
 *
 * @author iHDeveloper
 */
public abstract class TransformClassHelper {

    /**
     * @return The name used for the owner of a field or method, or a field type.
     */
    public final String getNameAsOwner() {
        return "L" + getName() + ";";
    }

    /**
     * @return The name used to identify this class
     */
    public String getTransformerName() {
        // The regex matches single slash or multiple and replace them with one dot
        return getName().replaceAll("(\\/)+", ".");
    }

    /**
     * @return The raw name of the class
     */
    public abstract String getName();
}
