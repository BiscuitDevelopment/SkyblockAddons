package codes.biscuit.skyblockaddons.asm.api.helper.normal;

import codes.biscuit.skyblockaddons.asm.api.helper.TransformClassHelper;

/**
 * A normal helper for transform class
 *
 * @author iHDeveloper
 */
public class NormalTransformClassHelper extends TransformClassHelper {

    private String name;

    /**
     * Create a helper for transform class with name
     *
     * @param name The name of the class
     */
    public NormalTransformClassHelper(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
