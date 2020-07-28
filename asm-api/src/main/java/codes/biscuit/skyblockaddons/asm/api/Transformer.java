package codes.biscuit.skyblockaddons.asm.api;

import codes.biscuit.skyblockaddons.asm.api.helper.TransformClassHelper;
import org.objectweb.asm.tree.ClassNode;

/**
 * Transforming the targeted classes in order to change how the class behave.
 * Or, injecting hooks triggers.
 *
 * @author iHDeveloper
 */
public abstract class Transformer {

    /**
     * Puts a single class helper into an array
     *
     * @param classHelper The class helper to put into an array
     * @return An array containing single class helper
     */
    protected TransformClassHelper[] single(TransformClassHelper classHelper) {
        return new TransformClassHelper[] { classHelper };
    }

    /**
     * Puts multiple class helpers into an array
     * @param classHelpers The class helpers to put into an array
     * @return An array containing multiple class helpers
     */
    protected TransformClassHelper[] multiple(TransformClassHelper... classHelpers) {
        return classHelpers;
    }

    /**
     * Get the targeted class helpers
     *
     * @return An array of class helpers
     */
    public abstract TransformClassHelper[] targets();

    /**
     * Transform a class node with its helper
     *
     * @param engine The engine which is performing the transformation process
     * @param targetClassHelper The class helper of the target to transform
     * @param node The class node of the target
     */
    public abstract void transform(TransformerEngine engine, TransformClassHelper targetClassHelper, ClassNode node);
}
